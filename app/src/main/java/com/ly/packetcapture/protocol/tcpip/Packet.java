package com.ly.packetcapture.protocol.tcpip;

import com.ly.packetcapture.util.LogUtil;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * IP包表示类
 */
public class Packet {
    public static final int IP4_HEADER_SIZE = 20;
    public static final int UDP_HEADER_SIZE = 8;
    public static final int TCP_HEADER_SIZE = 20;


    private static AtomicInteger globalPackId=new AtomicInteger();
    public int packId=globalPackId.addAndGet(1);
    public IP4Header ip4Header;
    public TCPHeader tcpHeader;
    public UDPHeader udpHeader;
    public ByteBuffer backingBuffer;

    public boolean isTCP;
    public boolean isUDP;

    public Packet() {

    }

    /**
     * 向外发送网络数据时使用的构造方法
     * @param buffer
     * @throws UnknownHostException
     */
    public Packet(ByteBuffer buffer) throws UnknownHostException {
        this.ip4Header = new IP4Header(buffer);
        if (this.ip4Header.protocol == TransportProtocol.TCP) {
            this.tcpHeader = new TCPHeader(buffer);
            this.isTCP = true;
        } else if (ip4Header.protocol == TransportProtocol.UDP) {
            this.udpHeader = new UDPHeader(buffer);
            this.isUDP = true;
        }
        this.backingBuffer = buffer;
//        logInfo();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Packet{");
        sb.append("ip4Header=").append(ip4Header);
        if (isTCP) sb.append(", tcpHeader=").append(tcpHeader);
        else if (isUDP) sb.append(", udpHeader=").append(udpHeader);
        sb.append(", payloadSize=").append(backingBuffer.limit() - backingBuffer.position());
        sb.append('}');
        return sb.toString();
    }

    public boolean isTCP() {
        return isTCP;
    }

    public boolean isUDP() {
        return isUDP;
    }

    /**
     * 更新TCP数据包信息
     * @param buffer
     * @param flags
     * @param sequenceNum
     * @param ackNum
     * @param payloadSize
     */
    public void updateTCPBuffer(ByteBuffer buffer, byte flags, long sequenceNum, long ackNum, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        tcpHeader.flags = flags;
        backingBuffer.put(IP4_HEADER_SIZE + 13, flags);

        tcpHeader.sequenceNumber = sequenceNum;
        backingBuffer.putInt(IP4_HEADER_SIZE + 4, (int) sequenceNum);

        tcpHeader.acknowledgementNumber = ackNum;
        backingBuffer.putInt(IP4_HEADER_SIZE + 8, (int) ackNum);

        // Reset header size, since we don't need options
        byte dataOffset = (byte) (TCP_HEADER_SIZE << 2);
        tcpHeader.dataOffsetAndReserved = dataOffset;
        backingBuffer.put(IP4_HEADER_SIZE + 12, dataOffset);

        updateTCPChecksum(payloadSize);

        int ip4TotalLength = IP4_HEADER_SIZE + TCP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(2, (short) ip4TotalLength);
        ip4Header.totalLength = ip4TotalLength;

        updateIP4Checksum();
    }

    /**
     * 更新UDP数据包信息
     * @param buffer
     * @param payloadSize
     */
    public void updateUDPBuffer(ByteBuffer buffer, int payloadSize) {
        buffer.position(0);
        fillHeader(buffer);
        backingBuffer = buffer;

        int udpTotalLength = UDP_HEADER_SIZE + payloadSize;
        backingBuffer.putShort(IP4_HEADER_SIZE + 4, (short) udpTotalLength);
        udpHeader.length = udpTotalLength;

        // Disable UDP checksum validation
        backingBuffer.putShort(IP4_HEADER_SIZE + 6, (short) 0);
        udpHeader.checksum = 0;

        int ip4TotalLength = IP4_HEADER_SIZE + udpTotalLength;
        backingBuffer.putShort(2, (short) ip4TotalLength);
        ip4Header.totalLength = ip4TotalLength;

        updateIP4Checksum();
    }

