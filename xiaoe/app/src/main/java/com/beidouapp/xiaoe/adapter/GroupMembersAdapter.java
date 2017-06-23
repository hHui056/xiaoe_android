package com.beidouapp.xiaoe.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.beidouapp.xiaoe.R;

import java.util.ArrayList;

/**
 * @author hHui
 */

public class GroupMembersAdapter extends BaseAdapter {
    ViewHolder viewHolder = null;
    Context context;
    ArrayList<String> list;

    public GroupMembersAdapter(Context context, ArrayList<String> list) {
        this.context = context;
        this.list = list;

    }

    @Override
    public int getCount() {
        return list.size() + 1;
    }

    @Override
    public Object getItem(int position) {

        if (position == list.size()) {
            return "";
        } else {
            return list.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(R.layout.group_member_item_layout, parent, false);
            viewHolder.txt_member_uid = (TextView) convertView.findViewById(R.id.txt_member_uid);
            viewHolder.layout_member_item = (RelativeLayout) convertView.findViewById(R.id.layout_member_item);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        if (position == list.size()) {//最后一项
            viewHolder.txt_member_uid.setText("");
            viewHolder.layout_member_item.setBackgroundResource(R.drawable.img_add_group_member);
        } else {
            viewHolder.txt_member_uid.setText(list.get(position));
            viewHolder.layout_member_item.setBackgroundResource(R.drawable.img_member_background);
        }
        return convertView;
    }

    class ViewHolder {
        RelativeLayout layout_member_item;
        TextView txt_member_uid;
    }
}
