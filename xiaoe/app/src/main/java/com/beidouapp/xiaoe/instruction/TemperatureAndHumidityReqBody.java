package com.beidouapp.xiaoe.instruction;

/**
 * @author hHui
 */
public class TemperatureAndHumidityReqBody extends Body {
    private byte data1;

    public TemperatureAndHumidityReqBody(byte data1) {
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

    public byte getData1() {
        return data1;
    }

    public void setData1(byte data1) {
        this.data1 = data1;
    }
}
