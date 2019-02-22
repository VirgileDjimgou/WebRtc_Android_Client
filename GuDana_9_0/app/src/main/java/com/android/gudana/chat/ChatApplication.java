package com.android.gudana.chat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.android.gudana.chat.model.User;
import com.android.gudana.hify.ui.activities.MainActivity_GuDDana;
import com.android.gudana.hify.utils.ServerConfig;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

import es.dmoral.toasty.Toasty;

public class ChatApplication extends Application {
    private static final String TAG = "ChatApplication";
    private String url;

    private boolean remember_me = false;

    private SharedPreferences sharedPreferences;

    private User user;

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseApp.initializeApp(this);
        FirebaseApp.initializeApp(this.getApplicationContext());
        FirebaseApp.initializeApp(ChatApplication.this.getApplicationContext());
        FirebaseApp.initializeApp(ChatApplication.this.getBaseContext());



        sharedPreferences =
                getBaseContext().getSharedPreferences("user", Context.MODE_PRIVATE);

        url = sharedPreferences.getString("url", null);
        user = new User(
                sharedPreferences.getInt("user_id", -1),
                sharedPreferences.getString("username", null),
                sharedPreferences.getString("session", null)
        );

        // if the user is already logged in, then the user has chosen to remember the credentials.
        remember_me = isLoggedIn();
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public boolean isLoggedIn() {
        return url != null && user.isLoggedIn();
    }

    /**
     * Must call setCredentials methods with non-null parameters before using this method
     */
    public void rememberCredentials() {
        if(!isLoggedIn()) {
            Log.e(TAG, "Error: Must set credentials first");
            return;
        }

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("url", url)
                .putInt("user_id", user.getUserID())
                .putString("username", user.getUsername())
                .putString("session", user.getSession()).apply();

        remember_me = true;
    }

    public void forgetCredentials() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear().apply();

        url = null;
        user = null;

        deleteFile("rooms.json");

        remember_me = false;
    }

    public boolean isRemembered() {
        return remember_me;
    }
}
