/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.gudana.chat.db;

import android.os.AsyncTask;
//import android.support.annotation.NonNull;
import android.util.Log;

import com.android.gudana.chat.model.MessageItem;
import com.android.gudana.chat.model.User;
//import com.example.android.persistence.codelab.db.AppDatabase;
//import com.example.android.persistence.codelab.db.Book;
//import com.example.android.persistence.codelab.db.Loan;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import androidx.annotation.NonNull;

public class DatabaseInitializer {

    // CRUD Operation database DAO  ...

    public static void insert_new_message_Async(@NonNull final AppDatabase db , MessageItem msg) {

        Insert_new_MessageDbAsync task = new Insert_new_MessageDbAsync(db ,msg);
        task.execute();
    }

    public  static List<MessageItem> load_All_Message(@NonNull final AppDatabase db ){
        List<MessageItem> List_of_Message = null;
        List_of_Message = db.MessageItemModel().loadAllMessage();
        return List_of_Message;
    }

    public static void UpdateMessage(AppDatabase db , MessageItem msg){

        Update_MessageDbAsync  task = new Update_MessageDbAsync(db, msg);
        task.execute();
    }

    public static void DeleteMessage(AppDatabase db , MessageItem msg){
        Delete_MessageObject_Async task = new Delete_MessageObject_Async(db , msg);
        task.execute();

    }

    // Create New  Message Object on the local Database    ..
    private static class Insert_new_MessageDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final MessageItem Msg_Item;

        Insert_new_MessageDbAsync(AppDatabase db , MessageItem Msg_Item) {
            mDb = db;
            this.Msg_Item = Msg_Item;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDb.MessageItemModel().insertMessage(Msg_Item);
            return null;
        }

    }

    // update locaal message   ...
    private static class Update_MessageDbAsync extends AsyncTask<Void, Void, Void> {

        private final AppDatabase mDb;
        private final MessageItem Msg_Item;

        Update_MessageDbAsync(AppDatabase db , MessageItem Msg_Item) {
            mDb = db;
            this.Msg_Item = Msg_Item;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDb.MessageItemModel().updateMessage(Msg_Item);
            return null;
        }

    }

    //  Delete Mesaage Object  ....

    private static  class  Delete_MessageObject_Async extends  AsyncTask<Void , Void , Void >{

        private final AppDatabase mDb;
        private final MessageItem Msg_Item;

        Delete_MessageObject_Async(AppDatabase db , MessageItem Msg_Item) {
            mDb = db;
            this.Msg_Item = Msg_Item;
        }

        @Override
        protected Void doInBackground(final Void... params) {
            mDb.MessageItemModel().deleteMessage(Msg_Item);
            return null;
        }

    }


}
