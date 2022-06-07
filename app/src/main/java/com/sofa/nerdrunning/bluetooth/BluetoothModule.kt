package com.sofa.nerdrunning.bluetooth

import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object BluetoothModule {

    @Provides
    fun provideBluetoothManager(@ApplicationContext context: Context) =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

}