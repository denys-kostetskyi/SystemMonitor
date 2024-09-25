package com.denyskostetskyi.systemmonitor.server

import android.app.ActivityManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.SystemClock

class SystemMonitorService : Service() {
    private val activityManager by lazy { getSystemService(ActivityManager::class.java) }

    private var serviceBootTime = 0L

    override fun onCreate() {
        super.onCreate()
        serviceBootTime = System.currentTimeMillis()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    private fun getServiceRunningTime() = (System.currentTimeMillis() - serviceBootTime) / 1000

    private fun getRunningProcessIds(): IntArray {
        val processInfoList = activityManager.runningAppProcesses ?: return IntArray(0)
        return processInfoList.flatMap { listOf(it.pid) }.toIntArray()
    }

    fun getSystemInfo(): String {
        val runningProcessesCount = activityManager.runningAppProcesses?.size ?: 0
        val uptimeSeconds = SystemClock.uptimeMillis() / 1000
        val uptimeString = getString(R.string.uptime_seconds, uptimeSeconds)
        val runningProcessesCountString =
            getString(R.string.running_processes, runningProcessesCount)
        return "$uptimeString, $runningProcessesCountString"
    }
}
