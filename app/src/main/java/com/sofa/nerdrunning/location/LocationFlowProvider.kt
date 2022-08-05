package com.sofa.nerdrunning.location

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.sofa.nerdrunning.log.logDebug
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class LocationFlowProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(context)
    }

    fun createFlow() = fusedLocationClient.locationFlow()
    // .onEach { logDebug("LocationFlowProvider", "on location ${it.latitude} ${it.longitude}") }

    @SuppressLint("MissingPermission")
    fun FusedLocationProviderClient.locationFlow() = callbackFlow {
        val locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let {
                    try {
                        trySend(it)
                    } catch (e: Exception) {
                        Log.e("Location", "Exception sending location.", e)
                    }
                }
            }
        }
        logDebug("LocationFlowProvider", "Request location updates")
        requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
            .addOnFailureListener { e ->
                close(e)
            }
        awaitClose {
            logDebug("LocationFlowProvider", "Remove location updates")
            removeLocationUpdates(locationCallback)
        }
    }

    companion object {
        val locationRequest: LocationRequest = LocationRequest.create().apply {
            interval = 1000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
    }
}
