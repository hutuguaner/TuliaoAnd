package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Parcelable {

    private String emailFrom;
    private String emailTo;
    private String content;
    private long timeStamp;

    public Message() {
    }

    protected Message(Parcel in) {
        emailFrom = in.readString();
        emailTo = in.readString();
        content = in.readString();
        timeStamp = in.readLong();
    }

    public static final Creator<Message> CREATOR = new Creator<Message>() {
        @Override
        public Message createFromParcel(Parcel in) {
            return new Message(in);
        }

        @Override
        public Message[] newArray(int size) {
            return new Message[size];
        }
    };

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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(emailFrom);
        dest.writeString(emailTo);
        dest.writeString(content);
        dest.writeLong(timeStamp);
    }
}
