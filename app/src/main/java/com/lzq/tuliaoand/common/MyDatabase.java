package com.lzq.tuliaoand.common;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.lzq.tuliaoand.bean.MessageForRoom;
import com.lzq.tuliaoand.dao.MessageDao;

@Database(entities = {MessageForRoom.class},version = 1,exportSchema = false)
public abstract class MyDatabase extends RoomDatabase {
    public abstract MessageDao messageDao();




}
