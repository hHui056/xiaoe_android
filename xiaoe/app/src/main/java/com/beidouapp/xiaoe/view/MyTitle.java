package com.beidouapp.xiaoe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.activity.BaseActivity;

/**
 * Created by hHui on 2017/6/1.
 */

public class MyTitle extends RelativeLayout {
    View view;
    Context context;
    ImageView back;
    TextView txt_title;
    OnClickListener myclick = new OnClickListener() {
        @Override
        public void onClick(View v) {
            BaseActivity activity = (BaseActivity) context;
            activity.finish();
            activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
    };

    public MyTitle(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public MyTitle(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public MyTitle(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    /**
     * 初始化视图
     */
    public void init() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.title_layout, this);
        back = (ImageView) view.findViewById(R.id.img_back);
        txt_title = (TextView) view.findViewById(R.id.txt_title);
        back.setOnClickListener(myclick);
    }

    /**
     * 设置标题名字
     *
     * @param titleName
     */
    public void setTitle(String titleName) {
        txt_title.setText(titleName);
    }
}
