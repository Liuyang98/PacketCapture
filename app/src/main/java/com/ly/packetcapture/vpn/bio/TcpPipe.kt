package com.ly.packetcapture.vpn.bio

import com.ly.packetcapture.vpn.protocol.tcpip.TCBStatus
import java.net.InetSocketAddress
import java.nio.ByteBuffer
import java.nio.channels.SocketChannel

/**
 * create by ly on 2021/8/23
 */
class TcpPipe {
    var mySequenceNum: Long = 0
    var theirSequenceNum: Long = 0
    var myAcknowledgementNum: Long = 0
    var theirAcknowledgementNum: Long = 0
    val tunnelId = tunnelIds++
    var tunnelKey: String? = null
    var sourceAddress: InetSocketAddress? = null
    var destinationAddress: InetSocketAddress? = null
    lateinit var remote: SocketChannel
    var tcbStatus = TCBStatus.SYN_SENT
    val remoteOutBuffer = ByteBuffer.allocate(8 * 1024)
    var upActive = true
    var downActive = true
    var packId = 1
    var timestamp = 0L
    var synCount = 0

    companion object {
        var tunnelIds = 0
    }
}