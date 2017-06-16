package com.beidouapp.xiaoe.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.utils.Constans;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * 绑定设备
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrsuccess);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        String appkey = intent.getStringExtra(Constans.Key.SCAN_APPKEY);
        String uid = intent.getStringExtra(Constans.Key.SCAN_UID);

        txtQrAppkey.setText(appkey);
        txtQrUid.setText(uid);
    }

    @OnClick({R.id.img_qr_back, R.id.btn_qr_bind_device})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_qr_back:
                finish();
                this.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.btn_qr_bind_device:
                break;
        }
    }
}
