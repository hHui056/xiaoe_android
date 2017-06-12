package com.beidouapp.xiaoe.instruction;

/**
 * Created by hHui on 2017/6/5.
 */

public class RGBControllerResBody extends Body {

    private boolean isSuccess=false;

    @Override
    public void parseContent(byte[] content) {
        if (content[1]==0x00){
            isSuccess=true;
        }
    }

    public boolean getIsSuccess(){
        return isSuccess;
    }
}
