package com.beidouapp.xiaoe.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IActionListener;
import com.beidouapp.et.Message;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.adapter.ShowMessageAdapter;
import com.beidouapp.xiaoe.bean.MessageItem;
import com.beidouapp.xiaoe.bean.MessageOwner;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.view.MyTitle;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author hHui
 *         <p>
 *         数据透传
 */
public class DataTransmissionActivity extends BaseActivity {

    public final int SEND_SUCCESS = 0x00;//消息发送成功

    public final int SEND_FAIL = 0x01;//消息发送失败

    public final int RECEIVE_MESSAGE = 0x02;//收到消息

    @BindView(R.id.title_shujutouchuan)
    MyTitle titleShujutouchuan;
    @BindView(R.id.list_data)
    ListView listData;
    @BindView(R.id.edit_message)
    EditText editMessage;
    @BindView(R.id.btn_send)
    Button btnSend;

    MyBroadcastReceiver rec;

    ShowMessageAdapter adapter = null;

    Toast toast;

    String uid = "";
    /**
     * 用来存所有的消息
     */
    ArrayList<MessageItem> messageLists = new ArrayList<MessageItem>();
    /**
     * 处理界面更新
     */
    Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if (msg.what == SEND_SUCCESS) {
                String data = (String) msg.obj;
                MessageItem item = new MessageItem(MessageOwner.me, data);
                messageLists.add(item);

                if (adapter == null) {
                    adapter = new ShowMessageAdapter(DataTransmissionActivity.this, messageLists);
                    listData.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

            } else if (msg.what == SEND_FAIL) {
                showErrorMessage(getString(R.string.msg_send_faild));
            } else if (msg.what == RECEIVE_MESSAGE) {
                String data = (String) msg.obj;
                MessageItem item = new MessageItem(MessageOwner.someone, data);
                messageLists.add(item);

                if (adapter == null) {
                    adapter = new ShowMessageAdapter(DataTransmissionActivity.this, messageLists);
                    listData.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_transmission);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        uid = intent.getStringExtra(Constans.Key.UID_KEY);

        titleShujutouchuan.setTitle(getString(R.string.shujutouchuan));
        registBroadcase();
    }

    @OnClick(R.id.btn_send)
    public void onClick() {
        sendMessageToDevice();
    }

    private void closeKeyboard() {

        View view = getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 注册广播
     */
    public void registBroadcase() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.RECEIVE_TRANSMISSION);
        rec = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(rec, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rec);
    }

    /**
     * 发送消息到设备
     */
    void sendMessageToDevice() {
        closeKeyboard();
        final String msg = editMessage.getText().toString().trim();
        if (msg == null || msg.equals("")) {
            showErrorMessage(getString(R.string.must_input));
            return;
        }
        Message message = new Message();
        message.setPayload(msg.getBytes());
        isdkContext.chatTo(uid, message, new IActionListener() {
            @Override
            public void onSuccess() {//发送成功
                android.os.Message mymsg = new android.os.Message();
                mymsg.what = SEND_SUCCESS;
                mymsg.obj = msg;
                UIHandler.sendMessage(mymsg);
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {//发送失败
                UIHandler.sendEmptyMessage(SEND_FAIL);
            }
        });
    }

    public void showToast(String str) {
        if (toast == null) {
            toast = Toast.makeText(DataTransmissionActivity.this, str, Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constans.RECEIVE_TRANSMISSION)) {//收到透传信息
                String topic = intent.getStringExtra(Constans.Key.TOPIC_KEY);
                if (topic.contains(uid)) {
                    String data = intent.getStringExtra(Constans.ILINK_MSG_KEY);
                    android.os.Message message = new android.os.Message();
                    message.obj = data;
                    message.what = RECEIVE_MESSAGE;
                    UIHandler.sendMessage(message);
                }
            }
        }
    }
}
