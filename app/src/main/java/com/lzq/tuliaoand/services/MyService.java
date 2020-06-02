package com.lzq.tuliaoand.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.MessageForRoom;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    public MyService() {
    }


    private TimerTask timerTask;
    private Timer timer;

    public void startTask() {
        if (timerTask == null) timerTask = new TimerTask() {
            @Override
            public void run() {
                List<Message> messageList = getMsgs();
                if (messageList != null) {
                    //
                    List<MessageForRoom> messageForRooms = new ArrayList<>();
                    for (int i=0;i<messageList.size();i++){
                        MessageForRoom messageForRoom = new MessageForRoom();
                        Message message = messageList.get(i);
                        messageForRoom.setFromEmail(message.getFrom().getEmail());
                        messageForRoom.setToEmail(message.getTo().getEmail());
                        messageForRoom.setContent(message.getContent());
                        messageForRoom.setConversationId(message.getFrom().getEmail()+message.getTo().getEmail());
                        messageForRoom.setTimeStamp(message.getTimeStamp());
                    }
                    App.myDatabase.messageDao().insert(messageForRooms);
                    //
                    Event event = new Event();
                    event.messageList = messageList;
                    EventBus.getDefault().post(event);
                }
            }
        };

        if (timer == null) timer = new Timer();

        timer.schedule(timerTask, 0, 1000 * 5);

    }


    public void stopTask() {
        if (timerTask != null) timerTask.cancel();
        if (timer != null) timer.purge();
        timerTask = null;
    }


    private List<Message> getMsgs() {
        String email = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            okhttp3.Response response = OkGo.<String>post(Const.GET_MSGS).upJson(jsonObject).execute();
            JSONObject result = new JSONObject(response.body().string());
            int code = result.getInt("code");
            if (code == 0) {
                JSONArray data = result.getJSONArray("data");
                List<Message> messages = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    String from = item.getString("from");
                    String to = item.getString("to");
                    String content = item.getString("content");
                    String time = item.getString("time");
                    Message message = new Message();
                    User userFrom = new User();
                    userFrom.setEmail(from);
                    User userTo = new User();
                    userTo.setEmail(to);
                    message.setFrom(userFrom);
                    message.setContent(content);
                    message.setTo(userTo);
                    if (!StringUtils.isTrimEmpty(time))
                        message.setTimeStamp(Long.parseLong(time));
                    messages.add(message);
                }
                return messages;
            } else if (code == 1) {
                return null;
            } else if (code == 2) {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private MyBinder myBinder = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return myBinder;
    }


    public class MyBinder extends Binder {
        public MyService getService() {
            return MyService.this;
        }
    }

}
