package com.ly.packetcapture.util

import java.nio.channels.SocketChannel
import java.util.HashMap
import kotlin.jvm.Synchronized

class ObjAttrUtil {
    private val objAttrs: MutableMap<SocketChannel?, MutableMap<String, Any>> = HashMap()
    @Synchronized
    fun getAttr(obj: Any?, k: String): Any? {
        val map = objAttrs[obj] ?: return null
        return map[k]
    }

    /**
     *
     * @param socketChannel 远程连接通道
     * @param k type-remote / pipe-TcpPipe对象 / key - SelectionKey
     * @param value
     */
    @Synchronized
    fun setAttr(socketChannel: SocketChannel?, k: String, value: Any) {
        var map = objAttrs[socketChannel]
        if (map == null) {
            objAttrs[socketChannel] = HashMap()
            map = objAttrs[socketChannel]
        }
        map!![k] = value
    }

    @Synchronized
    fun delAttr(socketChannel: SocketChannel, k: String, value: Any?) {
        var map = objAttrs[socketChannel]
        if (map == null) {
            objAttrs[socketChannel] = HashMap()
            map = objAttrs[socketChannel]
        }
        map!!.remove(k)
    }

    @Synchronized
    fun delObj(obj: Any?) {
        objAttrs.remove(obj)
    }
}