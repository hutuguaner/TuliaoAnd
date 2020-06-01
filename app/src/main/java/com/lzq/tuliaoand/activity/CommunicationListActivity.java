package com.lzq.tuliaoand.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.adapter.AdapterCommunicationList;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Message;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class CommunicationListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private RelativeLayout rlBack;
    private ListView lv;
    private AdapterCommunicationList adapterCommunicationList;

    private List<Conversation> conversations;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        conversations = getIntent().getParcelableArrayListExtra("data");
        setContentView(R.layout.activity_communication_list);
        initView();
    }

    private void initView() {
        rlBack = findViewById(R.id.rl_communication_list_back);
        rlBack.setOnClickListener(this);
        lv = findViewById(R.id.lv_communication_list);
        adapterCommunicationList = new AdapterCommunicationList(this, conversations);
        lv.setAdapter(adapterCommunicationList);
        lv.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_communication_list_back:
                this.finish();
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        startConversationActivity(position);
    }


    private void startConversationActivity(int postion) {
        ArrayList<Message> messages = conversations.get(postion).getMessages();
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("data", messages);
        startActivity(intent);
    }
}
