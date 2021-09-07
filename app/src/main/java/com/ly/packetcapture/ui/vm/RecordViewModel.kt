package com.ly.packetcapture.ui.vm

import androidx.lifecycle.ViewModel
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.db.DatabaseUtil
import kotlinx.coroutines.flow.flow

/**
 * create by ly on 2021/8/19
 */
class RecordViewModel : ViewModel() {

    /**
     * 保存选取情况
     */
    fun getDNSList(isDns: Boolean) = flow {
        emit(
            if (isDns) {
                DatabaseUtil.netDao.searchDNSByTaskId(Config.taskId)
            } else {
                DatabaseUtil.netDao.searchReqByTaskId(Config.taskId)
            }
        )
    }

}