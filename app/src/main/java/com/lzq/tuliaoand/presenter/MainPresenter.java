package com.lzq.tuliaoand.presenter;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.model.MainModel;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MainPresenter {


    private MainModel mainModel;

    public MainPresenter(MainModel mainModel) {
        this.mainModel = mainModel;
    }


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
                mainModel.uploadBroadcastStart();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                mainModel.uploadBroadcastError(response.body());
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                        mainModel.uploadBroadcastSuccess();
                    } else if (code == 1) {
                        String msg = result.getString("message");
                        mainModel.uploadBroadcastError(msg);
                    } else if (code == 2) {
                        //连接超时 重新登录
                        mainModel.connectTimeOut();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mainModel.uploadBroadcastError(e.getMessage());
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mainModel.uploadBroadcastFinish();
            }
        });
    }


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
                mainModel.uploadPositionStart();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                mainModel.uploadPositionError(response.body());
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                        mainModel.uploadPositionSuccess();
                    } else if (code == 1) {
                        String msg = result.getString("message");
                        mainModel.uploadPositionError(msg);
                    } else if (code == 2) {
                        //连接超时 重新登录
                        mainModel.connectTimeOut();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mainModel.uploadPositionError(e.getMessage());
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mainModel.uploadPositionFinish();
            }
        });
    }


    public void getMsgs() {
        String email = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>post(Const.GET_MSGS).upJson(jsonObject).execute(new StringCallback() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                mainModel.getMsgsStart();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                mainModel.getMsgsError(response.body());
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
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
                        mainModel.getMsgsSuccess(messages);
                    } else if (code==1){
                        String msg = result.getString("message");
                        mainModel.getMsgsError(msg);
                    }else if (code==2){
                        mainModel.connectTimeOut();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mainModel.getMsgsError(e.getMessage());
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mainModel.getMsgsFinish();
            }
        });
    }


    public void getUsers() {
        String email = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>post(Const.GET_ALL_USERS).upJson(jsonObject).execute(new StringCallback() {

            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                mainModel.getUsersStart();
            }

            @Override
            public void onSuccess(Response<String> response) {
                try {
                    JSONObject result = new JSONObject(response.body());
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
                        mainModel.getUsersSuccess(users);
                    } else if (code==1){
                        String msg = result.getString("message");
                        mainModel.getUsersError(msg);
                    }else if (code==2){
                        mainModel.connectTimeOut();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    mainModel.getUsersError(e.getMessage());
                }
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                mainModel.getUsersError(response.body());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                mainModel.getUsersFinish();
            }
        });
    }
}
