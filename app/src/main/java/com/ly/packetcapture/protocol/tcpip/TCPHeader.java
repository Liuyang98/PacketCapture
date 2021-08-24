package com.ly.packetcapture.protocol.tcpip;


import java.nio.ByteBuffer;

/**
 * create by ly on 2021/8/18
 */
public class TCPHeader {
    public static final int FIN = 0x01;//关闭连接
    public static final int SYN = 0x02;//建立连接
    public static final int RST = 0x04;//连接重置
    public static final int PSH = 0x08;//有DATA数据传输
    public static final int ACK = 0x10;//响应
    public static final int URG = 0x20;//紧急

    public int sourcePort;//源端口
    public int destinationPort;//目的端口

    public long sequenceNumber;
    public long acknowledgementNumber;

    public byte dataOffsetAndReserved;
    public int headerLength;
    public byte flags;
    public int window;

    public int checksum;
    public int urgentPointer;

    public byte[] optionsAndPadding;

    public TCPHeader(ByteBuffer buffer) {
        this.sourcePort = BitUtils.getUnsignedShort(buffer.getShort());
        this.destinationPort = BitUtils.getUnsignedShort(buffer.getShort());

        this.sequenceNumber = BitUtils.getUnsignedInt(buffer.getInt());
        this.acknowledgementNumber = BitUtils.getUnsignedInt(buffer.getInt());

        this.dataOffsetAndReserved = buffer.get();
        this.headerLength = (this.dataOffsetAndReserved & 0xF0) >> 2;
        this.flags = buffer.get();
        this.window = BitUtils.getUnsignedShort(buffer.getShort());

        this.checksum = BitUtils.getUnsignedShort(buffer.getShort());
        this.urgentPointer = BitUtils.getUnsignedShort(buffer.getShort());

        int optionsLength = this.headerLength - Packet.TCP_HEADER_SIZE;
        if (optionsLength > 0) {
            optionsAndPadding = new byte[optionsLength];
            buffer.get(optionsAndPadding, 0, optionsLength);
        }
    }

    public TCPHeader() {

    }

    public boolean isFIN() {
        return (flags & FIN) == FIN;
    }

    public boolean isSYN() {
        return (flags & SYN) == SYN;
    }



    public boolean isRST() {
        return (flags & RST) == RST;
    }

    public boolean isPSH() {
        return (flags & PSH) == PSH;
    }

    public boolean isACK() {
        return (flags & ACK) == ACK;
    }

    public boolean isURG() {
        return (flags & URG) == URG;
    }

    public void fillHeader(ByteBuffer buffer) {
        buffer.putShort((short) sourcePort);
        buffer.putShort((short) destinationPort);

        buffer.putInt((int) sequenceNumber);
        buffer.putInt((int) acknowledgementNumber);

        buffer.put(dataOffsetAndReserved);
        buffer.put(flags);
        buffer.putShort((short) window);

        buffer.putShort((short) checksum);
        buffer.putShort((short) urgentPointer);
    }

    public static String flagToString(byte flags){
        final StringBuilder sb = new StringBuilder("");
        if ((flags & FIN) == FIN) sb.append("FIN ");
        if ((flags & SYN) == SYN) sb.append("SYN " );
        if ((flags & RST) == RST) sb.append("RST ");
        if ((flags & PSH) == PSH) sb.append("PSH ");
        if ((flags & ACK) == ACK) sb.append("ACK " );
        if ((flags & URG) == URG) sb.append("URG ");
        return sb.toString();
    }
    public String printSimple() {
        final StringBuilder sb = new StringBuilder("");
        if (isFIN()) sb.append("FIN ");
        if (isSYN()) sb.append("SYN ");
        if (isRST()) sb.append("RST ");
        if (isPSH()) sb.append("PSH ");
        if (isACK()) sb.append("ACK ");
        if (isURG()) sb.append("URG ");
        sb.append("seq "+sequenceNumber +" ");
        sb.append("ack "+acknowledgementNumber+" ");
        return sb.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TCPHeader{");
        sb.append("sourcePort=").append(sourcePort);
        sb.append(", destinationPort=").append(destinationPort);
        sb.append(", sequenceNumber=").append(sequenceNumber);
        sb.append(", acknowledgementNumber=").append(acknowledgementNumber);
        sb.append(", headerLength=").append(headerLength);
        sb.append(", window=").append(window);
        sb.append(", checksum=").append(checksum);
        sb.append(", flags=");
        if (isFIN()) sb.append(" FIN");
        if (isSYN()) sb.append(" SYN");
        if (isRST()) sb.append(" RST");
        if (isPSH()) sb.append(" PSH");
        if (isACK()) sb.append(" ACK");
        if (isURG()) sb.append(" URG");
        sb.append('}');
        return sb.toString();
    }
}