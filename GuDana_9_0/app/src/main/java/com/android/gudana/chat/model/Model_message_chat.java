package com.android.gudana.chat.model;


import java.io.File;

/**
 * Created by anupamchugh on 09/02/16.
 */
public class Model_message_chat {


    public static final int TEXT_TYPE=0;
    public static final int IMAGE_TYPE=1;
    public static final int AUDIO_TYPE=2;
    public static final int Map_TYPE = 3;
    public static final int DOC_TYPE = 4;
    public static final int LIVE_LOCATION = 5;

    public int type;
    public int data;
    public String text;


    private int id;
    private  String user_id;
    private  String username;
    private  String message;
    private String datetime_utc;
    private boolean on_server;
    private String file_type;
    private String uuid_message;

    private File file_to_upload_compressed;
    private File local_file;
    private Boolean isUploaded = false;
    private Boolean try_Uploading = false;



    public Model_message_chat(int type, String text, int data)
    {
        this.type=type;
        this.data=data;
        this.text=text;

    }

    // definitv model message

    public Model_message_chat(String uuid_message , int type , int id, String user_id,
                              String file_type, String username,
                              String message, String datetime_utc ,
                              File local_file,  File file_to_upload_compressed ,
                              Boolean try_Uploading , Boolean on_server) {

        this.uuid_message = uuid_message;
        this.type = type;
        this.on_server = on_server;
        this.username = username;
        this.id = id;
        this.user_id = user_id;
        this.file_type = file_type;
        this.message = message;
        this.datetime_utc = datetime_utc;
        this.file_to_upload_compressed = file_to_upload_compressed;
        this.local_file = local_file;
        this.try_Uploading = try_Uploading;
    }

    public Model_message_chat(int id, String user_id, String file_type,
                       String message, String datetime_utc ,
                       File local_file,  File file_to_upload_compressed ,
                       Boolean try_Uploading) {
        this.on_server = true;
        this.id = id;
        this.user_id = user_id;
        this.file_type = file_type;
        this.message = message;
        this.datetime_utc = datetime_utc;
        this.file_to_upload_compressed = file_to_upload_compressed;
        this.local_file = local_file;
        this.try_Uploading = try_Uploading;
    }


    public Model_message_chat(int id, String user_id, String username, String message, String datetime_utc) {
        this.on_server = true;
        this.id = id;
        this.user_id = user_id;
        this.username = username;
        this.message = message;
        this.datetime_utc = datetime_utc;
    }

    public Model_message_chat(String user_id, String username, String message) {
        this.on_server = false;
        this.user_id = user_id;
        this.username = username;
        this.message = message;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public boolean isOn_server() {
        return on_server;
    }

    public void setOn_server(boolean on_server) {
        this.on_server = on_server;
    }

    public String getUuid_message() {
        return uuid_message;
    }

    public void setUuid_message(String uuid_message) {
        this.uuid_message = uuid_message;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public Boolean getTry_Uploading() {
        return try_Uploading;
    }

    public void setTry_Uploading(Boolean try_Uploading) {
        this.try_Uploading = try_Uploading;
    }

    public Boolean getUploaded() {
        return isUploaded;
    }

    public void setUploaded(Boolean uploaded) {
        isUploaded = uploaded;
    }


    public File getFile_to_upload_compressed() {
        return file_to_upload_compressed;
    }

    public void setFile_to_upload_compressed(File file_to_upload_compressed) {
        this.file_to_upload_compressed = file_to_upload_compressed;
    }

    public File getLocal_file() {
        return local_file;
    }

    public void setLocal_file(File local_file) {
        this.local_file = local_file;
    }

    public int getID() {
        return id;
    }

    public String getUserID() {
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
