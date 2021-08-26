package com.ly.packetcapture.vpn.service

import com.ly.packetcapture.db.DatabaseUtil
import com.ly.packetcapture.db.NetData
import com.ly.packetcapture.vpn.protocol.tcpip.Packet
import com.ly.packetcapture.util.ByteBufferPool
import com.ly.packetcapture.util.LogUtil
import java.io.*
import java.lang.Exception
import java.nio.ByteBuffer
import java.util.concurrent.BlockingQueue

/**
 * create by ly on 2021/8/23
 */


/**
 * 拦截网络请求的线程
 */
class VPNRunnable(//虚拟网卡的句柄
    private val vpnFileDescriptor: FileDescriptor, //向网络发的UDP请求队列
    private val deviceToNetworkUDPQueue: BlockingQueue<Packet>, //向网络发的TCP请求队列
    private val deviceToNetworkTCPQueue: BlockingQueue<Packet>, //网络数据接收队列
    private val networkToDeviceQueue: BlockingQueue<ByteBuffer>
) : Runnable {
    override fun run() {
        val vpnInput = FileInputStream(vpnFileDescriptor).channel
        val vpnOutput = FileOutputStream(vpnFileDescriptor).channel
        val t = Thread(WriteVpnThread(vpnOutput, networkToDeviceQueue))
        t.start()
        try {
            var bufferToNetwork: ByteBuffer
            //不断去读取虚拟网卡中的网络请求数据信息，并根据UDP/TCP进行分别处理
            while (!Thread.interrupted()) {
                bufferToNetwork = ByteBufferPool.acquire()
                //拦截发送的IP包
                val readBytes = vpnInput.read(bufferToNetwork)
                //                    MainActivity.upByte.addAndGet(readBytes);
                if (readBytes > 0) {
                    bufferToNetwork.flip()
                    val packet = Packet(bufferToNetwork)
                    when {
                        packet.isUDP() -> {
                            deviceToNetworkUDPQueue.offer(packet)
                            DatabaseUtil.insert(NetData(packet.ip4Header.destinationAddress.toString(),packet.udpHeader.destinationPort.toString(),"UDP"))
                        }
                        packet.isTCP() -> {
                            deviceToNetworkTCPQueue.offer(packet)
                            DatabaseUtil.insert(NetData(packet.ip4Header.destinationAddress.toString(),packet.tcpHeader.destinationPort.toString(),"TCP"))
                        }
                        else -> {
                            LogUtil.i(String.format("未知的协议类型 %d", packet.ip4Header.protocolNum))
                        }
                    }
                } else {
                    try {
                        Thread.sleep(10)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                }
            }
        } catch (e: IOException) {
            LogUtil.w(e.toString())
            e.printStackTrace()
        } finally {
            LogUtil.e("退出")
            closeResources(vpnInput, vpnOutput)
        }
    }

    private fun closeResources(vararg resources: Closeable) {
        for (resource in resources) {
            try {
                resource.close()
            } catch (e: Exception) {
                // Ignore
            }
        }
    }
}
