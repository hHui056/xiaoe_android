package com.beidouapp.xiaoe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.beidouapp.xiaoe.R;
import com.beidouapp.xiaoe.bean.MessageItem;
import com.beidouapp.xiaoe.bean.MessageOwner;

import java.util.ArrayList;

/**
 * Created by hHui on 2017/6/12.
 */

public class ShowMessageAdapter extends BaseAdapter {

    public static final int MESSAGE_ME = 0x00;
    public static final int MESSAGE_SOMEONE = 0x01;
    public static final int MESSAGE_SOUND = 0x02;
    Context context;
    ArrayList<MessageItem> list;
    ImageClickListener imageClickListener;
    private LayoutInflater mInflater;

    public ShowMessageAdapter(Context context, ArrayList<MessageItem> list) {
        this.context = context;
        this.list = list;
        mInflater = LayoutInflater.from(context);
    }

    public void setImageClickListener(ImageClickListener imageClickListener) {
        this.imageClickListener = imageClickListener;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        int msgType = getItemViewType(position);
        BaseHolder viewHolder = null;
        if (viewHolder == null && mInflater != null) {
            viewHolder = new BaseHolder();
            switch (msgType) {
                case MESSAGE_ME:
                    convertView = mInflater.inflate(R.layout.memsg_layout, parent, false);
                    viewHolder = new TextHolder();
                    ((TextHolder) viewHolder).msg = (TextView) convertView.findViewById(R.id.txt_me_msg);
                    convertView.setTag(viewHolder);
                    break;
                case MESSAGE_SOMEONE:
                    convertView = mInflater.inflate(R.layout.someonemsg_layout, parent, false);
                    viewHolder = new TextHolder();
                    ((TextHolder) viewHolder).msg = (TextView) convertView.findViewById(R.id.txt_someone_msg);
                    convertView.setTag(viewHolder);
                    break;
                case MESSAGE_SOUND:
                    convertView = mInflater.inflate(R.layout.sound_layout, parent, false);
                    viewHolder = new SoundHolder();
                    ((SoundHolder) viewHolder).image = (ImageView) convertView.findViewById(R.id.img_sound);
                    ((SoundHolder) viewHolder).image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (!list.get(position).getSoundPath().equals("")) {
                                String soundFilePath = list.get(position).getSoundPath();
                                imageClickListener.OnImageClick(soundFilePath);
                            }
                        }
                    });
                    convertView.setTag(viewHolder);
                    break;
                default:
                    break;
            }
        } else {
            viewHolder = (BaseHolder) convertView.getTag();
        }
        final MessageItem item = list.get(position);
        if (item != null) {
            if (!item.isSound()) {
                TextHolder textholder = (TextHolder) viewHolder;
                textholder.msg.setText(item.getMsg());
            }
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        MessageItem item = list.get(position);
        if (item.isSound()) {
            return MESSAGE_SOUND;
        } else {
            if (item.getOwner() == MessageOwner.me) {
                return MESSAGE_ME;
            } else {
                return MESSAGE_SOMEONE;
            }
        }
    }

    public interface ImageClickListener {
        public void OnImageClick(String soundpath);
    }

    class BaseHolder {

    }

    class TextHolder extends BaseHolder {
        TextView msg;
    }

    class SoundHolder extends BaseHolder {
        ImageView image;
    }
}
