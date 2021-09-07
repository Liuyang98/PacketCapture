package com.ly.packetcapture.util

import android.app.ActivityManager
import android.content.Context
import com.ly.packetcapture.config.MyApplication

/**
 * create by ly on 2021/8/19
 */
object ServiceUtil {
    fun isServiceRunning(serviceClassName: String): Boolean {
        val activityManager = MyApplication.context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val services = activityManager.getRunningServices(Int.MAX_VALUE)
        for (runningServiceInfo in services) {
            if (runningServiceInfo.service.className == serviceClassName) {
                return true
            }
        }
        return false
    }
}