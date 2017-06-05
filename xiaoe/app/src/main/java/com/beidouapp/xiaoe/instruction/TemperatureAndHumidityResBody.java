package com.beidouapp.xiaoe.instruction;


import com.beidouapp.xiaoe.utils.HexUtil;

/**
 * @author hHui
 */
public class TemperatureAndHumidityResBody extends Body {
    private byte devieType;
    private byte res;
    private String tempeInt;//温度整数部分
    private String tempeDec;//温度小数部分
    private String humInt;//湿度整数部分
    private String hunDec;//湿度小数部分


    public byte getDevieType() {
        return devieType;
    }

    public void setDevieType(byte devieType) {
        this.devieType = devieType;
    }

    public byte getRes() {
        return res;
    }

    public void setRes(byte res) {
        this.res = res;
    }

    public String getTempeInt() {
        return tempeInt;
    }

    public void setTempeInt(String tempeInt) {
        this.tempeInt = tempeInt;
    }

    public String getTempeDec() {
        return tempeDec;
    }

    public void setTempeDec(String tempeDec) {
        this.tempeDec = tempeDec;
    }

    public String getHumInt() {
        return humInt;
    }

    public void setHumInt(String humInt) {
        this.humInt = humInt;
    }

    public String getHunDec() {
        return hunDec;
    }

    public void setHunDec(String hunDec) {
        this.hunDec = hunDec;
    }

    @Override
    public void parseContent(byte[] content) {
        devieType = content[0];
        res = content[1];
        humInt = HexUtil.byteToHexStr(content[2]);
        hunDec = HexUtil.byteToHexStr(content[3]);
        tempeInt = HexUtil.byteToHexStr(content[4]);
        tempeDec = HexUtil.byteToHexStr(content[5]);
    }
}
