package com.tony.gardenflow

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.tony.gardenflow.notification.GardenNotificationManager
import com.tony.gardenflow.security.AppIntegrityService
import com.tony.gardenflow.worker.GardenWorkScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class GardenFlowApp : Application(), Configuration.Provider {
    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var notificationManager: GardenNotificationManager
    @Inject lateinit var workScheduler: GardenWorkScheduler
    @Inject lateinit var appIntegrityService: AppIntegrityService
    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder().setWorkerFactory(workerFactory).build()

    override fun onCreate() {
        super.onCreate()
        notificationManager.createChannels()
        workScheduler.scheduleDaily()
        workScheduler.scheduleWeatherRefresh()
        appScope.launch { appIntegrityService.checkIntegrity() }
    }
}
