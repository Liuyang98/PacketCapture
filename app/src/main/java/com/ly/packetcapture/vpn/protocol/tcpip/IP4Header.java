package com.ly.packetcapture.vpn.protocol.tcpip;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

/**
 * create by ly on 2021/8/18
 */

public  class IP4Header {
    public byte version;
    public byte IHL;
    public int headerLength;
    public short typeOfService;
    public int totalLength;

    public int identificationAndFlagsAndFragmentOffset;

    public short TTL;
    public short protocolNum;
    public TransportProtocol protocol;
    public int headerChecksum;
    public InetAddress sourceAddress; //源地址
    public InetAddress destinationAddress; //目的地址

    public int optionsAndPadding;


    public IP4Header() {

    }

    public IP4Header(ByteBuffer buffer) throws UnknownHostException {
        byte versionAndIHL = buffer.get();
        this.version = (byte) (versionAndIHL >> 4);
        this.IHL = (byte) (versionAndIHL & 0x0F);
        this.headerLength = this.IHL << 2;

        this.typeOfService = BitUtils.getUnsignedByte(buffer.get());
        this.totalLength = BitUtils.getUnsignedShort(buffer.getShort());

        this.identificationAndFlagsAndFragmentOffset = buffer.getInt();

        this.TTL = BitUtils.getUnsignedByte(buffer.get());
        this.protocolNum = BitUtils.getUnsignedByte(buffer.get());
        this.protocol = TransportProtocol.numberToEnum(protocolNum);
        this.headerChecksum = BitUtils.getUnsignedShort(buffer.getShort());

        byte[] addressBytes = new byte[4];
        buffer.get(addressBytes, 0, 4);
        this.sourceAddress = InetAddress.getByAddress(addressBytes);

        buffer.get(addressBytes, 0, 4);
        this.destinationAddress = InetAddress.getByAddress(addressBytes);

        //this.optionsAndPadding = buffer.getInt();
    }

    public void fillHeader(ByteBuffer buffer) {
        buffer.put((byte) (this.version << 4 | this.IHL));
        buffer.put((byte) this.typeOfService);
        buffer.putShort((short) this.totalLength);

        buffer.putInt(this.identificationAndFlagsAndFragmentOffset);

        buffer.put((byte) this.TTL);
        buffer.put((byte) this.protocol.getNumber());
        buffer.putShort((short) this.headerChecksum);

        buffer.put(this.sourceAddress.getAddress());
        buffer.put(this.destinationAddress.getAddress());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("IP4Header{");
        sb.append("version=").append(version);
        sb.append(", IHL=").append(IHL);
        sb.append(", typeOfService=").append(typeOfService);
        sb.append(", totalLength=").append(totalLength);
        sb.append(", identificationAndFlagsAndFragmentOffset=").append(identificationAndFlagsAndFragmentOffset);
        sb.append(", TTL=").append(TTL);
        sb.append(", protocol=").append(protocolNum).append(":").append(protocol);
        sb.append(", headerChecksum=").append(headerChecksum);
        sb.append(", sourceAddress=").append(sourceAddress.getHostAddress());
        sb.append(", destinationAddress=").append(destinationAddress.getHostAddress());
        sb.append('}');
        return sb.toString();
    }
}