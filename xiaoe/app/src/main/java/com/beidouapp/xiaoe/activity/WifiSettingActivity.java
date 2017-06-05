package com.beidouapp.xiaoe.activity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.view.MyTitle;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author hHui
 */

public class WifiSettingActivity extends BaseActivity {

    @BindView(R.id.title_wifi_setting)
    MyTitle titleWifiSetting;
    @BindView(R.id.edit_ssid)
    EditText editSsid;
    @BindView(R.id.edit_passwrod)
    EditText editPasswrod;
    @BindView(R.id.img_eyes)
    ImageView imgEyes;
    @BindView(R.id.btn_setting_wifi)
    Button btnSettingWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);
        ButterKnife.bind(this);
        titleWifiSetting.setTitle("配置WiFi");
    }

    @OnClick({R.id.img_eyes, R.id.btn_setting_wifi})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_eyes:
                break;
            case R.id.btn_setting_wifi:
                break;
        }
    }
}
