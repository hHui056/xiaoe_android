package com.beidouapp.xiaoe.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.beidouapp.et.ErrorInfo;
import com.beidouapp.et.IActionListener;
import com.beidouapp.et.Message;
import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.instruction.Instruction;
import com.beidouapp.xiaoe.instruction.RGBControllerReqBody;
import com.beidouapp.xiaoe.utils.Constans;
import com.beidouapp.xiaoe.utils.TestUtil;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.MPPointF;

import java.util.ArrayList;

/**
 * 灯光控制
 *
 * @author hHui
 */
public class RGBControllerActivity extends BaseActivity implements OnChartValueSelectedListener, View.OnClickListener {
    PieChart pieLight;
    Button btnBackToFirstpage;
    MyBroadcastReceiver rec;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rgbcontroller);

        initPieChart();
        registBroadcase();
    }

    /**
     * 初始化饼装图
     */
    void initPieChart() {
        pieLight = (PieChart) findViewById(R.id.pie_light);

        btnBackToFirstpage = (Button) findViewById(R.id.btn_back_to_firstpage);
        btnBackToFirstpage.setOnClickListener(this);

        pieLight.setUsePercentValues(false);
        pieLight.getDescription().setEnabled(false);
        pieLight.getLegend().setEnabled(false);
        pieLight.setExtraOffsets(5, 10, 5, 5);//左上右下的距离
        pieLight.setDragDecelerationFrictionCoef(0.95f);//拖拉阻力
        pieLight.setDrawHoleEnabled(true);//设置圆中间的孔
        pieLight.setHoleColor(Color.WHITE);//设置孔的颜色
        pieLight.setHoleRadius(50f);//设置孔半径
        pieLight.setTransparentCircleColor(Color.WHITE);//设置透明圆的颜色
        pieLight.setTransparentCircleAlpha(110);//设置透明圆的透明度
        pieLight.setTransparentCircleRadius(50f);//设置透明圆的半径
        pieLight.setDrawCenterText(false);//是否显示圆中文字
        pieLight.setRotationAngle(0);//设置旋转角度
        pieLight.setRotationEnabled(true);//是否可旋转
        pieLight.setHighlightPerTapEnabled(true);//是否可以高亮圆
        pieLight.setOnChartValueSelectedListener(this);
        ArrayList<PieEntry> entries = new ArrayList<PieEntry>();// PieEntry 切片实体
        for (int i = 0; i < 12; i++) {
            entries.add(new PieEntry(100 / 12));
        }
        PieDataSet dataSet = new PieDataSet(entries, "Election Results");//设置切片的种类
        dataSet.setDrawIcons(true);//设置是否显示每个切片外图片
        dataSet.setSliceSpace(1f);//相邻切片间的间距
        dataSet.setIconsOffset(new MPPointF(0, 500));//???
        dataSet.setSelectionShift(7.5f);//设置选中区域放大效果
        ArrayList<Integer> colors = new ArrayList<Integer>();
        for (int c : Constans.LIGHT_COLORS) {
            colors.add(c);
        }
        dataSet.setColors(colors);
        PieData data = new PieData(dataSet);//扇形区域上显示的文字类
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(0f);
        pieLight.setData(data);
        pieLight.highlightValues(null);
        pieLight.invalidate();
        pieLight.animateY(1400, Easing.EasingOption.EaseInOutQuad);
        pieLight.setEntryLabelColor(Color.WHITE);
        pieLight.setEntryLabelTextSize(12f);
    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {
        int index = (int) h.getX();
        controlLight(index);
    }

    @Override
    public void onNothingSelected() {//未选中任何一项--->关灯
        controlLight(12);
    }

    /**
     * 控制灯光颜色
     *
     * @param index
     */
    void controlLight(int index) {
        Instruction instruction = new Instruction.Builder().setCmd(Instruction.Cmd.CONTROL).setBody(new RGBControllerReqBody(index)).createInstruction();
        Message message = new Message();
        message.setPayload(instruction.toByteArray());
        isdkContext.chatTo(DEVICE_UID, message, new IActionListener() {
            @Override
            public void onSuccess() {
                TestUtil.showTest("控制RGB成功");
            }

            @Override
            public void onFailure(ErrorInfo errorInfo) {
                TestUtil.showTest("控制RGB失败  " + errorInfo.getCode() + errorInfo.getReason());
            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_back_to_firstpage) {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(rec);
    }

    /**
     * 注册广播
     */
    public void registBroadcase() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Constans.RECEIVE_MSG);
        filter.addAction(Constans.CMD_WRONG);
        rec = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(rec, filter);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constans.CMD_WRONG)) {
                showErrorMessage(getString(R.string.waring_msg));
            }
        }
    }
}
