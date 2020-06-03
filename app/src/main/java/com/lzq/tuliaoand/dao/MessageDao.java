package com.lzq.tuliaoand.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lzq.tuliaoand.bean.MessageForRoom;

import java.util.List;

@Dao
public interface MessageDao {

    @Insert
    void insert(MessageForRoom messageForRoom);

    @Insert
    void insert(List<MessageForRoom> messageForRooms);


    @Query("select * from message")
    List<MessageForRoom> getMessages();

    @Query("select * from message where email_from=:fromEmail and email_to=:toEmail")
    List<MessageForRoom> getMessagesBy(String fromEmail,String toEmail);

}
