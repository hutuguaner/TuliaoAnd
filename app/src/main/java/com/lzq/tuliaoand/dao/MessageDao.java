package com.lzq.tuliaoand.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.RawQuery;
import androidx.sqlite.db.SupportSQLiteQuery;

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


    @Query("delete from message")
    void delete();

    @Query("select * from message where email_from=:from and email_to=:to")
    List<MessageForRoom> getMessages(String from, String to);

    @Query("update message set has_read = 1 where email_from=:from")
    void setMessageHasReaded(String from);

}
