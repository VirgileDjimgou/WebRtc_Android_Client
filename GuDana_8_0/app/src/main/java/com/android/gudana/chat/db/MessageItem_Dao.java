package com.android.gudana.chat.db;


import com.android.gudana.chat.model.MessageItem;

import java.util.ArrayList;
import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.android.gudana.chat.model.MessageItem;

import java.util.List;

import static androidx.room.OnConflictStrategy.IGNORE;
import static androidx.room.OnConflictStrategy.REPLACE;

@Dao
public interface MessageItem_Dao {
    @Query("select * from MessageItem")
    List<MessageItem> loadAllMessage();

    @Insert(onConflict = IGNORE)
    void insertMessage(MessageItem Msg);

    @Delete
    void deleteMessage(MessageItem Msg);


    @Update(onConflict = REPLACE)
    void updateMessage(MessageItem Msg);


    @Insert(onConflict = IGNORE)
    void insertOrReplaceUsers(MessageItem... Msg_s);

}