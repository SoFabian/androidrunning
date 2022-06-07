package com.sofa.nerdrunning.images

import android.content.Context
import coil.ImageLoader
import coil.request.CachePolicy
import com.sofa.nerdrunning.R
import com.sofa.nerdrunning.dispatchers.IoDispatcher
import com.sofa.nerdrunning.persistentrun.RunRepository
import com.sofa.nerdrunning.resources.ResourcesProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher

@Module
@InstallIn(SingletonComponent::class)
class ImagesModule {

    @Provides
    fun provideImageLoader(
        @ApplicationContext context: Context,
        resourcesProvider: ResourcesProvider,
        @IoDispatcher ioDispatcher: CoroutineDispatcher,
        persistentImageFetcher: PersistentImageFetcherFactory,
        runRepository: RunRepository,
    ) = ImageLoader.Builder(context)
        .components {
            add(StaticMapsImageDBCacheInterceptor(runRepository))
            add(StaticMapImageRequestMapper(resourcesProvider.getString(R.string.MAPS_API_KEY)))
            add(persistentImageFetcher)
        }
        .fetcherDispatcher(ioDispatcher)
        .diskCachePolicy(CachePolicy.DISABLED)
        .build()

}
