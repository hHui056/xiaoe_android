package com.beidouapp.xiaoe.utils;

/**
 * @author hHui
 *         十六进制字节数组和字符串转换工具类。
 */
public class HexUtil {

    public static String byteArrayToHexStr(byte[] byteArray) {
        if (byteArray == null) {
            return null;
        }
        StringBuffer returnValue = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            String hex = Integer.toHexString(byteArray[i] & 0xFF);
            if (hex.length() == 1) {
                hex = '0' + hex;
            }
            returnValue.append(hex.toUpperCase());
        }
        return returnValue.toString();
    }

    public static byte[] hexStrToByteArray(String hexStr) {
        if (hexStr == null) {
            return null;
        }
        byte[] ret = new byte[hexStr.length() / 2];
        byte[] tmp = hexStr.getBytes();
        for (int i = 0; i < hexStr.length() / 2; i++) {
            ret[i] = uniteBytes(tmp[i * 2], tmp[i * 2 + 1]);
        }
        return ret;
    }

    /**
     * 将两个ASCII字符合成一个字节； 如："EF"--> 0xEF
     *
     * @param src0 byte
     * @param src1 byte
     * @return byte
     */
    public static byte uniteBytes(byte src0, byte src1) {
        byte _b0 = Byte.decode("0x" + new String(new byte[]{src0})).byteValue();
        _b0 = (byte) (_b0 << 4);
        byte _b1 = Byte.decode("0x" + new String(new byte[]{src1})).byteValue();
        byte ret = (byte) (_b0 ^ _b1);
        return ret;
    }

    public static String byteToHexStr(byte b) {
        String hex = null;
        hex = Integer.toHexString(b & 0xFF);
        if (hex.length() == 1) {
            hex = '0' + hex;
        }
        return hex;
    }
}
