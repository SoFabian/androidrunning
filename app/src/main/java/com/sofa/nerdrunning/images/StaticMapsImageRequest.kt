package com.sofa.nerdrunning.images

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import coil.ImageLoader
import coil.intercept.Interceptor
import coil.map.Mapper
import coil.request.CachePolicy
import coil.request.ImageRequest
import coil.request.ImageResult
import coil.request.Options
import coil.size.Size
import coil.transform.Transformation
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.PolyUtil
import com.sofa.nerdrunning.log.logDebug
import com.sofa.nerdrunning.persistentrun.RunId
import com.sofa.nerdrunning.persistentrun.RunRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

data class StaticMapsImageRequest(
    val runId: RunId,
    val path: List<LatLng>,
)

class StaticMapImageRequestMapper(private val mapsApiKey: String) :
    Mapper<StaticMapsImageRequest, Uri> {

    override fun map(data: StaticMapsImageRequest, options: Options): Uri? {
        if (data.path.isEmpty()) {
            return null
        }
        val staticMapsUri = createStaticMapsImageUri(data.path)
        logDebug("StaticMapImageRequestMapper", "Static Maps Url: $staticMapsUri")
        return staticMapsUri
    }

    private fun createStaticMapsImageUri(path: List<LatLng>): Uri =
        Uri.Builder().apply {
            scheme("https")
            authority("maps.googleapis.com")
            path("/maps/api/staticmap")
            appendQueryParameter("size", "400x440")
            appendQueryParameter("maptype", "satellite")
            appendQueryParameter("path", "enc:" + PolyUtil.encode(path))
            appendQueryParameter("key", mapsApiKey)
        }.build()

}

class StaticMapsImageDBCacheInterceptor(
    private val runRepository: RunRepository
) : Interceptor {

    override suspend fun intercept(chain: Interceptor.Chain): ImageResult {
        val data = chain.request.data
        val result = chain.proceed(chain.request)
        val drawable = result.drawable
        if (data is StaticMapsImageRequest && drawable is BitmapDrawable) {
            val runId = data.runId
            logDebug("StaticMapsImageDBCacheInterceptor", "Downloaded map for run $runId")
            val bitmap = drawable.bitmap
            val stream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bitmapData: ByteArray = stream.toByteArray()
            runRepository.insertImage(runId, bitmapData)
        }
        return result
    }

}

object StaticMapsFooterEraser : Transformation {

    override val cacheKey: String = javaClass.name

    override suspend fun transform(input: Bitmap, size: Size): Bitmap =
        Bitmap.createBitmap(input, 0, 20, 400, 400)

}

class StaticMapsImageRetriever @Inject constructor(
    private val imageLoader: ImageLoader,
    @ApplicationContext val context: Context,
) {

    fun loadAndPersist(
        runId: RunId,
        path: List<LatLng>,
    ) {
        imageLoader.enqueue(createImageRequest(runId, path))
    }

    private fun createImageRequest(
        runId: RunId,
        path: List<LatLng>,
    ) = ImageRequest.Builder(context).apply {
        data(StaticMapsImageRequest(runId, path))
        transformations(StaticMapsFooterEraser)
        memoryCachePolicy(CachePolicy.DISABLED)
            .listener(
                onError = { _, result ->
                    logDebug(
                        "StaticMapsImageRetriever",
                        "Error loading static maps image for run $runId. Cause: " +
                                "${result.throwable.message}. ${result.throwable.stackTraceToString()}"
                    )
                })
    }.build()

}
