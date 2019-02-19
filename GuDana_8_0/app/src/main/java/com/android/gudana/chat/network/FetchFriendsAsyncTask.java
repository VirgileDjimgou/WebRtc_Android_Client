package com.android.gudana.chat.network;

import android.os.AsyncTask;
import android.view.View;

import com.android.gudana.chat.ChatApplication;
import com.android.gudana.chat.adapters.FriendsAdapter;
import com.android.gudana.chat.fragments.FriendsListFragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class FetchFriendsAsyncTask extends AsyncTask<String, String, ArrayList<Object>> {

    FriendsListFragment friendsListFragment;
    String username, session;
    int user_id = -1;

    public FetchFriendsAsyncTask(FriendsListFragment friendsListFragment, String username, int user_id, String session) {
        this.friendsListFragment = friendsListFragment;
        this.username = username;
        this.user_id = user_id;
        this.session = session;
    }

    @Override
    protected ArrayList<Object> doInBackground(String... params) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("username", username);
            jsonObject.put("user_id", user_id);
            jsonObject.put("session", session);
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }

        JSONParser jsonParser = new JSONParser();
        JSONObject outputJSON = jsonParser.getJSONFromUrl(((ChatApplication) friendsListFragment.getActivity().getApplication()).getURL() + "/fetchfriends", jsonObject);
        if(outputJSON == null) return null;
        ArrayList<Object> friends;
        try {
            JSONArray friendsJSONArray = outputJSON.getJSONArray("friends");
            JSONObject friend;
            friends = new ArrayList<>(friendsJSONArray.length());

            for(int i = 0; i < friendsJSONArray.length(); i++) {
                friend = friendsJSONArray.getJSONObject(i);
                if(!friend.getBoolean("request")) {
                    FriendsAdapter.FriendItem item = new FriendsAdapter.FriendItem(
                            friend.getString("username"),
                            friend.getInt("user_id"),
                            friend.getString("date")
                    );

                    friends.add(item);
                } else {
                    FriendsAdapter.FriendRequestItem item = new FriendsAdapter.FriendRequestItem(
                            friend.getString("username"),
                            friend.getInt("user_id")
                    );

                    friends.add(item);
                }
            }
            return friends;
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(final ArrayList<Object> friends) {
        friendsListFragment.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                friendsListFragment.swipeContainer.setRefreshing(false);

                if(friends == null) {
                    friendsListFragment.swipeContainer.setVisibility(View.GONE);
                    friendsListFragment.statusLayout.setError("Unable to load friend list. Please try again later.");
                    return;
                }

                friendsListFragment.getAdapter().clear();
                friendsListFragment.getAdapter().addItems((ArrayList) friends);

                if(friendsListFragment.getAdapter().isEmpty()) {
                    friendsListFragment.swipeContainer.setVisibility(View.GONE);
                    friendsListFragment.statusLayout.setError("You haven't added any friends yet.");
                    return;
                }

                friendsListFragment.swipeContainer.setVisibility(View.VISIBLE);
                friendsListFragment.statusLayout.hide();
            }
        });
    }
}