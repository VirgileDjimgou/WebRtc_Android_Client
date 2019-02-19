package com.android.gudana.chat.model;


import java.io.File;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;


@Entity
public class MessageItem {

    public static final int TEXT_TYPE=0;
    public static final int IMAGE_TYPE=1;
    public static final int AUDIO_TYPE=2;
    public static final int Map_TYPE = 3;
    public static final int DOC_TYPE = 4;
    public static final int LIVE_LOCATION = 5;

    public int msg_type;
    public int data;
    public String text;


    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    private final int user_id;
    private  String username;
    private final String message;
    private String datetime_utc;
    private boolean on_server;
    private String msg_uuid;
    private String file_type;



    public MessageItem(int id, int user_id, String username, String message,
                       String datetime_utc , int msg_type , Boolean  on_server , String msg_uuid) {
        this.on_server = true;
        this.id = id;
        this.user_id = user_id;
        this.username = username;
        this.message = message;
        this.datetime_utc = datetime_utc;
        this.msg_type = msg_type;
        this.on_server = on_server;
        this.msg_uuid = msg_uuid;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDatetime_utc() {
        return datetime_utc;
    }

    public void setDatetime_utc(String datetime_utc) {
        this.datetime_utc = datetime_utc;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(int msg_type) {
        this.msg_type = msg_type;
    }

    public boolean isOn_server() {
        return on_server;
    }

    public void setOn_server(boolean on_server) {
        this.on_server = on_server;
    }

    public String getMsg_uuid() {
        return msg_uuid;
    }

    public void setMsg_uuid(String msg_uuid) {
        this.msg_uuid = msg_uuid;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public int getID() {
        return id;
    }

    public int getUserID() {
        return user_id;
    }

    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public String getDateTimeUTC() {
        return datetime_utc;
    }

    public void savedToServer(int id, String datetime_utc) {
        this.on_server = true;
        this.id = id;
        this.datetime_utc = datetime_utc;
    }

    @Override
    public String toString() {
        return username + ": " + message;
    }
}
