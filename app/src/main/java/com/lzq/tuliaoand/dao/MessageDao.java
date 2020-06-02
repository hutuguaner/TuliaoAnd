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

    @Query("delete from message where conversation_id=(:conversationId)")
    void delete(String conversationId);

    @Query("select * from message where conversation_id=(:conversationId)")
    List<MessageForRoom> getMessages(String conversationId);

    @Query("select * from message")
    List<MessageForRoom> getMessages();

}
