package com.sofa.nerdrunning.home

import androidx.lifecycle.ViewModel
import com.sofa.nerdrunning.heartrate.HeartRateMonitorProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val heartRateFlowProvider: HeartRateMonitorProvider,
) : ViewModel() {

    fun bondHR(hrAddress: String) {
        heartRateFlowProvider.setDevice(hrAddress)
    }

}
