package com.lzq.tuliaoand.model;

import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;

import java.util.List;

public interface MainModel {


    void connectTimeOut();

    //

    void getUsersStart();

    void getUsersError(String err);

    void getUsersSuccess(List<User> users);

    void getUsersFinish();

    //
    void uploadPositionStart();

    void uploadPositionError(String msg);

    void uploadPositionSuccess();

    void uploadPositionFinish();

    //

    void uploadBroadcastStart();

    void uploadBroadcastError(String msg);

    void uploadBroadcastSuccess();

    void uploadBroadcastFinish();


}
