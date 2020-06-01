package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message implements Parcelable, IMessage {

    private User from;
    private User to;
    private String content;
    private long timeStamp;

    public Message() {
    }

    protected Message(Parcel in) {
        from = in.readParcelable(User.class.getClassLoader());
        to = in.readParcelable(User.class.getClassLoader());
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


    public User getFrom() {
        return from;
    }

    public void setFrom(User from) {
        this.from = from;
    }

    public User getTo() {
        return to;
    }

    public void setTo(User to) {
        this.to = to;
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
        if (content.equals(compare.content)) {
            if (from.equals(compare.getFrom())) {
                if (to.equals(compare.to)) {
                    if (timeStamp == compare.getTimeStamp()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(from,0);
        dest.writeParcelable(to,0);
        dest.writeString(content);
        dest.writeLong(timeStamp);
    }

    @Override
    public String getId() {
        return from.getId() + content + to.getId() + timeStamp;
    }

    @Override
    public String getText() {
        return content;
    }

    @Override
    public IUser getUser() {


        return from;
    }

    @Override
    public Date getCreatedAt() {
        return new Date();
    }
}
