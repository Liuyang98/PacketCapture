package com.ly.packetcapture.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.ly.packetcapture.config.Config
import com.ly.packetcapture.util.LogUtil
import java.text.SimpleDateFormat
import java.util.*

/**
 * create by ly on 2021/8/20
 */

@Entity(tableName = "netTable")
class NetData {
    @PrimaryKey(autoGenerate = true)
    var dataId: Int? = null

    @ColumnInfo(name = "taskId")
    var taskId: Int? = Config.taskId

    @ColumnInfo(name = "time")
    var time: Long = System.currentTimeMillis()

    @ColumnInfo(name = "protocol")
    var protocol: String? = null

    @ColumnInfo(name = "dns")
    var dns: Int? = null

    @ColumnInfo(name = "domain")
    var domain: String? = null

    @ColumnInfo(name = "ip")
    var ip: String? = null

    @ColumnInfo(name = "port")
    var port: String? = null

    constructor() {

    }

    /**
     * DNS信息
     */
    @Ignore
    constructor(domain: String, ip: String) {
        this.dns = 1
        this.domain = domain
        this.ip = ip
        LogUtil.i("DNS信息：$domain:::$ip")
    }

    /**
     * 请求类型
     */
    @Ignore
    constructor(ip: String, port: String, protocol: String) {
        this.dns = 0
        this.ip = ip
        this.port = port
        this.protocol = protocol
        LogUtil.i("地址：$ip：端口：$port：$protocol")
    }


    override fun toString(): String {
        return if (dns == 0) {
            formatOrderTime(time) + " : " + ip!!.replace("/", "") + ":" + port + " - " + protocol
        }else{
            "$domain \n $ip"
        }
    }


    fun formatOrderTime(date: Long): String {
        return SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date(date))
    }

}