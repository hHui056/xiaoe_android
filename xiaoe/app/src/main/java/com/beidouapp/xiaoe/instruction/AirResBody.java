package com.beidouapp.xiaoe.instruction;

import com.beidouapp.xiaoe.utils.HexUtil;

/**
 * Created by hHui on 2017/6/5.
 */

public class AirResBody extends Body {
    /**
     * 海拔
     */
    private String high = "";
    /**
     * 大气压
     */
    private String air = "";

    public String getHigh() {
        return high;
    }

    public String getAir() {
        return air;
    }

    @Override
    public void parseContent(byte[] content) {
        super.parseContent(content);
        String num1 = HexUtil.byteToHexStr(content[3]);
        String num2 = HexUtil.byteToHexStr(content[4]);
        String num3 = HexUtil.byteToHexStr(content[5]);
        float airf = Float.parseFloat(num1 + num2 + num3);
        int highf = (int) ((1013.25 - airf / 100) * 9);

        air = (int) airf + "";
        high = highf + "";
    }

}
