package com.android.gudana.hify.ui.fragment;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.snackbar.Snackbar;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.gudana.hify.adapters.addFriends.AddFriendAdapter;
import com.android.gudana.hify.adapters.addFriends.RecyclerViewTouchHelper;
import com.android.gudana.hify.models.Friends;
import com.android.gudana.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.tylersuehr.esr.TextStateDisplay;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class AddFriends extends Fragment {

    View mView;
    private List<Friends> usersList;
    private AddFriendAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private EmptyStateRecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.hi_friends_add_frag, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = mView.findViewById(R.id.recyclerView);

        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerViewTouchHelper(0, ItemTouchHelper.LEFT, new RecyclerViewTouchHelper.RecyclerItemTouchHelperListener() {
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
                if (viewHolder instanceof AddFriendAdapter.ViewHolder) {
                    // get the removed item name to display it in snack bar
                    String name = usersList.get(viewHolder.getAdapterPosition()).getName();

                    // backup of removed item for undo purpose
                    final Friends deletedItem = usersList.get(viewHolder.getAdapterPosition());
                    final int deletedIndex = viewHolder.getAdapterPosition();

                    Snackbar snackbar = Snackbar
                            .make(mView.findViewById(R.id.layout), "Friend request sent to " + name, Snackbar.LENGTH_LONG);

                    // remove the item from recycler view
                    usersAdapter.removeItem(viewHolder.getAdapterPosition(), snackbar, deletedIndex, deletedItem);

                }
            }
        });

        usersList = new ArrayList<>();
        usersAdapter = new AddFriendAdapter(usersList, view.getContext(), mView.findViewById(R.id.layout));
        mRecyclerView.setItemAnimator(new FlipInTopXAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(mRecyclerView);
        mRecyclerView.setAdapter(usersAdapter);

        mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY,
                new TextStateDisplay(view.getContext(),"No user found","All Hify users can be seen here"));

        mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_LOADING,
                new TextStateDisplay(view.getContext(),"We found some users","We are getting information of those users.."));

        mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));


        getAllUsers();

    }

    public void getAllUsers() {
        firestore.collection("Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if (!queryDocumentSnapshots.getDocuments().isEmpty()) {

                            for (final DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {

                                    if (!doc.getDocument().getId().equals(mAuth.getCurrentUser().getUid())) {
                                        Friends friends = doc.getDocument().toObject(Friends.class).withId(doc.getDocument().getString("id"));
                                        usersList.add(friends);
                                        usersAdapter.notifyDataSetChanged();
                                    }

                                }
                            }

                        }else{
                            Toast.makeText(mView.getContext(), "It's empty in here.", Toast.LENGTH_SHORT).show();
                        }

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_ERROR);
                        Log.w("Error", "listen:error", e);
                    }
                });

    }


}
