package com.beidouapp.xiaoe.instruction;

import java.io.UnsupportedEncodingException;

/**
 * Created by hHui on 2017/6/5.
 */

public class LEDControllerReqBody extends Body {
    String data = null;
    int length = 0;

    public LEDControllerReqBody(String data) {
        this.data = data;
        length = data.length() * 2 + 1;
    }

    @Override
    public byte[] toByteArray() {
        try {
            byte[] dataBytes = data.getBytes("gbk");
            byte[] datas = new byte[length];
            datas[0] = 0x40;
            for (int i = 0; i < dataBytes.length; i++) {
                int a = (int) dataBytes[i];
                datas[i + 1] = (byte) (96 + a);//编码规则  gb2321
            }
            return datas;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public int getLength() {
        return length;
    }
}
