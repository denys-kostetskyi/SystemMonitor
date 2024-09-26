package com.denyskostetskyi.systemmonitor.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (isBootAction(intent.action) && !SystemMonitorService.isServiceRunning) {
            Log.d(TAG, "${intent.action} received. Starting SystemMonitorService")
            startSystemMonitorService(context)
        }
    }

    private fun isBootAction(action: String?) =
        action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED

    private fun startSystemMonitorService(context: Context) {
        val intent = SystemMonitorService.newIntent(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
