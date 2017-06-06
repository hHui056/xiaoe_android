package com.beidouapp.xiaoe.utils;

import android.graphics.Color;

/**
 * Created by hHui on 2017/6/1.
 */

public class Constans {
    /**
     * 服务器地址
     */
    public static final String SERVER_ADD = "lb.kaifakuai.com";
    /**
     * appkey
     */
    public static final String APPKEY = "4d66c9c0-8082-444637";
    /**
     * secretkey
     */
    public static final String SECRETKEY = "ebcc89f7a184167d9f156dae06aa5371";
    /**
     * 服务器端口
     */
    public static final int SERVER_PORT = 8085;

    public static final String TEST_UID = "Fc5wGsTuvumvyVvyM3mKeYJrkryaUF6NXj";

    public static final String TEST_DEVICE_UID = "Fc5wGsTuvumomVom5De2G4rEqLZHCb1iiC";

    /**
     * BroadcastReceiver action
     */
    public static final String CONNECT_SUCCESS = "connect_success";
    public static final String CONNECT_FAIL = "connect_fail";  //包括发现server失败，连接sever失败
    public static final String RECEIVE_MSG = "receive_msg";
    public static final String LOST_CONNECT = "lost_connect";
    public static final String WAITING_RECONNECT = "waiting_reconnect";//等待重连
    public static final String DEVICE_STATE_CHANGE = "device_state_change";
    public static final String CMD_WRONG = "cmd_wrong";//档位或者跳线帽错误回复

    /**
     * action data constans
     */
    public static final String DEVICE_STATE_KEY = "device_state_key";
    public static final String ILINK_MSG_KEY = "ilink_msg_key";
    public static final int[] LIGHT_COLORS = {
            Color.rgb(230, 29, 190), Color.rgb(250, 40, 11), Color.rgb(255, 126, 0),
            Color.rgb(255, 195, 13), Color.rgb(254, 255, 51), Color.rgb(200, 253, 58),
            Color.rgb(114, 244, 36), Color.rgb(0, 209, 103), Color.rgb(2, 198, 227),
            Color.rgb(5, 118, 247), Color.rgb(65, 0, 251), Color.rgb(150, 0, 255)
    };

    /**
     * 常用key
     */
    public class Key {
        public static final String SHARE_PREFERENCE = "share_preference";
        public static final String UID_KEY = "uid_key";
        public static final String DEVICE_UID_KEY = "device_uid_key";
        public static final String SCAN_APPKEY = "scan_appkey";
        public static final String SCAN_UID = "scan_uid";
    }

}
