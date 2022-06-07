package com.sofa.nerdrunning.finishedrun

import androidx.lifecycle.ViewModel
import androidx.paging.PagingSource
import com.sofa.nerdrunning.persistentrun.Run
import com.sofa.nerdrunning.persistentrun.RunRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListFinishedRunsViewModel @Inject constructor(
    private val runRepository: RunRepository,
) : ViewModel() {

    fun loadFinishedRuns(): PagingSource<Int, Run> = runRepository.readAll()

}
