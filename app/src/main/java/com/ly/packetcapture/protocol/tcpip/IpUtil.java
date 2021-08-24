package com.ly.packetcapture.protocol.tcpip;


import com.ly.packetcapture.util.ByteBufferPool;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

public class IpUtil {
    public static Packet buildUdpPacket(InetSocketAddress source, InetSocketAddress dest, int ipId) {
        Packet packet = new Packet();
        packet.isTCP = false;
        packet.isUDP = true;
        IP4Header ip4Header = new IP4Header();
        ip4Header.version = 4;
        ip4Header.IHL = 5;
        ip4Header.destinationAddress = dest.getAddress();
        ip4Header.headerChecksum = 0;
        ip4Header.headerLength = 20;

        //int ipId=0;
        int ipFlag = 0x40;
        int ipOff = 0;

        ip4Header.identificationAndFlagsAndFragmentOffset = ipId << 16 | ipFlag << 8 | ipOff;

        ip4Header.optionsAndPadding = 0;
        ip4Header.protocol = TransportProtocol.UDP;
        ip4Header.protocolNum = 17;
        ip4Header.sourceAddress = source.getAddress();
        ip4Header.totalLength = 60;
        ip4Header.typeOfService = 0;
        ip4Header.TTL = 64;

        UDPHeader udpHeader = new UDPHeader();
        udpHeader.sourcePort = source.getPort();
        udpHeader.destinationPort = dest.getPort();
        udpHeader.length = 0;

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        packet.ip4Header = ip4Header;
        packet.udpHeader = udpHeader;
        packet.backingBuffer = byteBuffer;
        logInfo(packet);
        return packet;
    }
    public static Packet buildTcpPacket(InetSocketAddress source, InetSocketAddress dest, byte flag, long ack, long seq, int ipId) {
        Packet packet = new Packet();
        packet.isTCP = true;
        packet.isUDP = false;
        IP4Header ip4Header = new IP4Header();
        ip4Header.version = 4;
        ip4Header.IHL = 5;
        ip4Header.destinationAddress = dest.getAddress();
        ip4Header.headerChecksum = 0;
        ip4Header.headerLength = 20;

        //int ipId=0;
        int ipFlag = 0x40;
        int ipOff = 0;

        ip4Header.identificationAndFlagsAndFragmentOffset = ipId << 16 | ipFlag << 8 | ipOff;

        ip4Header.optionsAndPadding = 0;
        ip4Header.protocol =TransportProtocol.TCP;
        ip4Header.protocolNum = 6;
        ip4Header.sourceAddress = source.getAddress();
        ip4Header.totalLength = 60;
        ip4Header.typeOfService = 0;
        ip4Header.TTL = 64;

        TCPHeader tcpHeader = new TCPHeader();
        tcpHeader.acknowledgementNumber = ack;
        tcpHeader.checksum = 0;
        tcpHeader.dataOffsetAndReserved = -96;
        tcpHeader.destinationPort = dest.getPort();
        tcpHeader.flags = flag;
        tcpHeader.headerLength = 40;
        tcpHeader.optionsAndPadding = null;
        tcpHeader.sequenceNumber = seq;
        tcpHeader.sourcePort = source.getPort();
        tcpHeader.urgentPointer = 0;
        tcpHeader.window = 65535;

        ByteBuffer byteBuffer = ByteBufferPool.acquire();
        byteBuffer.flip();

        packet.ip4Header = ip4Header;
        packet.tcpHeader = tcpHeader;
        packet.backingBuffer = byteBuffer;
        logInfo(packet);
        return packet;
    }

    public static void logInfo( Packet packet){
        StringBuilder info= new StringBuilder();
        info.append("应答包信息：：源地址："+packet.ip4Header.sourceAddress.toString().replace(".",",")+"::目的地址::"+packet.ip4Header.destinationAddress.toString().replace(".",",")+"::");
        if(packet.isTCP){
            info.append("源端口::"+packet.tcpHeader.sourcePort+"::目的端口::"+packet.tcpHeader.destinationPort+"::");
            info.append("序列号::"+packet.tcpHeader.sequenceNumber+"::确认应答号::"+packet.tcpHeader.acknowledgementNumber+"::");
            info.append("长度:"+packet.ip4Header.totalLength+":头长度:"+packet.ip4Header.headerLength);
            info.append("行为");
            if(packet.tcpHeader.isACK()){
                info.append("::ACK");
            }
            if(packet.tcpHeader.isFIN()){
                info.append("::FIN");
            }
            if(packet.tcpHeader.isPSH()){
                info.append("::PSH");
            }
            if(packet.tcpHeader.isRST()){
                info.append("::RST");
            }
            if(packet.tcpHeader.isSYN()){
                info.append("::SYN");
            }
            if(packet.tcpHeader.isURG()){
                info.append("::URG");
            }

        }else if(packet.isUDP) {
            info.append("源端口::"+packet.udpHeader.sourcePort+"::目的端口::"+packet.udpHeader.destinationPort);
        }
//        LogUtil.w(info.toString());
    }
}
