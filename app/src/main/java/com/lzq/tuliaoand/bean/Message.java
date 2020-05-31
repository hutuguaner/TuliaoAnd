package com.lzq.tuliaoand.bean;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

    private String emailFrom;
    private String emailTo;
    private String content;
    private long timeStamp;

    public Message() {
    }

    public String getEmailFrom() {
        return emailFrom;
    }

    public void setEmailFrom(String emailFrom) {
        this.emailFrom = emailFrom;
    }

    public String getEmailTo() {
        return emailTo;
    }

    public void setEmailTo(String emailTo) {
        this.emailTo = emailTo;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getTimeStringMsg() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(new Date(timeStamp));
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Message compare = (Message) obj;
        return ((emailFrom.equals(compare.getEmailFrom())) && (emailTo.equals(compare.emailTo)) && (content.equals(compare.getContent())) && (timeStamp == compare.getTimeStamp()));
    }
}
