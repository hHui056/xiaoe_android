package com.beidouapp.xiaoe.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Binder;
import android.os.Handler;
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
import com.beidouapp.xiaoe.activity.BaseActivity;
import com.beidouapp.xiaoe.instruction.Instruction;
import com.beidouapp.xiaoe.instruction.InstructionParser;
import com.beidouapp.xiaoe.utils.ConnectType;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;

import java.util.Timer;
import java.util.TimerTask;


public class IMService extends Service {
    public static final String TAG = "IMService";

    public static final int COUNT_DOWN = 0;//倒计时

    public static final int RECONNECT = 1;//重连

    public ISDKContext sdkContext;
    TimerTask task;
    /**
     * 重连计时  120s内未自动重连成功，则手动重连
     */
    private int time = 120;
    private MyBinder mBinder = new MyBinder();
    private SharedPreferences sp;
    private String userId = "";
    private ConnectType connectType = ConnectType.DISCONNECTED;
    /**
     * 处理重连动作的Handler
     */
    Handler reconnectHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case COUNT_DOWN:
                    if (time > 0 && connectType == ConnectType.DISCONNECTED) {
                        reconnectHandler.sendEmptyMessageDelayed(COUNT_DOWN, 1000);
                        time--;
                    }
                    break;
                case RECONNECT:
                    reconnectServer();
                    break;
                default:
                    break;
            }
        }
    };
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
        sdkContextParameters.setUid(BaseActivity.MY_UID);
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
                showLog(String.format("%s   %s", uid, code));
                // 用户状态改变
                if (uid.equals(BaseActivity.DEVICE_UID)) {//绑定的用户状态改变才通知用户
                    Intent intent = new Intent();
                    intent.setAction(Constans.DEVICE_STATE_CHANGE);
                    intent.putExtra(Constans.DEVICE_STATE_KEY, code);
                    LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent);
                }
            }

            @Override
            public void onMessage(MessageType type, String topic, Message message) {
                Intent intent1 = new Intent();
                intent1.setAction(Constans.RECEIVE_TRANSMISSION);
                intent1.putExtra(Constans.Key.TOPIC_KEY, topic);
                intent1.putExtra(Constans.ILINK_MSG_KEY, new String(message.getPayload()));
                LocalBroadcastManager.getInstance(IMService.this).sendBroadcast(intent1);

                if (type == MessageType.CHAT_TO && topic.equals(BaseActivity.DEVICE_UID)) {
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
            public void onBroken(Server svr, int errorCode, String reason) {//服务器断开连接--->
                TestUtil.showTest(String.format("断开连接:  %s  %s", errorCode, reason));
                connectType = ConnectType.DISCONNECTED;
                //与服务器异常断连回调
                if (errorCode != 1301) {//非异地登录,则开始自动重连
                    time = 120;
                    reconnectHandler.sendEmptyMessage(COUNT_DOWN);
                    reconnectHandler.sendEmptyMessageDelayed(RECONNECT, 10000);
                }
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
                Log.i("test", "正在扫描服务器...");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
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
        TestUtil.showTest("server IP is " + svr.getIp());
        ConnectOptions connectOptions = new ConnectOptions(); // 连接参数
        connectOptions.setConnectionTimeout(10); // 连接超时时间
        connectOptions.setKeepAliveInterval((short) 15); // 与服务器的保活时间间隔
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
                connectType = ConnectType.CONNECTED;
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

    void showLog(String log) {
        Log.v(TAG, log);
    }

    /**
     * 重连服务器
     */
    void reconnectServer() {
        if (connectType == ConnectType.CONNECTED) {
            return;
        }
        task = new TimerTask() {
            @Override
            public void run() {
                if (connectType == ConnectType.CONNECTED) {
                    task.cancel();
                    return;
                }
                if (time < 2) {//2min 内未重连成功--->取消自动重连（提示已经断开连接）
                    TestUtil.showTest("2min 内未重连上服务器");
                    task.cancel();
                    return;
                }
                TestUtil.showTest("开始重连服务器......");
                sdkContext.reConnect(new IActionListener() {
                    @Override
                    public void onSuccess() {
                        TestUtil.showTest("重连服务器成功");
                        connectType = ConnectType.CONNECTED;
                    }

                    @Override
                    public void onFailure(ErrorInfo errorInfo) {
                        TestUtil.showTest("重连服务器失败: " + errorInfo.getReason());
                        connectType = ConnectType.DISCONNECTED;
                    }
                });
            }
        };
        Timer timer = new Timer();
        // 5000，延时5秒后执行。
        // 5000，每隔5秒执行1次task。
        timer.schedule(task, 5000, 5000);

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
}
