package com.ly.packetcapture.config

import java.io.File

/**
 * 本地转发配置类
 */
object Config {
    //配置dns
    var dns = "114.114.114.114"

    //采编系统项目缓存位置
    val SD_PATH = MyApplication.context.externalCacheDir!!.path

    //数据缓存位置
    var CACHE_DIR = SD_PATH + File.separator + "share"

    //数据缓存文件名
    var CACHE_FILENAME = CACHE_DIR + File.separator + "data.txt"

    const val SEL_KEY = "SEL_KEY"

    var taskId = 0
}