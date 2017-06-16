package com.beidouapp.xiaoe.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IActionListener;
import com.beidouapp.et.ISDKContext;
import com.beidouapp.et.Message;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.adapter.ShowMessageAdapter;
import com.beidouapp.xiaoe.bean.MessageItem;
import com.beidouapp.xiaoe.bean.MessageOwner;
import com.beidouapp.xiaoe.instruction.AirReqBody;
import com.beidouapp.xiaoe.instruction.AirResBody;
import com.beidouapp.xiaoe.instruction.Body;
import com.beidouapp.xiaoe.instruction.Instruction;
import com.beidouapp.xiaoe.instruction.RGBControllerReqBody;
import com.beidouapp.xiaoe.instruction.RGBControllerResBody;
import com.beidouapp.xiaoe.instruction.TemperatureAndHumidityReqBody;
import com.beidouapp.xiaoe.instruction.TemperatureAndHumidityResBody;
import com.beidouapp.xiaoe.service.IMService;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;
import com.beidouapp.xiaoe.view.MyTitle;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 語音控制 && 语音留言
 *
 * @author hHui
 */
public class VoiceControlActivity extends BaseActivity {
    /**
     * 温湿度查询
     **/
    public static final int TEMP_QUREY = 0x00;
    /**
     * 大气压查询
     **/
    public static final int AIR_QUREY = 0x01;
    /**
     * 关灯
     **/
    public static final int CLOSE_LIGHT = 0x02;
    /**
     * 开灯
     **/
    public static final int OPEN_LIGHT = 0x03;


    @BindView(R.id.title_yuyinkongzhi)
    MyTitle titleYuyinkongzhi;
    @BindView(R.id.list_sounds)
    ListView listSounds;
    @BindView(R.id.btn_speak)
    Button btnSpeak;
    /**
     * 播放录音的player
     */
    MediaPlayer player;
    /**
     * 用于接收service消息的广播接收器
     */
    MyBroadcastReceiver rec;
    /**
     * IMService
     */
    Intent intentService;
    /**
     * sdk实例
     */
    ISDKContext sdkContext;
    /**
     * 语音识别结果
     */
    StringBuffer listenResult = new StringBuffer();