    private void updateIP4Checksum() {
        ByteBuffer buffer = backingBuffer.duplicate();
        buffer.position(0);

        // Clear previous checksum
        buffer.putShort(10, (short) 0);

        int ipLength = ip4Header.headerLength;
        int sum = 0;
        while (ipLength > 0) {
            sum += BitUtils.getUnsignedShort(buffer.getShort());
            ipLength -= 2;
        }
        while (sum >> 16 > 0)
            sum = (sum & 0xFFFF) + (sum >> 16);

        sum = ~sum;
        ip4Header.headerChecksum = sum;
        backingBuffer.putShort(10, (short) sum);
    }

    private void updateTCPChecksum(int payloadSize) {
        int sum = 0;
        int tcpLength = TCP_HEADER_SIZE + payloadSize;

        // Calculate pseudo-header checksum
        ByteBuffer buffer = ByteBuffer.wrap(ip4Header.sourceAddress.getAddress());
        sum = BitUtils.getUnsignedShort(buffer.getShort()) + BitUtils.getUnsignedShort(buffer.getShort());

        buffer = ByteBuffer.wrap(ip4Header.destinationAddress.getAddress());
        sum += BitUtils.getUnsignedShort(buffer.getShort()) + BitUtils.getUnsignedShort(buffer.getShort());

        sum += TransportProtocol.TCP.getNumber() + tcpLength;

        buffer = backingBuffer.duplicate();
        // Clear previous checksum
        buffer.putShort(IP4_HEADER_SIZE + 16, (short) 0);

        // Calculate TCP segment checksum
        buffer.position(IP4_HEADER_SIZE);
        while (tcpLength > 1) {
            sum += BitUtils.getUnsignedShort(buffer.getShort());
            tcpLength -= 2;
        }
        if (tcpLength > 0)
            sum += BitUtils.getUnsignedByte(buffer.get()) << 8;

        while (sum >> 16 > 0)
            sum = (sum & 0xFFFF) + (sum >> 16);

        sum = ~sum;
        tcpHeader.checksum = sum;
        backingBuffer.putShort(IP4_HEADER_SIZE + 16, (short) sum);
    }

    private void fillHeader(ByteBuffer buffer) {
        ip4Header.fillHeader(buffer);
        if (isUDP)
            udpHeader.fillHeader(buffer);
        else if (isTCP)
            tcpHeader.fillHeader(buffer);
    }



    public void logInfo(){
        StringBuilder info= new StringBuilder();
        info.append("请求包信息：：源地址："+ip4Header.sourceAddress.toString().replace(".",",")+"::目的地址::"+ip4Header.destinationAddress.toString().replace(".",",")+"::");
        if(this.isTCP){
            info.append("源端口::"+this.tcpHeader.sourcePort+"::目的端口::"+this.tcpHeader.destinationPort+"::");
            info.append("序列号::"+this.tcpHeader.sequenceNumber+"::确认应答号::"+this.tcpHeader.acknowledgementNumber+"::");
            info.append("行为");
            if(this.tcpHeader.isACK()){
                info.append("::ACK");
            }
            if(this.tcpHeader.isFIN()){
                info.append("::FIN");
            }
            if(this.tcpHeader.isPSH()){
                info.append("::PSH");
            }
            if(this.tcpHeader.isRST()){
                info.append("::RST");
            }
            if(this.tcpHeader.isSYN()){
                info.append("::SYN");
            }
            if(this.tcpHeader.isURG()){
                info.append("::URG");
            }
        }else if(this.isUDP) {
            info.append("源端口::"+this.udpHeader.sourcePort+"::目的端口::"+this.udpHeader.destinationPort);
        }
        LogUtil.INSTANCE.d(info.toString());
    }

}

