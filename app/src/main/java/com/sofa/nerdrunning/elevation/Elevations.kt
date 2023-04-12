package com.sofa.nerdrunning.elevation

import com.google.android.gms.maps.model.LatLng
import com.sofa.nerdrunning.dispatchers.IoDispatcher
import com.sofa.nerdrunning.http.JSON
import com.sofa.nerdrunning.log.logDebug
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.internal.http.promisesBody
import org.json.JSONArray
import org.json.JSONObject
import org.json.JSONTokener
import java.io.IOException
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

data class ElevationLocation(val latitude: String, val longitude: String)

fun LatLng.toElevationLocation(): ElevationLocation {
    val decimalFormatSymbols = DecimalFormatSymbols(Locale.ENGLISH)
    val df = DecimalFormat("0.####", decimalFormatSymbols)
    df.roundingMode = RoundingMode.HALF_UP
    return ElevationLocation(df.format(latitude), df.format(longitude))
}

data class LocationToElevationMap(private val elevations: Map<ElevationLocation, Int>) {
    operator fun get(location: LatLng): Int? = elevations[location.toElevationLocation()]
}

class Elevations @Inject constructor(
    private val httpClient: OkHttpClient,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    suspend fun getElevationsForLocations(locations: List<LatLng>): LocationToElevationMap {
        val elevationLocations = locations.map { it.toElevationLocation() }.distinct()
        return LocationToElevationMap(try {
            val deferredList =
                elevationLocations.chunked(100).map { tryToGetElevationChunkAsync(it) }
            val resultList = deferredList.awaitAll()
            resultList.flatMap { it.entries }.associate { it.key to it.value }
        } catch (e: Exception) {
            logDebug("Elevations", "Unable to await elevation calls cause by $e")
            defaultElevations(elevationLocations)
        })
    }

    private suspend fun tryToGetElevationChunkAsync(
        locations: List<ElevationLocation>
    ): Deferred<Map<ElevationLocation, Int>> {
        for (elevationAPI in listOf(OpenMeteo, OpenElevation)) {
            try {
                return getElevationChunkAsync(locations, elevationAPI)
            } catch (e: Exception) {
                logDebug(
                    "Elevations",
                    "Unable to load elevations from ${elevationAPI::class.simpleName} caused by $e"
                )
            }
        }
        val resultIfError = defaultElevations(locations)
        return CompletableDeferred<Map<ElevationLocation, Int>>().apply { complete(resultIfError) }
    }

    private fun defaultElevations(locations: List<ElevationLocation>): Map<ElevationLocation, Int> =
        locations.associateWith { 200 }

    private suspend fun getElevationChunkAsync(
        locations: List<ElevationLocation>,
        elevationAPI: ElevationAPI,
    ): Deferred<Map<ElevationLocation, Int>> =
        withContext(ioDispatcher) {
            async {
                suspendCoroutine { continuation ->
                    httpClient.newCall(elevationAPI.createRequest(locations))
                        .enqueue(object : Callback {
                            override fun onFailure(call: Call, e: IOException) {
                                continuation.resumeWithException(e)
                            }

                            override fun onResponse(call: Call, response: Response) {
                                response.use {
                                    val elevations: List<Int> = elevationAPI.parseResponse(it)
                                    val map = locations.zip(elevations).toMap()
                                    continuation.resume(map)
                                }
                            }
                        })
                }
            }
        }
}

sealed interface ElevationAPI {
    fun createRequest(path: List<ElevationLocation>): Request
    fun parseResponse(response: Response): List<Int> {
        if (!response.isSuccessful || !response.promisesBody()) return emptyList()
        val responseBody = response.body?.string()
        responseBody?.let { logDebug("Elevations", it) }
        return if (responseBody != null) parseResponseString(responseBody) else emptyList()
    }

    fun parseResponseString(responseBody: String): List<Int>
}

object OpenElevation : ElevationAPI {

    override fun createRequest(path: List<ElevationLocation>): Request {
        val locations = path.map { it.toJSONObject() }
        val body = JSONObject().apply {
            put("locations", JSONArray(locations))
        }
        val req = Request.Builder()
            .url("https://api.open-elevation.com/api/v1/lookup")
            .post(body.toString().toRequestBody(JSON))
            .build()
        logDebug("Elevations", req.toString())
        return req
    }

    override fun parseResponseString(responseBody: String): List<Int> {
        val json = JSONTokener(responseBody).nextValue() as JSONObject
        val elevations = mutableListOf<Int>()
        val results = json.getJSONArray("results")
        for (i in 0 until results.length()) {
            val elevation = results.getJSONObject(i).getInt("elevation")
            elevations.add(elevation)
        }
        return elevations
    }

    private fun ElevationLocation.toJSONObject(): JSONObject =
        JSONObject().apply {
            put("latitude", latitude)
            put("longitude", longitude)
        }

}

object OpenMeteo : ElevationAPI {

    override fun createRequest(path: List<ElevationLocation>): Request {
        val latitudes = path.joinToString(",") { it.latitude }
        val longitudes = path.joinToString(",") { it.longitude }
        val req = Request.Builder()
            .url("https://api.open-meteo.com/v1/elevation?latitude=$latitudes&longitude=$longitudes")
            .get()
            .build()
        logDebug("Elevations", req.toString())
        return req
    }

    override fun parseResponseString(responseBody: String): List<Int> {
        val json = JSONTokener(responseBody).nextValue() as JSONObject
        val elevations = mutableListOf<Int>()
        val results = json.getJSONArray("elevation")
        for (i in 0 until results.length()) {
            val elevation = results.getInt(i)
            elevations.add(elevation)
        }
        return elevations
    }

}
