package com.denyskostetskyi.systemmonitor.server

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            val serviceIntent = SystemMonitorService.newIntent(context)
            context.startService(serviceIntent)
        }
    }
}
