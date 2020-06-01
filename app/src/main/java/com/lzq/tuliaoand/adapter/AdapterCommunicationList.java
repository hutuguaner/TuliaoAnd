package com.lzq.tuliaoand.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.StringUtils;
import com.bumptech.glide.Glide;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Message;

import java.util.List;

public class AdapterCommunicationList extends BaseAdapter {

    private Context context;

    private List<Conversation> conversations;

    public AdapterCommunicationList(Context context, List<Conversation> conversations) {
        this.context = context;
        this.conversations = conversations;
    }

    @Override
    public int getCount() {
        return conversations.size();
    }

    @Override
    public Object getItem(int position) {
        return conversations.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.adapter_communication_list, null);
            viewHolder = new ViewHolder();
            viewHolder.ivAvatar = convertView.findViewById(R.id.iv_adapter_communication_list_avatar);
            viewHolder.tvMsgNewest = convertView.findViewById(R.id.tv_adapter_communication_list_msgnewest);
            viewHolder.tvTimeNewestMsg = convertView.findViewById(R.id.tv_adapter_communication_list_timenewestmsg);
            viewHolder.tvSizeUncheckMsg = convertView.findViewById(R.id.tv_adapter_communication_list_uncheckmsgsize);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        Conversation conversation = (Conversation) getItem(position);

        List<Message> messages = conversation.getMessages();

        viewHolder.tvMsgNewest.setText(messages.get(messages.size()-1).getContent());
        viewHolder.tvTimeNewestMsg.setText(messages.get(messages.size()-1).getTimeStringMsg());
        viewHolder.tvSizeUncheckMsg.setText(Integer.toString(messages.size()));

        return convertView;
    }

    class ViewHolder {
        ImageView ivAvatar;
        TextView tvMsgNewest;
        TextView tvTimeNewestMsg;
        TextView tvSizeUncheckMsg;
    }
}
