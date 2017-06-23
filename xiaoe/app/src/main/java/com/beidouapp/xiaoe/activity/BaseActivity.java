package com.beidouapp.xiaoe.activity;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import com.beidouapp.et.ISDKContext;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * @author hHui
 */
public class BaseActivity extends Activity {
    /**
     * 我的UID
     */
    public static String MY_UID = "";
    /**
     * 设备UID
     */
    public static String DEVICE_UID = "";
    public static ISDKContext isdkContext = null;
    public static String Where_From = "MainActivity";
    public SharedPreferences sp = null;
    /**
     * 加载dialog
     */
    SweetAlertDialog sweetProgressDialog = null;
    /**
     * 警告框
     */
    SweetAlertDialog sweetAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TestUtil.showTest("BaseActivity oncreate");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
        sp = getSharedPreferences(Constans.SHARE_PREFERENCE, Context.MODE_PRIVATE);
        if (MY_UID.equals("")) {
            MY_UID = sp.getString(Constans.Key.UID_KEY, "");
            // MY_UID = "Fc5wGsTuvumvyVvyM3mKeYJrkryaUF6NXj";
        }

        if (DEVICE_UID.equals("")) {
            DEVICE_UID = sp.getString(Constans.Key.DEVICE_UID_KEY, "");
            //DEVICE_UID = "Fc5wGsTuvumomVom5De2G4rEqLZHCb1iiC";
        }
    }

    public void ShowProgressDialog() {
        if (sweetProgressDialog == null) {
            sweetProgressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
        }
        sweetProgressDialog.getProgressHelper().setBarColor(Color.parseColor("#FF9900"));
        sweetProgressDialog.setTitleText("Loading");
        sweetProgressDialog.setCancelable(false);
        sweetProgressDialog.show();
    }

    public void MissProgressDialog() {
        if (sweetProgressDialog.isShowing()) {
            sweetProgressDialog.dismiss();
        }
    }

    public void showMessageOnDialog(String data) {
        if (sweetProgressDialog != null) {
            if (sweetProgressDialog.isShowing()) {
                sweetProgressDialog.dismiss();
            }
        }
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#FF9900"));
        sweetAlertDialog.setContentText(data);
        sweetAlertDialog.setTitleText("");
        sweetAlertDialog.show();
    }

    public boolean getProgressDialogStates() {
        return sweetProgressDialog.isShowing();
    }

    public void showErrorMessage(String data) {
        if (sweetProgressDialog != null) {
            if (sweetProgressDialog.isShowing()) {
                sweetProgressDialog.dismiss();
            }
        }
        sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE);
        sweetAlertDialog.getProgressHelper().setBarColor(Color.parseColor("#FF9900"));
        sweetAlertDialog.setContentText(data);
        sweetAlertDialog.show();
    }
}
