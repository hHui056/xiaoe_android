package com.beidouapp.xiaoe.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.beidouapp.et.ConnectOptions;
import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IActionListener;
import com.beidouapp.et.ISDKContext;
import com.beidouapp.et.ISDKContextCallback;
import com.beidouapp.et.Message;
import com.beidouapp.et.MessageType;
import com.beidouapp.et.SDKContextManager;
import com.beidouapp.et.SDKContextParameters;
import com.beidouapp.et.Server;
import com.beidouapp.et.client.domain.DocumentInfo;
import com.beidouapp.xiaoe.instruction.Instruction;
import com.beidouapp.xiaoe.instruction.InstructionParser;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;


public class IMService extends Service {
    public static final String TAG = "IMService ";
    public ISDKContext sdkContext;
    private MyBinder mBinder = new MyBinder();
    private SharedPreferences sp;
    private String userId = "";
    /**
     * 可用的服务
     */
    private Server server = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sp = getSharedPreferences(Constans.Key.SHARE_PREFERENCE, Context.MODE_PRIVATE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        userId = sp.getString(Constans.Key.UID_KEY, "");
        SDKContextParameters sdkContextParameters = new SDKContextParameters();
        sdkContextParameters.setBlanceServerAddress(Constans.SERVER_ADD);
        sdkContextParameters.setAppKey(Constans.APPKEY);
        sdkContextParameters.setSecretKey(Constans.SECRETKEY);
        sdkContextParameters.setBlanceServerPort(Constans.SERVER_PORT);
        // sdkContextParameters.setUid(userId);
        sdkContextParameters.setUid(Constans.TEST_UID);
        sdkContext = SDKContextManager.createContext(sdkContextParameters, this);
        sdkContext.setCallback(setContextCallback());
        discoverSvrs();
        return super.onStartCommand(intent, flags, startId);
    }

    private ISDKContextCallback setContextCallback() {
        return new ISDKContextCallback() {
            @Override
            public void onServer(Server svr) {
                if (svr.getType() == Server.TYPE_LAN) {
                    return;
                }
                server = svr;
                // 发现可用的服务器
                connectSvrs(svr);
            }

            @Override
            public void onPeerState(String uid, String code) {
                showLog(String.format("%s   %s",uid,code));
                // 用户状态改变
                if (uid.equals(Constans.TEST_DEVICE_UID)) {//绑定的用户状态改变才通知用户
                    Intent intent = new Intent();
                    intent.setAction(Constans.DEVICE_STATE_CHANGE);
                    intent.putExtra(Constans.DEVICE_STATE_KEY, code);
                    LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
                }
            }

            @Override
            public void onMessage(MessageType type, String topic, Message message) {
                if (type == MessageType.CHAT_TO && topic.equals(Constans.TEST_DEVICE_UID)) {
                    Instruction instruction = new InstructionParser().parseInstruction(message.getPayload());
                    if (instruction != null) {
                        Log.i(TAG, "指令解析正确");
                        Intent intent = new Intent();
                        intent.setAction(Constans.RECEIVE_MSG);
                        intent.putExtra(Constans.ILINK_MSG_KEY, instruction);
                        LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
                    } else {
                        Intent intent = new Intent();
                        intent.setAction(Constans.CMD_WRONG);
                        LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
                    }
                }
            }

            @Override
            public void onFileReceived(String sendId, DocumentInfo documentInfo) {
                // 有新的文件需要下载
            }

            @Override
            public void onBroken(Server svr, int errorCode, String reason) {

            }
        };
    }

    /**
     * 发现服务器
     */
    public void discoverSvrs() {
        sdkContext.discoverServers(10, new IActionListener() {
            @Override
            public void onSuccess() {
                // 正在扫描服务器，
                // 扫描到的服务器通过sdk全局回调返回。
                Log.i("test", "正在扫描服务器...");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // 扫描服务器失败
                Intent intent = new Intent();
                intent.setAction(Constans.CONNECT_FAIL);
                LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
            }
        });
    }

    /**
     * 连接服务器
     *
     * @param svr
     */
    public void connectSvrs(final Server svr) {
        ConnectOptions connectOptions = new ConnectOptions(); // 连接参数
        connectOptions.setConnectionTimeout(5); // 连接超时时间
        connectOptions.setKeepAliveInterval((short) 10); // 与服务器的保活时间间隔
        connectOptions.setCleanSession(true);//清除离线消息
        // svr为discover发现到的可用服务器
        sdkContext.connect(svr, connectOptions, new IActionListener() {
            @Override
            public void onSuccess() {
                // 连接服务器成功
                TestUtil.showTest(TAG + "连接服务器成功");
                Intent intent = new Intent();
                intent.setAction(Constans.CONNECT_SUCCESS);
                LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // 连接服务器失败
                Intent intent = new Intent();
                intent.setAction(Constans.CONNECT_FAIL);
                LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
            }

        });
    }

    /**
     * 断开服务器连接
     */
    public void disconnectServer() {
        sdkContext.disconnect(server, new IActionListener() {
            @Override
            public void onSuccess() {
                // 断开连接成功

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                // 断开连接失败

            }
        });
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return mBinder;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public class MyBinder extends Binder {

        public ISDKContext isdkContext;

        /**
         * 初始化管理器
         */
        public ISDKContext getSdkContext() {
            this.isdkContext = sdkContext;
            return isdkContext;
        }

        /**
         * 断开服务器连接
         */
        public void disconnect() {
            disconnectServer();
        }

        /**
         * 停止服务
         */
        public void stopService() {
            stopSelf();
        }
    }
void showLog(String log){
    Log.v(TAG,log);
}

}
