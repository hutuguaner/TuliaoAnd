package com.lzq.tuliaoand.bean;

import androidx.annotation.Nullable;

import java.util.List;

public class Conversation {

    private String oppositeEmail;
    private List<Message> messages;

    public Conversation() {
    }

    public String getOppositeEmail() {
        return oppositeEmail;
    }

    public void setOppositeEmail(String oppositeEmail) {
        this.oppositeEmail = oppositeEmail;
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setMessages(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        Conversation compare = (Conversation) obj;
        return oppositeEmail.equals(compare.getOppositeEmail());
    }
}
