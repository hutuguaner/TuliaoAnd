package com.lzq.tuliaoand.bean;


import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "message")
public class MessageForRoom {
    @NonNull
    @PrimaryKey
    @ColumnInfo(name = "conversation_id")
    private String conversationId;//規則，当前用户email拼接上对方email
    @ColumnInfo(name = "email_from")
    private String fromEmail;
    @ColumnInfo(name = "email_to")
    private String toEmail;
    @ColumnInfo(name = "content")
    private String content;
    @ColumnInfo(name = "time_stamp")
    private long timeStamp;//时间戳 统一用十位的
    @ColumnInfo(name = "has_read")
    private boolean hasRead = false;

    public MessageForRoom() {
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
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

    public boolean isHasRead() {
        return hasRead;
    }

    public void setHasRead(boolean hasRead) {
        this.hasRead = hasRead;
    }
}
