package com.lzq.tuliaoand.presenter;

import android.os.Handler;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPUtils;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.MessageForRoom;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.model.CommunicationListModel;

import java.util.ArrayList;
import java.util.List;

public class CommunicationListPresenter {

    private CommunicationListModel communicationListModel;
    private Handler handler;

    public CommunicationListPresenter(@NonNull CommunicationListModel communicationListModel) {
        this.communicationListModel = communicationListModel;
        handler = new Handler();
    }


    public void getMessagesFromDB() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                List<MessageForRoom> messageForRooms = App.myDatabase.messageDao().getMessages();
                if (messageForRooms == null || messageForRooms.size() < 1) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            communicationListModel.onMessageFromDB(null);
                        }
                    });
                } else {
                    final List<Conversation> conversations = new ArrayList<>();
                    for (int i = 0; i < messageForRooms.size(); i++) {
                        MessageForRoom messageForRoom = messageForRooms.get(i);
                        String msgFromEmail = messageForRoom.getFromEmail();
                        String msgToEmail = messageForRoom.getToEmail();
                        String content = messageForRoom.getContent();
                        long time = messageForRoom.getTimeStamp();

                        Message message = new Message();
                        User msgFromUser = new User();
                        msgFromUser.setEmail(msgFromEmail);
                        User msgToUser = new User();
                        msgToUser.setEmail(msgToEmail);
                        message.setFrom(msgFromUser);
                        message.setTo(msgToUser);
                        message.setContent(content);
                        message.setTimeStamp(time);


                        Conversation conversation = new Conversation();
                        User me = new User();
                        me.setEmail(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));
                        User opposite = new User();
                        opposite.setEmail(msgFromEmail.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName())) ? msgToEmail : msgFromEmail);
                        ArrayList<Message> messages = new ArrayList<>();
                        messages.add(message);
                        conversation.setMe(me);
                        conversation.setOpposite(opposite);
                        conversation.setMessages(messages);

                        if (conversations.contains(conversation)) {
                            int index = conversations.indexOf(conversation);
                            conversations.get(index).getMessages().add(message);
                        } else {
                            conversations.add(conversation);
                        }

                    }

                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            communicationListModel.onMessageFromDB(conversations);

                        }
                    });

                }
            }
        }).start();
    }

}
