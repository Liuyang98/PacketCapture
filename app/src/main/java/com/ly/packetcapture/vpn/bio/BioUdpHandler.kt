package com.ly.packetcapture.vpn.bio

import android.net.VpnService
import com.ly.packetcapture.db.DatabaseUtil.insert
import com.ly.packetcapture.db.NetData
import com.ly.packetcapture.vpn.protocol.dns.DnsPacket
import com.ly.packetcapture.vpn.protocol.tcpip.IpUtil
import com.ly.packetcapture.vpn.protocol.tcpip.Packet
import com.ly.packetcapture.util.ByteBufferPool.acquire
import com.ly.packetcapture.util.LogUtil
import java.io.IOException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.DatagramChannel
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.atomic.AtomicInteger
import kotlin.system.exitProcess

/**
 * BIO UDP处理器
 * 从queue获取udp包，转发给指定的dns
 */
class BioUdpHandler(var queue: BlockingQueue<Packet>, var networkToDeviceQueue: BlockingQueue<ByteBuffer>, var vpnService: VpnService) : Runnable {
    private lateinit var selector: Selector
    private class UdpDownWorker(var selector: Selector?, var networkToDeviceQueue: BlockingQueue<ByteBuffer>, var tunnelQueue: BlockingQueue<UdpTunnel>) : Runnable {
        @Throws(IOException::class)
        private fun sendUdpPack(tunnel: UdpTunnel, source: InetSocketAddress, data: ByteArray?) {
            var dataLen = 0
            if (data != null) {
                dataLen = data.size
            }

            val packet = IpUtil.buildUdpPacket(tunnel.remote, tunnel.local, ipId.addAndGet(1))
            val byteBuffer = acquire()
            byteBuffer.position(HEADER_SIZE)
            data?.let { byteBuffer.put(it) }

            packet.updateUDPBuffer(byteBuffer, dataLen)
            logInfo(packet)
            byteBuffer.position(HEADER_SIZE + dataLen)
            networkToDeviceQueue.offer(byteBuffer)
        }

        private fun logInfo(packet: Packet) {
            if (packet.isUDP() && packet.udpHeader.sourcePort == 53) {
                val mDNSBuffer = (ByteBuffer.wrap(packet.backingBuffer.array())
                    .position(28) as ByteBuffer).slice()
                mDNSBuffer.clear()
                mDNSBuffer.limit(packet.ip4Header.totalLength - 8)
                val dnsPacket = DnsPacket.fromBytes(mDNSBuffer)
                if (dnsPacket != null && dnsPacket.Header.QuestionCount > 0) {
                    val writeByteBuffer = ByteBuffer.allocate(4000)
                    dnsPacket.toBytes(writeByteBuffer)
                } else {
                    return
                }
                val length = dnsPacket.Resources.size
                for (index in 0 until length) {
                    if (dnsPacket.Resources[index].Type.toInt() == 1) {
                        var ips = Arrays.toString(dnsPacket.Resources[index].Data)
                        ips = ips.substring(1, ips.length - 1)
                        val ipList = ips.split(",").toTypedArray()
                        val ipSb = StringBuilder()
                        for (ip in ipList) {
                            val ipInt = ip.trim { it <= ' ' }.toInt()
                            if (ipInt >= 0) {
                                ipSb.append(ipInt)
                            } else {
                                ipSb.append(ipInt + 256)
                            }
                            ipSb.append(".")
                        }
                        insert(NetData(dnsPacket.Resources[index].Domain, ipSb.substring(0, ipSb.length - 1)))
                    }
                }
            }
        }

