package com.denyskostetskyi.systemmonitor.library

import android.content.ComponentName
import android.content.Intent

class SystemMonitorServiceHelper {
    companion object {
        private const val PACKAGE_NAME = "com.denyskostetskyi.systemmonitor.server"
        private const val CLASS_NAME = "$PACKAGE_NAME.SystemMonitorService"

        fun newIntent() = Intent().apply {
            setComponent(ComponentName(PACKAGE_NAME, CLASS_NAME))
        }
    }
}
