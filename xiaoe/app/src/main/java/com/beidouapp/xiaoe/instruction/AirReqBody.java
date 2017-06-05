package com.beidouapp.xiaoe.instruction;

/**
 * Created by hHui on 2017/6/5.
 */

public class AirReqBody extends Body {
    private byte data1;

    public AirReqBody(byte data1) {
        this.data1 = data1;
    }

    @Override
    public byte[] toByteArray() {
        byte[] bytes = new byte[1];
        bytes[0] = data1;
        return bytes;
    }

    @Override
    public int getLength() {
        return 1;
    }
}
