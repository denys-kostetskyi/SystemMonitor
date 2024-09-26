package com.denyskostetskyi.systemmonitor.server

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.denyskostetskyi.systemmonitor.library.ISystemMonitorService

class SystemMonitorService : Service() {
    private val activityManager by lazy { getSystemService(ActivityManager::class.java) }
    private var serviceBootTime = 0L

    private val binder = object : ISystemMonitorService.Stub() {
        override fun getServiceRunningTime() =
            this@SystemMonitorService.getServiceRunningTime()

        override fun getRunningProcessIds() =
            this@SystemMonitorService.getRunningProcessIds()

        override fun getSystemInfo() =
            this@SystemMonitorService.getSystemInfo()
    }

    override fun onCreate() {
        super.onCreate()
        serviceBootTime = System.currentTimeMillis()
        Log.d(TAG, "onCreate")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        startForeground()
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(TAG, "onBind")
        return binder
    }

    private fun getServiceRunningTime() =
        millisToSeconds(System.currentTimeMillis() - serviceBootTime)

    private fun getRunningProcessIds(): IntArray {
        val processInfoList = activityManager.runningAppProcesses ?: return IntArray(0)
        return processInfoList.flatMap { listOf(it.pid) }.toIntArray()
    }

    fun getSystemInfo(): String {
        val runningProcessesCount = activityManager.runningAppProcesses?.size ?: 0
        val uptimeSeconds = millisToSeconds(SystemClock.uptimeMillis())
        val uptimeString = getString(R.string.uptime_seconds, uptimeSeconds)
        val runningProcessesCountString =
            getString(R.string.running_processes, runningProcessesCount)
        return "$uptimeString, $runningProcessesCountString"
    }

    private fun startForeground() {
        createNotificationChannel()
        val notification = createNotification()
        val foregroundServiceType =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            } else {
                0
            }
        ServiceCompat.startForeground(
            this,
            SERVICE_ID,
            notification,
            foregroundServiceType
        )
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.service_notification_channel),
                NotificationManager.IMPORTANCE_HIGH
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(getString(R.string.system_monitor_service))
            .setContentText(getString(R.string.service_is_running))
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    companion object {
        private const val TAG = "SystemMonitorService"
        private const val CHANNEL_ID = "SystemMonitorServiceChannel"
        private const val SERVICE_ID = 1

        private fun millisToSeconds(millis: Long) = millis / 1000

        fun newIntent(context: Context) = Intent(context, SystemMonitorService::class.java)
    }
}
