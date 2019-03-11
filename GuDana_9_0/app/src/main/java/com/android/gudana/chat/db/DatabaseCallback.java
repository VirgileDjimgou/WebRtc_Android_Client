package com.android.gudana.chat.db;

import java.util.List;

/**
 * Created by alahammad on 10/4/17.
 */

public interface DatabaseCallback {

    // void onMsgsLoaded(List<User> users);

    void onMsgDeleted();

    void onMsgAdded();

    void onDataNotAvailable();

    void onMsgUpdated();
}
