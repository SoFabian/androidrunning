package com.sofa.nerdrunning.images

import android.content.Context
import coil.ImageLoader
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

object ImageLoaderAccessor {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface ImageLoaderHiltEntryPoint {
        fun imageLoader(): ImageLoader
    }

    fun get(context: Context): ImageLoader {
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            ImageLoaderHiltEntryPoint::class.java
        )
        return hiltEntryPoint.imageLoader()
    }

}
