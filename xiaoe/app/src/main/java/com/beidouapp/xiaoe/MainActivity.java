package com.beidouapp.xiaoe;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IActionListener;
import com.beidouapp.et.ISDKContext;
import com.beidouapp.et.Message;
import com.beidouapp.et.StatusListener;
import com.beidouapp.xiaoe.activity.BaseActivity;
import com.beidouapp.xiaoe.activity.CaptureActivity;
import com.beidouapp.xiaoe.activity.DataTransmissionActivity;
import com.beidouapp.xiaoe.activity.GroupManageActivity;
import com.beidouapp.xiaoe.activity.RGBControllerActivity;
import com.beidouapp.xiaoe.activity.VisualInteractiveActivity;
import com.beidouapp.xiaoe.activity.VoiceControlActivity;
import com.beidouapp.xiaoe.activity.VoiceSendActivity;
import com.beidouapp.xiaoe.activity.WifiSettingActivity;
import com.beidouapp.xiaoe.instruction.AirReqBody;
import com.beidouapp.xiaoe.instruction.AirResBody;
import com.beidouapp.xiaoe.instruction.Body;
import com.beidouapp.xiaoe.instruction.Instruction;
import com.beidouapp.xiaoe.instruction.TemperatureAndHumidityReqBody;
import com.beidouapp.xiaoe.instruction.TemperatureAndHumidityResBody;
import com.beidouapp.xiaoe.service.IMService;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.QureyType;
import com.beidouapp.xiaoe.utils.TestUtil;
import com.beidouapp.xiaoe.view.OptionView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.pedant.SweetAlert.SweetAlertDialog;

