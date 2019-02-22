package com.android.gudana.hify.ui.activities;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;


import android.content.Intent;
import android.provider.MediaStore;
import androidx.annotation.Nullable;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.android.gudana.GuDFeed.AbActivity;
import com.android.gudana.hify.utils.AndroidMultiPartEntity;
import com.android.gudana.hify.utils.Config;
import com.android.gudana.hify.utils.JSONParser;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.kbeanie.multipicker.api.CacheLocation;
import com.kbeanie.multipicker.api.CameraImagePicker;
import com.kbeanie.multipicker.api.CameraVideoPicker;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.VideoPicker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.callbacks.MediaPickerCallback;
import com.kbeanie.multipicker.api.callbacks.VideoPickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenFile;
import com.kbeanie.multipicker.api.entity.ChosenImage;
import com.kbeanie.multipicker.api.entity.ChosenVideo;
import com.android.gudana.GuDFeed.adapters.MediaResultsAdapter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.android.gudana.R;
import com.yarolegovich.lovelydialog.LovelyInfoDialog;
import com.yarolegovich.lovelydialog.LovelyStandardDialog;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Actions.EmojIconActions;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import id.zelory.compressor.Compressor;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;


public class create_post extends AbActivity implements
        VideoPickerCallback ,
        ImagePickerCallback ,
        MediaPickerCallback {

    private ListView lvResults;
    private Button Post;
    private ImageView video_taker;
    private String pickerPath;
    private ImageView btEmoji;
    private EmojIconActions emojIcon;
    private EmojiconEditText messageEditText;
    private ListView recyclerView;
    private MediaResultsAdapter adapter = null;
    private List<ChosenFile> files_to_post = new ArrayList<>();
    private ProgressDialog mDialog;



    private FirebaseFirestore mFirestore;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private Map<String, Object> postMap;
    private StorageReference mStorage;

    /// image   url upload
    public static List<String> uploadedImagesUrl_hybrid = new ArrayList<>();
    public static List<String> uploadedImagesUrl_hybrid_video = new ArrayList<>();


    public static int fileUploaded = 0;
    public static int numb_of_file = 0;

    private static final int MY_CAMERA_REQUEST_CODE = 100;


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, create_post.class);
        context.startActivity(intent);
    }

    @NonNull
    public static String random() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(10);
        char tempChar;
        for (int i = 0; i < randomLength; i++) {
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }

    private void hashtag_dialog(){

        new LovelyInfoDialog(this)
                .setTopColorRes(R.color.blue)
                .setIcon(R.mipmap.ic_hashtag)
                .setTitle("Hashtag GuDana")
                .setMessage("Add hashtag  to help people on GuDana Network see your post or your services : use the symbol hashtag  # " +
                        "to start you own or choose GuDana sugested hashtag ")
                //This will add Don't show again checkbox to the dialog. You can pass any ID as argument
                .setNotShowAgainOptionEnabled(0)
                .setNotShowAgainOptionChecked(true)
                .show();
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_picker_activity);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        getSupportActionBar().setTitle(R.string.create_post);
        getSupportActionBar().setSubtitle("Create your storytelling");
        messageEditText = (EmojiconEditText) findViewById(R.id.editTextMessage);
        recyclerView = findViewById(R.id.lvResults);

        // Will handle typing feature, 0 means no typing, 1 typing, 2 deleting and 3 thinking (5+ sec delay)

        // Checking if root layout changed to detect soft keyboard

        final LinearLayout root = findViewById(R.id.root);
        root.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int previousHeight = root.getRootView().getHeight() - root.getHeight() - recyclerView.getHeight();

            @Override
            public void onGlobalLayout() {
                int height = root.getRootView().getHeight() - root.getHeight() - recyclerView.getHeight();

                if (previousHeight != height) {
                    if (previousHeight > height) {
                        previousHeight = height;
                    } else if (previousHeight < height) {
                        // recyclerView.scrollToPosition(messagesList.size() - 1);

                        previousHeight = height;
                    }
                }
            }
        });


        btEmoji = (ImageView) findViewById(R.id.buttonEmoji);
        btEmoji = (ImageView) findViewById(R.id.buttonEmoji);
        emojIcon = new EmojIconActions(this, root, messageEditText, btEmoji);
        emojIcon.ShowEmojIcon();

        lvResults = (ListView) findViewById(R.id.lvResults);
        video_taker = (ImageView) findViewById(R.id.take_video);
        video_taker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (files_to_post.size()>0){

                    chechkNumberofvide();
                }else{
                    dialog_video();

                }
                // pickVideoSingle();
            }
        });



        Post = (Button) findViewById(R.id.post_button);

        // Post.setEnabled(false);
        Post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // chehck if you are adding some files
                if(!files_to_post.isEmpty()){
                    Post.setEnabled(false);
                    video_taker.setEnabled(false);
                    UploadOnStorage(files_to_post);
                    //new AsynchronePost().execute("");
                }else{
                    Toasty.info(create_post.this, "please select a video file", Toast.LENGTH_SHORT).show();
                }

            }
        });


        // detect all kind of problem
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .detectAll()// or .detectAll() for all detectable problems
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());

        //
        //initRecyclerView();

        // init storage
        mStorage= FirebaseStorage.getInstance().getReference();
        postMap = new HashMap<>();
        mFirestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

        // hide Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     askPermission();
     // hastag dialog
        hashtag_dialog();
    }



    private void askPermission() {

        Dexter.withActivity(this)
                .withPermissions(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.CAMERA,
                        android.Manifest.permission.READ_EXTERNAL_STORAGE
                )
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if(report.isAnyPermissionPermanentlyDenied()){
                            Toast.makeText(create_post.this, "You have denied some permissions permanently, if the app force close try granting permission from settings.", Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {

                    }
                }).check();

    }



    /*
    private void initRecyclerView() {
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.rv_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        SimpleAdapter adapter = new SimpleAdapter();
        recyclerView.setAdapter(adapter);
        adapter.setCount(10);
    }
    */


    public void dialog_Images() {

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.purple)
                .setButtonsColorRes(R.color.black)
                .setIcon(R.mipmap.ic_picture)
                .setTitle("Video Choice")
                .setMessage(" Options to select a video ")
                .setPositiveButton("use Camera", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        takePicture();
                    }
                })
                .setNegativeButton("from gallery (Single Image)", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickImageSingle();
                    }
                })
                .setNeutralButton("from gallery (Multiple Images)", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        pickImageMultiple();
                    }
                })
                .show();
    }

    public void chechkNumberofvide(){

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.blue)
                .setButtonsColorRes(R.color.blue)
                .setIcon(R.mipmap.ic_video)
                .setTitle("choose another video ? ")
                .setMessage("you can only post 1 video at a time ... do you want  to delete they already taken and chose another one ? ")
                .setPositiveButton("continue ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog_video();
                        // Toast.makeText(create_post.this, "positive clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Toast.makeText(create_post.this, "positive clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();

    }

    public void dialog_video() {

        new LovelyStandardDialog(this, LovelyStandardDialog.ButtonLayout.VERTICAL)
                .setTopColorRes(R.color.purple)
                .setButtonsColorRes(R.color.blue)
                .setIcon(R.mipmap.ic_video)
                .setTitle("Video Choice")
                .setMessage(" Maximale Video duration should be 120 seconds ")
                .setPositiveButton("use Camera", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // always rest the number of video  if something ist in puffer
                        files_to_post.clear();
                        takeVideo();
                        // Toast.makeText(create_post.this, "positive clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("from gallery ", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        files_to_post.clear();
                        pickVideoSingle();
                        //Toast.makeText(create_post.this, "positive clicked", Toast.LENGTH_SHORT).show();
                    }
                })
                .show();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //NavUtils.navigateUpFromSameTask(this);
                //MainActivity_with_Drawer.tabLayout.getTabAt(3);
                //MainActivity_with_Drawer.mViewPager.setCurrentItem(3);
                create_post.this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    /// ############################ Vide
    private VideoPicker videoPicker;

    private void pickVideoSingle() {
        videoPicker = new VideoPicker(this);
        videoPicker.shouldGenerateMetadata(true);
        videoPicker.shouldGeneratePreviewImages(true);
        videoPicker.setVideoPickerCallback(this);
        videoPicker.pickVideo();
    }


    private CameraVideoPicker cameraPicker_video;

    private void takeVideo() {
        cameraPicker_video = new CameraVideoPicker(this);
        cameraPicker_video.shouldGenerateMetadata(true);
        cameraPicker_video.setCacheLocation(CacheLocation.INTERNAL_APP_DIR);
        cameraPicker_video.shouldGeneratePreviewImages(true);
        Bundle extras = new Bundle();
        // For capturing Low quality videos; Default is 1: HIGH
        extras.putInt(MediaStore.EXTRA_VIDEO_QUALITY, 0);
        // Set the duration of the video
        extras.putInt(MediaStore.EXTRA_DURATION_LIMIT, 120);
        cameraPicker_video.setExtras(extras);
        cameraPicker_video.setVideoPickerCallback(this);
        pickerPath = cameraPicker_video.pickVideo();
    }


    ////////////// ##########   Images ...

    private ImagePicker imagePicker;

    public void pickImageSingle() {
        imagePicker = new ImagePicker(this);
        imagePicker.setDebugglable(true);
        imagePicker.setFolderName("Random");
        imagePicker.setRequestId(1234);
        imagePicker.ensureMaxSize(500, 500);
        imagePicker.shouldGenerateMetadata(true);
        imagePicker.shouldGenerateThumbnails(true);
        imagePicker.setImagePickerCallback(this);
        Bundle bundle = new Bundle();
        bundle.putInt("android.intent.extras.CAMERA_FACING", 1);
        imagePicker.setCacheLocation(CacheLocation.EXTERNAL_STORAGE_PUBLIC_DIR);
        imagePicker.pickImage();
    }

    public void pickImageMultiple() {
        imagePicker = new ImagePicker(this);
        imagePicker.setImagePickerCallback(this);
        imagePicker.allowMultiple();
        imagePicker.pickImage();
    }

    private CameraImagePicker cameraPicker;

    public void takePicture() {
        cameraPicker = new CameraImagePicker(this);
        cameraPicker.setDebugglable(true);
        cameraPicker.setCacheLocation(CacheLocation.EXTERNAL_STORAGE_APP_DIR);
        cameraPicker.setImagePickerCallback(this);
        cameraPicker.shouldGenerateMetadata(true);
        cameraPicker.shouldGenerateThumbnails(true);
        pickerPath = cameraPicker.pickImage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == Picker.PICK_VIDEO_DEVICE) {
                if (videoPicker == null) {
                    videoPicker = new VideoPicker(this);
                    videoPicker.setVideoPickerCallback(this);
                }
                videoPicker.submit(data);

            } else if (requestCode == Picker.PICK_VIDEO_CAMERA) {
                if (cameraPicker_video == null) {
                    cameraPicker_video = new CameraVideoPicker(this, pickerPath);
                }
                cameraPicker_video.submit(data);
            }

            // images
            if (requestCode == Picker.PICK_IMAGE_DEVICE) {
                if (imagePicker == null) {
                    imagePicker = new ImagePicker(this);
                    imagePicker.setImagePickerCallback(this);
                }
                imagePicker.submit(data);
            } else if (requestCode == Picker.PICK_IMAGE_CAMERA) {
                if (cameraPicker == null) {
                    cameraPicker = new CameraImagePicker(this);
                    cameraPicker.setImagePickerCallback(this);
                    cameraPicker.reinitialize(pickerPath);
                }
                cameraPicker.submit(data);
            }

        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // You have to save path in case your activity is killed.
        // In such a scenario, you will need to re-initialize the CameraVideoPicker
        outState.putString("picker_path", pickerPath);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        // After Activity recreate, you need to re-intialize these
        // path value to be able to re-intialize CameraVideoPicker
        if (savedInstanceState.containsKey("picker_path")) {
            pickerPath = savedInstanceState.getString("picker_path");
        }
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onVideosChosen(List<ChosenVideo> files) {
        // reset recycler
        if (files_to_post.size() < 6) {

            files_to_post.addAll(files);
            long video_duration = files.get(0).getDuration();
            if(video_duration >= 61090*2){
                Toasty.error(this, "your video exceed 120 seconds ....please reduce the duration of your video !" + files.get(0).getDuration(), Toast.LENGTH_LONG).show();
            }else{

                // files_to_post.clear();
                MediaResultsAdapter adapter = new MediaResultsAdapter(files_to_post, this);
                this.adapter = this.adapter;
                lvResults.setAdapter(adapter);

            }


        } else {
            files_to_post.clear();
            Toasty.warning(create_post.this,
                    "you cannot  post more than  6 Items ! ", Toast.LENGTH_LONG, true).show();

        }


    }

    @Override
    public void onMediaChosen(List<ChosenImage> images, List<ChosenVideo> videos) {
        List<ChosenFile> files = new ArrayList<>();
        if (images != null) {
            files.addAll(images);
        }
        if (videos != null) {
            files.addAll(videos);
        }
        MediaResultsAdapter adapter = new MediaResultsAdapter(files, this);
        lvResults.setAdapter(adapter);
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(create_post.this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure you want to close the Post Section ? ");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                create_post.this.finish();
                //Intent intent = new Intent(Intent.ACTION_MAIN);
                //intent.addCategory(Intent.CATEGORY_HOME);
                //intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //startActivity(intent);
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    public void onError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }


    // added  FileOpen.openFile(context,localFile);

    @Override
    public void onImagesChosen(List<ChosenImage> images) {
        if (files_to_post.size() < 10) {
            files_to_post.addAll(images);
            this.adapter = new MediaResultsAdapter(files_to_post, this);
            lvResults.setAdapter(adapter);
        } else {

            Toasty.warning(create_post.this,
                    "you can not  post more than  10 Items ! ", Toast.LENGTH_LONG, true).show();

        }

    }

    // file uploader in reference  ...
    protected void UploadOnStorage(List<ChosenFile> files) {

                try{

                    mDialog = new ProgressDialog(this);
                    mDialog.setMessage("Posting...");
                    mDialog.setIndeterminate(true);
                    mDialog.setCancelable(false);
                    mDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            try{
                                mDialog.dismiss();
                                dialog.dismiss();
                                uploadedImagesUrl_hybrid.clear();
                                uploadedImagesUrl_hybrid_video.clear();
                                //uploadedImagesUrl_hybrid_video = null;
                                //uploadedImagesUrl_hybrid = null;
                                fileUploaded = 0;
                                create_post.this.finish();

                            }catch(Exception ex){
                                ex.printStackTrace();
                            }

                        }
                    });
                    mDialog.setCanceledOnTouchOutside(false);
                    mDialog.show();


                    numb_of_file = files.size();
                    if(!files.isEmpty()){

                        for(int i=0;i<files.size();i++) {

                            //Uri fileUri = Uri.fromFile(new File(files.get(i).getOriginalPath()));
                            //final ChosenVideo video = (ChosenVideo) files.get(i);
                            //Glide.with(context).load(Uri.fromFile(new File(video.getPreviewThumbnail()))).into(ivImage);
                            // String fileName = files.get(i).get
                            //final StorageReference fileToUploadStorage = mStorage.child("post_images").child(random() + ".png");
                            //Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(files.get(i).getOriginalPath(),MediaStore.Images.Thumbnails.MINI_KIND);


                            //new UploadImagesToServer(files.get(i).getOriginalPath(), Config.IMAGES_UPLOAD_URL,i).execute();
                            new Compressor(this)
                                    .compressToFileAsFlowable(new File(files.get(i).getOriginalPath()))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(new Consumer<File>() {
                                        @Override
                                        public void accept(File file) {
                                            //compressedImage = file;
                                            // new PostImage.UploadFileToServer(file, Config.IMAGES_UPLOAD_URL,finalI).execute();
                                            new UploadImagesToServer(file, Config.IMAGES_UPLOAD_URL,0).execute();


                                        }
                                    }, new Consumer<Throwable>() {
                                        @Override
                                        public void accept(Throwable throwable) {
                                            throwable.printStackTrace();
                                            Toasty.error(create_post.this, throwable.getMessage().toString(), Toast.LENGTH_SHORT).show();
                                            //showError(throwable.getMessage());
                                        }
                                    });

                            /*
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                            byte[] data = baos.toByteArray();
                            fileToUploadStorage.putBytes(data).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    fileToUploadStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            try{
                                                //Toast.makeText(create_post.this, "Send : " + uri.toString(), Toast.LENGTH_SHORT).show();
                                                uploadedImagesUrl_hybrid.add(uri.toString() );


                                            }catch (Exception ex){

                                            }

                                        }
                                    });

                                }
                            });
                            */


                            // after that send video   ......
                            // UploadVideoToServer(String filePath , String Url_Server_to_upload , int finalI);
                            new UploadVideoToServer(files.get(i).getOriginalPath(), Config.VIDEOS_UPLOAD_URL,i).execute();

                            /*
                            // StorageReference file = FirebaseStorage.getInstance().getReference().child("message_doc").child(messageId +"#"+filename+"."+ ext);
                            final StorageReference VideoUploadStorage = mStorage.child("post_video").child(random() + files.get(i).getExtension());
                            VideoUploadStorage.putFile(fileUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                                    VideoUploadStorage.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            try{
                                                //Toast.makeText(create_post.this, "Send : " + uri.toString(), Toast.LENGTH_SHORT).show();

                                                uploadedImagesUrl_hybrid_video.add(uri.toString());
                                                fileUploaded = fileUploaded +1;
                                                if(fileUploaded >= numb_of_file ){
                                                    // than update firestore ...
                                                    mDialog.dismiss();
                                                    uploadPost();

                                                }

                                            }catch (Exception ex){

                                            }

                                        }
                                    });

                                }
                            });
                            */

                        }



                    }

                }catch(Exception ex){
                    ex.printStackTrace();
                }


        }
    // uploader   document on firestore  database

    private void uploadPost() {

        try{

            mDialog = new ProgressDialog(this);
            mDialog.setMessage("Posting...");
            mDialog.setIndeterminate(true);
            mDialog.setCancelable(false);
            mDialog.setCanceledOnTouchOutside(false);

            if (!uploadedImagesUrl_hybrid.isEmpty()) {

                mDialog.show();
                mFirestore.collection("Users").document(mCurrentUser.getUid()).get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {

                        postMap.put("userId", documentSnapshot.getString("id"));
                        postMap.put("username", documentSnapshot.getString("username"));
                        postMap.put("name", documentSnapshot.getString("name"));
                        postMap.put("userimage", documentSnapshot.getString("image"));
                        postMap.put("timestamp", String.valueOf(System.currentTimeMillis()));
                        postMap.put("image_count", uploadedImagesUrl_hybrid.size());
                        try {
                            postMap.put("image_url_0", uploadedImagesUrl_hybrid.get(0));
                            postMap.put("image_video_0", uploadedImagesUrl_hybrid_video.get(0));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_1", uploadedImagesUrl_hybrid.get(1));
                            postMap.put("image_video_1", uploadedImagesUrl_hybrid_video.get(1));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_2", uploadedImagesUrl_hybrid.get(2));
                            postMap.put("image_video_2", uploadedImagesUrl_hybrid_video.get(2));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_3", uploadedImagesUrl_hybrid.get(3));
                            postMap.put("image_video_3", uploadedImagesUrl_hybrid_video.get(3));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_4", uploadedImagesUrl_hybrid.get(4));
                            postMap.put("image_video_4", uploadedImagesUrl_hybrid_video.get(4));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_5", uploadedImagesUrl_hybrid.get(5));
                            postMap.put("image_video_5", uploadedImagesUrl_hybrid_video.get(5));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        try {
                            postMap.put("image_url_6", uploadedImagesUrl_hybrid.get(6));
                            postMap.put("image_video_6", uploadedImagesUrl_hybrid_video.get(6));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        postMap.put("likes", "0");
                        postMap.put("favourites", "0");
                        postMap.put("description", messageEditText.getText().toString());
                        postMap.put("color", "0");
                        postMap.put("is_video_post", true);

                        mFirestore.collection("Posts")
                                .add(postMap)
                                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                    @Override
                                    public void onSuccess(DocumentReference documentReference) {
                                        mDialog.dismiss();
                                        Toasty.info(create_post.this, "Post sent", Toast.LENGTH_SHORT).show();

                                        try{

                                            mDialog.dismiss();
                                            uploadedImagesUrl_hybrid.clear();
                                            uploadedImagesUrl_hybrid_video.clear();
                                            //uploadedImagesUrl_hybrid_video = null;
                                            //uploadedImagesUrl_hybrid = null;
                                            fileUploaded = 0;
                                            create_post.this.finish();

                                        }catch(Exception ex){
                                            ex.printStackTrace();
                                        }
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        mDialog.dismiss();
                                        Log.e("Error sending post", e.getMessage());
                                    }
                                });


                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        mDialog.dismiss();
                        Log.e("Error getting user", e.getMessage());
                    }
                });

            } else {
                mDialog.dismiss();
                Toast.makeText(this, "No image has been uploaded, Please wait or try again", Toast.LENGTH_SHORT).show();
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }

    }


    // Asynchrone Task  to send file to server ...
    private class AsynchronePost extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
          // method to do in Background ...
            UploadOnStorage(files_to_post);
            return "Executed";
        }

        @Override
        protected void onPostExecute(String result) {

            Toasty.info(create_post.this, "your new post is online", Toast.LENGTH_SHORT).show();
            // might want to change "executed" for the returned string passed
            // into onPostExecute() but that is upto you
        }

        @Override
        protected void onPreExecute() {}

        @Override
        protected void onProgressUpdate(Void... values) {}
    }


    // Upload Images
    /**
     * Uploading the file to server
     * */
    private class UploadImagesToServer extends AsyncTask<Void, Integer, String> {
        long totalSize = 0;
        private File filePath = null;
        private String Url_Server = null;
        private int finalI ;
        private String url_file_uploaded;
        private JSONParser jsonParser = new JSONParser();


        public UploadImagesToServer(File filePath , String Url_Server_to_upload , int finalI) {
            super();
            this.filePath = filePath;
            this.Url_Server = Url_Server_to_upload;
            this.finalI = finalI;
            // do stuff
        }

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            //progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            //progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            //progressBar.setProgress(progress[0]);

            // updating percentage value
            //txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(this.Url_Server);
            //HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);


            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = filePath;

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("www.androidhive.info"));
                entity.addPart("email", new StringBody("abc@gmail.com"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity r_entity = response.getEntity();

                int statusCode = response.getStatusLine().getStatusCode();
                if (statusCode == 200) {
                    // Server response
                    responseString = EntityUtils.toString(r_entity);
                } else {
                    responseString = "Error occurred! Http Status Code: "
                            + statusCode;
                }

            } catch (ClientProtocolException e) {
                responseString = e.toString();
            } catch (IOException e) {
                responseString = e.toString();
            }
            catch (Exception e) {
                e.printStackTrace();
            }


            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("PostImagesClasses", "Response from server: " + result);
            JSONObject json_data = null;

            try {
                json_data = new JSONObject(result);
                Boolean error = json_data.getBoolean("error");
                String url_file = json_data.getString("file_path");
                String message = json_data.getString("message");

                if(error == false){

                    this.url_file_uploaded = url_file;
                    try{
                        //Toast.makeText(create_post.this, "Send : " + uri.toString(), Toast.LENGTH_SHORT).show();
                        uploadedImagesUrl_hybrid.add(this.url_file_uploaded);


                    }catch (Exception ex){

                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            super.onPostExecute(result);
        }

    }


    // Upload Video
    // Upload file
    /**
     * Uploading the file to server
     * */
    private class UploadVideoToServer extends AsyncTask<Void, Integer, String> {
        long totalSize = 0;
        private String filePath = null;
        private String Url_Server = null;
        private int finalI ;
        private String url_file_uploaded;
        private JSONParser jsonParser = new JSONParser();


        public UploadVideoToServer(String filePath , String Url_Server_to_upload , int finalI) {
            super();
            this.filePath = filePath;
            this.Url_Server = Url_Server_to_upload;
            this.finalI = finalI;
            // do stuff
        }

        @Override
        protected void onPreExecute() {
            // setting progress bar to zero
            //progressBar.setProgress(0);
            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            // Making progress bar visible
            //progressBar.setVisibility(View.VISIBLE);

            // updating progress bar value
            //progressBar.setProgress(progress[0]);

            // updating percentage value
            //txtPercentage.setText(String.valueOf(progress[0]) + "%");
        }

        @Override
        protected String doInBackground(Void... params) {
            return uploadFile();
        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(this.Url_Server);
            //HttpPost httppost = new HttpPost(Config.FILE_UPLOAD_URL);


            try {
                AndroidMultiPartEntity entity = new AndroidMultiPartEntity(
                        new AndroidMultiPartEntity.ProgressListener() {

                            @Override
                            public void transferred(long num) {
                                publishProgress((int) ((num / (float) totalSize) * 100));
                            }
                        });

                File sourceFile = new File(filePath);

                // Adding file data to http body
                entity.addPart("image", new FileBody(sourceFile));

                // Extra parameters if you want to pass to server
                entity.addPart("website",
                        new StringBody("www.androidhive.info"));
                entity.addPart("email", new StringBody("abc@gmail.com"));

                totalSize = entity.getContentLength();
                httppost.setEntity(entity);

                // Making server call
                try{
                    HttpResponse response = httpclient.execute(httppost);
                    HttpEntity r_entity = response.getEntity();

                    int statusCode = response.getStatusLine().getStatusCode();
                    if (statusCode == 200) {
                        // Server response
                        responseString = EntityUtils.toString(r_entity);
                    } else {
                        responseString = "Error occurred! Http Status Code: "
                                + statusCode;
                    }

                }catch(Exception ex){
                    ex.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("PostImagesClasses", "Response from server: " + result);
            JSONObject json_data = null;

            try {
                json_data = new JSONObject(result);
                Boolean error = json_data.getBoolean("error");
                String url_file = json_data.getString("file_path");
                String message = json_data.getString("message");

                if(error == false){

                    this.url_file_uploaded = url_file;
                    try{
                        //Toast.makeText(create_post.this, "Send : " + uri.toString(), Toast.LENGTH_SHORT).show();

                        uploadedImagesUrl_hybrid_video.add( this.url_file_uploaded );
                        fileUploaded = fileUploaded +1;
                        if(fileUploaded >= numb_of_file ){
                            // than update firestore ...
                            mDialog.dismiss();
                            uploadPost();

                        }

                    }catch (Exception ex){
                        ex.printStackTrace();

                    }

                }

            } catch (Exception e) {
                e.printStackTrace();
            }


            super.onPostExecute(result);
        }

    }





}
