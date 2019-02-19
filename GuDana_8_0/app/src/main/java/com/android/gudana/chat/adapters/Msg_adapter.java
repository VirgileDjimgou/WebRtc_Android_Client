package com.android.gudana.chat.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gudana.R;
import com.android.gudana.chat.activities.ChatActivity;
import com.android.gudana.chat.db.DatabaseInitializer;
import com.android.gudana.chat.model.MessageItem;
import com.android.gudana.hify.utils.AndroidMultiPartEntity;
import com.android.gudana.hify.utils.JSONParser;
import com.android.gudana.hify.utils.NetworkUtil;

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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import es.dmoral.toasty.Toasty;


/**
 * Created by anupamchugh on 09/02/16.
 */
public class Msg_adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // added

    private static final String TAG = "adapters/MessageAdapter";

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_BROADCAST = 1;

    private static final int MSG_MENU_COPY_TEXT = 0;
    private static final int MSG_MENU_VIEW_DETAILS = 1;
    private static final int MSG_MENU_VIEW_PROFILE = 2;
    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    private Pattern pattern_url;
    private  Context context;
    private LayoutInflater inflater;
    private ViewGroup parent_local;
    private View convertView_local;
    private ChatActivity ParentActivity;
    private  String username;
    private int type_message_or_boracast = TYPE_MESSAGE;


    // end added
    public final ArrayList<MessageItem> dataSet = new ArrayList<>();
   // private ArrayList<MessageItem> dataSet;
    Context mContext;
    int total_types;
    MediaPlayer mPlayer;
    private boolean fabStateVolume = false;
    //private ChatActivity ParentActivity;
    private SparseBooleanArray mSelectedItemsIds;




    // text view holder
    public  class TextTypeViewHolder extends RecyclerView.ViewHolder {


        TextView txt_message , details_display;
        CardView cardView;

        public TextTypeViewHolder(View itemView) {
            super(itemView);

            this.details_display = (TextView) itemView.findViewById(R.id.details_display);
            this.txt_message = (TextView) itemView.findViewById(R.id.message_display);
            this.cardView = (CardView) itemView.findViewById(R.id.card_view);

            /*
            this.cardView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    MessageItem SelectedMsg  = getItem(getLayoutPosition());

                    if (cardView.isSelected()) {
                        cardView.setBackgroundColor(Color.LTGRAY);
                    } else {
                        cardView .setBackground(null);
                    }
                    cardView.setSelected(cardView.isSelected());
                    showActionsDialog(SelectedMsg , getLayoutPosition() );

                    //Toast.makeText(context, "Long click listerner .... ", Toast.LENGTH_SHORT).show();
                    return false;
                }
            });
            */

        }

    }

    // Action Dialog ...

    private void showActionsDialog(MessageItem MsgObject, final int position) {
        // fetch the note from db
        try {

            CharSequence colors[] = new CharSequence[]{"Edit", "delete"};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Context Menu");
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == 0) {
                        DatabaseInitializer.UpdateMessage(ParentActivity.mDb , MsgObject);
                        ParentActivity.runOnUiThread(new Runnable() {
                            public void run() {

                                try{
                                    Thread.sleep(300);
                                    // Do stuff…
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                            }
                        });
                    } else {
                        DatabaseInitializer.DeleteMessage(ParentActivity.mDb , MsgObject);
                        // delete this message
                        ParentActivity.runOnUiThread(new Runnable() {
                            public void run() {

                                try{
                                    Thread.sleep(500);
                                    // Do stuff…
                                }catch (Exception ex){
                                    ex.printStackTrace();
                                }
                                deleteItem(position);
                            }
                        });

                    }
                }
            });
            builder.show();

        } catch (Exception e) {
            // TODO - handle error
            // show the possible error  here ...
            e.printStackTrace();
        }
    }




    /***
     * Methods required for do selections, remove selections, etc.
     */

    //Toggle selection methods
    public void toggleSelection(int position) {
        selectView(position, !mSelectedItemsIds.get(position));
    }


    //Remove selected selections
    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }


    //Put or delete selected position into SparseBooleanArray
    public void selectView(int position, boolean value) {
        if (value)
            // set select gray
            mSelectedItemsIds.put(position, value);
        else
            // set unselected ...
            mSelectedItemsIds.delete(position);

        notifyDataSetChanged();
    }

    //Get total selected count
    public int getSelectedCount() {
        return mSelectedItemsIds.size();
    }

    //Return all selected ids
    public SparseBooleanArray getSelectedIds() {
        return mSelectedItemsIds;
    }



    // ImageView holder
    public static class ImageTypeViewHolder extends RecyclerView.ViewHolder {


        TextView txtType;
        ImageView image;

        public ImageTypeViewHolder(View itemView) {
            super(itemView);

            this.txtType = (TextView) itemView.findViewById(R.id.type);
            this.image = (ImageView) itemView.findViewById(R.id.background);

        }

    }

    // AudioViewHolder
    public static class AudioTypeViewHolder extends RecyclerView.ViewHolder {


        TextView txtType;
        ImageView fab_image;

        public AudioTypeViewHolder(View itemView) {
            super(itemView);

            this.txtType = (TextView) itemView.findViewById(R.id.type);
            this.fab_image = (ImageView) itemView.findViewById(R.id.fab_image);

        }
    }

    // Live Location holder
    public static class  LiveLoacationholder extends  RecyclerView.ViewHolder{

        public LiveLoacationholder( View itemView) {
            super(itemView);

        }
    }

    // Map Type hoder
    public static class Mapholder extends  RecyclerView.ViewHolder{
        public Mapholder(View itemView) {
            super(itemView);
        }
    }

    // documents type
    public static class Documentsholder extends  RecyclerView.ViewHolder{

        public Documentsholder(@NonNull View itemView) {
            super(itemView);
        }
    }


    /*
    public Msg_adapter(ArrayList<Object> data, Context context) {
        //this.dataSet = data;
        this.mContext = context;
        total_types = dataSet.size();
    }
    */


    public Msg_adapter(Context context, String username , final ChatActivity ParentActivity) {
        this.context = context;
        this.username = username;
        this.ParentActivity = ParentActivity;
        mSelectedItemsIds = new SparseBooleanArray();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view;
        switch (viewType) {
            case MessageItem.TEXT_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_text_type, parent, false);
                return new TextTypeViewHolder(view);
            case MessageItem.IMAGE_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_image_type, parent, false);
                return new ImageTypeViewHolder(view);
            case MessageItem.AUDIO_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_audio_type, parent, false);
                return new AudioTypeViewHolder(view);

            case MessageItem.LIVE_LOCATION:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_live_location_type, parent, false);
                return new LiveLoacationholder(view);
            case MessageItem.Map_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_map_type, parent, false);
                return new Mapholder(view);
            case MessageItem.DOC_TYPE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_doc_type, parent, false);
                return new Documentsholder(view);
        }
        return null;


    }


    @Override
    public int getItemViewType(int position) {

        switch (dataSet.get(position).msg_type) {
            case 0:
                return MessageItem.TEXT_TYPE;
            case 1:
                return MessageItem.IMAGE_TYPE;
            case 2:
                return MessageItem.AUDIO_TYPE;
            case 3:
                return MessageItem.LIVE_LOCATION;
            case 4:
                return MessageItem.Map_TYPE;
            case 5:
                return MessageItem.DOC_TYPE;
            default:
                return -1;
        }


    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int listPosition) {

        MessageItem object = dataSet.get(listPosition);

        // change Background  ...
        /** Change background color of the selected items in list view  **/
        holder.itemView.setBackgroundColor(mSelectedItemsIds.get(listPosition) ? 0x9934B5E4
                        : Color.TRANSPARENT);

        if (object != null) {
            switch (object.msg_type) {
                case MessageItem.TEXT_TYPE:
                    ((TextTypeViewHolder) holder).txt_message.setText(object.getMessage());
                    ((TextTypeViewHolder) holder).details_display.setText(object.getUsername()+" "+object.getDateTimeUTC());

                    break;
                case MessageItem.IMAGE_TYPE:
                    ((ImageTypeViewHolder) holder).txtType.setText(object.text);
                    ((ImageTypeViewHolder) holder).image.setImageResource(object.data);
                    break;
                case MessageItem.AUDIO_TYPE:

                    ((AudioTypeViewHolder) holder).txtType.setText(object.text);


                    ((AudioTypeViewHolder) holder).fab_image.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            if (fabStateVolume) {
                                if (mPlayer.isPlaying()) {
                                    mPlayer.stop();

                                }
                                ((AudioTypeViewHolder) holder).fab_image.setImageResource(R.drawable.volume);
                                fabStateVolume = false;

                            } else {
                                mPlayer = MediaPlayer.create(mContext, R.raw.hify_sound);
                                mPlayer.setLooping(true);
                                mPlayer.start();
                                ((AudioTypeViewHolder) holder).fab_image.setImageResource(R.drawable.mute);
                                fabStateVolume = true;

                            }
                        }
                    });

                    break;
                case MessageItem.LIVE_LOCATION:

                    break;

                case MessageItem.Map_TYPE:

                    break;

                case MessageItem.DOC_TYPE:

                    break;

                    default:
                        // maybe text type  to print  some kind of error ..
            }
        }

    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }


    // add form old message adapter  ...

    public void deleteItem(final int position){
        dataSet.remove(position);
        notifyDataSetChanged();
        // delete Item  for the the selected position ....

    }

    public int addItem(final MessageItem item) {
        dataSet.add(item);
        notifyDataSetChanged();
        return dataSet.size() - 1;
    }

    public int addItem_and_uploadtask(final ChatActivity ParentActivity, final MessageItem item) {

        this.ParentActivity = ParentActivity;

        dataSet.add(item);
        notifyDataSetChanged();
        try{

            Thread.sleep(5);

            // new UploadFileToServer_chat(file, Config.FILE_UPLOAD_URL,"Doc",processin).execute();

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return dataSet.size() - 1;

    }

    public int moveItemToEndOfList(int index) {
        dataSet.add(dataSet.remove(index));
        return dataSet.size() - 1;
    }

    public void addItems(final List<MessageItem> items) {
        dataSet.addAll(items);
        notifyDataSetChanged();
    }

    // TODO: fix for broadcastItem
    public int getFirstID() {
        if (getCount() == 0) return -1;
        return ((MessageItem) dataSet.get(0)).getID();
    }

    // TODO: fix for broadcastItem
    public int getLastID() {
        if (getCount() == 0) return -1;
        return ((MessageItem) dataSet.get(getCount() - 1)).getID();
    }

    public void prependItems(final ArrayList<MessageItem> items) {
        if (getCount() > 0) {
            dataSet.addAll(0, items);
        } else {
            addItems(items);
        }
        notifyDataSetChanged();
    }

    public int getCount() {
        return dataSet.size();
    }

    public MessageItem getItem(int position) {
        return dataSet.get(position);
    }

    public long getItemId(int position) {
        return position;
    }


    public int getViewTypeCount() {
        return 2;
    }

    public static class BroadcastItem {
        private final String message;

        public BroadcastItem(String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }
    }

    public static class MessageViewHolder {
        // image layout
        public LinearLayout layout_image;
        public ProgressBar upload_file_progressbar_image;
        public ImageView stop_upload_task_image;
        public ImageView ImageContainer;

        // map layout
        public LinearLayout layout_map;

        // Live Location Linear layou
        public LinearLayout layout_live_Location;
        public Button Stop_Sharing;

        // doc layout
        public LinearLayout layout_doc;

        // voice message
        public LinearLayout layout_voice_chat;
        public ImageView ButtonPlayStop;

        // message layout
        public LinearLayout message;
        public TextView detailsText;
        public TextView messageText;
        public Drawable bg;

        // Images message ...
        public ImageView Images_message;

        // upload task
    }

    public static class BroadcastViewHolder {
        public TextView broadcastMsg;
    }


    // upload task   file

    private class UploadFileToServer_chat extends AsyncTask<Void, Integer, String> {
        long totalSize = 0;
        private File filePath = null;
        private String Url_Server = null;
        private String UploadType ;
        private String url_file_uploaded;
        private JSONParser jsonParser = new JSONParser();
        private ProgressBar processin_upload;
        private ImageView stop_upload;
        String  msg_uniqueId;
        private MessageAdapter.MessageItem msg;


        public UploadFileToServer_chat(File filePath , String Url_Server_to_upload , String Type_Upload ,
                                       ProgressBar process_upload , ImageView Stop_uploadTask , MessageAdapter.MessageItem msg) {
            super();
            this.filePath = filePath;
            this.Url_Server = Url_Server_to_upload;
            this.UploadType = Type_Upload;

            this.processin_upload = process_upload;
            this.stop_upload = Stop_uploadTask;

            this.msg = msg;
            // do stuff
        }

        @Override
        protected void onPreExecute() {

            processin_upload.setMax(100);
            processin_upload.setProgress(0);

            msg_uniqueId = UUID.randomUUID().toString();
            processin_upload.setVisibility(View.VISIBLE);
            processin_upload.setIndeterminate(true);

            // Initrecycler view
            Date currentTime = Calendar.getInstance().getTime();


            super.onPreExecute();
        }

        @Override
        protected void onProgressUpdate(final Integer... progress) {
            // Making progress bar visible
            /*
            runOnUiThread(new Runnable() {
                public void run() {



                    System.out.println(msg_uniqueId +" update  : "+ progress[0]);
                    // updating progress bar value
                    processin_upload.setProgress(progress[0]);
                    // Do stuff…
                }
            });

             */

            processin_upload.post(new Runnable() {
                @Override
                public void run() {
                    if(progress[0]> 0){
                        processin_upload.setIndeterminate(false);
                    }
                    //System.out.println(msg_uniqueId +" update  : "+ progress[0]);
                    processin_upload.setProgress(progress[0]);
                }
            });

        }

        @Override
        protected String doInBackground(Void... params) {

            // always  chek Internet connection
            if(NetworkUtil.isNetworkAvailable(context)){

                // network is avaiilable true
                return uploadFile();

            }else{
                // return  false network ist not available ...s
                Toasty.error(context, "please check  your internet connection ! ", Toast.LENGTH_SHORT).show();
                return null;
            }


        }

        @SuppressWarnings("deprecation")
        private String uploadFile() {
            String responseString = null;

            try{


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

                    // File sourceFile = new File(filePath);
                    File sourceFile = filePath;

                    // Adding file data to http body
                    entity.addPart("image", new FileBody(sourceFile));

                    // Extra parameters if you want to pass to server
                    entity.addPart("website",
                            new StringBody("www.gudana.com"));
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


            }catch (Exception ex){
                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {

                        Drawable uploaderror = context.getResources().getDrawable(R.drawable.ic_file_upload_retry_retry_24dp);
                        //stop_upload.setImageDrawable(uploaderror);
                        //stop_upload.setImageResource(R.drawable.ic_file_upload_retry_retry_24dp);
                        processin_upload.setProgress(0);
                        processin_upload.setIndeterminate(false);
                        stop_upload.setBackground(uploaderror);
                        msg.setUploaded(false);
                    }
                });
                Toasty.error(context, "Error  while sending a file  ", Toast.LENGTH_SHORT).show();
                ex.printStackTrace();
            }finally {

            }
            return responseString;

        }

        @Override
        protected void onPostExecute(String result) {
            Log.e("PostImagesClasses", "Response from server: " + result);
            JSONObject json_data = null;
            Boolean error = null;
            String url_file = null;
            String message = null;

            try{
                json_data = new JSONObject(result);

            }catch (Exception ex){
                ex.printStackTrace();
            }


            if(json_data != null){

                try{

                    error = json_data.getBoolean("error");
                    url_file = json_data.getString("file_path");
                    message = json_data.getString("message");

                }catch (Exception ex){
                    ex.printStackTrace();
                }



                try {

                    if(error != null  && error == false){

                        ((Activity) context).runOnUiThread(new Runnable() {
                            public void run() {

                                Drawable uploaderror = context.getResources().getDrawable(R.drawable.ic_done_black_24dp);
                                processin_upload.setProgress(100);
                                stop_upload.setBackground(uploaderror);
                                msg.setUploaded(true);
                            }
                        });



                        Toasty.info(context, "file uploaded ", Toast.LENGTH_SHORT).show();
                        this.url_file_uploaded = url_file;

                        if(this.UploadType.equalsIgnoreCase("Images")){
                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    ParentActivity.images_upload_images_to_server(url_file_uploaded);
                                    //images_upload_images_to_firebase(url_file_uploaded);
                                    // Do stuff…
                                }
                            });

                            //images_upload_images_to_firebase(this.url_file_uploaded);
                        }

                        if(this.UploadType.equalsIgnoreCase("Voice")){

                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {
                                    ParentActivity.upload_messagev_voice(url_file_uploaded);

                                }
                            });


                        }

                        if(this.UploadType.equalsIgnoreCase("Doc")){

                            ((Activity) context).runOnUiThread(new Runnable() {
                                public void run() {

                                    ParentActivity.doc_file__upload_to_Server(url_file_uploaded);

                                }
                            });
                        }

                    }else{
                        // we must manage the error  here

                        Drawable uploaderror = context.getResources().getDrawable(R.drawable.ic_file_upload_retry_retry_24dp);
                        //stop_upload.setImageDrawable(uploaderror);
                        stop_upload.setBackground(uploaderror);
                        //stop_upload.setImageResource(R.drawable.ic_file_upload_retry_retry_24dp);
                        processin_upload.setProgress(0);
                        processin_upload.setIndeterminate(false);
                        msg.setUploaded(false);


                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }else{
                // error


                ((Activity) context).runOnUiThread(new Runnable() {
                    public void run() {
                        Drawable uploaderror = context.getResources().getDrawable(R.drawable.ic_file_upload_retry_retry_24dp);
                        //stop_upload.setImageDrawable(uploaderror);
                        //stop_upload.setImageResource(R.drawable.ic_file_upload_retry_retry_24dp);
                        processin_upload.setProgress(0);
                        processin_upload.setIndeterminate(false);
                        stop_upload.setBackground(uploaderror);
                        msg.setUploaded(false);
                    }
                });

            }

            super.onPostExecute(result);
        }

    }



}
