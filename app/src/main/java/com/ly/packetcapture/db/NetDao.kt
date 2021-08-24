package com.ly.packetcapture.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

/**
 * create by ly on 2021/8/20
 */
@Dao
interface NetDao {

    @Insert
    fun insert(netData: NetData)

    @Query("SELECT * FROM netTable WHERE taskId = (:taskId) AND dns = 1 ORDER BY domain")
    fun searchDNSByTaskId(taskId: Int): MutableList<NetData>

    @Query("SELECT * FROM netTable WHERE taskId = (:taskId) AND dns = 0 ORDER BY time DESC")
    fun searchReqByTaskId(taskId: Int): MutableList<NetData>
}