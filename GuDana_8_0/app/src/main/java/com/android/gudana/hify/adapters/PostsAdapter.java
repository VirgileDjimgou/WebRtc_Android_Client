package com.android.gudana.hify.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.gudana.hify.models.MultipleImage;
import com.android.gudana.hify.models.Post;
import com.android.gudana.hify.ui.activities.friends.FriendProfile;
import com.android.gudana.hify.ui.activities.post.CommentsActivity;
import com.android.gudana.R;
import com.android.gudana.hify.utils.Config;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.github.marlonlom.utilities.timeago.TimeAgo;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.rd.PageIndicatorView;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import me.grantland.widget.AutofitTextView;
import pl.droidsonroids.gif.GifImageView;


/**
 * Created by amsavarthan on 22/2/18.
 */

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private List<Post> postList;
    private Context context;
    private FirebaseFirestore mFirestore;
    private FirebaseUser mCurrentUser;
    private boolean isOwner;
    private Activity activity;
    private static final DecelerateInterpolator DECCELERATE_INTERPOLATOR = new DecelerateInterpolator();
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR = new AccelerateInterpolator();
    private static final OvershootInterpolator OVERSHOOT_INTERPOLATOR = new OvershootInterpolator(4);

    public PostsAdapter(List<Post> postList, Context context, Activity activity) {
        this.postList = postList;
        this.activity=activity;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.hi_item_feed_post, parent, false);
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mFirestore = FirebaseFirestore.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        // text is image methode
        // getIsVideoOrImagesPost(holder);


        if(postList.get(position).getUserId().equals(mCurrentUser.getUid())){
            isOwner=true;
            holder.delete.setVisibility(View.VISIBLE);

            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                   new MaterialDialog.Builder(context)
                           .title("Delete post")
                           .content("Are you sure do you want to delete this post?")
                           .positiveText("Yes")
                           .negativeText("No")
                           .onPositive(new MaterialDialog.SingleButtonCallback() {
                               @Override
                               public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                                   final ProgressDialog pdialog=new ProgressDialog(context);
                                   pdialog.setMessage("Please wait...");
                                   pdialog.setIndeterminate(true);
                                   pdialog.setCancelable(false);
                                   pdialog.setCanceledOnTouchOutside(false);
                                   pdialog.show();

                                   dialog.dismiss();
                                   FirebaseFirestore.getInstance().collection("Posts")
                                           .document(postList.get(holder.getAdapterPosition()).postId)
                                           .delete()
                                           .addOnSuccessListener(new OnSuccessListener<Void>() {
                                               @Override
                                               public void onSuccess(Void aVoid) {
                                                   pdialog.dismiss();
                                                   notifyItemRemoved(holder.getAdapterPosition());
                                                   notifyDataSetChanged();
                                                   Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                               }
                                           })
                                           .addOnFailureListener(new OnFailureListener() {
                                               @Override
                                               public void onFailure(@NonNull Exception e) {
                                                   pdialog.dismiss();
                                                   Log.e("error",e.getLocalizedMessage());
                                               }
                                           });

                               }
                           })
                           .onNegative(new MaterialDialog.SingleButtonCallback() {
                               @Override
                               public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                   dialog.dismiss();
                               }
                           }).show();
                }
            });

        }else{
            isOwner=false;
            holder.delete.setVisibility(View.GONE);
        }

        try {
            setupViews(holder);
        }catch (Exception e){
            e.printStackTrace();
        }


        try {
            mFirestore.collection("Users")
                    .document(postList.get(position).getUserId())
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(final DocumentSnapshot documentSnapshot) {

                            try {
                                if (!documentSnapshot.getString("username").equals(postList.get(holder.getAdapterPosition()).getUsername()) &&
                                        !documentSnapshot.getString("image").equals(postList.get(holder.getAdapterPosition()).getUserimage())) {

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("username", documentSnapshot.getString("username"));
                                    postMap.put("userimage", documentSnapshot.getString("image"));

                                    mFirestore.collection("Posts")
                                            .document(postList.get(holder.getAdapterPosition()).postId)
                                            .update(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("post_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("post_update", "failure");
                                                }
                                            });

                                    holder.user_name.setText(documentSnapshot.getString("username"));

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(documentSnapshot.getString("image"))
                                            .into(holder.user_image);


                                } else if (!documentSnapshot.getString("username").equals(postList.get(holder.getAdapterPosition()).getUsername())) {


                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("username", documentSnapshot.getString("username"));

                                    mFirestore.collection("Posts")
                                            .document(postList.get(holder.getAdapterPosition()).postId)
                                            .update(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("post_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("post_update", "failure");
                                                }
                                            });

                                    holder.user_name.setText(documentSnapshot.getString("username"));

                                } else if (!documentSnapshot.getString("image").equals(postList.get(holder.getAdapterPosition()).getUserimage())) {

                                    Map<String, Object> postMap = new HashMap<>();
                                    postMap.put("userimage", documentSnapshot.getString("image"));

                                    mFirestore.collection("Posts")
                                            .document(postList.get(holder.getAdapterPosition()).postId)
                                            .update(postMap)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    Log.i("post_update", "success");
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    Log.i("post_update", "failure");
                                                }
                                            });

                                    Glide.with(context)
                                            .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.default_user_art_g_2))
                                            .load(documentSnapshot.getString("image"))
                                            .into(holder.user_image);

                                }


                            }catch(Exception e){
                                e.printStackTrace();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Error", e.getMessage());
                        }
                    });
        }catch (Exception ex){
            Log.w("error","fastscrolled",ex);
        }

        holder.user_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FriendProfile.startActivity(context,postList.get(holder.getAdapterPosition()).getUserId());
            }
        });

        holder.comment_btn.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
            @Override
            public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {


                    String desc = "<b>" + postList.get(holder.getAdapterPosition()).getUsername() + "</b> " + postList.get(holder.getAdapterPosition()).getDescription();
                    CommentsActivity.startActivity(context, postList,desc, holder.getAdapterPosition(),isOwner);


            }
        });

    }

    private Uri getBitmapUri(Bitmap bitmap, ViewHolder holder, String name) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), bitmap, postList.get(holder.getAdapterPosition()).postId, "Post by " + name);
        return Uri.parse(path);
    }

    private Bitmap getBitmap(FrameLayout view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#212121"));
        }
        view.draw(canvas);

        return bitmap;
    }

    private Bitmap getBitmap(ImageView view) {

        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null) {
            bgDrawable.draw(canvas);
        } else {
            canvas.drawColor(Color.parseColor("#212121"));
        }
        view.draw(canvas);

        return bitmap;
    }

    private void setupViews(final ViewHolder holder) {

        int pos = holder.getAdapterPosition();

        getLikeandFav(holder);
        getCounts(holder);

        // getIsVideoOrImagesPost(final ViewHolder holder);
        holder.user_name.setText(postList.get(pos).getUsername());

        Glide.with(context)
                .setDefaultRequestOptions(new RequestOptions().placeholder(R.drawable.placeholder))
                .load(postList.get(pos).getUserimage())
                .into(holder.user_image);

        String timeAgo = TimeAgo.using(Long.parseLong(postList.get(pos).getTimestamp()));

        holder.timestamp.setText(timeAgo);

        // setup video  if video
        if(postList.get(pos).isIs_video_post()==true){
            holder.video_indicator_Image.setVisibility(View.VISIBLE);
        }


        if (postList.get(pos).getImage_count()==0) {

            holder.pager_layout.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.GONE);
            setmImageHolderBg(postList.get(pos).getColor(), holder.mImageholder);
            holder.post_text.setVisibility(View.VISIBLE);
            holder.post_text.setText(postList.get(pos).getDescription());

            holder.share_btn.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
                @Override
                public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                    if (postList.get(holder.getAdapterPosition()).getImage_count()==0) {

                        Intent intent = new Intent(Intent.ACTION_SEND)
                                .setType("image/*");
                        intent.putExtra(Intent.EXTRA_STREAM, getBitmapUri(getBitmap(holder.mImageholder), holder, "hify_user_" + postList.get(holder.getAdapterPosition()).getUserId()));
                        try {
                            context.startActivity(Intent.createChooser(intent, "Share using..."));
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                        }

                    } else {
                        holder.share_btn.setVisibility(View.GONE);
                    }
                }

            });


        } else if(postList.get(pos).getImage_count()==1) { //   one image

            ArrayList<MultipleImage> multipleImages=new ArrayList<>();
            PostPhotosAdapter photosAdapter=new PostPhotosAdapter(context,activity,multipleImages,false , postList.get(pos).isIs_video_post() , postList.get(pos).getImage_video_0() , holder);
            setUrls(holder,multipleImages,photosAdapter);

            holder.pager.setAdapter(photosAdapter);
            holder.indicator.setViewPager(holder.pager);
            holder.indicator_holder.setVisibility(View.GONE);
            photosAdapter.notifyDataSetChanged();

            holder.pager_layout.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            String desc = "<b>" + postList.get(pos).getUsername() + "</b> " + postList.get(pos).getDescription();
            holder.post_desc.setText(Html.fromHtml(desc));

            holder.share_btn.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
                @Override
                public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                    Toast.makeText(context, "Click the image to view it, then save it for sharing :)", Toast.LENGTH_SHORT).show();
                }

            });


        }else if(postList.get(pos).getImage_count()>0) { /// more than one image

            ArrayList<MultipleImage> multipleImages=new ArrayList<>();
            PostPhotosAdapter photosAdapter=new PostPhotosAdapter(context,activity,multipleImages,false, false , postList.get(pos).getImage_video_0() , holder);
            setUrls(holder,multipleImages,photosAdapter);

            holder.pager.setAdapter(photosAdapter);
            holder.indicator.setViewPager(holder.pager);
            photosAdapter.notifyDataSetChanged();

            holder.pager_layout.setVisibility(View.VISIBLE);
            holder.post_text.setVisibility(View.GONE);
            holder.post_desc.setVisibility(View.VISIBLE);
            String desc = "<b>" + postList.get(pos).getUsername() + "</b> " + postList.get(pos).getDescription();
            holder.post_desc.setText(Html.fromHtml(desc));
            holder.share_btn.setOnFavoriteAnimationEndListener(new MaterialFavoriteButton.OnFavoriteAnimationEndListener() {
                @Override
                public void onAnimationEnd(MaterialFavoriteButton buttonView, boolean favorite) {
                    Toast.makeText(context, "Click the image to view it, then save it for sharing :)", Toast.LENGTH_SHORT).show();
                }

            });


        }
    }

    private void setUrls(ViewHolder holder, ArrayList<MultipleImage> multipleImages, PostPhotosAdapter photosAdapter) {

        int pos=holder.getAdapterPosition();
        String url0,url1,url2,url3,url4,url5,url6;

        url0=postList.get(pos).getImage_url_0();
        url1=postList.get(pos).getImage_url_1();
        url2=postList.get(pos).getImage_url_2();
        url3=postList.get(pos).getImage_url_3();
        url4=postList.get(pos).getImage_url_4();
        url5=postList.get(pos).getImage_url_5();
        url6=postList.get(pos).getImage_url_6();


        if(!TextUtils.isEmpty(url0)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url0).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url0",url0);
        }

        if(!TextUtils.isEmpty(url1)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url1).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url1",url1);
        }

        if(!TextUtils.isEmpty(url2)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url2).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url2",url2);
        }

        if(!TextUtils.isEmpty(url3)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url3).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url3",url3);
        }

        if(!TextUtils.isEmpty(url4)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url4).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url4",url4);
        }

        if(!TextUtils.isEmpty(url5)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url5).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("url5",url5);
        }

        if(!TextUtils.isEmpty(url6)){
            MultipleImage image=new MultipleImage((Config.Media_Server.trim() + url6).trim());
            multipleImages.add(image);
            photosAdapter.notifyDataSetChanged();
            Log.i("ur6",url6);
        }



    }

    private void getLikeandFav(final ViewHolder holder) {

        //forLiked
        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Liked_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            boolean liked = documentSnapshot.getBoolean("liked");

                            // profite et get le type de document
                            try {
                                if(documentSnapshot.getString("type_post") != null) {
                                    String type_doc = documentSnapshot.getString("type_post");
                                    if (type_doc.equalsIgnoreCase("video")) {
                                        //  Toast.makeText(context, "youpi video ! ", Toast.LENGTH_SHORT).show();

                                    } else {

                                    }
                                }

                            }catch (Exception ex){
                                ex.printStackTrace();
                            }


                            if (liked) {
                                holder.like_btn.setFavorite(true,false);
                            } else {
                                holder.like_btn.setFavorite(false,false);
                            }
                        } else {
                            Log.e("Like", "No document found");

                        }

                        holder.like_btn.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                            @Override
                            public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                if(favorite) {
                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("liked", true);

                                    try {

                                        mFirestore.collection("Posts")
                                                .document(postList.get(holder.getAdapterPosition()).postId)
                                                .collection("Liked_Users")
                                                .document(mCurrentUser.getUid())
                                                .set(likeMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //holder.like_count.setText(String.valueOf(Integer.parseInt(holder.like_count.getText().toString())+1));
                                                        //Toast.makeText(context, "Liked post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Error like", e.getMessage());
                                                    }
                                                });
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }else{
                                    Map<String, Object> likeMap = new HashMap<>();
                                    likeMap.put("liked", false);

                                    try {

                                        mFirestore.collection("Posts")
                                                .document(postList.get(holder.getAdapterPosition()).postId)
                                                .collection("Liked_Users")
                                                .document(mCurrentUser.getUid())
                                                .set(likeMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        //holder.like_count.setText(String.valueOf(Integer.parseInt(holder.like_count.getText().toString())-1));
                                                        //Toast.makeText(context, "Unliked post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Error unlike", e.getMessage());
                                                    }
                                                });
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });



                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Like", e.getMessage());
                    }
                });

        //forFavourite
        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Saved_Users")
                .document(mCurrentUser.getUid())
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        if (documentSnapshot.exists()) {
                            boolean fav = documentSnapshot.getBoolean("Saved");

                            if (fav) {
                                holder.sav_button.setFavorite(true,false);
                            } else {
                                holder.sav_button.setFavorite(false,false);
                            }
                        } else {
                            Log.e("Fav", "No document found");

                        }

                        holder.sav_button.setOnFavoriteChangeListener(new MaterialFavoriteButton.OnFavoriteChangeListener() {
                            @Override
                            public void onFavoriteChanged(MaterialFavoriteButton buttonView, boolean favorite) {
                                if(favorite) {

                                    Map<String, Object> favMap = new HashMap<>();
                                    favMap.put("Saved", true);

                                    try {

                                        mFirestore.collection("Posts")
                                                .document(postList.get(holder.getAdapterPosition()).postId)
                                                .collection("Saved_Users")
                                                .document(mCurrentUser.getUid())
                                                .set(favMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        Map<String, Object> postMap = new HashMap<>();

                                                        try {
                                                        postMap.put("userId", postList.get(holder.getAdapterPosition()).getUserId());
                                                        postMap.put("name", postList.get(holder.getAdapterPosition()).getName());
                                                        postMap.put("username", postList.get(holder.getAdapterPosition()).getUsername());
                                                        postMap.put("timestamp", postList.get(holder.getAdapterPosition()).getTimestamp());
                                                        postMap.put("image_count", postList.get(holder.getAdapterPosition()).getImage_count());
                                                        postMap.put("description", postList.get(holder.getAdapterPosition()).getDescription());
                                                        postMap.put("color", postList.get(holder.getAdapterPosition()).getColor());


                                                            postMap.put("image_url_0", postList.get(holder.getAdapterPosition()).getImage_url_0());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            postMap.put("image_url_1", postList.get(holder.getAdapterPosition()).getImage_url_1());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            postMap.put("image_url_2", postList.get(holder.getAdapterPosition()).getImage_url_2());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            postMap.put("image_url_3", postList.get(holder.getAdapterPosition()).getImage_url_3());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            postMap.put("image_url_4", postList.get(holder.getAdapterPosition()).getImage_url_4());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            postMap.put("image_url_5", postList.get(holder.getAdapterPosition()).getImage_url_5());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }
                                                        try {
                                                            postMap.put("image_url_6", postList.get(holder.getAdapterPosition()).getImage_url_6());
                                                        }catch (Exception e){
                                                            e.printStackTrace();
                                                        }

                                                        try{
                                                            mFirestore.collection("Users")
                                                                    .document(mCurrentUser.getUid())
                                                                    .collection("Saved_Posts")
                                                                    .document(postList.get(holder.getAdapterPosition()).postId)
                                                                    .set(postMap)
                                                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                        @Override
                                                                        public void onSuccess(Void aVoid) {
                                                                            // Toast.makeText(context, "Added to Saved_Posts, post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                                                        }
                                                                    }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    Log.e("Error add fav", e.getMessage());
                                                                }
                                                            });


                                                        }catch(Exception ex){
                                                            ex.printStackTrace();

                                                        }


                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Error fav", e.getMessage());
                                                    }
                                                });
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }

                                }else {

                                    Map<String, Object> favMap = new HashMap<>();
                                    favMap.put("Saved", false);

                                    try {

                                        mFirestore.collection("Posts")
                                                .document(postList.get(holder.getAdapterPosition()).postId)
                                                .collection("Saved_Users")
                                                .document(mCurrentUser.getUid())
                                                .set(favMap)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {

                                                        mFirestore.collection("Users")
                                                                .document(mCurrentUser.getUid())
                                                                .collection("Saved_Posts")
                                                                .document(postList.get(holder.getAdapterPosition()).postId)
                                                                .delete()
                                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                    @Override
                                                                    public void onSuccess(Void aVoid) {
                                                                        // Toast.makeText(context, "Removed from Saved_Posts, post '" + postList.get(holder.getAdapterPosition()).postId, Toast.LENGTH_SHORT).show();
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("Error remove fav", e.getMessage());
                                                                    }
                                                                });

                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Log.e("Error fav", e.getMessage());
                                                    }
                                                });

                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error Fav", e.getMessage());
                    }
                });

    }

    private void setmImageHolderBg(String color, FrameLayout mImageholder) {
        switch (Integer.parseInt(color)) {
            case 1:
                mImageholder.setBackgroundResource(R.drawable.gradient_9);
                break;
            case 2:
                mImageholder.setBackgroundResource(R.drawable.gradient_7);
                break;
            case 3:
                mImageholder.setBackgroundResource(R.drawable.gradient_8);
                break;
            case 4:
                mImageholder.setBackgroundResource(R.drawable.gradient_4);
                break;
            case 5:
                mImageholder.setBackgroundResource(R.drawable.gradient_1);
                break;
            case 6:
                mImageholder.setBackgroundResource(R.drawable.gradient_3);
                break;
            case 7:
                mImageholder.setBackgroundResource(R.drawable.gradient_2);
                break;
            case 8:
                mImageholder.setBackgroundResource(R.drawable.gradient_11);
                break;
        }

    }

    private void animatePhotoLike(final ViewHolder holder) {
            holder.vBgLike.setVisibility(View.VISIBLE);
            holder.ivLike.setVisibility(View.VISIBLE);

            holder.vBgLike.setScaleY(0.1f);
            holder.vBgLike.setScaleX(0.1f);
            holder.vBgLike.setAlpha(1f);
            holder.ivLike.setScaleY(0.1f);
            holder.ivLike.setScaleX(0.1f);

            AnimatorSet animatorSet = new AnimatorSet();

            ObjectAnimator bgScaleYAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleY", 0.1f, 1f);
            bgScaleYAnim.setDuration(200);
            bgScaleYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgScaleXAnim = ObjectAnimator.ofFloat(holder.vBgLike, "scaleX", 0.1f, 1f);
            bgScaleXAnim.setDuration(200);
            bgScaleXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator bgAlphaAnim = ObjectAnimator.ofFloat(holder.vBgLike, "alpha", 1f, 0f);
            bgAlphaAnim.setDuration(200);
            bgAlphaAnim.setStartDelay(150);
            bgAlphaAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleUpYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 0.1f, 1f);
            imgScaleUpYAnim.setDuration(300);
            imgScaleUpYAnim.setInterpolator(DECCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleUpXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 0.1f, 1f);
            imgScaleUpXAnim.setDuration(300);
            imgScaleUpXAnim.setInterpolator(DECCELERATE_INTERPOLATOR);

            ObjectAnimator imgScaleDownYAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleY", 1f, 0f);
            imgScaleDownYAnim.setDuration(300);
            imgScaleDownYAnim.setInterpolator(ACCELERATE_INTERPOLATOR);
            ObjectAnimator imgScaleDownXAnim = ObjectAnimator.ofFloat(holder.ivLike, "scaleX", 1f, 0f);
            imgScaleDownXAnim.setDuration(300);
            imgScaleDownXAnim.setInterpolator(ACCELERATE_INTERPOLATOR);

            animatorSet.playTogether(bgScaleYAnim, bgScaleXAnim, bgAlphaAnim, imgScaleUpYAnim, imgScaleUpXAnim);
            animatorSet.play(imgScaleDownYAnim).with(imgScaleDownXAnim).after(imgScaleUpYAnim);

            animatorSet.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    resetLikeAnimationState(holder);
                    holder.like_btn.setFavorite(true,true);
                }

                @Override
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    Map<String, Object> likeMap = new HashMap<>();
                    likeMap.put("liked", true);

                    try {

                        mFirestore.collection("Posts")
                                .document(postList.get(holder.getAdapterPosition()).postId)
                                .collection("Liked_Users")
                                .document(mCurrentUser.getUid())
                                .set(likeMap)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i("post", "liked");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("Error like", e.getMessage());
                                    }
                                });
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                }
            });
            animatorSet.start();

    }

    private void resetLikeAnimationState(ViewHolder holder) {
        holder.vBgLike.setVisibility(View.INVISIBLE);
        holder.ivLike.setVisibility(View.INVISIBLE);
    }

    private void getCounts(final ViewHolder holder) {

        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Liked_Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot querySnapshot) {
                       // holder.like_count.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error",e.getMessage());
                    }
                });

        /*mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("Saved_Users")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot querySnapshot) {
                        fav.setText(String.valueOf(querySnapshot.size()));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error",e.getMessage());
                    }
                });*/


    }

    private void getIsVideoOrImagesPost_(final ViewHolder holder) {

        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("type_post")
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(final QuerySnapshot querySnapshot) {
                        // holder.like_count.setText(String.valueOf(querySnapshot.size()));
                        Toast.makeText(context, querySnapshot.toString(), Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error",e.getMessage());
                    }
                });


        mFirestore.collection("Posts")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .collection("type_post")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("succes", document.getId() + " => " + document.getData());
                            }
                        } else {
                            Log.w("erro", "Error getting documents.", task.getException());
                        }
                    }
                });


        mFirestore.collection("Users")
                .document(postList.get(holder.getAdapterPosition()).postId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(final DocumentSnapshot documentSnapshot) {

                        try {
                            if (documentSnapshot.getString("username") !=null) {

                                Toast.makeText(context, documentSnapshot.getString("username"), Toast.LENGTH_SHORT).show();

                            }


                        }catch(Exception e){
                            e.printStackTrace();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Error", e.getMessage());
                    }
                });


    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private CircleImageView user_image;
        private TextView user_name, timestamp, post_desc;
        private MaterialFavoriteButton sav_button, like_btn, share_btn, comment_btn;
        private FrameLayout mImageholder;
        private FrameLayout pager_layout;
        private RelativeLayout indicator_holder;
        private AutofitTextView post_text;
        private ImageView delete;
        private GifImageView VideoIndicator;
        private GifImageView Loading_VideoIndicator;
        private ImageView video_indicator_Image;

        private TextView Numb_comment_message_likes;
        private ViewPager pager;
        private PageIndicatorView indicator;
        private View vBgLike;
        private ImageView ivLike;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            video_indicator_Image = mView.findViewById(R.id.play_video_ok);
            VideoIndicator  = mView.findViewById(R.id.video_indicator);
            Loading_VideoIndicator  = mView.findViewById(R.id.loading_video_indicator);
            Numb_comment_message_likes = mView.findViewById(R.id.post_message_like_infos);

            user_image = mView.findViewById(R.id.post_user_image);
            like_btn = mView.findViewById(R.id.like_button);
            vBgLike = mView.findViewById(R.id.vBgLike);
            ivLike = mView.findViewById(R.id.ivLike);
            user_name = mView.findViewById(R.id.post_username);
            timestamp = mView.findViewById(R.id.post_timestamp);
            post_desc = mView.findViewById(R.id.post_desc);
            post_text = mView.findViewById(R.id.post_text);
            indicator=mView.findViewById(R.id.indicator);
            indicator_holder=mView.findViewById(R.id.indicator_holder);
            pager=mView.findViewById(R.id.pager);
            pager_layout=mView.findViewById(R.id.pager_layout);
            like_btn = mView.findViewById(R.id.like_button);
            comment_btn = mView.findViewById(R.id.comment_button);
            share_btn = mView.findViewById(R.id.share_button);
            delete = mView.findViewById(R.id.delete_button);
            sav_button = mView.findViewById(R.id.save_button);
            mImageholder = mView.findViewById(R.id.image_holder);


        }
    }

}
