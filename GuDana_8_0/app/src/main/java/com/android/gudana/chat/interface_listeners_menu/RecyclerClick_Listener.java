package com.android.gudana.chat.interface_listeners_menu;

import android.view.View;

/**
 * Created by Djimgou Patrick on 15/03/16.
 */
public interface RecyclerClick_Listener {
    /**
     * Interface for Recycler View Click listener
     **/

    void onClick(View view, int position);

    void onLongClick(View view, int position);
}

