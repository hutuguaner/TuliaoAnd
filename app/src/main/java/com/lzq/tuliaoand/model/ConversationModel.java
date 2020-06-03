package com.lzq.tuliaoand.model;

import com.lzq.tuliaoand.bean.Message;

import java.util.List;

public interface ConversationModel {

    void connectTimeOut();

    //

    void sendMsgStart();
    void sendMsgError(String msg);
    void sendMsgSuccess();
    void sendMsgFinish();


    //
    void onMsgFromDB(List<Message> messages);


}
