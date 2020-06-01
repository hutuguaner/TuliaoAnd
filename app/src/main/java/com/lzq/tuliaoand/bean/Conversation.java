package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Conversation implements Parcelable {

    private String oppositeEmail;
    private ArrayList<Message> messages;

    public Conversation() {
    }

    protected Conversation(Parcel in) {
        oppositeEmail = in.readString();
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

    public String getOppositeEmail() {
        return oppositeEmail;
    }

    public void setOppositeEmail(String oppositeEmail) {
        this.oppositeEmail = oppositeEmail;
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
        return oppositeEmail.equals(compare.getOppositeEmail());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(oppositeEmail);
        dest.writeTypedList(messages);
    }
}
