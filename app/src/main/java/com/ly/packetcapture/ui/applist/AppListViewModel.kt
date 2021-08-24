package com.ly.packetcapture.ui.applist

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.config.MyApplication
import com.ly.packetcapture.util.LogUtil
import com.ly.packetcapture.util.SharePrefUtil

/**
 * create by ly on 2021/8/19
 */
class AppListViewModel : ViewModel() {
    val listSys = mutableListOf<AppListBean>()
    val listNor = mutableListOf<AppListBean>()

     fun searchLocalAppList() {
        val selList = SharePrefUtil.getString(Config.SEL_KEY)
        val mPackageManager = MyApplication.context.packageManager
        val startTime = System.currentTimeMillis()
        var m = 10000
        while (m < 11500) {
                val localObj = mPackageManager.getPackagesForUid(m)
                if (!localObj.isNullOrEmpty()) {
                    val packCount = localObj.size
                    var n = 0
                    while (n < packCount) {
                        try {
                            addToList(mPackageManager.getPackageInfo(localObj[n], 0), selList)
                        } catch (e: Exception) {
                            LogUtil.e("错误信息：：" + StringBuilder().append("pkg info get exception:").append(e.message))
                        }
                        n += 1
                    }
            }
            m += 1
        }
        LogUtil.e("系统应用数量 ---- " + listSys.size)
        LogUtil.e("普通应用数量 ---- " + listNor.size)
        LogUtil.e("searchLocalAppList： 耗时" + (System.currentTimeMillis() - startTime))
    }

    private fun addToList(paramPackageInfo: PackageInfo, selList: String) {
        //过滤自己
        if (paramPackageInfo.packageName == MyApplication.context.packageName) {
            return
        }

        if (paramPackageInfo.applicationInfo.flags and 0x1 != 0) {
            listSys.add(AppListBean(paramPackageInfo))
        } else {
            listNor.add(AppListBean(paramPackageInfo, selList.contains(paramPackageInfo.packageName)))
        }
    }

    /**
     * 保存选取情况
     */
    fun saveSelInfo() {
        val stringBuilder = java.lang.StringBuilder()
        for (bean in listNor) {
            if (bean.selected) {
                if (stringBuilder.isNotEmpty()) {
                    stringBuilder.append(",")
                }
                stringBuilder.append(bean.info.packageName)
            }
        }
        SharePrefUtil.setString(Config.SEL_KEY, stringBuilder.toString())
    }

}