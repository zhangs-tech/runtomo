package com.zhangs_tech.runtomo.activity

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zhangs_tech.runtomo.auth.TokenStore
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class StepSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val activityRepository: ActivityRepository,
    private val tokenStore: TokenStore,
    private val backgroundSync: BackgroundSync,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        if (tokenStore.read() == null || !activityRepository.canReadInBackground()) {
            backgroundSync.cancel()
            return Result.success()
        }
        activityRepository.sync(activityRepository.readTodaySteps(), skipIfUnchanged = true)
        return Result.success()
    }
}