    ShowMessageAdapter.ImageClickListener imageClickListener = new ShowMessageAdapter.ImageClickListener() {
        @Override
        public void OnImageClick(String soundpath) {
            if (player.isPlaying()) {
                player.stop();
                player.release();
                player = new MediaPlayer();
                try {
                    player.setDataSource(soundpath);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                player = new MediaPlayer();
                try {
                    player.setDataSource(soundpath);
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    TestUtil.showTest("异常  " + e);
                    e.printStackTrace();
                }
            }

        }
    };


    private IMService.MyBinder mBinder;
    private String recordPath = "";//当前录音路径
    /**
     * 存所有对话消息
     */
    private ArrayList<MessageItem> messageList = new ArrayList<MessageItem>();
    private ShowMessageAdapter adapter;
    RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        @Override
        public void onResult(final RecognizerResult result, boolean b) {
            if (null != result) {
                String str = result.getResultString();//识别结果
                try {
                    JSONObject object = new JSONObject(str);
                    if (!object.optBoolean("ls")) {//识别中
                        JSONArray wss = object.optJSONArray("ws");
                        for (int i = 0; i < wss.length(); i++) {
                            JSONArray cws = wss.optJSONObject(i).optJSONArray("cw");
                            for (int j = 0; j < cws.length(); j++) {
                                JSONObject w = cws.optJSONObject(j);
                                String data = w.optString("w");
                                listenResult.append(data);
                            }
                        }
                    } else {//识别结束
                        String sendResult = listenResult.toString();
                        TestUtil.showTest("识别结束: result " + sendResult);
                        parseListenResult(sendResult);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {

            }
        }

        @Override
        public void onError(SpeechError speechError) {

        }
    };
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            TestUtil.showTest("service 断开连接");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (IMService.MyBinder) service;
            sdkContext = mBinder.getSdkContext();
        }
    };
    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            if (code != ErrorCode.SUCCESS) {
                Toast.makeText(VoiceControlActivity.this, getString(R.string.voice_init_faild), Toast.LENGTH_SHORT).show();
            }
        }
    };

    @OnClick(R.id.btn_speak)
    public void tospeak() {
        showVoiceDialog();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_control);
        SpeechUtility.createUtility(this, SpeechConstant.APPID + Constans.XUNFEI_APPID);
        ButterKnife.bind(this);

        titleYuyinkongzhi.setTitle(getResources().getString(R.string.yuyinkongzhi));
        registBroadcase();
        bindService();
        player = new MediaPlayer();
    }

    /**
     * 注册广播
     */
    public void registBroadcase() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.RECEIVE_MSG);
        filter.addAction(Constans.CMD_WRONG);
        filter.addAction(Constans.DEVICE_STATE_CHANGE);
        rec = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(rec, filter);
    }

    /**
     * 绑定service
     */
    void bindService() {
        intentService = new Intent(this.getApplicationContext(), IMService.class);
        bindService(intentService, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rec);
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }
    }

    /**
     * 向设备发送指令
     */
    public void sendOrderToDevice(int code) {
        Instruction instruction = null;
        switch (code) {
            case TEMP_QUREY:
                instruction = new Instruction.Builder().setCmd(Instruction.Cmd.QUERY).setBody(new TemperatureAndHumidityReqBody(Instruction.DATA0.TEMPERA_HUM.BOTH))
                        .createInstruction();
                break;
            case AIR_QUREY:
                instruction = new Instruction.Builder().setCmd(Instruction.Cmd.QUERY).setBody(new AirReqBody(Instruction.DATA0.AIRPRESS))
                        .createInstruction();

                break;
            case CLOSE_LIGHT:
                instruction = new Instruction.Builder().setCmd(Instruction.Cmd.CONTROL).setBody(new RGBControllerReqBody(12)).createInstruction();
                break;
            case OPEN_LIGHT:
                instruction = new Instruction.Builder().setCmd(Instruction.Cmd.CONTROL).setBody(new RGBControllerReqBody(13)).createInstruction();
                break;
            default:
                break;
        }
        if (instruction != null) {
            finalChatToDevice(instruction);
        }
    }

    public void finalChatToDevice(Instruction instruction) {

        Message message = new Message();
        message.setPayload(instruction.toByteArray());
        sdkContext.chatTo(Constans.TEST_DEVICE_UID, message, new IActionListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {

            }
        });
    }

    /**
     * 显示语音输入dialog
     */
    public void showVoiceDialog() {
        //1.创建 RecognizerDialog 对象
        RecognizerDialog mDialog = new RecognizerDialog(this, mInitListener);
        //若要将 RecognizerDialog 用于语义理解，必须添加以下参数设置，设置之后 onResult 回调返回将是语义理解的结果
        mDialog.setListener(mRecognizerDialogListener);
        mDialog.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        String fileName = getCurrentDate();//录音文件名字
        recordPath = String.format(Environment.getExternalStorageDirectory() + "/msc/%s.wav", fileName);
        mDialog.setParameter(SpeechConstant.ASR_AUDIO_PATH, recordPath);
        //4.显示 dialog，接收语音输入
        mDialog.show();
    }

    // 以当前时间作为文件名
    private String getCurrentDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy_MM_dd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
        String str = formatter.format(curDate);
        return str;
    }

    /**
     * 处理识别结果
     *
     * @param result
     */
    public void parseListenResult(String result) {
        MessageItem sound = new MessageItem(true, recordPath);
        messageList.add(sound);
        if (adapter == null) {
            adapter = new ShowMessageAdapter(VoiceControlActivity.this, messageList);
            adapter.setImageClickListener(imageClickListener);
            listSounds.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        listenResult = new StringBuffer();
        if (result.contains("温") || result.contains("湿") || result.contains("度")) {//查询温湿度
            sendOrderToDevice(TEMP_QUREY);
        } else if (result.contains("大气") || result.contains("气压")) {//查询气压
            sendOrderToDevice(AIR_QUREY);
        } else if (result.contains("关")) {//关灯
            sendOrderToDevice(CLOSE_LIGHT);
        } else if (result.contains("开")) {//开灯
            sendOrderToDevice(OPEN_LIGHT);
        } else {//指令无法识别------>不发送消息(更新UI)
            MessageItem item = new MessageItem(MessageOwner.someone, getString(R.string.order_wrong));
            messageList.add(item);
            adapter.notifyDataSetChanged();
        }
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constans.CMD_WRONG)) {
                MessageItem item = new MessageItem(MessageOwner.someone, getResources().getString(R.string.waring_msg));
                messageList.add(item);
                if (adapter == null) {
                    adapter = new ShowMessageAdapter(VoiceControlActivity.this, messageList);
                    adapter.setImageClickListener(imageClickListener);
                    listSounds.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            } else if (action.equals(Constans.RECEIVE_MSG)) {
                Instruction instruction = (Instruction) intent.getSerializableExtra(Constans.ILINK_MSG_KEY);
                Body body = instruction.getBody();
                if (body instanceof TemperatureAndHumidityResBody) {//温湿度查询反馈
                    TemperatureAndHumidityResBody tempbody = (TemperatureAndHumidityResBody) body;
                    String result = String.format("温度(℃) %s.%s ℃\n\n湿度(RH) %s.%s", tempbody.getTempeInt(), tempbody.getTempeDec(), tempbody.getHumInt(), tempbody.getHunDec()) + "%";
                    MessageItem item = new MessageItem(MessageOwner.someone, result);
                    messageList.add(item);
                    adapter.notifyDataSetChanged();
                } else if (body instanceof AirResBody) {//大气压查询反馈
                    AirResBody airbody = (AirResBody) body;
                    String result = String.format("大气压(Pa) %s \n\n海拔(m) %s", airbody.getAir(), airbody.getHigh());
                    MessageItem item = new MessageItem(MessageOwner.someone, result);
                    messageList.add(item);
                    adapter.notifyDataSetChanged();
                } else if (body instanceof RGBControllerResBody) {//灯光控制反馈
                    RGBControllerResBody rgbbody = (RGBControllerResBody) body;
                    MessageItem item = new MessageItem(MessageOwner.someone, getString(R.string.light_control_success));
                    messageList.add(item);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }
}