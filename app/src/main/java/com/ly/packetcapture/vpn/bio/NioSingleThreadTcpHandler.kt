package com.ly.packetcapture.vpn.bio

import com.ly.packetcapture.util.LogUtil.i
import com.ly.packetcapture.util.LogUtil.d
import com.ly.packetcapture.util.LogUtil.v
import android.net.VpnService
import android.util.Log
import com.ly.packetcapture.util.ObjAttrUtil
import com.ly.packetcapture.vpn.protocol.tcpip.TCBStatus
import kotlin.Throws
import com.ly.packetcapture.vpn.protocol.tcpip.IpUtil
import com.ly.packetcapture.vpn.protocol.tcpip.TCPHeader
import com.ly.packetcapture.vpn.protocol.tcpip.Packet
import com.ly.packetcapture.util.LogUtil
import java.lang.Exception
import java.lang.RuntimeException
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SelectionKey
import java.nio.channels.Selector
import java.nio.channels.ServerSocketChannel
import java.nio.channels.SocketChannel
import java.util.HashMap
import java.util.concurrent.BlockingQueue
import kotlin.experimental.and

/**
 * 从queue中获取请求，向外部网络发送请求包
 */
class NioSingleThreadTcpHandler
/**
 * @param queue                用于读包
 * @param networkToDeviceQueue 用于写数据
 * @param vpnService           用于保护地址
 */(//用于读包
    var queue: BlockingQueue<Packet>, //用于写数据
    var networkToDeviceQueue: BlockingQueue<ByteBuffer>, //用于保护地址
    var vpnService: VpnService
) : Runnable {
    private val objAttrUtil = ObjAttrUtil()
    private var selector: Selector? = null

    //Key格式：destinationAddress.getHostAddress() + ":" + destinationPort + ":" + sourcePort;
    private val pipes: MutableMap<String?, TcpPipe> = HashMap()


    @Throws(Exception::class)
    private fun initPipe(packet: Packet): TcpPipe {
        val pipe = TcpPipe()
        pipe.sourceAddress =
            InetSocketAddress(packet.ip4Header.sourceAddress, packet.tcpHeader.sourcePort)
        pipe.destinationAddress =
            InetSocketAddress(packet.ip4Header.destinationAddress, packet.tcpHeader.destinationPort)
        pipe.remote = SocketChannel.open()
        objAttrUtil.setAttr(pipe.remote, "type", "remote")
        objAttrUtil.setAttr(pipe.remote, "pipe", pipe)
        pipe.remote.configureBlocking(false)
        val key = pipe.remote.register(selector, SelectionKey.OP_CONNECT)
        objAttrUtil.setAttr(pipe.remote, "key", key)
        //very important, protect
        vpnService.protect(pipe.remote.socket())
        val b1 = pipe.remote.connect(pipe.destinationAddress)
        pipe.timestamp = System.currentTimeMillis()
        LogUtil.i(TAG, String.format("initPipe %s %s", pipe.destinationAddress.toString().replace(".", ","), b1))
        return pipe
    }

    /**
     * 发送TCP数据包
     *
     * @param pipe 通道信息类
     * @param flag 标志位信息
     * @param data 数据
     */
    private fun sendTcpPack(pipe: TcpPipe?, flag: Byte, data: ByteArray?) {
        var dataLen = 0
        if (data != null) {
            dataLen = data.size
        }
        val packet = IpUtil.buildTcpPacket(
            pipe!!.destinationAddress,
            pipe.sourceAddress,
            flag,
            pipe.myAcknowledgementNum,
            pipe.mySequenceNum,
            pipe.packId
        )
        pipe.packId += 1
        val byteBuffer = ByteBuffer.allocate(HEADER_SIZE + dataLen)
        //
        byteBuffer.position(HEADER_SIZE)
        if (data != null) {
            if (byteBuffer.remaining() < data.size) {
                System.currentTimeMillis()
            }
            byteBuffer.put(data)
        }
        //
        packet.updateTCPBuffer(
            byteBuffer,
            flag,
            pipe.mySequenceNum,
            pipe.myAcknowledgementNum,
            dataLen
        )
        byteBuffer.position(HEADER_SIZE + dataLen)
        //
        networkToDeviceQueue.offer(byteBuffer)
        if (flag.and(TCPHeader.SYN.toByte()).toInt() != 0) {
            pipe.mySequenceNum += 1
        }

        if (flag.and(TCPHeader.FIN.toByte()).toInt() != 0) {
            pipe.mySequenceNum += 1
        }
        if (flag.and(TCPHeader.ACK.toByte()).toInt() != 0) {
            pipe.mySequenceNum += dataLen.toLong()
        }
    }

    private fun handleSyn(packet: Packet, pipe: TcpPipe?) {
        if (pipe!!.tcbStatus == TCBStatus.SYN_SENT) {
            pipe.tcbStatus = TCBStatus.SYN_RECEIVED
            Log.i(TAG, String.format("handleSyn %s %s", pipe.destinationAddress, pipe.tcbStatus))
        }
        Log.i(TAG, String.format("handleSyn  %d %d", pipe.tunnelId, packet.packId))
        val tcpHeader = packet.tcpHeader
        if (pipe.synCount == 0) {
            pipe.mySequenceNum = 1
            pipe.theirSequenceNum = tcpHeader.sequenceNumber
            pipe.myAcknowledgementNum = tcpHeader.sequenceNumber + 1
            pipe.theirAcknowledgementNum = tcpHeader.acknowledgementNumber
            sendTcpPack(pipe, (TCPHeader.SYN or TCPHeader.ACK).toByte(), null)
        } else {
            pipe.myAcknowledgementNum = tcpHeader.sequenceNumber + 1
        }
        pipe.synCount += 1
    }

    private fun handleRst(packet: Packet, pipe: TcpPipe?) {
        Log.i(TAG, String.format("handleRst %d", pipe!!.tunnelId))
        pipe.upActive = false
        pipe.downActive = false
        cleanPipe(pipe)
        pipe.tcbStatus = TCBStatus.CLOSE_WAIT
    }

    @Throws(Exception::class)
    private fun handleAck(packet: Packet, pipe: TcpPipe?) {
        if (pipe!!.tcbStatus == TCBStatus.SYN_RECEIVED) {
            pipe.tcbStatus = TCBStatus.ESTABLISHED
            Log.i(TAG, String.format("handleAck %s %s", pipe.destinationAddress, pipe.tcbStatus))
        }

//        if (Config.logAck) {
        Log.d(TAG, String.format("handleAck %d ", packet.packId))
        //        }
        val tcpHeader = packet.tcpHeader
        val payloadSize = packet.backingBuffer.remaining()
        if (payloadSize == 0) {
            return
        }
        val newAck = tcpHeader.sequenceNumber + payloadSize
        if (newAck <= pipe.myAcknowledgementNum) {
//            if (Config.logAck) {
            Log.d(TAG, String.format("handleAck duplicate ack", pipe.myAcknowledgementNum, newAck))
            //            }
            return
        }
        pipe.myAcknowledgementNum = tcpHeader.sequenceNumber
        pipe.theirAcknowledgementNum = tcpHeader.acknowledgementNumber
        pipe.myAcknowledgementNum += payloadSize.toLong()
        pipe.remoteOutBuffer.put(packet.backingBuffer)
        pipe.remoteOutBuffer.flip()
        tryFlushWrite(pipe, pipe.remote)
        sendTcpPack(pipe, TCPHeader.ACK.toByte(), null)
        System.currentTimeMillis()
    }

    private fun getKey(channel: SocketChannel): SelectionKey {
        return objAttrUtil.getAttr(channel, "key") as SelectionKey
    }

    @Throws(Exception::class)
    private fun tryFlushWrite(pipe: TcpPipe?, channel: SocketChannel?): Boolean {
        val buffer = pipe!!.remoteOutBuffer
        if (pipe.remote!!.socket().isOutputShutdown && buffer.remaining() != 0) {
            sendTcpPack(pipe, (TCPHeader.FIN or TCPHeader.ACK).toByte(), null)
            buffer.compact()
            return false
        }
        if (!channel!!.isConnected) {
            i("not yet connected")
            val key = objAttrUtil.getAttr(channel, "key") as SelectionKey?
            val ops = key!!.interestOps() or SelectionKey.OP_WRITE
            key.interestOps(ops)
            System.currentTimeMillis()
            buffer.compact()
            return false
        }
        while (buffer.hasRemaining()) {
            var n = 0
            n = channel.write(buffer)
            if (n > 4000) {
                System.currentTimeMillis()
            }
            d(String.format("tryFlushWrite write %s", n))
            //            LogUtil.i("数据："+Arrays.toString(buffer.array()));
            if (n <= 0) {
                i("write fail")
                //
                val key = objAttrUtil.getAttr(channel, "key") as SelectionKey?
                val ops = key!!.interestOps() or SelectionKey.OP_WRITE
                key.interestOps(ops)
                System.currentTimeMillis()
                buffer.compact()
                return false
            }
        }
        buffer.clear()
        if (!pipe.upActive) {
            pipe.remote!!.shutdownOutput()
        }
        return true
    }

    @Throws(Exception::class)
    private fun closeUpStream(pipe: TcpPipe) {
        Log.i(TAG, String.format("closeUpStream %d", pipe.tunnelId))
        try {
            if (/*pipe.remote != null && */pipe.remote.isOpen) {
                if (pipe.remote.isConnected) {
                    pipe.remote.shutdownOutput()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        Log.i(TAG, String.format("closeUpStream %d", pipe.tunnelId))
        pipe.upActive = false
        if (isClosedTunnel(pipe)) {
            cleanPipe(pipe)
        }
    }

    @Throws(Exception::class)
    private fun handleFin(packet: Packet, pipe: TcpPipe) {
        Log.i(TAG, String.format("handleFin %d", pipe!!.tunnelId))
        pipe.myAcknowledgementNum = packet.tcpHeader.sequenceNumber + 1
        pipe.theirAcknowledgementNum = packet.tcpHeader.acknowledgementNumber
        sendTcpPack(pipe, TCPHeader.ACK.toByte(), null)
        closeUpStream(pipe)
        pipe.tcbStatus = TCBStatus.CLOSE_WAIT
        Log.i(TAG, String.format("handleFin %s %s", pipe.destinationAddress, pipe.tcbStatus))
    }

    /**
     * 处理PackAet信息
     *
     * @param pipe
     * @param packet
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun handlePacket(pipe: TcpPipe, packet: Packet) {
        var end = false
        val tcpHeader = packet.tcpHeader
        if (tcpHeader.isSYN) {
            handleSyn(packet, pipe)
            end = true
        }
        if (!end && tcpHeader.isRST) {
            handleRst(packet, pipe)
            return
        }
        if (!end && tcpHeader.isFIN) {
            handleFin(packet, pipe)
            end = true
        }
        if (!end && tcpHeader.isACK) {
            handleAck(packet, pipe)
        }
    }

    /**
     * 处理从VPNService读取的网络请求
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun handleReadFromVpn() {
        while (true) {
            val currentPacket = queue.poll() ?: return
            val destinationAddress = currentPacket.ip4Header.destinationAddress
            val tcpHeader = currentPacket.tcpHeader
            //Log.d(TAG, String.format("get pack %d tcp " + tcpHeader.printSimple() + " ", currentPacket.packId));
            val destinationPort = tcpHeader.destinationPort
            val sourcePort = tcpHeader.sourcePort
            val ipAndPort =
                destinationAddress.hostAddress + ":" + destinationPort + ":" + sourcePort
            if (!pipes.containsKey(ipAndPort)) {
                val tcpTunnel = initPipe(currentPacket)
                tcpTunnel.tunnelKey = ipAndPort
                pipes[ipAndPort] = tcpTunnel
            }
            val pipe = pipes[ipAndPort]!!
            handlePacket(pipe, currentPacket)
        }
    }

    @Throws(Exception::class)
    private fun doAccept(serverChannel: ServerSocketChannel) {
        throw RuntimeException("")
    }

    @Throws(Exception::class)
    private fun doRead(channel: SocketChannel) {
        val buffer = ByteBuffer.allocate(4 * 1024)
        var quitType = ""
        val pipe = objAttrUtil.getAttr(channel, "pipe") as TcpPipe
        while (true) {
            buffer.clear()

            val n = channel.read(buffer)
            Log.i(TAG,  String.format("read %d %s ", n, channel.toString()))
            if (n == -1) {
                quitType = "fin"
                break
            } else if (n == 0) {
                break
            } else {
                if (pipe.tcbStatus != TCBStatus.CLOSE_WAIT) {
                    buffer.flip()
                    val data = ByteArray(buffer.remaining())
                    buffer[data]
                    sendTcpPack(pipe, TCPHeader.ACK.toByte(), data)
                }
            }
        }
        if (quitType == "fin") {
            closeDownStream(pipe)
        }
    }

    private fun cleanPipe(pipe: TcpPipe) {
        try {
            if (/*pipe.remote != null && */pipe.remote.isOpen) {
                pipe.remote.close()
            }
            pipes.remove(pipe.tunnelKey)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @Throws(Exception::class)
    private fun closeRst(pipe: TcpPipe) {
        Log.i(TAG, String.format("closeRst %d", pipe.tunnelId))
        cleanPipe(pipe)
        sendTcpPack(pipe, TCPHeader.RST.toByte(), null)
        pipe.upActive = false
        pipe.downActive = false
    }

    @Throws(Exception::class)
    private fun closeDownStream(pipe: TcpPipe) {
        Log.i(TAG, String.format("closeDownStream %d", pipe.tunnelId))
        if (pipe.remote.isConnected) {
            pipe.remote.shutdownInput()
            val ops = getKey(pipe.remote).interestOps() and SelectionKey.OP_READ.inv()
            getKey(pipe.remote).interestOps(ops)
        }
        sendTcpPack(pipe, (TCPHeader.FIN or TCPHeader.ACK).toByte(), null)
        pipe.downActive = false
        if (isClosedTunnel(pipe)) {
            cleanPipe(pipe)
        }
    }

    private fun isClosedTunnel(tunnel: TcpPipe): Boolean {
        return !tunnel!!.upActive && !tunnel.downActive
    }

    @Throws(Exception::class)
    private fun doConnect(socketChannel: SocketChannel) {
        Log.i(TAG, String.format("tick %s", tick))
        val type = objAttrUtil.getAttr(socketChannel, "type") as String
        val pipe = objAttrUtil.getAttr(socketChannel, "pipe") as TcpPipe
        val key = objAttrUtil.getAttr(socketChannel, "key") as SelectionKey
        v(
            "doConnect::::" + "::RemoteAddress::" + socketChannel.remoteAddress.toString()
                .replace(".", ",") + "::localAddress::" + socketChannel.localAddress.toString()
                .replace(".", ",")
        )
        if (type == "remote") {
            val b1 = socketChannel.finishConnect()
            Log.i(
                TAG,
                String.format(
                    "connect %s %s %s",
                    pipe.destinationAddress,
                    b1,
                    System.currentTimeMillis() - pipe.timestamp
                )
            )
            pipe.timestamp = System.currentTimeMillis()
            pipe.remoteOutBuffer.flip()
            key.interestOps(SelectionKey.OP_READ or SelectionKey.OP_WRITE)
        }
    }

    @Throws(Exception::class)
    private fun doWrite(socketChannel: SocketChannel) {
        Log.i(TAG, String.format("tick %s", tick))
        val pipe = objAttrUtil.getAttr(socketChannel, "pipe") as TcpPipe?
        val flushed = tryFlushWrite(pipe, socketChannel)
        if (flushed) {
            val key1 = objAttrUtil.getAttr(socketChannel, "key") as SelectionKey?
            key1!!.interestOps(SelectionKey.OP_READ)
        }
    }

    /**
     * 处理Sockets
     *
     * @throws Exception
     */
    @Throws(Exception::class)
    private fun handleSockets() {
        while (selector!!.selectNow() > 0) {
            val it: MutableIterator<*> = selector!!.selectedKeys().iterator()
            while (it.hasNext()) {
                val key = it.next() as SelectionKey
                it.remove()
                val pipe = objAttrUtil.getAttr(key.channel(), "pipe") as TcpPipe?
                if (key.isValid) {
                    try {
                        if (key.isAcceptable) {
                            doAccept(key.channel() as ServerSocketChannel)
                        } else if (key.isReadable) {
                            doRead(key.channel() as SocketChannel)
                        } else if (key.isConnectable) {
                            doConnect(key.channel() as SocketChannel)
                        } else if (key.isWritable) {
                            doWrite(key.channel() as SocketChannel)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        //                        Log.e(TAG, e.getMessage(), e);
                        pipe?.let { closeRst(it) }
                    }
                }
            }
        }
    }

    private var tick: Long = 0
    override fun run() {
        try {
            selector = Selector.open()
            while (true) {
                handleReadFromVpn()
                handleSockets()
                tick += 1
                //                Thread.sleep(1);
            }
        } catch (e: Exception) {
            Log.e(e.message, "", e)
        }
    }

    companion object {
        private val TAG = NioSingleThreadTcpHandler::class.java.simpleName
        private const val HEADER_SIZE = Packet.IP4_HEADER_SIZE + Packet.TCP_HEADER_SIZE
    }
}