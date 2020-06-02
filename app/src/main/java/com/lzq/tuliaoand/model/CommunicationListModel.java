package com.lzq.tuliaoand.model;

import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Message;

import java.util.List;

public interface CommunicationListModel {

    //
    void onMessageFromDB(List<Conversation> conversations);
}
