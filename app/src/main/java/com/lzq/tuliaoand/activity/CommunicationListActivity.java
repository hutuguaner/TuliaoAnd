package com.lzq.tuliaoand.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.adapter.AdapterCommunicationList;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.services.MyService;
import com.orhanobut.logger.Logger;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.dialogs.DialogsList;
import com.stfalcon.chatkit.dialogs.DialogsListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

public class CommunicationListActivity extends BaseActivity implements View.OnClickListener, AdapterView.OnItemClickListener, DialogsListAdapter.OnDialogClickListener {

    private RelativeLayout rlBack;

    private List<Conversation> conversations = new ArrayList<>();

    private DialogsList dlCommunication;
    private DialogsListAdapter adapterCommunicationList;

    private MyService myService;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MyBinder myBinder = (MyService.MyBinder) iBinder;
            myService = myBinder.getService();
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
        unbindService(serviceConnection);
    }

    private void initView() {
        rlBack = findViewById(R.id.rl_communication_list_back);
        rlBack.setOnClickListener(this);
        dlCommunication = findViewById(R.id.dl_communication_list);
        adapterCommunicationList = new DialogsListAdapter(new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Glide.with(CommunicationListActivity.this).load(url).into(imageView);
            }
        });
        adapterCommunicationList.setItems(conversations);
        adapterCommunicationList.setOnDialogClickListener(this);
        dlCommunication.setAdapter(adapterCommunicationList);
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
        if (event == null) return;
        if (event.type != Event.TYPE_COMMUNICATION) return;
        if (event != null) {
            if (!event.isConnectTimeOut) {
                if (event.conversations != null && event.conversations.size() > 0) {

                    for (int i = 0; i < event.conversations.size(); i++) {
                        Conversation item = event.conversations.get(i);

                        if (conversations.contains(item)) {
                            //更新
                            int index = conversations.indexOf(item);
                            conversations.get(index).setMessages(item.getMessages());
                            boolean isUpdateOk = adapterCommunicationList.updateDialogWithMessage(item.getId(), item.getLastMessage());
                        } else {
                            conversations.add(item);
                            //adapterCommunicationList.addItem(item);
                        }
                    }

                }
            } else {
                ToastUtils.showLong("连接超时，请重新登录");
                startActivity(new Intent(this, LoginActivity.class));
            }
        }
    }


    @Override
    public void onDialogClick(IDialog dialog) {
        Conversation conversation = (Conversation) dialog;
        final String oppositeEmail = conversation.getOpposite().getEmail();
        //
        new Thread(new Runnable() {
            @Override
            public void run() {
                App.myDatabase.messageDao().setMessageHasReaded(oppositeEmail);
            }
        }).start();
        //
        Intent intent = new Intent(this, ConversationActivity.class);
        intent.putExtra("email", oppositeEmail);
        this.startActivity(intent);
    }
}
