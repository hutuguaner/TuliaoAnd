package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Conversation implements Parcelable {

    private User from;
    private User to;
    private ArrayList<Message> messages;

    public Conversation() {
    }

    protected Conversation(Parcel in) {
        from = in.readParcelable(User.class.getClassLoader());
        to = in.readParcelable(User.class.getClassLoader());
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

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Conversation compare = (Conversation) obj;
        if (from.equals(compare.getFrom())) {
            if (to.equals(compare.getTo())) {
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
        dest.writeParcelable(from, 0);
        dest.writeParcelable(to, 0);
        dest.writeTypedList(messages);
    }
}
