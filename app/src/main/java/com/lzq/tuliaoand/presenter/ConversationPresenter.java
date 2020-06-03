package com.lzq.tuliaoand.presenter;

import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.SPUtils;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.MessageForRoom;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.model.ConversationModel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ConversationPresenter {

    private ConversationModel conversationModel;

    private Handler handler;

    public ConversationPresenter(@NonNull ConversationModel conversationModel) {
        this.conversationModel = conversationModel;
        handler = new Handler();
    }


    public void getMessagesFromDB(final String oppositeEmail) {
        new Thread(new Runnable() {
            @Override
            public void run() {

                List<MessageForRoom> messageForRooms = new ArrayList<>();

                List<MessageForRoom> oppositeSendToMe = App.myDatabase.messageDao().getMessagesBy(oppositeEmail,SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));
                for (int i=0;i<oppositeSendToMe.size();i++){
                    Log.i("lala","对方发给我 : "+oppositeSendToMe.get(i).getContent());
                }
                List<MessageForRoom> meSendToOpposite = App.myDatabase.messageDao().getMessagesBy(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()),oppositeEmail);
                for (int i=0;i<meSendToOpposite.size();i++){
                    Log.i("lala","我发给对方 : "+meSendToOpposite.get(i).getContent());
                }

                messageForRooms.addAll(oppositeSendToMe);
                messageForRooms.addAll(meSendToOpposite);

                List<Message> messages = new ArrayList<>();
                for (int i = 0; i < messageForRooms.size(); i++) {
                    MessageForRoom messageForRoom = messageForRooms.get(i);
                    String fromEmail = messageForRoom.getFromEmail();
                    String toEmail = messageForRoom.getToEmail();
                    String conent = messageForRoom.getContent();
                    long time = messageForRoom.getTimeStamp();

                    Message message = new Message();
                    message.setContent(conent);
                    message.setTimeStamp(time);
                    User fromUser = new User();
                    fromUser.setEmail(fromEmail);

                    User toUser = new User();
                    toUser.setEmail(toEmail);
                    message.setFrom(fromUser);
                    message.setTo(toUser);

                    messages.add(message);
                }

                conversationModel.onMsgFromDB(messages);
            }
        }).start();
    }

    public void sendMessage(final Message message) {
        JSONObject params = new JSONObject();
        try {
            params.put("from", message.getFrom().getEmail());
            params.put("to", message.getTo().getEmail());
            params.put("content", message.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkGo.<String>post(Const.UPLOAD_MSG).upJson(params).execute(new StringCallback() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                conversationModel.sendMsgStart();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                conversationModel.sendMsgError(response.body());
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                        saveMessageInDB(message);
                        conversationModel.sendMsgSuccess();
                    } else if (code == 1) {
                        String msg = result.getString("message");
                        conversationModel.sendMsgError(msg);
                    } else if (code == 2) {
                        conversationModel.connectTimeOut();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    conversationModel.sendMsgError(e.getMessage());
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                conversationModel.sendMsgFinish();
            }
        });

    }

    private void saveMessageInDB(final Message message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                MessageForRoom messageForRoom = new MessageForRoom();
                messageForRoom.setFromEmail(message.getFrom().getEmail());
                messageForRoom.setContent(message.getContent());
                messageForRoom.setToEmail(message.getTo().getEmail());
                messageForRoom.setTimeStamp(message.getTimeStamp());
                App.myDatabase.messageDao().insert(messageForRoom);
                Log.i("lala","from : "+messageForRoom.getFromEmail()+" content : "+messageForRoom.getContent()+" to : "+messageForRoom.getToEmail());
            }
        }).start();
    }

}
