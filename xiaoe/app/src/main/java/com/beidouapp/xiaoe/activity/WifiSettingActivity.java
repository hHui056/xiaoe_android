package com.beidouapp.xiaoe.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.beidouapp.mylibrary.EsptouchTask;
import com.beidouapp.mylibrary.IEsptouchListener;
import com.beidouapp.mylibrary.IEsptouchResult;
import com.beidouapp.mylibrary.IEsptouchTask;
import com.beidouapp.mylibrary.demo_activity.EspWifiAdminSimple;
import com.beidouapp.mylibrary.task.__IEsptouchTask;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.bean.WifiInfo;
import com.beidouapp.xiaoe.utils.TestUtil;
import com.beidouapp.xiaoe.view.MyTitle;

import net.tsz.afinal.FinalDb;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author hHui
 *         <p>
 *         wifi配置
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

    AlertDialog dialog;

    StringBuffer IPList = null;

    Button btn_ok;

    TextView txt_notify;

    boolean isFirst = true;

    FinalDb finalDb = null;

    List<WifiInfo> infos = null;
    /**
     * 检查WiFi状态的定时器
     */
    TimerTask task;
    boolean firstcheackNet = true;
    String connect = null;
    private EspWifiAdminSimple mWifiAdmin;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                String apSsid = mWifiAdmin.getWifiConnectedSsid();
                editSsid.setText(apSsid);
                String mywifi = editSsid.getText().toString().trim();
                /**
                 * 获取数据库保存的wifi信息
                 */
                infos = finalDb.findAllByWhere(WifiInfo.class, " wifiname=\"" + mywifi + "\"");
                if (infos.size() > 0 && infos != null) {
                    WifiInfo info = infos.get(0);
                    editPasswrod.setText(info.getWifipsd());
                } else {
                    editPasswrod.setText("");
                }
                if (mywifi == null || mywifi.equals("")) {//无网络
                    if (dialog == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettingActivity.this);
                        builder.setMessage("手机未连接WiFi");
                        dialog = builder.create();
                        dialog.show();
                    } else {
                        if (!dialog.isShowing()) {
                            dialog.show();
                        }
                    }
                    connect = "NO";
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    connect = "YES";
                }
            }
            firstcheackNet = false;
        }
    };
    private AlertDialog mProgressDialog;
    private IEsptouchTask mEsptouchTask;
    private IEsptouchListener myListener = new IEsptouchListener() {

        @Override
        public void onEsptouchResultAdded(final IEsptouchResult result) {//配置成功回调
            onEsptoucResultAddedPerform(result);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi_setting);
        ButterKnife.bind(this);
        titleWifiSetting.setTitle("配置WiFi");
        mWifiAdmin = new EspWifiAdminSimple(this);
        init();
        startCheackWifi();
    }

    @OnClick({R.id.img_eyes, R.id.btn_setting_wifi})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.img_eyes://密码是否可见
                if (editPasswrod.getInputType() == 129) {
                    editPasswrod.setInputType(131073);
                    editPasswrod.setSingleLine(true);
                    Editable etext = editPasswrod.getText();
                    Selection.setSelection(etext, etext.length());
                } else {
                    editPasswrod.setInputType(129);
                    Editable etext = editPasswrod.getText();
                    Selection.setSelection(etext, etext.length());
                }
                break;
            case R.id.btn_setting_wifi://开始配置wifi
                settingWifi();
                break;
        }
    }

    public void init() {
        IPList = new StringBuffer();
        finalDb = FinalDb.create(WifiSettingActivity.this, "info.db", true);
    }

    public void settingWifi() {
        task.cancel();
        IPList = new StringBuffer();
        isFirst = true;
        String apSsid = editSsid.getText().toString();
        String apPassword = editPasswrod.getText().toString();
        if (apPassword.length() > 64) {
            Toast.makeText(WifiSettingActivity.this, "wifi密码超长！！！", Toast.LENGTH_SHORT).show();
            return;
        }
        String apBssid = mWifiAdmin.getWifiConnectedBssid();
        String isSsidHiddenStr = "NO";
        String taskResultCountStr = "0";//期望配置wifi的设备个数
        if (__IEsptouchTask.DEBUG) {
            Log.d("test", "mBtnConfirm is clicked, mySSID = " + apSsid
                    + ", " + " myPassWord = " + apPassword);
        }
        if (apSsid == null || apBssid == null) {
            if (dialog == null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(WifiSettingActivity.this);
                builder.setMessage("手机未连接WiFi");
                dialog = builder.create();
                dialog.show();
            } else {
                if (!dialog.isShowing()) {
                    dialog.show();
                }
            }
            return;
        }
        new WifiSettingActivity.EsptouchAsyncTask3().execute(apSsid, apBssid, apPassword,
                isSsidHiddenStr, taskResultCountStr);

    }

    private void onEsptoucResultAddedPerform(final IEsptouchResult result) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                IPList.append("IP: " + result.getInetAddress().getHostAddress() + "\n");
                txt_notify.setText(IPList.toString());
                btn_ok.setText("配置完成");
                btn_ok.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Toast.makeText(WifiSettingActivity.this, "配置完成", Toast.LENGTH_SHORT).show();
                        mProgressDialog.dismiss();
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                });

                if (isFirst) {
                    if (infos.size() <= 0 || infos == null) {//为查到保存过的wifi信息，新建一條數據
                        WifiInfo info = new WifiInfo();
                        info.setWifiname(editSsid.getText().toString().trim());
                        info.setWifipsd(editPasswrod.getText().toString().trim());
                        finalDb.save(info);
                    } else {
                        WifiInfo info = infos.get(0);
                        info.setWifipsd(editPasswrod.getText().toString().trim());
                        info.setWifiname(editSsid.getText().toString().trim());
                        finalDb.update(info);
                    }
                    isFirst = false;
                }
                Toast.makeText(WifiSettingActivity.this, "有新设备连接", Toast.LENGTH_LONG).show();
            }
        });
    }

    public void startCheackWifi() {
        task = new TimerTask() {
            @Override
            public void run() {
                TestUtil.showTest("======check wifi");
                String apSsid = mWifiAdmin.getWifiConnectedSsid();
                if (firstcheackNet) {
                    handler.sendEmptyMessage(1);
                    return;
                }
                /**
                 * wifi状态改变时通知改变UI
                 */
                if (apSsid == null || apSsid.equals("")) {//未连接wifi
                    if (connect.equals("YES")) {//之前已连接
                        handler.sendEmptyMessage(1);
                    }
                } else {//已连接wifi
                    if (connect.equals("NO")) {//之前未连接
                        handler.sendEmptyMessage(1);
                    }
                }
            }
        };
        Timer timer = new Timer();
        // 1000，延时1秒后执行。
        // 1000，每隔2秒执行1次task。
        timer.schedule(task, 1000, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        task.cancel();
    }

    private class EsptouchAsyncTask3 extends AsyncTask<String, Void, List<IEsptouchResult>> {
        private final Object mLock = new Object();
        private AlertDialog.Builder builder;

        @Override
        protected void onPreExecute() {//任务开始执行之前调用
            builder = new AlertDialog.Builder(WifiSettingActivity.this, cn.pedant.SweetAlert.R.style.alert_dialog);

            LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.wifi_dialog_layout, null);
            txt_notify = (TextView) view.findViewById(R.id.txt_list_ip);
            btn_ok = (Button) view.findViewById(R.id.btn_ok);
            mProgressDialog = builder.create();
            mProgressDialog.setView(view, 0, 0, 0, 0);
            txt_notify.setText(getResources().getString(R.string.settingnotify));
            mProgressDialog.setCanceledOnTouchOutside(false);
            mProgressDialog.setCancelable(false);//设置不能按返回键取消
            mProgressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    synchronized (mLock) {
                        if (__IEsptouchTask.DEBUG) {
                            Log.i("test", "progress dialog is canceled");
                        }
                        if (mEsptouchTask != null) {
                            mEsptouchTask.interrupt();
                        }
                    }
                }
            });
            btn_ok.setText("取  消");
            btn_ok.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mProgressDialog.dismiss();
                    mProgressDialog.cancel();
                }
            });
            mProgressDialog.show();
        }

        @Override
        protected List<IEsptouchResult> doInBackground(String... params) {//后台执行
            int taskResultCount = -1;
            synchronized (mLock) {
                String apSsid = params[0];
                String apBssid = params[1];
                String apPassword = params[2];
                String isSsidHiddenStr = params[3];
                String taskResultCountStr = params[4];
                boolean isSsidHidden = false;
                if (isSsidHiddenStr.equals("YES")) {
                    isSsidHidden = true;
                }
                taskResultCount = Integer.parseInt(taskResultCountStr);
                mEsptouchTask = new EsptouchTask(apSsid, apBssid, apPassword,
                        isSsidHidden, WifiSettingActivity.this);
                mEsptouchTask.setEsptouchListener(myListener);
            }
            List<IEsptouchResult> resultList = mEsptouchTask.executeForResults(taskResultCount);
            return resultList;
        }

        @Override
        protected void onPostExecute(List<IEsptouchResult> result) {//处理返回结果
            IEsptouchResult firstResult = result.get(0);
            // check whether the task is cancelled and no results received
            if (!firstResult.isCancelled()) {
                //int count = 0;
                // final int maxDisplayCount = 5;
                if (firstResult.isSuc()) {
                    StringBuilder sb = new StringBuilder();
                    for (IEsptouchResult resultInList : result) {
                        sb.append("IP: " + resultInList.getInetAddress()
                                .getHostAddress() + "\n");
                    }
                    Log.v("test", "config success ！！！");

                    btn_ok.setText("配置完成");
                    btn_ok.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(WifiSettingActivity.this, "配置完成", Toast.LENGTH_SHORT).show();
                            mProgressDialog.dismiss();
                        }
                    });
                    txt_notify.setText(sb.toString());
                } else {
                    btn_ok.setText("确认");
                    txt_notify.setText("配置超时，请重启设备再次配置");
                }
            }
        }
    }
}
