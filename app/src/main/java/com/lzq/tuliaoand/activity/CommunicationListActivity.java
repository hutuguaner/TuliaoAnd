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
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.model.CommunicationListModel;
import com.lzq.tuliaoand.presenter.CommunicationListPresenter;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class CommunicationListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, CommunicationListModel {

    private RelativeLayout rlBack;
    private ListView lv;
    private AdapterCommunicationList adapterCommunicationList;

    private List<Conversation> conversations;

    private CommunicationListPresenter presenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_list);
        initView();
        presenter = new CommunicationListPresenter(this);
        presenter.getMessagesFromDB();
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    private void initView() {
        rlBack = findViewById(R.id.rl_communication_list_back);
        rlBack.setOnClickListener(this);
        lv = findViewById(R.id.lv_communication_list);
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
        Conversation conversation = conversations.get(postion);
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("email", conversation.getOpposite());
        startActivity(intent);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event){

    }

    @Override
    public void onMessageFromDB(List<Conversation> conversations) {
        this.conversations = conversations;
        adapterCommunicationList = new AdapterCommunicationList(this, conversations);
        lv.setAdapter(adapterCommunicationList);
    }
}
