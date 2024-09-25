package com.denyskostetskyi.systemmonitor.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            Log.d(TAG, "ACTION_BOOT_COMPLETED received. Starting SystemMonitorService")
            val serviceIntent = SystemMonitorService.newIntent(context)
            context.startService(serviceIntent) //TODO use startForegroundService
        }
    }

    companion object {
        private const val TAG = "BootReceiver"
    }
}