public class MainActivity extends BaseActivity {
    public static final String TAG = "MainActivity ";
    public static final int REFRESH_DEVICE_STATE = 0;//刷新用户状态
    @BindView(R.id.btn_wifi_setting)
    Button btnWifiSetting;
    @BindView(R.id.img_device)
    ImageView imgDevice;
    @BindView(R.id.txt_device_state)
    TextView txtDeviceState;
    @BindView(R.id.opt_wenshidu)
    OptionView optWenshidu;
    @BindView(R.id.opt_daqiya)
    OptionView optDaqiya;
    @BindView(R.id.opt_yuyinkongzhi)
    OptionView optYuyinkongzhi;
    @BindView(R.id.opt_qunzuguanli)
    OptionView optQunzuguanli;
    @BindView(R.id.opt_keshijiaohu)
    OptionView optKeshijiaohu;
    @BindView(R.id.opt_duocaidengguang)
    OptionView optDuocaidengguang;
    @BindView(R.id.opt_yuyinliuyan)
    OptionView optYuyinliuyan;
    @BindView(R.id.opt_shujutouchuan)
    OptionView optShujutouchuan;
    Toast toast;
    Intent intentService;
    MyBroadcastReceiver rec;
    /**
     * 是否处理错误消息
     */
    boolean isShowErrorMessage = true;
    /**
     * 输入透传板Uid对话框
     */
    SweetAlertDialog InputUidDialog = null;
    /**
     * 设备是否在线
     */
    private boolean isDeviceOnline = false;
    Handler uiHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case REFRESH_DEVICE_STATE:
                    if (isDeviceOnline) {//在线
                        txtDeviceState.setText(getString(R.string.deviceonline));
                    } else {
                        txtDeviceState.setText(getString(R.string.deviceoutofline));
                    }
                    break;
                default:
                    break;
            }
        }
    };
    /**
     * 是否连接上服务器
     */
    private boolean isConnectServer = false;
    /**
     * 当前温湿度查询状态
     */
    private QureyType TempQureyType = QureyType.QUREYEND;
    /**
     * 大气压查询状态
     */
    private QureyType AirQureyType = QureyType.QUREYEND;
    /**
     * sdk context
     */
    private ISDKContext sdkContext;
    private IMService.MyBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {

        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TestUtil.showTest("MainActivity 绑定IMservice");
            mBinder = (IMService.MyBinder) service;
            sdkContext = mBinder.getSdkContext();
            isdkContext = sdkContext;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initData();//初始视图数据
        registBroadcase();//注册广播接收iLink消息
        startClientServer();//开始连接服务器
    }

    /**
     * 初始化视图
     */

    public void initData() {
        optKeshijiaohu.setName(getResources().getString(R.string.keshijiaohu));
        optKeshijiaohu.setDescribe(getResources().getString(R.string.keshijiaohu_describe));
        optKeshijiaohu.setImage(R.drawable.keshijiaohu);

        optDaqiya.setName(getResources().getString(R.string.daqiya));
        optDaqiya.setDescribe(getResources().getString(R.string.daqiya_describe));
        optDaqiya.setImage(R.drawable.daqiya);

        optDuocaidengguang.setName(getResources().getString(R.string.duocaidengguang));
        optDuocaidengguang.setDescribe(getResources().getString(R.string.duocaidengguang_describe));
        optDuocaidengguang.setImage(R.drawable.duocaidengguang);

        optYuyinkongzhi.setName(getResources().getString(R.string.yuyinkongzhi));
        optYuyinkongzhi.setDescribe(getResources().getString(R.string.yuyinkongzhi_describe));
        optYuyinkongzhi.setImage(R.drawable.yuyinkongzhi);

        optYuyinliuyan.setName(getResources().getString(R.string.yuyinliuyan));
        optYuyinliuyan.setDescribe(getResources().getString(R.string.yuyinliuyan_describe));
        optYuyinliuyan.setImage(R.drawable.yuyinliuyan);

        optQunzuguanli.setName(getResources().getString(R.string.qunzuguanli));
        optQunzuguanli.setDescribe(getResources().getString(R.string.qunzuguanli_describe));
        optQunzuguanli.setImage(R.drawable.qunzuguanli);

        optShujutouchuan.setName(getResources().getString(R.string.shujutouchuan));
        optShujutouchuan.setDescribe(getResources().getString(R.string.shujutouchuan_describe));
        optShujutouchuan.setImage(R.drawable.shujutouchuan);
    }

    public void showToast(String str) {
        if (toast == null) {
            toast = Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    @OnClick({R.id.btn_wifi_setting, R.id.img_device, R.id.opt_wenshidu, R.id.opt_daqiya, R.id.opt_yuyinkongzhi, R.id.opt_qunzuguanli,
            R.id.opt_keshijiaohu, R.id.opt_duocaidengguang, R.id.opt_yuyinliuyan, R.id.opt_shujutouchuan})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_wifi_setting:
                startActivity(new Intent(MainActivity.this, WifiSettingActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.img_device:
                Where_From = "MainActivity";
                Intent intent = new Intent(MainActivity.this, CaptureActivity.class);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.opt_wenshidu:
                QureyTempAndHum();
                break;
            case R.id.opt_daqiya:
                QureyAir();
                break;
            case R.id.opt_yuyinkongzhi:
                if (!isConnectServer) {
                    showErrorMessage(getString(R.string.server_not_connected));
                    return;
                }
                if (!isDeviceOnline) {
                    showErrorMessage(getString(R.string.device_not_online));
                    return;
                }
                isShowErrorMessage = false;
                startActivity(new Intent(MainActivity.this, VoiceControlActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.opt_qunzuguanli:
                if (!isConnectServer) {
                    showErrorMessage(getString(R.string.server_not_connected));
                    return;
                }
                if (!isDeviceOnline) {
                    showErrorMessage(getString(R.string.device_not_online));
                    return;
                }
                startActivity(new Intent(MainActivity.this, GroupManageActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.opt_keshijiaohu:
                if (!isConnectServer) {
                    showErrorMessage(getString(R.string.server_not_connected));
                    return;
                }
                if (!isDeviceOnline) {
                    showErrorMessage(getString(R.string.device_not_online));
                    return;
                }
                isShowErrorMessage = false;
                startActivity(new Intent(MainActivity.this, VisualInteractiveActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.opt_duocaidengguang:
                if (!isConnectServer) {
                    showErrorMessage(getString(R.string.server_not_connected));
                    return;
                }
                if (!isDeviceOnline) {
                    showErrorMessage(getString(R.string.device_not_online));
                    return;
                }
                isShowErrorMessage = false;
                startActivity(new Intent(MainActivity.this, RGBControllerActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.opt_yuyinliuyan:
                if (!isConnectServer) {
                    showErrorMessage(getString(R.string.server_not_connected));
                    return;
                }
                if (!isDeviceOnline) {
                    showErrorMessage(getString(R.string.device_not_online));
                    return;
                }
                startActivity(new Intent(MainActivity.this, VoiceSendActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.opt_shujutouchuan:
                showInputUidDialog();
                break;
        }
    }

    /**
     * 开始连接iLink服务器
     */
    public void startClientServer() {
        // 启动服务 , 初始化sdk
        intentService = new Intent(this.getApplicationContext(), IMService.class);
        startService(intentService);
        bindService(intentService, connection, Context.BIND_AUTO_CREATE);
        ShowProgressDialog();
    }

    /**
     * 注册广播
     */
    public void registBroadcase() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.CONNECT_SUCCESS);
        filter.addAction(Constans.CONNECT_FAIL);
        filter.addAction(Constans.RECEIVE_MSG);
        filter.addAction(Constans.CMD_WRONG);
        filter.addAction(Constans.LOST_CONNECT);
        filter.addAction(Constans.WAITING_RECONNECT);
        filter.addAction(Constans.DEVICE_STATE_CHANGE);
        rec = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(rec, filter);
    }

    /**
     * 查询温湿度
     */
    public void QureyTempAndHum() {
        if (!isConnectServer) {
            showErrorMessage(getString(R.string.server_not_connected));
            return;
        }
        if (!isDeviceOnline) {
            showErrorMessage(getString(R.string.device_not_online));
            return;
        }
        Instruction instruction = new Instruction.Builder().setCmd(Instruction.Cmd.QUERY).setBody(new TemperatureAndHumidityReqBody(Instruction.DATA0.TEMPERA_HUM.BOTH))
                .createInstruction();
        Message message = new Message();
        message.setPayload(instruction.toByteArray());
        ShowProgressDialog();
        TempQureyType = QureyType.QUREYING;
        TestUtil.showTest("-------------①-------------");
        TestUtil.showTest("------------" + sdkContext + "----------");
        sdkContext.chatTo(DEVICE_UID, message, new IActionListener() {
            @Override
            public void onSuccess() {
                TestUtil.showTest(TAG + "温湿度指令发送成功");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

                TestUtil.showTest(TAG + "温湿度指令发送失败 " + errorInfo.getReason());
            }
        });
        TestUtil.showTest("-------------②-------------");
        checkTempIsHaveRes();
    }

    /**
     * 查询大气压
     */
    public void QureyAir() {
        if (!isConnectServer) {
            showErrorMessage(getString(R.string.server_not_connected));
            return;
        }
        if (!isDeviceOnline) {
            showErrorMessage(getString(R.string.device_not_online));
            return;
        }
        Instruction instruction = new Instruction.Builder().setCmd(Instruction.Cmd.QUERY).setBody(new AirReqBody(Instruction.DATA0.AIRPRESS))
                .createInstruction();
        Message message = new Message();
        message.setPayload(instruction.toByteArray());
        ShowProgressDialog();
        AirQureyType = QureyType.QUREYING;
        sdkContext.chatTo(DEVICE_UID, message, new IActionListener() {
            @Override
            public void onSuccess() {
                TestUtil.showTest(TAG + "大气压指令发送成功");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                TestUtil.showTest(TAG + "大气压指令发送失败 " + errorInfo.getReason());
            }
        });
        checkAirIsHaveRes();
    }

    /**
     * 10s 过后检查是否有查詢回复
     */
    public void checkTempIsHaveRes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (TempQureyType == QureyType.QUREYING) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MissProgressDialog();
                                showMessageOnDialog(getString(R.string.qurey_temp_faild));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 10s 过后检查是否有查詢回复
     */
    public void checkAirIsHaveRes() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(10000);
                    if (AirQureyType == QureyType.QUREYING) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                MissProgressDialog();
                                showMessageOnDialog(getString(R.string.qurey_air_faild));
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取设备在线状态
     */
    private void getDeviceStates() {
        sdkContext.getUserState(DEVICE_UID, new StatusListener() {
            @Override
            public void onSuccess(String s, int code) {
                if (code == 1) {
                    isDeviceOnline = true;
                    showLog("设备在线");
                } else {
                    isDeviceOnline = false;
                    showLog("设备离线");
                }
                uiHandler.sendEmptyMessage(REFRESH_DEVICE_STATE);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        showLog("onResume");
        if (isConnectServer) {
            getDeviceStates();
        }
        isShowErrorMessage = true;
    }

    void showLog(String log) {
        Log.v(TAG, log);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rec);
    }

    /**
     * 显示输入uid对话框
     */
    void showInputUidDialog() {
        InputUidDialog = new SweetAlertDialog(MainActivity.this);
        InputUidDialog.show();
        InputUidDialog.showCancelButton(true);
        InputUidDialog.showEditText(true);
        InputUidDialog.setEditTextString(DEVICE_UID);

        InputUidDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
            @Override
            public void onClick(SweetAlertDialog sweetAlertDialog) {
                String uid = InputUidDialog.getEditUid();
                if (uid == null || uid.equals("") || uid.length() != 34) {
                    showToast(getString(R.string.must_input_uid));
                    return;
                }
                Intent intent = new Intent(MainActivity.this, DataTransmissionActivity.class);
                intent.putExtra(Constans.Key.UID_KEY, uid);
                startActivity(intent);
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                InputUidDialog.dismiss();
            }
        });
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constans.CONNECT_SUCCESS)) {//连接成功
                MissProgressDialog();
                isConnectServer = true;
                showToast(getString(R.string.connect_success));
                getDeviceStates();
            } else if (action.equals(Constans.CONNECT_FAIL)) {//连接失败
                MissProgressDialog();
                showErrorMessage(getString(R.string.network_wrong));
            } else if (action.equals(Constans.RECEIVE_MSG)) {//收到控制或查询反馈
                if (!isShowErrorMessage) {
                    return;
                }
                Instruction instruction = (Instruction) intent.getSerializableExtra(Constans.ILINK_MSG_KEY);
                Body body = instruction.getBody();
                if (body instanceof TemperatureAndHumidityResBody) {//收到温湿度查询反馈
                    TestUtil.showTest("----收到温湿度查询反馈----");
                    TempQureyType = QureyType.QUREYEND;
                    TemperatureAndHumidityResBody tempbody = (TemperatureAndHumidityResBody) body;
                    showMessageOnDialog(String.format("温度(℃) %s.%s ℃\n\n湿度(RH) %s.%s", tempbody.getTempeInt(),
                            tempbody.getTempeDec(), tempbody.getHumInt(), tempbody.getHunDec()) + "%");
                } else if (body instanceof AirResBody) {//收到大气压查询反馈
                    TestUtil.showTest("----收到大气压查询反馈----");
                    AirQureyType = QureyType.QUREYEND;
                    AirResBody airbody = (AirResBody) body;
                    showMessageOnDialog(String.format("大气压(Pa) %s \n\n海拔(m) %s", airbody.getAir(), airbody.getHigh()));
                }
            } else if (action.equals(Constans.CMD_WRONG)) {
                TempQureyType = QureyType.QUREYEND;
                AirQureyType = QureyType.QUREYEND;
                if (isShowErrorMessage) {
                    showErrorMessage(getString(R.string.waring_msg));
                }
            } else if (action.equals(Constans.LOST_CONNECT)) {//失去连接

            } else if (action.equals(Constans.DEVICE_STATE_CHANGE)) {//设备状态改变
                String status = intent.getStringExtra(Constans.DEVICE_STATE_KEY);
                if (status.equals("0")) {
                    isDeviceOnline = false;
                } else {
                    isDeviceOnline = true;
                }
                uiHandler.sendEmptyMessage(REFRESH_DEVICE_STATE);
            } else if (action.equals(Constans.WAITING_RECONNECT)) {//异常断连等待重连

            }
        }
    }
}
