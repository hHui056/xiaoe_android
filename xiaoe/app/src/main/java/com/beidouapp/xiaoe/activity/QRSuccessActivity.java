package com.beidouapp.xiaoe.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouapp.xiaoe.MainActivity;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 绑定设备
 *
 * @author hHui
 */
public class QRSuccessActivity extends BaseActivity {

    @BindView(R.id.img_qr_back)
    ImageView imgQrBack;
    @BindView(R.id.img_qr_icon)
    ImageView imgQrIcon;
    @BindView(R.id.txt_qr_appkey)
    TextView txtQrAppkey;
    @BindView(R.id.txt_qr_uid)
    TextView txtQrUid;
    @BindView(R.id.btn_qr_bind_device)
    Button btnQrBindDevice;
    String uid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrsuccess);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String appkey = intent.getStringExtra(Constans.Key.SCAN_APPKEY);
        uid = intent.getStringExtra(Constans.Key.SCAN_UID);

        txtQrAppkey.setText(appkey);
        txtQrUid.setText(uid);
        if (Where_From.equals("GroupManageActivity")) {
            btnQrBindDevice.setText(getString(R.string.sure_add));
        }
    }

    @OnClick({R.id.img_qr_back, R.id.btn_qr_bind_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_qr_back://返回
                finish();
                this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.btn_qr_bind_device://确认绑定
                TestUtil.showTest("click 确定");
                SharedPreferences sp = getSharedPreferences(Constans.SHARE_PREFERENCE, Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Constans.Key.DEVICE_UID_KEY, uid);
                editor.commit();
                if (Where_From.equals("GroupManageActivity")) {
                    Intent resultIntent = new Intent();
                    resultIntent.putExtra(Constans.Key.UID_KEY, uid);
                    this.setResult(Constans.CODE_3, resultIntent);
                    finish();
                } else {
                    Intent intent = new Intent(QRSuccessActivity.this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
        }
    }
}
