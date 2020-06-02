package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Conversation implements Parcelable {

    private User opposite;
    private User me;
    private ArrayList<Message> messages;

    public Conversation() {
    }

    protected Conversation(Parcel in) {
        opposite = in.readParcelable(User.class.getClassLoader());
        me = in.readParcelable(User.class.getClassLoader());
        messages = in.createTypedArrayList(Message.CREATOR);
    }

    public static final Creator<Conversation> CREATOR = new Creator<Conversation>() {
        @Override
        public Conversation createFromParcel(Parcel in) {
            return new Conversation(in);
        }

        @Override
        public Conversation[] newArray(int size) {
            return new Conversation[size];
        }
    };

    public User getOpposite() {
        return opposite;
    }

    public void setOpposite(User opposite) {
        this.opposite = opposite;
    }

    public User getMe() {
        return me;
    }

    public void setMe(User me) {
        this.me = me;
    }

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Conversation compare = (Conversation) obj;
        if (me.equals(compare.getMe())) {
            if (opposite.equals(compare.getOpposite())) {
                return true;
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
        dest.writeParcelable(opposite, 0);
        dest.writeParcelable(me, 0);
        dest.writeTypedList(messages);
    }
}
