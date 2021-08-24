package com.ly.packetcapture.util

import java.nio.ByteBuffer

/**
 * 数据缓冲区
 */
object ByteBufferPool {
    private const val BUFFER_SIZE = 16384 // XXX: Is this ideal?
    @JvmStatic
    fun acquire(): ByteBuffer {
        return ByteBuffer.allocate(BUFFER_SIZE)
    }
}