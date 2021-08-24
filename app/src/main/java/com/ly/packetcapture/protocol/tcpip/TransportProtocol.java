package com.ly.packetcapture.protocol.tcpip;

/**
 * create by ly on 2021/8/18
 */

public enum TransportProtocol {
    TCP(6),
    UDP(17),
    Other(0xFF);

    private int protocolNumber;

    TransportProtocol(int protocolNumber) {
        this.protocolNumber = protocolNumber;
    }

    public static TransportProtocol numberToEnum(int protocolNumber) {
        if (protocolNumber == 6)
            return TCP;
        else if (protocolNumber == 17)
            return UDP;
        else
            return Other;
    }

    public int getNumber() {
        return this.protocolNumber;
    }
}