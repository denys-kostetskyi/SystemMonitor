// ISystemMonitorService.aidl
package com.denyskostetskyi.systemmonitor.library;

// Declare any non-default types here with import statements

interface ISystemMonitorService {
    long getServiceRunningTime();

    int[] getRunningProcessIds();

    String getSystemInfo();
}
