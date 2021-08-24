package com.ly.packetcapture.bio

import android.util.Log
import kotlin.Throws
import java.io.IOException
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

object BioUtil {
    private val TAG = BioUtil::class.java.simpleName
    @Throws(IOException::class)
    fun write(channel: SocketChannel, byteBuffer: ByteBuffer?): Int {
        val len = channel.write(byteBuffer)
        Log.i(TAG, String.format("write %d %s ", len, channel.toString()))
        return len
    }

    /**
     * 读取网络数据
     * @param channel 与节点的通道
     * @param byteBuffer 数据缓存区
     * @return 数据长度
     */
    @Throws(IOException::class)
    fun read(channel: SocketChannel, byteBuffer: ByteBuffer?): Int {
        val len = channel.read(byteBuffer)
        Log.d(TAG, String.format("read %d %s ", len, channel.toString()))
        return len
    }

    fun byteToString(data: ByteArray, off: Int, len: Int): String {
        var len = len
        len = Math.min(128, len)
        val sb = StringBuilder()
        for (i in off until off + len) {
            sb.append(String.format("%02x ", data[i]))
        }
        return sb.toString()
    }
}