package com.beidouapp.xiaoe.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beidouapp.xiaoe.R;

/**
 * Created by hHui on 2017/5/31.
 * <p>
 * <p>
 * 功能模块
 */

public class OptionView extends RelativeLayout {
    TextView txt_option_name, txt_option_describe;
    ImageView img_option;
    Context context;
    View view;

    public OptionView(Context context) {
        super(context);
        this.context = context;
        init();
    }

    public OptionView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        init();
    }

    public OptionView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
        init();
    }

    public void init() {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.option_layout, this);

        img_option = (ImageView) view.findViewById(R.id.img_option);
        txt_option_name = (TextView) view.findViewById(R.id.txt_option_name);
        txt_option_describe = (TextView) view.findViewById(R.id.txt_option_describe);
    }

    /**
     * 设置功能名称
     *
     * @param name
     */
    public void setName(String name) {
        txt_option_name.setText(name);
    }

    /**
     * 设置功能描述信息
     *
     * @param describe
     */
    public void setDescribe(String describe) {
        txt_option_describe.setText(describe);
    }

    /**
     * 设置功能图片
     *
     * @param image
     */
    public void setImage(int image) {
        img_option.setImageResource(image);
    }

}