        override fun run() {
            try {
                while (true) {
                    val readyChannels = selector!!.select()
                    while (true) {
                        val tunnel = tunnelQueue.poll()
                        if (tunnel == null) {
                            break
                        } else {
                            try {
                                val key = tunnel.channel!!.register(
                                    selector, SelectionKey.OP_READ, tunnel
                                )
                                key.interestOps(SelectionKey.OP_READ)
                                val isvalid = key.isValid
                            } catch (e: IOException) {
                                LogUtil.d("register fail")
                                e.printStackTrace()
                            }
                        }
                    }
                    if (readyChannels == 0) {
                        selector!!.selectedKeys().clear()
                        continue
                    }
                    val keys = selector!!.selectedKeys()
                    val keyIterator = keys.iterator()
                    while (keyIterator.hasNext()) {
                        val key = keyIterator.next()
                        keyIterator.remove()
                        if (key.isValid && key.isReadable) {
                            try {
                                val inputChannel = key.channel() as DatagramChannel
                                val receiveBuffer = acquire()
                                val readBytes = inputChannel.read(receiveBuffer)
                                receiveBuffer.flip()
                                val data = ByteArray(receiveBuffer.remaining())
                                receiveBuffer[data]
                                sendUdpPack(
                                    key.attachment() as UdpTunnel,
                                    inputChannel.localAddress as InetSocketAddress,
                                    data
                                )
                            } catch (e: IOException) {
                                LogUtil.e("error", e)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                LogUtil.e("error", e)
                exitProcess(0)
            } finally {
                LogUtil.d("BioUdpHandler quit")
            }
        }

        companion object {
            private val ipId = AtomicInteger()
        }
    }

    var udpSockets: MutableMap<String, DatagramChannel> = mutableMapOf()

    private class UdpTunnel {
        var local: InetSocketAddress? = null
        var remote: InetSocketAddress? = null
        var channel: DatagramChannel? = null
    }

    override fun run() {
        try {
            val tunnelQueue: BlockingQueue<UdpTunnel> = ArrayBlockingQueue(100)
            selector = Selector.open()
            val t = Thread(UdpDownWorker(selector, networkToDeviceQueue, tunnelQueue))
            t.start()
            while (true) {
                val packet = queue.take()
                val destinationAddress = packet.ip4Header.destinationAddress
                val header = packet.udpHeader
                val destinationPort = header.destinationPort
                val sourcePort = header.sourcePort
                val ipAndPort =
                    destinationAddress.hostAddress + ":" + destinationPort + ":" + sourcePort
                //如果没有根据目的IP和端口号建立索引，则建立通道（DatagramChannel是收发UDP包的通道）
                if (!udpSockets.containsKey(ipAndPort)) {
                    val outputChannel = DatagramChannel.open()
                    vpnService.protect(outputChannel.socket())
                    outputChannel.socket().bind(null)
                    outputChannel.connect(InetSocketAddress(destinationAddress, destinationPort))
                    outputChannel.configureBlocking(false)
                    val tunnel = UdpTunnel()
                    tunnel.local =
                        InetSocketAddress(packet.ip4Header.sourceAddress, header.sourcePort)
                    tunnel.remote = InetSocketAddress(
                        packet.ip4Header.destinationAddress,
                        header.destinationPort
                    )
                    tunnel.channel = outputChannel
                    tunnelQueue.offer(tunnel)
                    selector.wakeup()
                    udpSockets[ipAndPort] = outputChannel
                }
                val outputChannel = udpSockets[ipAndPort]
                val buffer = packet.backingBuffer
                try {
                    while (packet.backingBuffer.hasRemaining()) {
                        val w = outputChannel!!.write(buffer)
                        LogUtil.d(String.format("write udp pack %d len %d %s ", packet.packId, w, ipAndPort))
                    }
                } catch (e: IOException) {
                    //UDP 请求超时、请求完结处理
                    LogUtil.e("udp write error", e)
                    outputChannel!!.close()
                    udpSockets.remove(ipAndPort)
                }
            }
        } catch (e: Exception) {
            LogUtil.e("error", e)
        }
    }

    companion object {
        private const val HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.UDP_HEADER_SIZE
    }
}