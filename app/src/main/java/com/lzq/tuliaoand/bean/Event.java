package com.lzq.tuliaoand.bean;

import java.util.List;

public class Event {

    public List<Message> messageList;

    public List<User> users;

    public List<Conversation> conversations;

    public boolean isConnectTimeOut = false;

    public int type;

    public static final int TYPE_MAIN = 0;//发给主页的 event

    public static final int TYPE_CONVERSATION = 1;//发给回话页面的 event

    public static final int TYPE_COMMUNICATION = 2;//发给消息列表页的 event

}
