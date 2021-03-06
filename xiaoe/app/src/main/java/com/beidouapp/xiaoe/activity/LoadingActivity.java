package com.beidouapp.xiaoe.activity;

import android.content.Intent;
import android.os.Bundle;

import com.beidouapp.xiaoe.MainActivity;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.utils.Constans;

/**
 * @author hHui
 */
public class LoadingActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                    if (sp.getString(Constans.Key.DEVICE_UID_KEY, "").equals("")) {//没有绑定过设备
                        startActivity(new Intent(LoadingActivity.this, FirstUseActivity.class));
                        finish();
                    } else {
                        startActivity(new Intent(LoadingActivity.this, MainActivity.class));
                        finish();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }
}
