package com.lzq.tuliaoand.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.common.SPKey;

import java.util.List;

public class AdapterMessageList extends BaseAdapter {

    private Context context;

    private List<Message> messages;

    public AdapterMessageList(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int position) {
        return messages.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_message_list, null);
            holder.rlLeft = convertView.findViewById(R.id.rl_message_adapter_left);
            holder.rlRight = convertView.findViewById(R.id.rl_message_adapter_right);
            holder.tvLeft = convertView.findViewById(R.id.tv_message_adapter_left);
            holder.tvRight = convertView.findViewById(R.id.tv_message_adapter_right);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Message message = messages.get(position);
        String from = message.getEmailFrom();
        String content = message.getContent();

        if (from.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            holder.rlLeft.setVisibility(View.GONE);
            holder.tvRight.setText(content);
        } else {
            holder.rlRight.setVisibility(View.GONE);
            holder.tvLeft.setText(content);
        }


        return convertView;
    }

    class ViewHolder {
        RelativeLayout rlLeft;
        RelativeLayout rlRight;
        TextView tvLeft;
        TextView tvRight;
    }

}
