package com.lzq.tuliaoand.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.sqlite.db.SimpleSQLiteQuery;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.JsonObject;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.MessageForRoom;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.bean.Version;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.common.SocketIOEvent;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.util.LocationUtils;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MyService extends Service {

    private Socket socket;

    @Override
    public void onCreate() {
        super.onCreate();
        App app = (App) getApplication();
        socket = app.getSocket();
        socket.on(Socket.EVENT_CONNECT, onConnectListener);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnectListener);
        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener);
        socket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener);
        socket.on(SocketIOEvent.SEND_POSITION, onSendPositionListener);
        socket.on(SocketIOEvent.SEND_BROADCAST, onSendBroadCastListener);
        socket.on(SocketIOEvent.SEND_MSG, onSendChatMsgListener);
        socket.on(SocketIOEvent.RECEIVE_POSITION, onReceivePositionListener);
        socket.on(SocketIOEvent.RECEIVE_BROADCAST, onReceiveBroadcastListener);
        socket.on(SocketIOEvent.RECEIVE_MSG, onReceiveChatMsgListener);
        socket.on(SocketIOEvent.OFFLINE, onOfflineListener);
        socket.connect();
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        socket.disconnect();
        socket.off(Socket.EVENT_CONNECT, onConnectListener);
        socket.off(Socket.EVENT_DISCONNECT, onDisconnectListener);
        socket.off(Socket.EVENT_CONNECT_ERROR, onConnectErrorListener);
        socket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectErrorListener);
        socket.off(SocketIOEvent.SEND_POSITION, onSendPositionListener);
        socket.off(SocketIOEvent.SEND_BROADCAST, onSendBroadCastListener);
        socket.off(SocketIOEvent.SEND_MSG, onSendChatMsgListener);
        socket.off(SocketIOEvent.RECEIVE_POSITION, onReceivePositionListener);
        socket.off(SocketIOEvent.RECEIVE_BROADCAST, onReceiveBroadcastListener);
        socket.off(SocketIOEvent.RECEIVE_MSG, onReceiveChatMsgListener);
        socket.off(SocketIOEvent.OFFLINE, onOfflineListener);
    }

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


    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    public void sendPositionToServer() {
        final Location location = LocationUtils.getLastKnownLocation((LocationManager) getSystemService(LOCATION_SERVICE));
        if (location != null) {
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("lng",location.getLongitude());
                jsonObject.put("lat",location.getLatitude());
                jsonObject.put("email",SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            socket.emit(SocketIOEvent.SEND_POSITION, jsonObject.toString());
        }
    }

    public void sendBroadcastToServer(String broadcast){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("broadcast",broadcast);
            jsonObject.put("email",SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        socket.emit(SocketIOEvent.SEND_BROADCAST,jsonObject.toString());
    }

    public void sendChatMsgToServer(Message message){
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("from",SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()));
            jsonObject.put("to",message.getTo().getEmail());
            jsonObject.put("msg",message.getContent());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        socket.emit(SocketIOEvent.SEND_MSG,jsonObject.toString());
    }

    private void quit(){
        socket.disconnect();
        socket.close();
        socket = null;
    }

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
    //
    private Emitter.Listener onConnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };
    //
    private Emitter.Listener onDisconnectListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    //
    private Emitter.Listener onConnectErrorListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    //
    private Emitter.Listener onSendPositionListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    //
    private Emitter.Listener onSendBroadCastListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };

    //
    private Emitter.Listener onSendChatMsgListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {

        }
    };
    //
    private Emitter.Listener onReceivePositionListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //有用户位置更新
            Log.i("lala","用户位置更新 : "+args[0]);
        }
    };
    //
    private Emitter.Listener onReceiveBroadcastListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //有用户发了广播
            Log.i("lala","收到广播 : "+args[0]);
        }
    };
    //
    private Emitter.Listener onReceiveChatMsgListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //有用户给你发了聊天消息
            Log.i("lala","收到消息 : "+args[0]);
        }
    };
    //
    private Emitter.Listener onOfflineListener = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            //有用户下线了
            Log.i("lala","收到下线 : "+args[0]);
        }
    };

    //----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------


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
