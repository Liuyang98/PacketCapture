package com.ly.packetcapture.ui.vm

import android.content.pm.PackageInfo
import androidx.lifecycle.ViewModel
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.config.MyApplication
import com.ly.packetcapture.bean.AppListBean
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
                            LogUtil.d("错误信息：：" + StringBuilder().append("pkg info get exception:").append(e.message))
                        }
                        n += 1
                    }
            }
            m += 1
        }
    }

    private fun addToList(paramPackageInfo: PackageInfo, selList: String) {
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