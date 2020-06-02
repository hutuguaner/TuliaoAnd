package com.lzq.tuliaoand.activity;

import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
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
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.model.ConversationModel;
import com.lzq.tuliaoand.presenter.ConversationPresenter;
import com.stfalcon.chatkit.commons.ImageLoader;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import java.util.List;


public class ConversationActivity extends BaseActivity implements View.OnClickListener, ConversationModel {

    private List<Message> messages;

    private RelativeLayout rlBack;
    private EditText etInput;
    private ImageView ivSend;
    private TextView tvTitle;

    private ConversationPresenter conversationPresenter;

    private MessagesList messagesList;
    protected final String senderId = "0";
    protected ImageLoader imageLoader;
    protected MessagesListAdapter<Message> messagesAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        messages = getIntent().getParcelableArrayListExtra("data");
        setContentView(R.layout.activity_conversation);
        initView();
        conversationPresenter = new ConversationPresenter(this);
    }

    private void initView() {
        rlBack = findViewById(R.id.rl_conversation_back);
        rlBack.setOnClickListener(this);
        etInput = findViewById(R.id.et_conversation_broad);
        ivSend = findViewById(R.id.iv_conversation_paperairplane);
        ivSend.setOnClickListener(this);
        tvTitle = findViewById(R.id.tv_conversation_title);
        messagesList = findViewById(R.id.ml_conversation);
        messagesAdapter = new MessagesListAdapter<>("1", new ImageLoader() {
            @Override
            public void loadImage(ImageView imageView, String url, Object payload) {
                Glide.with(ConversationActivity.this).load(url).into(imageView);
            }
        });
        messagesAdapter.addToEnd(messages, false);
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
                        messagesAdapter.addToStart(message, true);

                        conversationPresenter.sendMessage(message);
                    } else {
                        ToastUtils.showShort("输入内容过长");
                    }
                }

                break;
        }
    }



    @Override
    public void connectTimeOut() {
        ToastUtils.showLong("连接断开，请重新登录");
        SPUtils.getInstance().put(SPKey.EMAIL_LOGINED.getUniqueName(),"");
        startActivity(new Intent(this, LoginActivity.class));
    }

    @Override
    public void sendMsgStart() {

    }

    @Override
    public void sendMsgError(String msg) {

    }

    @Override
    public void sendMsgSuccess() {

    }

    @Override
    public void sendMsgFinish() {

    }
}