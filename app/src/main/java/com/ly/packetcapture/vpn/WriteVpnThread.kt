package com.ly.packetcapture.vpn

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
                    val w = vpnOutput.write(bufferFromNetwork)
                    //                        LogUtil.e("写入虚拟网卡：w=" + w + "::内容::" + Arrays.toString(bufferFromNetwork.array()));
                    if (w > 0) {
//                            MainActivity.downByte.addAndGet(w);
                    }
                    //                        LogUtil.i( "写入虚拟网卡 w = " + w);
                }
            } catch (e: Exception) {
                LogUtil.i("WriteVpnThread fail")
                e.printStackTrace()
            }
        }
    }
}