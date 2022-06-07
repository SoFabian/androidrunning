package com.sofa.nerdrunning.persistentrun

import com.sofa.nerdrunning.dispatchers.IoDispatcher
import com.sofa.nerdrunning.finishedrun.FinishedRun
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class RunRepository @Inject constructor(
    private val runDao: RunDao,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) {

    fun readAll() = runDao.readAll()

    suspend fun readRunDetail(runId: RunId) = withContext(ioDispatcher) {
        runDao.readRunDetail(runId)
    }

    suspend fun insert(finishedRun: FinishedRun) = withContext(ioDispatcher) {
        runDao.insert(finishedRun)
    }

    suspend fun insertImage(runId: RunId, image: ByteArray) = withContext(ioDispatcher) {
        runDao.insertImage(StaticMapsImage(runId, image))
    }

    suspend fun readImage(runId: RunId) = withContext(ioDispatcher) {
        runDao.readImage(runId)
    }

    suspend fun deleteRun(runId: RunId) = withContext(ioDispatcher) {
        runDao.deleteRun(RunIdHolder(runId))
    }

}
