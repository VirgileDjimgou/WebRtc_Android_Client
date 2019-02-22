package com.android.gudana.hify.ui.fragment;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.android.gudana.hify.ui.activities.friends.SearchUsersActivity;
import com.android.gudana.R;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class FriendsFragment extends Fragment {

    View mView;
    FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.friends_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       fab=mView.findViewById(R.id.searchFab);
       fab.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               gotoSearch();
           }
       });
        loadFragment(new FriendRequests());

        BottomNavigationView bottomNavigationView=mView.findViewById(R.id.bottom_nav);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_view_request:
                        loadFragment(new FriendRequests());
                        break;
                    case R.id.action_add:
                        loadFragment(new AddFriends());
                        break;
                    default:

                }
                return true;
            }
        });


    }

    private void loadFragment(Fragment fragment) {
        getFragmentManager()
                .beginTransaction()
                .replace(R.id.container_2, fragment)
                .commit();
    }

    public void gotoSearch() {
        SearchUsersActivity.startActivity(getActivity(), mView.getContext(), fab);
    }

}
