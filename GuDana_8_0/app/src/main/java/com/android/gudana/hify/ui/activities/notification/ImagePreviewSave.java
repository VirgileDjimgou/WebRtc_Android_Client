package com.android.gudana.hify.ui.activities.notification;

import android.app.DownloadManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.gudana.hify.models.MultipleImage;
import com.android.gudana.hify.utils.Config;
import com.android.gudana.R;
import com.android.gudana.hify.utils.OnSwipeTouchListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.firebase.auth.FirebaseAuth;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ImagePreviewSave extends AppCompatActivity {

    private ArrayList<MultipleImage> multipleImage;
    String intent_URI,intent_URL;
    ArrayList<Long> list = new ArrayList<>();
    private PhotoView photoView;
    private long refid;
    private String sender_name;
    private int position = 0;
    private ArrayList<String>  list_of_images= null;
    private FrameLayout photo_view_frame;
    public BroadcastReceiver onComplete = new BroadcastReceiver() {

        public void onReceive(Context ctxt, Intent intent) {

            long referenceId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
            list.remove(referenceId);
            if (list.isEmpty()) {
                NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    setupChannels(notificationManager);
                }
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                        ImagePreviewSave.this, Config.ADMIN_CHANNEL_ID);

                android.app.Notification notification;
                notification = mBuilder
                        .setAutoCancel(true)
                        .setContentTitle("Download success")
                        .setColorized(true)
                        .setSound(Uri.parse("android.resource://"+getPackageName()+"/"+R.raw.hify_sound))
                        .setColor(Color.parseColor("#2591FC"))
                        .setSmallIcon(R.drawable.ic_file_download_accent_24dp)
                        .setContentText("Image saved in /Downloads/gudana/" + sender_name)
                        .build();

                notificationManager.notify(0, notification);
                Toasty.info(ctxt, "Image saved in /Downloads/gudana/" + sender_name, Toast.LENGTH_LONG).show();
            }
        }

    };
    private DownloadManager downloadManager;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(onComplete);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.hi_slide_from_right, R.anim.hi_slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.hi_slide_from_left, R.anim.hi_slide_to_right);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hi_activity_image_preview_save);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        intent_URI=getIntent().getStringExtra("uri");
        intent_URL=getIntent().getStringExtra("url");
        sender_name=getIntent().getStringExtra("sender_name");
        list_of_images = getIntent().getStringArrayListExtra("Images");
        // ArrayList<CustomInput> fields = dw.getFields();



        ImageView return_button  = (ImageView) findViewById(R.id.return_button);
        return_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        registerReceiver(onComplete,
                new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));

        downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        photoView = findViewById(R.id.photo_view);

        if(!TextUtils.isEmpty(intent_URI)) {
            photoView.setImageURI(Uri.parse(intent_URI));

        }else {

            String Url_to_load = intent_URL.trim();
            Glide.with(ImagePreviewSave.this)
                    .setDefaultRequestOptions(
                            new RequestOptions().placeholder(getResources().getDrawable(R.drawable.placeholder)))
                    .load(Url_to_load)
                    .into(photoView);


        }

        ImageButton ShareButton = (ImageButton) findViewById(R.id.share);
        ShareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // share Button  ...
                share_image_view();

            }
        });

        photo_view_frame = findViewById(R.id.photo_view_frame);

        // images touch listener ...
        photoView.setOnTouchListener(new OnSwipeTouchListener(ImagePreviewSave.this) {
            public void onSwipeTop() {
                //Toast.makeText(ImagePreviewSave.this, "top", Toast.LENGTH_SHORT).show();
            }
            public void onSwipeRight() {
                //Toast.makeText(ImagePreviewSave.this, "right", Toast.LENGTH_SHORT).show();
                if(list_of_images.size()>0){

                    if(position < list_of_images.size()-1){
                        position = position +1;
                        load_image(list_of_images.get(position));
                    }else{
                        load_image(list_of_images.get(position));
                    }
                }
            }
            public void onSwipeLeft() {
                //Toast.makeText(ImagePreviewSave.this, "left", Toast.LENGTH_SHORT).show();
                if(list_of_images.size() >0){
                    if(position > 0){
                        position = position -1;
                        load_image(list_of_images.get(position));
                    }else{
                        load_image(list_of_images.get(position));
                    }
                }
            }
            public void onSwipeBottom() {
               // Toast.makeText(ImagePreviewSave.this, "bottom", Toast.LENGTH_SHORT).show();
            }

        });


    }

    private void load_image (String url_to_load){


        Glide.with(ImagePreviewSave.this)
                .setDefaultRequestOptions(new RequestOptions().placeholder(getResources().getDrawable(R.drawable.placeholder)))
                .load(url_to_load.trim())
                .into(photoView);

    }

    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void downloadImage(final String ImageURI) {

       new MaterialDialog.Builder(this)
                .title("Save Image")
                .content("Do you want to save this image?")
                .positiveText("YES")
                .negativeText("NO")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {

                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(ImageURI));
                        request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
                        request.setAllowedOverRoaming(true);
                        request.setTitle("Hify");
                        request.setDescription("Downloading image...");
                        request.setVisibleInDownloadsUi(true);
                        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, "/Gudana Images/"+sender_name  + "/HFY_" +  System.currentTimeMillis() + ".jpeg");

                        refid = downloadManager.enqueue(request);
                        list.add(refid);


                    }
                }).onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        dialog.dismiss();
                    }
                }).show();

    }

    public void saveImage(View view) {

        Dexter.withActivity(this)
                .withPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        if(isOnline()) {
                            if (!TextUtils.isEmpty(intent_URI)) {
                                downloadImage(intent_URI);
                            } else if(list_of_images.size()>0){

                                downloadImage(list_of_images.get(position));

                            }else {
                                downloadImage(intent_URL);
                            }
                        }else{
                            Toasty.error(ImagePreviewSave.this, "No internet connection", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if(response.isPermanentlyDenied()){
                                    DialogOnDeniedPermissionListener.Builder
                                            .withContext(ImagePreviewSave.this)
                                            .withTitle("Storage permission")
                                            .withMessage("Storage permission is needed for downloading images.")
                                            .withButtonText(android.R.string.ok)
                                            .withIcon(R.mipmap.logo)
                                            .build();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                    }
                }).check();

    }

    public void share_image_view(){

        /*
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            sendIntent.putExtra(Intent.EXTRA_TEXT, list_of_images.get(position));
            sendIntent.setType("text/plain");
            startActivity(sendIntent);

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, uriToImage);
            shareIntent.setType("image/jpeg");
            startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.send_to)));

            */


            String shareBody = list_of_images.get(position).toString();
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Image from OkMboa");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
            startActivity(Intent.createChooser(sharingIntent, "Share using ...."));

            /*
                try {

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("image/*");
                    intent.putExtra(Intent.EXTRA_STREAM, list_of_images.get(position), holder, R.string.app_name +"_user_" + FirebaseAuth.getInstance().getUid());
                    startActivity(Intent.createChooser(intent, "Share using..."));

                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }


            */

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setupChannels(NotificationManager notificationManager) {
        CharSequence adminChannelName = "Downloads";
        String adminChannelDescription = "Used to show the progress of downloads";
        NotificationChannel adminChannel;
        adminChannel = new NotificationChannel(Config.ADMIN_CHANNEL_ID, adminChannelName, NotificationManager.IMPORTANCE_DEFAULT);
        adminChannel.setDescription(adminChannelDescription);
        adminChannel.enableVibration(true);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(adminChannel);
        }
    }

}
