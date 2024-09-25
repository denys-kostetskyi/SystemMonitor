package com.denyskostetskyi.systemmonitor.server

import android.app.ActivityManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import com.denyskostetskyi.systemmonitor.library.ISystemMonitorService

//TODO add startForeground call
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

    companion object {
        private const val TAG = "SystemMonitorService"

        private fun millisToSeconds(millis: Long) = millis / 1000

        fun newIntent(context: Context) = Intent(context, SystemMonitorService::class.java)
    }
}
