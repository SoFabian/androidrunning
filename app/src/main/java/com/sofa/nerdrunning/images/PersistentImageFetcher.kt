package com.sofa.nerdrunning.images

import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import androidx.core.graphics.drawable.toDrawable
import coil.ImageLoader
import coil.decode.DataSource
import coil.fetch.DrawableResult
import coil.fetch.FetchResult
import coil.fetch.Fetcher
import coil.request.Options
import com.sofa.nerdrunning.persistentrun.RunId
import com.sofa.nerdrunning.persistentrun.RunRepository
import javax.inject.Inject

internal class PersistentImageFetcher(
    private val runId: RunId,
    private val options: Options,
    private val runRepository: RunRepository,
) : Fetcher {

    override suspend fun fetch(): FetchResult? {
        val image = runRepository.readImage(runId) ?: return null
        return DrawableResult(
            drawable = toDrawable(image),
            isSampled = false,
            dataSource = DataSource.DISK
        )
    }

    private fun toDrawable(image: ByteArray): Drawable {
        val bitmap = BitmapFactory.decodeByteArray(image, 0, image.size)
        return bitmap.toDrawable(options.context.resources)
    }

}

class PersistentImageFetcherFactory @Inject constructor(
    private val runRepository: RunRepository,
) : Fetcher.Factory<RunId> {

    override fun create(data: RunId, options: Options, imageLoader: ImageLoader): Fetcher =
        PersistentImageFetcher(data, options, runRepository)

}
