package com.lzq.tuliaoand.services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

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
import com.orhanobut.logger.Logger;

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

//-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
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
                Event event = getUsers();
                if (event != null && event.users != null && event.users.size() > 0)
                    EventBus.getDefault().post(event);

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
                    }
                    App.myDatabase.messageDao().insert(messageForRooms);
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
