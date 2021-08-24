package com.ly.packetcapture.ui.applist

import android.content.pm.PackageInfo
import android.graphics.drawable.Drawable
import com.ly.packetcapture.config.MyApplication

/**
 * create by ly on 2021/6/8
 */
data class AppListBean(var info: PackageInfo, var selected: Boolean = false){
    var appLabel:String?=null
    get() {
        if(field==null){
            field= MyApplication.context.packageManager.getApplicationLabel(info.applicationInfo).toString()
        }
        return field
    }

    var appIcon:Drawable?=null
    get() {
        if(field==null){
            field= MyApplication.context.packageManager.getApplicationIcon(info.applicationInfo)
        }
        return field
    }

}
