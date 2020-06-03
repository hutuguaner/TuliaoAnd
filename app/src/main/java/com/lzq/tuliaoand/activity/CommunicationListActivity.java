package com.lzq.tuliaoand.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.adapter.AdapterCommunicationList;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.services.MyService;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class CommunicationListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    private RelativeLayout rlBack;
    private ListView lv;
    private AdapterCommunicationList adapterCommunicationList;

    private List<Conversation> conversations;

    private MyService myService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MyBinder myBinder = (MyService.MyBinder) iBinder;
            myService = myBinder.getService();
            myService.startGetConversationFromDbTask();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_communication_list);
        initView();
        EventBus.getDefault().register(this);
        bindService(new Intent(this, MyService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (myService != null) myService.stopGetConversationFromDBTask();
        unbindService(serviceConnection);
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
    public void onEvent(Event event) {
        if (event != null) {
            if (!event.isConnectTimeOut) {
                if (event.conversations != null && event.conversations.size() > 0) {
                    this.conversations = event.conversations;
                    adapterCommunicationList = new AdapterCommunicationList(this, conversations);
                    lv.setAdapter(adapterCommunicationList);
                }
            } else {
                ToastUtils.showLong("连接超时，请重新登录");
                startActivity(new Intent(this, LoginActivity.class));
            }
        }
    }


}
