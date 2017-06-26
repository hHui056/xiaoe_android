package com.beidouapp.xiaoe.activity;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

import com.beidouapp.et.client.callback.FileCallBack;
import com.beidouapp.et.client.domain.DocumentInfo;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.adapter.ShowMessageAdapter;
import com.beidouapp.xiaoe.bean.MessageItem;
import com.beidouapp.xiaoe.bean.MessageOwner;
import com.beidouapp.xiaoe.recorder.AudioRecorder;
import com.beidouapp.xiaoe.recorder.RecordButton;
import com.beidouapp.xiaoe.utils.TestUtil;
import com.beidouapp.xiaoe.view.MyTitle;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * 语音留言
 *
 * @author hHui
 */
public class VoiceSendActivity extends BaseActivity {

    private final int SEND_FILE_SUCCESS = 0x00;//语音文件发送成功

    private final int SEND_FILE_FAILD = 0x01;//语音文件发送失败

    @BindView(R.id.title_yuyinliuyan)
    MyTitle titleYuyinliuyan;
    @BindView(R.id.hh_list_send_sounds)
    ListView hhListSendSounds;
    @BindView(R.id.hh_btn_speak)
    RecordButton hhBtnSpeak;

    /**
     * 播放录音的player
     */
    MediaPlayer player;
    /**
     * 存所有消息的集合
     */
    ArrayList<MessageItem> messageList = new ArrayList<MessageItem>();

    ShowMessageAdapter adapter = null;

    Handler UIHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SEND_FILE_FAILD) {
                MessageItem item = new MessageItem(MessageOwner.someone, getString(R.string.send_sound_fail));
                messageList.add(item);
                adapter.notifyDataSetChanged();
            } else if (msg.what == SEND_FILE_SUCCESS) {
                MessageItem item = new MessageItem(MessageOwner.someone, getString(R.string.send_sound_success));
                messageList.add(item);
                adapter.notifyDataSetChanged();
            }
        }
    };

    ShowMessageAdapter.ImageClickListener imageClickListener = new ShowMessageAdapter.ImageClickListener() {
        @Override
        public void OnImageClick(String soundpath) {
            TestUtil.showTest("点击的录音路径是:  " + soundpath);

            if (player.isPlaying()) {
                player.stop();
                player.release();
                player = new MediaPlayer();
                try {
                    File tempFile = new File(soundpath);
                    FileInputStream fis = new FileInputStream(tempFile);
                    player.reset();
                    player.setDataSource(fis.getFD());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                player = new MediaPlayer();
                try {
                    File tempFile = new File(soundpath);
                    FileInputStream fis = new FileInputStream(tempFile);
                    player.reset();
                    player.setDataSource(fis.getFD());
                    player.prepare();
                    player.start();
                } catch (IOException e) {
                    TestUtil.showTest("异常  " + e);
                    e.printStackTrace();
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_send);
        ButterKnife.bind(this);

        titleYuyinliuyan.setTitle(getString(R.string.yuyinliuyan));
        hhBtnSpeak.setAudioRecord(AudioRecorder.getInstance());
        hhBtnSpeak.setRecordListener(new RecordButton.RecordListener() {
            @Override
            public void recordEnd(String filePath) {
                TestUtil.showTest("录音保存路径  " + filePath);
                MessageItem item = new MessageItem(true, filePath);
                messageList.add(item);
                if (adapter == null) {
                    adapter = new ShowMessageAdapter(VoiceSendActivity.this, messageList);
                    adapter.setImageClickListener(imageClickListener);
                    hhListSendSounds.setAdapter(adapter);
                } else {
                    adapter.notifyDataSetChanged();
                }

                sendFileToDevice(filePath);
            }
        });

        player = new MediaPlayer();
    }

    /**
     * 发送语音文件到开发板上
     *
     * @param filePath
     */
    void sendFileToDevice(String filePath) {
        TestUtil.showTest("开始发送语音文件");
        filePath = "/storage/emulated/0/360Log/test.txt";
        isdkContext.fileTo(DEVICE_UID, filePath, "sound", new FileCallBack() {
            @Override
            public void onProcess(DocumentInfo documentInfo, String s, long l, long l1) {
                TestUtil.showTest(String.format("文件发送中 %s/%s", l, l1));
            }

            @Override
            public void onSuccess(DocumentInfo documentInfo, String s) {
                UIHandler.sendEmptyMessage(SEND_FILE_SUCCESS);
            }

            @Override
            public void onFailure(String s, Throwable throwable) {
                TestUtil.showTest("发送语音文件失败  " + throwable);
                UIHandler.sendEmptyMessage(SEND_FILE_FAILD);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player.isPlaying()) {
            player.stop();
            player.release();
        }
    }
}
