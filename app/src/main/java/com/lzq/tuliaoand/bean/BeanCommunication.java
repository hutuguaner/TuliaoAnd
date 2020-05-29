package com.lzq.tuliaoand.bean;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BeanCommunication {
    private String urlAvatar;
    private String msgNewest;
    private long timeNewestMsg;
    private long sizeUncheckMsg;

    public BeanCommunication() {
    }

    public String getUrlAvatar() {
        return urlAvatar;
    }

    public void setUrlAvatar(String urlAvatar) {
        this.urlAvatar = urlAvatar;
    }

    public String getMsgNewest() {
        return msgNewest;
    }

    public void setMsgNewest(String msgNewest) {
        this.msgNewest = msgNewest;
    }

    public long getTimeNewestMsg() {
        return timeNewestMsg;
    }

    public void setTimeNewestMsg(long timeNewestMsg) {
        this.timeNewestMsg = timeNewestMsg;
    }

    public long getSizeUncheckMsg() {
        return sizeUncheckMsg;
    }

    public void setSizeUncheckMsg(long sizeUncheckMsg) {
        this.sizeUncheckMsg = sizeUncheckMsg;
    }

    public String getTimeStringNewestMsg(){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(timeNewestMsg));
    }
}
