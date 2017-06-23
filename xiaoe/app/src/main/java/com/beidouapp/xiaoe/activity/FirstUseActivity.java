package com.beidouapp.xiaoe.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IFriendsActionListener;
import com.beidouapp.et.SDKContextManager;
import com.beidouapp.et.client.domain.UserInfo;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author hHui
 *         <p>
 *         绑定设备（第一次使用）
 */
public class FirstUseActivity extends BaseActivity {

    @BindView(R.id.btn_go_setting_wifi)
    Button btnGoSettingWifi;
    @BindView(R.id.btn_bangdingshebei)
    Button btnBangdingshebei;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_use);
        ButterKnife.bind(this);

        if (MY_UID.equals("")) {
            register();
        }
    }

    @OnClick({R.id.btn_go_setting_wifi, R.id.btn_bangdingshebei})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_go_setting_wifi://配置WiFi
                startActivity(new Intent(FirstUseActivity.this, WifiSettingActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
            case R.id.btn_bangdingshebei://绑定设备
                startActivity(new Intent(FirstUseActivity.this, CaptureActivity.class));
                overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                break;
        }
    }

    /**
     * 通过Android设备唯一标识
     * <p>
     * 用戶名  设备唯一标识作为用户名
     * 暱稱    唯一标识前5位作为昵称
     */
    public void register() {

        SDKContextManager.addUser(getMyPhoneId(), getMyPhoneId().substring(0, 5), Constans.SERVER_ADD, Constans.SERVER_PORT, Constans.APPKEY, Constans.SECRETKEY, new IFriendsActionListener() {
            @Override
            public void onResultData(Object o) {
                UserInfo info = (UserInfo) o;
                MY_UID = info.getUserid();
                SharedPreferences.Editor editor = sp.edit();
                editor.putString(Constans.Key.UID_KEY, MY_UID);
                editor.commit();
                TestUtil.showTest("注册成功:  " + MY_UID);
            }

            @Override
            public void onSuccess() {

            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                Toast.makeText(FirstUseActivity.this, getString(R.string.register_fail), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * 获取Android设备唯一标识
     */
    public String getMyPhoneId() {
        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        String phoneId = TelephonyMgr.getDeviceId();
        return phoneId;
    }
}
