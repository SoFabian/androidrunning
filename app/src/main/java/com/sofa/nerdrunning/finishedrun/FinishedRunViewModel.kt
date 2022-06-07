package com.sofa.nerdrunning.finishedrun

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sofa.nerdrunning.loading.Loaded
import com.sofa.nerdrunning.loading.Loading
import com.sofa.nerdrunning.loading.LoadingStatus
import com.sofa.nerdrunning.log.logDebug
import com.sofa.nerdrunning.persistentrun.RunId
import com.sofa.nerdrunning.persistentrun.RunRepository
import com.sofa.nerdrunning.persistentrun.toFinishedRun
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FinishedRunViewModel @Inject constructor(
    private val runRepository: RunRepository,
) : ViewModel() {

    private val _finishedRunLoadingStatus: MutableStateFlow<LoadingStatus<FinishedRun>> =
        MutableStateFlow(Loading())
    val finishedRunLoadingStatus = _finishedRunLoadingStatus.asStateFlow()

    fun loadRun(runId: RunId) {
        viewModelScope.launch {
            val runDetail = runRepository.readRunDetail(runId)
            if (runDetail != null) {
                _finishedRunLoadingStatus.value = Loaded(runDetail.toFinishedRun())
            }
        }
    }

    private val _deletedRunLoadingStatus: MutableStateFlow<DeletionStatus> =
        MutableStateFlow(DeletionStatus.NOT_TRIGGERED)
    val deletedRunLoadingStatus = _deletedRunLoadingStatus.asStateFlow()

    fun deleteRun(runId: RunId) {
        logDebug("FinishedRunViewModel", "Delete run $runId")
        _deletedRunLoadingStatus.value = DeletionStatus.DELETING
        viewModelScope.launch {
            runRepository.deleteRun(runId)
            _deletedRunLoadingStatus.value = DeletionStatus.DELETED
        }
    }

}
