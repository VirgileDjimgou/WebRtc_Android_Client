package com.android.gudana.hify.ui.fragment;

import androidx.fragment.app.Fragment;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.gudana.hify.adapters.UsersAdapter;
import com.android.gudana.hify.models.Users;
import com.android.gudana.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.tylersuehr.esr.EmptyStateRecyclerView;
import com.tylersuehr.esr.ImageTextStateDisplay;
import com.tylersuehr.esr.TextStateDisplay;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.animators.FlipInTopXAnimator;

/**
 * Created by amsavarthan on 29/3/18.
 */

public class SendMessage extends Fragment {

    private View mView;
    private List<Users> usersList;
    private UsersAdapter usersAdapter;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private EmptyStateRecyclerView mRecyclerView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.hi_send_message_fragment, container, false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        mRecyclerView = mView.findViewById(R.id.messageList);
        mView.findViewById(R.id.navigator).setVisibility(View.GONE);

        usersList = new ArrayList<>();
        usersAdapter = new UsersAdapter(usersList, view.getContext());
        mRecyclerView.setItemAnimator(new FlipInTopXAnimator());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(view.getContext(), DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(usersAdapter);

        mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_EMPTY,
                new ImageTextStateDisplay(view.getContext(),R.mipmap.no_image,"No friends found","Add some friends to send them flash messages."));

        mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_LOADING,
                new TextStateDisplay(view.getContext(),"We found some of your friends","We are getting information of your friends.."));

        mRecyclerView.setStateDisplay(EmptyStateRecyclerView.STATE_ERROR,
                new TextStateDisplay(view.getContext(),"Sorry for inconvenience","Something went wrong :("));

        startListening();

    }

    public void startListening() {
        usersList.clear();
        firestore.collection("Users")
                .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {

                        if(!queryDocumentSnapshots.isEmpty()) {
                            for (DocumentChange doc : queryDocumentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    Users users = doc.getDocument().toObject(Users.class).withId(doc.getDocument().getId());
                                    usersList.add(users);
                                    usersAdapter.notifyDataSetChanged();
                                }
                            }
                        }else{
                            mRecyclerView.invokeState(EmptyStateRecyclerView.STATE_EMPTY);
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
