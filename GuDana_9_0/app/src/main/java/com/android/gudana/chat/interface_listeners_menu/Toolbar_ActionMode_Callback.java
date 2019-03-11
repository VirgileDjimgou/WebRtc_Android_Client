package com.android.gudana.chat.interface_listeners_menu;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.gudana.R;
import com.android.gudana.chat.activities.ChatActivity;
import com.android.gudana.chat.adapters.Msg_adapter;
import com.android.gudana.chat.db.DatabaseInitializer;
import com.android.gudana.chat.model.MessageItem;

import java.util.ArrayList;

import androidx.appcompat.view.ActionMode;
import androidx.core.view.MenuItemCompat;

/**
 * Created by SONU on 22/03/16.
 */
public class Toolbar_ActionMode_Callback implements ActionMode.Callback {

    private Context context;
    private Msg_adapter recyclerView_adapter;
    //private ListView_Adapter listView_adapter;
    private ArrayList<MessageItem> message_models;
    private boolean isListViewFragment;
    private ChatActivity ParentActivity;


    public Toolbar_ActionMode_Callback(ChatActivity ParentActivity , Context context, Msg_adapter recyclerView_adapter , ArrayList<MessageItem> message_models, boolean isListViewFragment) {

        this.ParentActivity = ParentActivity;
        this.context = context;
        this.recyclerView_adapter = recyclerView_adapter;
        //this.listView_adapter = listView_adapter;
        this.message_models = message_models;
        this.isListViewFragment = isListViewFragment;
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.menu_message_modify, menu);//Inflate the menu over action mode
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {

        //Sometimes the meu will not be visible so for that we need to set their visibility manually in this method
        //So here show action menu according to SDK Levels
        if (Build.VERSION.SDK_INT < 11) {
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_delete), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_copy), MenuItemCompat.SHOW_AS_ACTION_NEVER);
            MenuItemCompat.setShowAsAction(menu.findItem(R.id.action_forward), MenuItemCompat.SHOW_AS_ACTION_NEVER);
        } else {
            menu.findItem(R.id.action_delete).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_copy).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
            menu.findItem(R.id.action_forward).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        }

        return true;
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:

                //Check if current action mode is from ListView Fragment or RecyclerView Fragment
                // get selected Row  ....
                //Get selected ids on basis of current fragment action mode
                SparseBooleanArray selected;
                selected = recyclerView_adapter.getSelectedIds();

                int selectedMessageSize = selected.size();

                //Loop to all selected items
                for (int i = (selectedMessageSize - 1); i >= 0; i--) {
                    if (selected.valueAt(i)) {
                        //get selected data in Model
                        MessageItem SelectedMessage = message_models.get(selected.keyAt(i));
                        String title = SelectedMessage.getMessage();
                        String subTitle = SelectedMessage.getUsername();
                        //Print the data to show if its working properly or not
                        // get selected  row  and chehck

                        ParentActivity.databaseInitializer.deleteUser(ParentActivity , ParentActivity.mDb , SelectedMessage );
                        message_models.remove(selected.keyAt(i));

                        // remove selected key



                        // this.ParentActivity.databaseInitializer.DeleteMessage(this.ParentActivity.mDb , SelectedMessage);
                        // delete model ....
                        /*
                        Toast.makeText(context,  "adapter size  : " + this.ParentActivity.adapter.dataSet.size() +
                                "\n" + "dataset Size :  - " + this.ParentActivity.adapter.getItemCount(), Toast.LENGTH_SHORT).show();
                        Log.e("Selected Items", "Title - " + title + "\n" + "Sub Title - " + subTitle);
                        */

                    }
                }
                Toast.makeText(context, "You deleted a message .", Toast.LENGTH_SHORT).show();//Show toast
                mode.finish();//Finish action mode
                break;
            case R.id.action_copy:

                //Get selected ids on basis of current fragment action mode
                SparseBooleanArray selected_copy;
                selected_copy = recyclerView_adapter.getSelectedIds();

                int selectedMessageSize_copy = selected_copy.size();

                //Loop to all selected items
                for (int i = (selectedMessageSize_copy - 1); i >= 0; i--) {
                    if (selected_copy.valueAt(i)) {
                        //get selected data in Model
                        MessageItem model = message_models.get(selected_copy.keyAt(i));
                        String title = model.getMessage();
                        String subTitle = model.getUsername();
                        //Print the data to show if its working properly or not
                        // get selected  row  and chehck
                        Toast.makeText(context,  "Title - " + title + "\n" + "Sub Title - " + subTitle, Toast.LENGTH_SHORT).show();
                        Log.e("Selected Items", "Title - " + title + "\n" + "Sub Title - " + subTitle);

                    }
                }
                Toast.makeText(context, "You selected Copy menu.", Toast.LENGTH_SHORT).show();//Show toast
                mode.finish();//Finish action mode
                break;
            case R.id.action_forward:
                Toast.makeText(context, "You selected Forward menu.", Toast.LENGTH_SHORT).show();//Show toast
                mode.finish();//Finish action mode
                break;
        }

        return false;
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {

        //When action mode destroyed remove selected selections and set action mode to null
        //First check current fragment action mode
        recyclerView_adapter.removeSelection();  // remove selection
        ParentActivity.setNullToActionMode();//Set action mode null


    }
}
