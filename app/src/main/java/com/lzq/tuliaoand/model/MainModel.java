package com.lzq.tuliaoand.model;

import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;

import java.util.List;

public interface MainModel {

    void getUsersStart();

    void getUsersError(String err);

    void getUsersSuccess(List<User> users);

    void getUsersFinish();

    //
    void getMsgsStart();

    void getMsgsError(String msg);

    void getMsgsSuccess(List<Message> messages);

    void getMsgsFinish();

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

    //
    void uploadMsgStart();
    void uploadMsgError(String msg);
    void uploadMsgSuccess();
    void uploadMsgFinish();
}
