package com.denyskostetskyi.systemmonitor.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d(TAG, "ACTION_BOOT_COMPLETED received. Starting SystemMonitorService")
            startSystemMonitorService(context)
        }
    }

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
