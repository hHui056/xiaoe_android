package com.beidouapp.xiaoe.bean;

/**
 * Created by hHui on 2017/6/20.
 */

public class Config {
    private String device_uid = "";
    private String my_uid = "";

    public Config(String device_uid, String my_uid) {
        this.device_uid = device_uid;
        this.my_uid = my_uid;
    }


    public String getDevice_uid() {
        return device_uid;
    }

    public void setDevice_uid(String device_uid) {
        this.device_uid = device_uid;
    }

    public String getMy_uid() {
        return my_uid;
    }

    public void setMy_uid(String my_uid) {
        this.my_uid = my_uid;
    }
}
