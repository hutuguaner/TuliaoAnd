package com.lzq.tuliaoand.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.Glide;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.services.MyService;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Logger;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;


public class ConversationActivity extends BaseActivity implements View.OnClickListener {

    private List<Message> messages = new ArrayList<>();
    private String oppositeEmail;
    private RelativeLayout rlBack;
    private EditText etInput;
    private ImageView ivSend;
    private TextView tvTitle;

    private MessagesList messagesList;
    protected MessagesListAdapter<Message> messagesAdapter;

    private MyService myService;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MyBinder myBinder = (MyService.MyBinder) iBinder;
            myService = myBinder.getService();
            myService.startGetMessageFromDBTask(oppositeEmail);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        oppositeEmail = getIntent().getStringExtra("email");
        setContentView(R.layout.activity_conversation);
        initView();
        EventBus.getDefault().register(this);
        bindService(new Intent(this, MyService.class), serviceConnection, BIND_AUTO_CREATE);
    }


    private void initView() {
        rlBack = findViewById(R.id.rl_conversation_back);
        rlBack.setOnClickListener(this);
        etInput = findViewById(R.id.et_conversation_broad);
        ivSend = findViewById(R.id.iv_conversation_paperairplane);
        ivSend.setOnClickListener(this);
        tvTitle = findViewById(R.id.tv_conversation_title);
        tvTitle.setText(oppositeEmail);
        messagesList = findViewById(R.id.ml_conversation);
        messagesAdapter = new MessagesListAdapter<>("1", new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Glide.with(ConversationActivity.this).load(url).into(imageView);
            }
        });
        messagesList.setAdapter(messagesAdapter);

    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        if (myService != null) myService.stopGetMessageFromDBTask();
        unbindService(serviceConnection);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        oppositeEmail = getIntent().getStringExtra("email");
        tvTitle.setText(oppositeEmail);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.rl_conversation_back:
                this.finish();
                break;
            case R.id.iv_conversation_paperairplane:
                String input = etInput.getText().toString();


                if (!StringUtils.isTrimEmpty(input)) {
                    if (input.length() <= 25) {
                        Message message = new Message();

                        message.setContent(input);
                        User fromUser = new User();
                        fromUser.setEmail(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));
                        User toUser = new User();
                        toUser.setEmail(oppositeEmail);
                        message.setTimeStamp(System.currentTimeMillis() / 1000);
                        message.setFrom(fromUser);
                        message.setTo(toUser);
                        myService.sendMessage(message);
                    } else {
                        ToastUtils.showShort("输入内容过长");
                    }
                    etInput.setText("");
                }

                break;
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        List<Message> messages = event.messageList;
        if (messages == null || messages.size() < 1) return;

        //判断当前 是否已有

        for (int i = 0; i < messages.size(); i++) {
            final Message message = messages.get(i);
            if (this.messages.contains(message)) {

            } else {
                this.messages.add(message);
                messagesAdapter.addToStart(messages.get(i), true);
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        App.myDatabase.messageDao().setMessageHasReaded(message.getFrom().getEmail());
                    }
                }).start();
            }

        }
    }
}