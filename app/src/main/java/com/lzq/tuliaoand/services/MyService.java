package com.lzq.tuliaoand.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.MessageForRoom;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.bean.Version;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MyService extends Service {
    public MyService() {
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private TimerTask getVersionTask;
    private Timer getVersionTimer;

    public void startGetVersionTask() {
        if (getVersionTask == null) getVersionTask = new TimerTask() {
            @Override
            public void run() {
                if (!StringUtils.isTrimEmpty(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                    Version version = getVersion();
                    if (version != null) {
                        Event event = new Event();
                        event.type = Event.TYPE_MAIN;
                        event.version = version;
                        EventBus.getDefault().post(event);

                    }
                }

            }
        };
        if (getVersionTimer == null) getVersionTimer = new Timer();
        getVersionTimer.schedule(getVersionTask, 1000 * 5, 1000 * 20);
    }

    public void stopGetVersionTask() {
        if (getVersionTask != null) getVersionTask.cancel();
        if (getVersionTimer != null) getVersionTimer.purge();
        getVersionTask = null;
    }

    private Version getVersion() {
        Version version = null;
        try {
            okhttp3.Response response = OkGo.<String>post(Const.GET_VERSION).execute();
            String result = response.body().string();
            Log.i("lala", " 获取版本信息 ： " + result);
            response.close();
            JSONObject jsonObject = new JSONObject(result);
            int code = jsonObject.getInt("code");
            if (code == 0) {
                JSONObject data = jsonObject.getJSONObject("data");
                version = new Version();
                version.setVersionCode(data.getInt("versionCode"));
                version.setVersionName(data.getString("versionName"));
                version.setForceUpdate(data.getInt("forceUpdate"));
                version.setVersionDesc(data.getString("versionDesc"));
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            version = null;
        }

        return version;

    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    private TimerTask getMessageFromDBTask;
    private Timer getMessageFromDBTimer;

    public void startGetMessageFromDBTask(final String oppositeEmail) {
        if (getMessageFromDBTask == null) getMessageFromDBTask = new TimerTask() {
            @Override
            public void run() {
                List<Message> messageList = getMessagesFromDB(oppositeEmail);

                if (messageList != null && messageList.size() > 0) {
                    Event event = new Event();
                    event.messageList = messageList;
                    event.type = Event.TYPE_CONVERSATION;
                    EventBus.getDefault().post(event);
                }
            }
        };
        if (getMessageFromDBTimer == null) getMessageFromDBTimer = new Timer();
        getMessageFromDBTimer.schedule(getMessageFromDBTask, 0, 1000);
    }

    public void stopGetMessageFromDBTask() {
        if (getMessageFromDBTask != null) getMessageFromDBTask.cancel();
        if (getMessageFromDBTimer != null) getMessageFromDBTimer.purge();
        getMessageFromDBTask = null;
    }


    public List<Message> getMessagesFromDB(final String oppositeEmail) {

        List<MessageForRoom> messageForRooms = new ArrayList<>();

        List<MessageForRoom> meSendToOpposite = App.myDatabase.messageDao().getMessages(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()), oppositeEmail);
        List<MessageForRoom> oppositeToMe = App.myDatabase.messageDao().getMessages(oppositeEmail, SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));

        messageForRooms.addAll(meSendToOpposite);
        messageForRooms.addAll(oppositeToMe);

       /* messageForRooms = App.myDatabase.messageDao().getMessages();
        Log.i("lala","消息数量 ： "+messageForRooms.size());

        Iterator<MessageForRoom> iterator = messageForRooms.iterator();
        while (iterator.hasNext()) {
            MessageForRoom messageForRoom = iterator.next();
            String from = messageForRoom.getFromEmail();
            String to = messageForRoom.getToEmail();
            Log.i("lala",from+" "+to+" "+" | "+SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName())+" "+oppositeEmail);
            if (from.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))){
                if (to.equals(oppositeEmail)){

                }else{
                    iterator.remove();
                }
            }else if (from.equals(oppositeEmail)){
                if (to.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))){

                }else{
                    iterator.remove();
                }
            }else{
                iterator.remove();
            }

        }*/


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
        Collections.sort(messages);
        return messages;
    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private TimerTask getConverationFromDBTask;
    private Timer getConversationFromDBTimer;

    public void startGetConversationFromDbTask() {
        if (getConverationFromDBTask == null) getConverationFromDBTask = new TimerTask() {
            @Override
            public void run() {
                List<Conversation> conversations = getConversationFromDB();
                if (conversations != null && conversations.size() > 0) {
                    Event event = new Event();
                    event.conversations = conversations;
                    event.type = Event.TYPE_COMMUNICATION;
                    EventBus.getDefault().post(event);
                }
            }
        };
        if (getConversationFromDBTimer == null) getConversationFromDBTimer = new Timer();
        getConversationFromDBTimer.schedule(getConverationFromDBTask, 0, 1000);
    }

    public void stopGetConversationFromDBTask() {
        if (getConverationFromDBTask != null) getConverationFromDBTask.cancel();
        if (getConversationFromDBTimer != null) getConversationFromDBTimer.purge();
        getConverationFromDBTask = null;
    }

    private List<Conversation> getConversationFromDB() {

        List<MessageForRoom> messageForRooms = App.myDatabase.messageDao().getMessages();
        if (messageForRooms == null || messageForRooms.size() < 1) {
            return null;
        } else {
            final List<Conversation> conversations = new ArrayList<>();
            for (int i = 0; i < messageForRooms.size(); i++) {
                MessageForRoom messageForRoom = messageForRooms.get(i);
                String msgFromEmail = messageForRoom.getFromEmail();
                String msgToEmail = messageForRoom.getToEmail();
                String content = messageForRoom.getContent();
                long time = messageForRoom.getTimeStamp();
                int hasRead = messageForRoom.getHasRead();

                Message message = new Message();
                User msgFromUser = new User();
                msgFromUser.setEmail(msgFromEmail);
                User msgToUser = new User();
                msgToUser.setEmail(msgToEmail);
                message.setFrom(msgFromUser);
                message.setTo(msgToUser);
                message.setContent(content);
                message.setTimeStamp(time);
                message.setHasRead(hasRead);


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
                    if (conversation.getMe().getEmail().equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName())))
                        conversations.add(conversation);
                }

            }

            return conversations;
        }

    }


    //-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void sendMessage(final Message message) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                JSONObject params = new JSONObject();
                try {
                    params.put("from", message.getFrom().getEmail());
                    params.put("to", message.getTo().getEmail());
                    params.put("content", message.getContent());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //上云
                try {
                    okhttp3.Response response = OkGo.<String>post(Const.UPLOAD_MSG).upJson(params).execute();
                    String result = response.body().string();
                    //Log.i("lala", "消息上云 : " + result);
                    response.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //本地库存储
                MessageForRoom messageForRoom = new MessageForRoom();
                messageForRoom.setTimeStamp(message.getTimeStamp());
                messageForRoom.setContent(message.getContent());
                messageForRoom.setToEmail(message.getTo().getEmail());
                messageForRoom.setFromEmail(message.getFrom().getEmail());
                messageForRoom.setHasRead(1);
                App.myDatabase.messageDao().insert(messageForRoom);
            }
        }).start();

    }


    //---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void uploadBroadCast(String broadcast) {
        String email = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("broadcast", broadcast);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>post(Const.UPLOAD_BROADCAST).upJson(params).execute(new StringCallback() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                    } else if (code == 1) {
                        String msg = result.getString("message");
                    } else if (code == 2) {
                        //连接超时 重新登录
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private TimerTask getUserTask;
    private Timer getUserTimer;


    public void startGetUserTask() {
        if (getUserTask == null) getUserTask = new TimerTask() {
            @Override
            public void run() {
                if (!StringUtils.isTrimEmpty(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                    Event event = getUsers();
                    if (event != null)
                        event.type = Event.TYPE_MAIN;
                    EventBus.getDefault().post(event);
                }


            }
        };

        if (getUserTimer == null) getUserTimer = new Timer();

        getUserTimer.schedule(getUserTask, 0, 1000 * 5);

    }


    public void stopGetUserTask() {
        if (getUserTask != null) getUserTask.cancel();
        if (getUserTimer != null) getUserTimer.purge();
        getUserTask = null;
    }


    private Event getUsers() {
        Event event = new Event();
        String me = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", me);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            okhttp3.Response response = OkGo.<String>post(Const.GET_ALL_USERS).upJson(jsonObject).execute();

            JSONObject result = new JSONObject(response.body().string());
            response.close();

            int code = result.getInt("code");
            if (code == 0) {
                JSONArray data = result.getJSONArray("data");
                List<User> users = new ArrayList<>();
                for (int i = 0; i < data.length(); i++) {
                    JSONObject item = data.getJSONObject(i);
                    String email = item.getString("email");
                    String broadcast = item.getString("broadcast");
                    String lng = item.getString("lng");
                    String lat = item.getString("lat");
                    User user = new User();
                    user.setEmail(email);
                    user.setBroadcast(broadcast);
                    if (!StringUtils.isTrimEmpty(lng))
                        user.setLng(Double.parseDouble(lng));
                    if (!StringUtils.isTrimEmpty(lat))
                        user.setLat(Double.parseDouble(lat));
                    users.add(user);
                }
                event.users = users;
            } else if (code == 1) {
                String msg = result.getString("message");
            } else if (code == 2) {

                event.isConnectTimeOut = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return event;
    }

    //-----------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void uploadPosition(double lng, double lat) {
        String email = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("lng", Double.toString(lng));
            params.put("lat", Double.toString(lat));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>post(Const.UPLOAD_POSITION).upJson(params).execute(new StringCallback() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
            }

            @Override
            public void onSuccess(Response<String> response) {
                //Log.i("lala", "上传位置 ： " + response.body());
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                    } else if (code == 1) {
                        String msg = result.getString("message");
                    } else if (code == 2) {
                        //连接超时 重新登录
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
            }
        });
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    private TimerTask getMessageTask;
    private Timer getMessageTimer;


    public void startGetMessageTask() {
        if (getMessageTask == null) getMessageTask = new TimerTask() {
            @Override
            public void run() {
                if (!StringUtils.isTrimEmpty(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                    List<Message> messageList = getMsgs();
                    if (messageList != null && messageList.size() > 0) {


                        //
                        List<MessageForRoom> messageForRooms = new ArrayList<>();
                        for (int i = 0; i < messageList.size(); i++) {
                            MessageForRoom messageForRoom = new MessageForRoom();
                            Message message = messageList.get(i);
                            messageForRoom.setFromEmail(message.getFrom().getEmail());
                            messageForRoom.setToEmail(message.getTo().getEmail());
                            messageForRoom.setContent(message.getContent());
                            messageForRoom.setTimeStamp(message.getTimeStamp());
                            messageForRooms.add(messageForRoom);
                        }
                        App.myDatabase.messageDao().insert(messageForRooms);

                        //告诉主页 有消息了
                        Event event = new Event();
                        event.messageList = messageList;
                        event.type = Event.TYPE_MAIN;
                        EventBus.getDefault().post(event);

                    }
                }

            }
        };

        if (getMessageTimer == null) getMessageTimer = new Timer();

        getMessageTimer.schedule(getMessageTask, 0, 1000 * 1);

    }


    public void stopGetMessageTask() {
        if (getMessageTask != null) getMessageTask.cancel();
        if (getMessageTimer != null) getMessageTimer.purge();
        getMessageTask = null;
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
            //Log.i("lala","从服务器获取消息 : "+result.toString());
            response.close();
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
