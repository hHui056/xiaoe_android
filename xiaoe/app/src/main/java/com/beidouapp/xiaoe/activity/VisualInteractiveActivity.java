package com.beidouapp.xiaoe.activity;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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
import com.beidouapp.xiaoe.instruction.Body;
import com.beidouapp.xiaoe.instruction.Instruction;
import com.beidouapp.xiaoe.instruction.LEDControllerReqBody;
import com.beidouapp.xiaoe.instruction.LEDControllerResBody;
import com.beidouapp.xiaoe.service.IMService;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.view.MyTitle;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VisualInteractiveActivity extends BaseActivity {

    @BindView(R.id.title_keshijiaohu)
    MyTitle titleKeshijiaohu;
    @BindView(R.id.list_messages)
    ListView listMessages;
    @BindView(R.id.edit_message_content)
    EditText editMessageContent;
    @BindView(R.id.btn_send_message)
    Button btnSendMessage;
    @BindView(R.id.layout_send)
    LinearLayout layoutSend;

    Intent intentService;

    ISDKContext sdkContext;

    MyBroadcastReceiver rec;

    Toast toast;
    /**
     * 用来存所有的消息
     */
    ArrayList<MessageItem> messageLists = new ArrayList<MessageItem>();
    ShowMessageAdapter adapter;
    private IMService.MyBinder mBinder;
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (IMService.MyBinder) service;
            sdkContext = mBinder.getSdkContext();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visual_interactive);
        ButterKnife.bind(this);
        titleKeshijiaohu.setTitle(getResources().getString(R.string.keshijiaohu));


        registBroadcase();
        bindService();
    }

    /**
     * 发送消息
     */
    @OnClick(R.id.btn_send_message)
    public void onClick() {
        sendMessage(editMessageContent.getText().toString().trim());
    }


    /**
     * 绑定service
     */
    void bindService() {
        intentService = new Intent(this.getApplicationContext(), IMService.class);
        bindService(intentService, connection, Context.BIND_AUTO_CREATE);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(connection);
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rec);
    }

    private void closeKeyboard() {

        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 向led发送显示消息
     *
     * @param data
     */
    public void sendMessage(final String data) {
        closeKeyboard();
         if (!isChinese(data)){
            showErrorMessage("只能输入中文");
             editMessageContent.setText("");
             return;
         }
        Instruction instruction = new Instruction.Builder().setCmd(Instruction.Cmd.CONTROL).setBody(new LEDControllerReqBody(data))
                .createInstruction();
        Message message = new Message();
        message.setPayload(instruction.toByteArray());


        /** -----------添加一个messageitem项----------------  **/
        MessageItem item = new MessageItem(MessageOwner.me, data);
        messageLists.add(item);
        if (adapter == null) {
            adapter = new ShowMessageAdapter(VisualInteractiveActivity.this, messageLists);
            listMessages.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        editMessageContent.setText("");
        /** ---------------------------  **/

        sdkContext.chatTo(Constans.TEST_DEVICE_UID, message, new IActionListener() {
            @Override
            public void onSuccess() {
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                MessageItem item1 = new MessageItem(MessageOwner.someone, "控制失败");
                messageLists.add(item1);
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void showToast(String str) {
        if (toast == null) {
            toast = Toast.makeText(VisualInteractiveActivity.this, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    public boolean isChinese(String data) {
        String reg = "[\\u4e00-\\u9fa5]+";
        return data.matches(reg);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constans.CMD_WRONG)) {
                MessageItem item = new MessageItem(MessageOwner.someone, "开发板档位或跳线帽不正确");
                messageLists.add(item);
                if (adapter == null) {
                    adapter = new ShowMessageAdapter(VisualInteractiveActivity.this, messageLists);
                    listMessages.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

            } else if (action.equals(Constans.RECEIVE_MSG)) {
                Instruction instruction = (Instruction) intent.getSerializableExtra(Constans.ILINK_MSG_KEY);
                Body body = instruction.getBody();
                if (body instanceof LEDControllerResBody) {//led控制反馈
                    MessageItem item = new MessageItem(MessageOwner.someone, "LED控制成功");
                    messageLists.add(item);
                    if (adapter == null) {
                        adapter = new ShowMessageAdapter(VisualInteractiveActivity.this, messageLists);
                        listMessages.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
