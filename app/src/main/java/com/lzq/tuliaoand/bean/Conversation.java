package com.lzq.tuliaoand.bean;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Conversation implements Parcelable, IDialog<Message> {

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

    @Override
    public String getId() {
        return me.getEmail() + opposite.getEmail();
    }

    @Override
    public String getDialogPhoto() {
        return "https://timgsa.baidu.com/timg?image&quality=80&size=b9999_10000&sec=1591246176561&di=7ac4bc5a3e308ff2e758587fb4a1cf14&imgtype=0&src=http%3A%2F%2Fimgsrc.baidu.com%2Fforum%2Fw%3D580%2Fsign%3D09b795cc9e2f07085f052a08d925b865%2F75dffbf2b21193133e1f783365380cd790238d75.jpg";
    }

    @Override
    public String getDialogName() {
        return opposite.getName();
    }

    @Override
    public List<? extends IUser> getUsers() {
        return Arrays.asList(me, opposite);
    }

    @Override
    public Message getLastMessage() {
        return messages.get(messages.size()-1);
    }

    @Override
    public void setLastMessage(Message message) {

    }

    @Override
    public int getUnreadCount() {
        int unReadSize = 0;
        for (Message message : messages) {
            if (message.getHasRead() == 0) unReadSize++;
        }
        return unReadSize;
    }
}
