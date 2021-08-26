package com.ly.packetcapture.ui.vm

import android.content.Context
import androidx.lifecycle.ViewModel
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.db.DatabaseUtil
import com.ly.packetcapture.util.FileUtil
import com.ly.packetcapture.util.SharePrefUtil

/**
 * create by ly on 2021/8/25
 */
class MainViewModel : ViewModel() {
    //任务ID
    private val TASK_ID = "TASK_ID"

    fun hasSelectApp():Boolean{
        return SharePrefUtil.getString(Config.SEL_KEY).isNotEmpty()
    }

    fun startDataSave(){
        Config.taskId = SharePrefUtil.getInt(TASK_ID, 0) + 1
        SharePrefUtil.setInt(TASK_ID, Config.taskId)
    }

    fun dealShareDnsData(context: Context){
        val list = DatabaseUtil.netDao.searchDNSByTaskId(Config.taskId)
        val sb = StringBuilder()
        for (bean in list) {
            sb.append(bean.toString()).append("\n\n")
        }
        FileUtil.saveTxt(sb.toString(), context)
    }

    fun dealShareReqData(context: Context){
        val list = DatabaseUtil.netDao.searchReqByTaskId(Config.taskId)
        val sb = StringBuilder()
        for (bean in list) {
            sb.append(bean.toString()).append("\n")
        }
        FileUtil.saveTxt(sb.toString(), context)
    }
}