package com.beidouapp.xiaoe.activity;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * @author hHui
 */
public class BaseActivity extends Activity {
    SweetAlertDialog sweetProgressDialog = null;

    SweetAlertDialog sweetAlertDialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //透明导航栏
            //  getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
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
