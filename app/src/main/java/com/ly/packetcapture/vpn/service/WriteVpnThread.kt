package com.ly.packetcapture.vpn.service

import com.ly.packetcapture.util.LogUtil
import java.lang.Exception
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.BlockingQueue

/**
 * create by ly on 2021/8/23
 */
class WriteVpnThread internal constructor(var vpnOutput: FileChannel, private val networkToDeviceQueue: BlockingQueue<ByteBuffer>) : Runnable {
    override fun run() {
        while (true) {
            try {
                val bufferFromNetwork = networkToDeviceQueue.take()
                bufferFromNetwork!!.flip()
                while (bufferFromNetwork.hasRemaining()) {
                   vpnOutput.write(bufferFromNetwork)
                }
            } catch (e: Exception) {
                LogUtil.i("WriteVpnThread fail")
                e.printStackTrace()
            }
        }
    }
}