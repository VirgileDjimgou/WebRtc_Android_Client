package com.android.gudana.hify.adapters.viewFriends;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gudana.hify.models.ViewFriends;
import com.android.gudana.hify.ui.activities.FriendsUpdates;
import com.android.gudana.hify.ui.activities.friends.FriendProfile;
import com.android.gudana.R;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.javiersantos.bottomdialogs.BottomDialog;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by amsavarthan on 22/2/18.
 */

public class ViewFriendAdapter extends RecyclerView.Adapter<ViewFriendAdapter.ViewHolder> {

    private List<ViewFriends> usersList = null;
    private List<String> usersList_group = new ArrayList<>();;

    private Context context;
    private HashMap<String, Object> userMap;
    private String FromClasse ;
    private boolean ViewSelected = false;

    public ViewFriendAdapter(List<ViewFriends> usersList, Context context , String  FromClasse) {
        this.usersList = usersList;
        this.context = context;
        this.FromClasse = FromClasse;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.hi_friend_item_list_added,parent,false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {


        holder.name.setText(usersList.get(position).getName());
        holder.username.setText("@"+usersList.get(position).getUsername());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                .load(usersList.get(position).getImage())
                .into(holder.image);

        try {

            FirebaseFirestore.getInstance().collection("Users")
                    .document(usersList.get(position).getId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            try {
                                if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                        !documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getUsername()) &&
                                !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", documentSnapshot.getString("name"));
                                    user.put("username", documentSnapshot.getString("username"));
                                    user.put("image", documentSnapshot.getString("image"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });

                                    holder.name.setText(documentSnapshot.getString("name"));
                                    holder.username.setText("@"+documentSnapshot.getString("username"));

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(documentSnapshot.getString("image"))
                                            .into(holder.image);


                                }else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                        !documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getUsername())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", documentSnapshot.getString("name"));
                                    user.put("username", documentSnapshot.getString("username"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });

                                    holder.name.setText(documentSnapshot.getString("name"));
                                    holder.username.setText("@"+documentSnapshot.getString("username"));


                                }else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName()) &&
                                        !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", documentSnapshot.getString("name"));
                                    user.put("image", documentSnapshot.getString("image"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });

                                    holder.name.setText(documentSnapshot.getString("name"));

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(documentSnapshot.getString("image"))
                                            .into(holder.image);


                                }else if (!documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getUsername()) &&
                                        !documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", documentSnapshot.getString("username"));
                                    user.put("image", documentSnapshot.getString("image"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });

                                    holder.username.setText("@"+documentSnapshot.getString("username"));

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(documentSnapshot.getString("image"))
                                            .into(holder.image);


                                } else if (!documentSnapshot.getString("name").equals(usersList.get(holder.getAdapterPosition()).getName())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("name", documentSnapshot.getString("name"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });


                                    holder.name.setText(documentSnapshot.getString("name"));

                                } else if (!documentSnapshot.getString("image").equals(usersList.get(holder.getAdapterPosition()).getImage())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("image", documentSnapshot.getString("image"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });


                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(documentSnapshot.getString("image"))
                                            .into(holder.image);

                                }else if (!documentSnapshot.getString("username").equals(usersList.get(holder.getAdapterPosition()).getName())) {

                                    Map<String, Object> user = new HashMap<>();
                                    user.put("username", documentSnapshot.getString("username"));

                                    FirebaseFirestore.getInstance().collection("Users")
                                            .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .collection("Friends")
                                            .document(usersList.get(holder.getAdapterPosition()).getId())
                                            .update(user)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("friend_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("friend_update", "failure");
                                                }
                                            });


                                    holder.username.setText("@"+documentSnapshot.getString("username"));

                                }
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }
                        }
                    });

        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(FromClasse.equalsIgnoreCase("new_group")){

                    /*
                    Intent IntentUpdate =  new Intent(context, FriendsUpdates.class);
                    IntentUpdate.putExtra("user_id", usersList.get(holder.getAdapterPosition()).getId());
                    context.startActivity(IntentUpdate);
                             */
                    // holder
                    // Drawable resId = R.drawable.round_background;

                    if(usersList_group.size() > 0){

                    }

                    try{
                        if(holder.viewForeground.isSelected() == true){
                            holder.viewForeground.setSelected(false);
                            holder.viewForeground.setBackground(ContextCompat.getDrawable(context, R.drawable.round_background));
                            usersList_group.add(usersList.get(holder.getAdapterPosition()).getId());
                            // holder    ....  marketing   ... je ne suis pas  trop trope ...
                            Toast.makeText(context, " number of  Users " + usersList_group.size(), Toast.LENGTH_SHORT).show();

                        }else{
                            holder.viewForeground.setSelected(true);
                            holder.viewForeground.setBackground(ContextCompat.getDrawable(context, R.color.white));
                            usersList_group.remove(usersList.get(holder.getAdapterPosition()).getId());
                            Toast.makeText(context, " number of  Users " + usersList_group.size(), Toast.LENGTH_SHORT).show();
                        }

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }

                }

                if(FromClasse.equalsIgnoreCase("FriendsLastUpdates")){

                    Intent IntentUpdate =  new Intent(context, FriendsUpdates.class);
                    IntentUpdate.putExtra("user_id", usersList.get(holder.getAdapterPosition()).getId());
                    context.startActivity(IntentUpdate);

                }

                if(FromClasse.equalsIgnoreCase("Friends")){

                    FriendProfile.startActivity(context, usersList.get(holder.getAdapterPosition()).getId());
                }
                // other fullScreen  if the chechk the last Update of this User  ...
            }
        });

    }


    public void removeItem(final int position) {

        new BottomDialog.Builder(context)
                .setTitle("Unfriend " + usersList.get(position).getName())
                .setContent("Are you sure do you want to remove " + usersList.get(position).getName() + " from your friend list?")
                .setPositiveText("Yes")
                .setPositiveBackgroundColorResource(R.color.colorAccentt)
                .setNegativeText("No")
                .setCancelable(false)
                .onPositive(new BottomDialog.ButtonCallback() {
                    @Override
                    public void onClick(@NonNull BottomDialog dialog) {
                        removeUser(position);
                        dialog.dismiss();
                    }
                }).onNegative(new BottomDialog.ButtonCallback() {
            @Override
            public void onClick(@NonNull BottomDialog dialog) {
                dialog.dismiss();
                notifyDataSetChanged();
            }
        }).show();

    }

    private void removeUser(final int position) {

        FirebaseFirestore.getInstance().collection("Users").document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .collection("Friends").document(usersList.get(position).getId()).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {

                FirebaseFirestore.getInstance()
                        .collection("Users")
                        .document(usersList.get(position).getId())
                        .collection("Friends")
                        .document(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .delete()
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        usersList.remove(position);
                        notifyItemRemoved(position);
                    }
                });

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }


    @Override
    public int getItemCount() {
        return usersList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView name, listenerText,username;
        RelativeLayout viewBackground, viewForeground;
        private View mView;
        private CircleImageView image;

        public ViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
            image=mView.findViewById(R.id.image);
            username=(TextView)mView.findViewById(R.id.username);
            name=mView.findViewById(R.id.name);
            viewBackground =mView.findViewById(R.id.view_background);
            viewForeground =mView.findViewById(R.id.view_foreground);
            listenerText =mView.findViewById(R.id.view_foreground_text);

        }
    }

    // getter and setter  for  liste of selected Users  ..


    public List<String> getUsersList_group() {
        return usersList_group;
    }

    public void setUsersList_group(List<String> usersList_group) {
        this.usersList_group = usersList_group;
    }
}
