package com.beidouapp.xiaoe.instruction;

/**
 * Created by hHui on 2017/6/5.
 */

public class RGBControllerReqBody extends Body {
    byte[] data = new byte[4];

    public RGBControllerReqBody(int index) {
        data[0] = 0x10;
        switch (index) {
            case 0://紫红
                data[1] = (byte) 230;
                data[2] = (byte) 29;
                data[3] = (byte) 190;
                break;
            case 1://大红
                data[1] = (byte) 250;
                data[2] = (byte) 40;
                data[3] = (byte) 11;
                break;
            case 2://橘红
                data[1] = (byte) 255;
                data[2] = (byte) 126;
                data[3] = (byte) 0;
                break;
            case 3://橘黄
                data[1] = (byte) 255;
                data[2] = (byte) 195;
                data[3] = (byte) 13;
                break;
            case 4://柠檬黄
                data[1] = (byte) 254;
                data[2] = (byte) 255;
                data[3] = (byte) 51;
                break;
            case 5://草绿
                data[1] = (byte) 200;
                data[2] = (byte) 253;
                data[3] = (byte) 58;
                break;
            case 6://中绿
                data[1] = (byte) 114;
                data[2] = (byte) 244;
                data[3] = (byte) 36;
                break;
            case 7://绿色
                data[1] = (byte) 0;
                data[2] = (byte) 209;
                data[3] = (byte) 103;
                break;
            case 8://淡蓝
                data[1] = (byte) 2;
                data[2] = (byte) 198;
                data[3] = (byte) 227;
                break;
            case 9://钴蓝
                data[1] = (byte) 5;
                data[2] = (byte) 118;
                data[3] = (byte) 247;
                break;
            case 10://靛青
                data[1] = (byte) 65;
                data[2] = (byte) 0;
                data[3] = (byte) 251;
                break;
            case 11://紫色
                data[1] = (byte) 150;
                data[2] = (byte) 0;
                data[3] = (byte) 255;
                break;
            default:
                break;
        }
    }

    @Override
    public byte[] toByteArray() {
        return data;
    }

    @Override
    public int getLength() {
        return 4;
    }
}
