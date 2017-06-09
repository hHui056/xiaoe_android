package com.beidouapp.xiaoe.bean;

import net.tsz.afinal.annotation.sqlite.Id;
import net.tsz.afinal.annotation.sqlite.Table;

/**
 * Created by allen on 2016/11/16.
 */
@Table(name = "my_wifi")
public class WifiInfo {
    @Id
    private int id;
    private String wifiname;
    private String wifipsd;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getWifiname() {
        return wifiname;
    }

    public void setWifiname(String wifiname) {
        this.wifiname = wifiname;
    }

    public String getWifipsd() {
        return wifipsd;
    }

    public void setWifipsd(String wifipsd) {
        this.wifipsd = wifipsd;
    }
}
