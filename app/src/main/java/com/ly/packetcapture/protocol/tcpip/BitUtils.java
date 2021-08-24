package com.ly.packetcapture.protocol.tcpip;

/**
 * create by ly on 2021/8/18
 */

public class BitUtils {
     static short getUnsignedByte(byte value) {
        return (short) (value & 0xFF);
    }

     static int getUnsignedShort(short value) {
        return value & 0xFFFF;
    }

     static long getUnsignedInt(int value) {
        return value & 0xFFFFFFFFL;
    }
}
