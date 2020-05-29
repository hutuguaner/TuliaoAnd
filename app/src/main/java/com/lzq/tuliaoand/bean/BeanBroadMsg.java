package com.lzq.tuliaoand.bean;

public class BeanBroadMsg {

    private String content;

    private UserBean from;

    public BeanBroadMsg() {
    }


    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public UserBean getFrom() {
        return from;
    }

    public void setFrom(UserBean from) {
        this.from = from;
    }
}
