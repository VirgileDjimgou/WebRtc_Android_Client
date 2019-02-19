package com.android.gudana.chat.adapters;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import androidx.core.content.ContextCompat;

import android.os.AsyncTask;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gudana.MoD.MoD_Live_Location_receiver_Activity;
import com.android.gudana.R;
import com.android.gudana.chat.activities.ChatActivity;
import com.android.gudana.chat.utilities.Utility;
import com.android.gudana.hify.ui.activities.notification.ImagePreviewSave;
import com.android.gudana.hify.utils.AndroidMultiPartEntity;
import com.android.gudana.hify.utils.AsyncTaskExecutor;
import com.android.gudana.hify.utils.Config;
import com.android.gudana.hify.utils.JSONParser;
import com.android.gudana.hify.utils.NetworkUtil;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import es.dmoral.toasty.Toasty;

public class MessageAdapter extends BaseAdapter {
    private static final String TAG = "adapters/MessageAdapter";

    private static final int TYPE_MESSAGE = 0;
    private static final int TYPE_BROADCAST = 1;

    private static final int MSG_MENU_COPY_TEXT = 0;
    private static final int MSG_MENU_VIEW_DETAILS = 1;
    private static final int MSG_MENU_VIEW_PROFILE = 2;
    public static final String URL_REGEX = "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$";

    private Pattern pattern_url;
    private final Context context;
    private final ArrayList<Object> mArrayList = new ArrayList<>();
    private LayoutInflater inflater;
    private ViewGroup parent_local;
    private View convertView_local;
    private ChatActivity ParentActivity;
    private final String username;


    LinearLayout.LayoutParams detailsLinearLayoutParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
    );

    // audio recorder  attribut

    private int type_message_or_boracast = TYPE_MESSAGE;

    public MessageAdapter(Context context, String username) {
        this.context = context;
        this.username = username;
    }

    public int addItem(final Object item) {
        mArrayList.add(item);
        notifyDataSetChanged();
        return mArrayList.size() - 1;
    }

    public int addItem_and_uploadtask(final ChatActivity ParentActivity, final Object item) {

        this.ParentActivity = ParentActivity;

        mArrayList.add(item);
        notifyDataSetChanged();
        try{

            Thread.sleep(10);

            // new UploadFileToServer_chat(file, Config.FILE_UPLOAD_URL,"Doc",processin).execute();

        }catch (Exception ex){
            ex.printStackTrace();
        }

        return mArrayList.size() - 1;

    }

    public int moveItemToEndOfList(int index) {
        mArrayList.add(mArrayList.remove(index));
        return mArrayList.size() - 1;
    }

    public void addItems(final ArrayList<Object> items) {
        mArrayList.addAll(items);
        notifyDataSetChanged();
    }

    // TODO: fix for broadcastItem
    public int getFirstID() {
        if (getCount() == 0) return -1;
        return ((MessageItem) mArrayList.get(0)).getID();
    }

    // TODO: fix for broadcastItem
    public int getLastID() {
        if (getCount() == 0) return -1;
        return ((MessageItem) mArrayList.get(getCount() - 1)).getID();
    }

    public void prependItems(final ArrayList<Object> items) {
        if (getCount() > 0) {
            mArrayList.addAll(0, items);
        } else {
            addItems(items);
        }
        notifyDataSetChanged();
    }



    @Override
    public int getCount() {
        return mArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return mArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        if (mArrayList.get(position) instanceof MessageItem) {
            type_message_or_boracast = TYPE_MESSAGE;
        } else if (mArrayList.get(position) instanceof BroadcastItem) {
            type_message_or_boracast = TYPE_BROADCAST;
        }

        return type_message_or_boracast;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        inflater = LayoutInflater.from(context);
        pattern_url = Pattern.compile(URL_REGEX);
        parent_local = parent;
        convertView_local = convertView;

        switch (getItemViewType(position)) {
            case TYPE_BROADCAST:
                System.out.println("Type  broadcast UI ... ");
                Toast.makeText(context, "Type Boadrcast  ...  please update a ui", Toast.LENGTH_SHORT).show();
                break;
            case TYPE_MESSAGE:
                // split the message and swich ..
                try {
                    convertView = TextMessageLayout(position);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }

                break;
            default:
                break;
        }

        return convertView;
    }

    public static class MessageItem {

        public static final int TEXT_TYPE=0;
        public static final int IMAGE_TYPE=1;
        public static final int AUDIO_TYPE=2;
        public static final int Map_TYPE = 3;
        public static final int DOC_TYPE = 4;
        public static final int LIVE_LOCATION = 5;

        public int type;
        public int data;
        public String text;



        private int id;
        private final int user_id;
        private  String username;
        private final String message;
        private String datetime_utc;
        private boolean on_server;
        private String file_type;

        private File file_to_upload_compressed;
        private File local_file;
        private Boolean isUploaded = false;
        private Boolean try_Uploading = false;

        public MessageItem(int id, int user_id, String file_type,
                           String message, String datetime_utc ,
                           File local_file,  File file_to_upload_compressed ,
                           Boolean try_Uploading) {
            this.on_server = true;
            this.id = id;
            this.user_id = user_id;
            this.file_type = file_type;
            this.message = message;
            this.datetime_utc = datetime_utc;
            this.file_to_upload_compressed = file_to_upload_compressed;
            this.local_file = local_file;
            this.try_Uploading = try_Uploading;
        }


        public MessageItem(int id, int user_id, String username, String message, String datetime_utc) {
            this.on_server = true;
            this.id = id;
            this.user_id = user_id;
            this.username = username;
            this.message = message;
            this.datetime_utc = datetime_utc;
        }

        public MessageItem(int user_id, String username, String message) {
            this.on_server = false;
            this.user_id = user_id;
            this.username = username;
            this.message = message;
        }


        public String getFile_type() {
            return file_type;
        }

        public void setFile_type(String file_type) {
            this.file_type = file_type;
        }

        public Boolean getTry_Uploading() {
            return try_Uploading;
        }

        public void setTry_Uploading(Boolean try_Uploading) {
            this.try_Uploading = try_Uploading;
        }

        public Boolean getUploaded() {
            return isUploaded;
        }

        public void setUploaded(Boolean uploaded) {
            isUploaded = uploaded;
        }


        public File getFile_to_upload_compressed() {
            return file_to_upload_compressed;
        }

        public void setFile_to_upload_compressed(File file_to_upload_compressed) {
            this.file_to_upload_compressed = file_to_upload_compressed;
        }

        public File getLocal_file() {
            return local_file;
        }

        public void setLocal_file(File local_file) {
            this.local_file = local_file;
        }

        public int getID() {
            return id;
        }

        public int getUserID() {
            return user_id;
        }

        public String getUsername() {
            return username;
        }

        public String getMessage() {
            return message;
        }

        public String getDateTimeUTC() {
            return datetime_utc;
        }

        public void savedToServer(int id, String datetime_utc) {
            this.on_server = true;
            this.id = id;
            this.datetime_utc = datetime_utc;
        }

        @Override
        public String toString() {
            return username + ": " + message;
        }
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

    // Images  messages Layout

    public View TextMessageLayout(int position) {
        try {

            final MessageItem msg_item = (MessageItem) getItem(position);
            // chehck type of message   ..


            System.out.println("messagelayout");
            final MessageViewHolder messageViewHolder;
            if (convertView_local == null) {
                convertView_local = inflater.inflate(R.layout.chat_listview_messages, parent_local, false);

                // text message  layout
                messageViewHolder = new MessageViewHolder();
                messageViewHolder.detailsText = (TextView) convertView_local.findViewById(R.id.details_display);
                messageViewHolder.messageText = (TextView) convertView_local.findViewById(R.id.message_display);
                messageViewHolder.message = (LinearLayout) convertView_local.findViewById(R.id.message);
                messageViewHolder.bg = messageViewHolder.messageText.getBackground();

                // imagevieyw  layout
                messageViewHolder.layout_image = (LinearLayout) convertView_local.findViewById(R.id.layout_image);
                messageViewHolder.ImageContainer = (ImageView) convertView_local.findViewById(R.id.image_message);
                messageViewHolder.upload_file_progressbar_image = (ProgressBar) convertView_local.findViewById(R.id.upload_file_progressbar_image) ;
                messageViewHolder.stop_upload_task_image = (ImageView) convertView_local.findViewById(R.id.stop_upload_task_image);

                // Layout map
                messageViewHolder.layout_map = (LinearLayout) convertView_local.findViewById(R.id.layout_map);

                // layout doc
                messageViewHolder.layout_doc = (LinearLayout) convertView_local.findViewById(R.id.layout_doc);

                // Voice message
                messageViewHolder.layout_voice_chat = (LinearLayout) convertView_local.findViewById(R.id.layout_voice_chat);
                messageViewHolder.ButtonPlayStop = (ImageView) convertView_local.findViewById(R.id.ButtonPlayStop);

                // live location
                messageViewHolder.layout_live_Location = (LinearLayout) convertView_local.findViewById(R.id.layout_live_loc);
                messageViewHolder.Stop_Sharing = (Button) convertView_local.findViewById(R.id.stop_sharing);



                convertView_local.setTag(messageViewHolder);
            } else {
                messageViewHolder = (MessageViewHolder) convertView_local.getTag();
            }


            // type of file to upload  image , doc  or voice ....
            if(msg_item.getFile_type() != null){

                if (msg_item.getFile_type().equalsIgnoreCase("image")) {
                    messageViewHolder.layout_image.setVisibility(View.VISIBLE);
                    messageViewHolder.layout_map.setVisibility(View.GONE);
                    messageViewHolder.layout_doc.setVisibility(View.GONE);
                    messageViewHolder.layout_voice_chat.setVisibility(View.GONE);
                    messageViewHolder.layout_live_Location.setVisibility(View.GONE);

                    AsyncTask<Void, Integer, String> TaskUpload = null;

                    if(msg_item.getTry_Uploading()== true){
                        msg_item.setTry_Uploading(false);


                    TaskUpload = new UploadFileToServer_chat(msg_item.getFile_to_upload_compressed(), Config.FILE_UPLOAD_URL,
                            "Doc",messageViewHolder.upload_file_progressbar_image ,
                            messageViewHolder.stop_upload_task_image, msg_item);

                    AsyncTaskExecutor.executeConcurrently(TaskUpload,null);

                    }


                    final AsyncTask<Void, Integer, String> finalTaskUpload = TaskUpload;

                messageViewHolder.stop_upload_task_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if(finalTaskUpload != null){

                            try {
                                finalTaskUpload.cancel(true);
                                messageViewHolder.upload_file_progressbar_image.setProgress(0);
                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

                        }

                    }
                });

                    final String url_image_local = msg_item.getLocal_file().toString();

                    messageViewHolder.layout_image.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {

                            ArrayList<String> ImagesList = new ArrayList<>();
                            ImagesList.add(url_image_local);

                            Intent intent=new Intent(context,ImagePreviewSave.class)
                                    .putExtra("uri","")
                                    .putExtra("sender_name","Gudana_Image_User")
                                    .putExtra("url",url_image_local.trim())                                                                               .putStringArrayListExtra("Images",ImagesList)
                                    .putStringArrayListExtra("Images",ImagesList);
                            context.startActivity(intent);

                        }
                    });

                    Glide.with(context)
                            .setDefaultRequestOptions(new RequestOptions().fitCenter().centerCrop().
                                            placeholder(context.getResources().getDrawable(R.drawable.ic_broken_image_black_24dp)))
                            .load(url_image_local)
                            .into(messageViewHolder.ImageContainer);

                }// voice  upload  ....
                else if(msg_item.getFile_type().equalsIgnoreCase("voice")){
                    messageViewHolder.layout_voice_chat.setVisibility(View.VISIBLE);

                    messageViewHolder.layout_map.setVisibility(View.GONE);
                    messageViewHolder.layout_doc.setVisibility(View.GONE);
                    messageViewHolder.layout_image.setVisibility(View.GONE);
                    messageViewHolder.layout_live_Location.setVisibility(View.GONE);


                    final String voice_url = msg_item.getLocal_file().toString();
                    messageViewHolder.layout_voice_chat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChatActivity.showDiag_voice(context, voice_url);

                        }
                    });

                    messageViewHolder.ButtonPlayStop.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ChatActivity.showDiag_voice(context, voice_url);
                        }
                    });

                }// doc upload  ....

                else if(msg_item.getFile_type().equalsIgnoreCase("doc")){

                    messageViewHolder.layout_doc.setVisibility(View.VISIBLE);
                    messageViewHolder.layout_map.setVisibility(View.GONE);
                    messageViewHolder.layout_image.setVisibility(View.GONE);
                    messageViewHolder.layout_voice_chat.setVisibility(View.GONE);
                    messageViewHolder.layout_live_Location.setVisibility(View.GONE);


                    final String Doc_url = msg_item.getLocal_file().toString();
                    messageViewHolder.layout_doc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String filename = "";
                            try {
                                ChatActivity.download_and_open_document(context, Doc_url, filename);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                }

            }


            else {
                System.out.println("normal message ....  to process here ");
                final String[] type_of_message = msg_item.getMessage().split(ChatActivity.splitter_pattern_message);
                if(type_of_message !=null && type_of_message[0] != null && type_of_message.length >1 ){

                    if(type_of_message[0].equalsIgnoreCase(ChatActivity.Type_Text)){
                        messageViewHolder.layout_image.setVisibility(View.GONE);
                        messageViewHolder.layout_doc.setVisibility(View.GONE);
                        messageViewHolder.layout_map.setVisibility(View.GONE);
                        messageViewHolder.layout_voice_chat.setVisibility(View.GONE);
                        messageViewHolder.layout_live_Location.setVisibility(View.GONE);

                    }else if(type_of_message[0].equalsIgnoreCase(ChatActivity.Type_voice)){
                        messageViewHolder.layout_voice_chat.setVisibility(View.VISIBLE);

                        messageViewHolder.layout_map.setVisibility(View.GONE);
                        messageViewHolder.layout_doc.setVisibility(View.GONE);
                        messageViewHolder.layout_image.setVisibility(View.GONE);
                        messageViewHolder.layout_live_Location.setVisibility(View.GONE);


                        final String voice_url = Config.Media_Server.trim()+type_of_message[1].trim();
                        messageViewHolder.layout_voice_chat.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ChatActivity.showDiag_voice(context, voice_url);

                            }
                        });

                        messageViewHolder.ButtonPlayStop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ChatActivity.showDiag_voice(context, voice_url);
                            }
                        });

                    }else if(type_of_message[0].equalsIgnoreCase(ChatActivity.Type_map)){
                        messageViewHolder.layout_map.setVisibility(View.VISIBLE);
                        messageViewHolder.layout_doc.setVisibility(View.GONE);
                        messageViewHolder.layout_image.setVisibility(View.GONE);
                        messageViewHolder.layout_voice_chat.setVisibility(View.GONE);
                        messageViewHolder.layout_live_Location.setVisibility(View.GONE);


                        messageViewHolder.layout_map.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                ChatActivity.showDiag_gps_menu(context , ChatActivity.Startposition_Custom_dialog  ,type_of_message[1].toString());

                            }
                        });

                    }else if(type_of_message[0].equalsIgnoreCase(ChatActivity.Type_live_location)){

                        messageViewHolder.layout_live_Location.setVisibility(View.VISIBLE);
                        messageViewHolder.layout_map.setVisibility(View.GONE);
                        messageViewHolder.layout_doc.setVisibility(View.GONE);
                        messageViewHolder.layout_image.setVisibility(View.GONE);
                        messageViewHolder.layout_voice_chat.setVisibility(View.GONE);



                        messageViewHolder.Stop_Sharing.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Toasty.warning(context, "Stop Live Location ist not implemented  yet  ...", Toast.LENGTH_SHORT).show();

                            }
                        });

                        messageViewHolder.layout_live_Location.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent LiveLocationReceiver = new Intent(context , MoD_Live_Location_receiver_Activity.class);
                                LiveLocationReceiver.putExtra("data",type_of_message[1].trim());
                                context.startActivity(LiveLocationReceiver);
                                // ChatActivity.showDiag_gps_menu(context , ChatActivity.Startposition_Custom_dialog  ,type_of_message[1].toString());
                            }
                        });

                    }

                }


                // need to reset listener (expensive but necessary to fetch correct user ID for user profile)
                messageViewHolder.message.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        final AlertDialog.Builder messageChoice = new AlertDialog.Builder(context);

                        messageChoice
                                .setTitle("Message options")
                                .setItems(R.array.message_dialog_choice_list, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        switch (which) {
                                            case MSG_MENU_COPY_TEXT:
                                                ClipboardManager clipMan = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                                                ClipData clip = ClipData.newPlainText("chat message", messageViewHolder.messageText.getText().toString());
                                                clipMan.setPrimaryClip(clip);

                                                Toast.makeText(context, "Message copied to clipboard.", Toast.LENGTH_LONG).show();
                                                break;
                                            case MSG_MENU_VIEW_DETAILS:
                                                AlertDialog.Builder detailsDialog = new AlertDialog.Builder(context);
                                                StringBuilder messageStr = new StringBuilder();

                                                messageStr.append("Type: Message");
                                                messageStr.append("\n");
                                                messageStr.append("From: ");
                                                messageStr.append(msg_item.getUsername());
                                                messageStr.append("\n");
                                                messageStr.append("Sent: ");
                                                if(msg_item.on_server) {
                                                    messageStr.append(
                                                            new SimpleDateFormat("d MMMM yyyy h:mm a")
                                                                    .format(Utility.parseDateAsUTC(msg_item.getDateTimeUTC()))
                                                                    .replace("AM", "am")
                                                                    .replace("PM", "pm")
                                                    );
                                                } else {
                                                    messageStr.append("Sending...");
                                                }

                                                detailsDialog
                                                        .setTitle("Message details")
                                                        .setMessage(messageStr.toString())
                                                        .show();
                                                break;
                                            case MSG_MENU_VIEW_PROFILE:
                                                //Intent intent = new Intent(context, UserProfileActivity.class);
                                                //intent.putExtra("user_id", msg_item.user_id);
                                                //context.startActivity(intent);
                                                break;
                                            default:
                                                break;
                                        }
                                    }
                                });

                        messageChoice.show();
                        return true;
                    }
                });


                try{
                    String[] separated = msg_item.getMessage().split(ChatActivity.splitter_pattern_message);
                    if(separated[0].equalsIgnoreCase(ChatActivity.Type_Text) && separated != null && separated.length > 1 &&   separated[1] != null){
                        messageViewHolder.messageText.setText(separated[1].toString());
                    }else{
                        // messageViewHolder.messageText.setText("invalid message ");
                    }
                }catch (Exception ex){
                    ex.printStackTrace();
                }



                //messageViewHolder.messageText.setText(msg_item.getMessage());
                // add all links if message contains them
                try{
                    Linkify.addLinks(messageViewHolder.messageText, Linkify.ALL);


                }catch (Exception ex){
                    ex.printStackTrace();
                }


                Matcher m = pattern_url.matcher(msg_item.getMessage().toString());//replace with string to compare
                if(m.find()) {

                    int color = ContextCompat.getColor(context, R.color.blue);
                    messageViewHolder.messageText.setTextColor(color);
                }

                if(username.equals(msg_item.getUsername())) {
                    messageViewHolder.message.setGravity(Gravity.END);
                    detailsLinearLayoutParams.setMargins(0, 0, 32, 32);
                    messageViewHolder.detailsText.setLayoutParams(detailsLinearLayoutParams);
                    if (messageViewHolder.bg instanceof ShapeDrawable) {
                        ((ShapeDrawable) messageViewHolder.bg).getPaint().setColor(Color.parseColor("#731CFF"));
                        messageViewHolder.detailsText.setTextColor(context.getResources().getColor( R.color.black));
                        messageViewHolder.messageText.setTextColor(context.getResources().getColor( R.color.white));
                    } else if (messageViewHolder.bg instanceof GradientDrawable) {
                        ((GradientDrawable) messageViewHolder.bg).setColor(Color.parseColor("#731CFF"));
                        messageViewHolder.detailsText.setTextColor(context.getResources().getColor( R.color.black));
                        messageViewHolder.messageText.setTextColor(context.getResources().getColor( R.color.white));
                    }
                } else {
                    messageViewHolder.message.setGravity(Gravity.START);
                    detailsLinearLayoutParams.setMargins(32, 0, 0, 32);
                    messageViewHolder.detailsText.setLayoutParams(detailsLinearLayoutParams);  // initial color "#FFDDDDDD"
                    if (messageViewHolder.bg instanceof ShapeDrawable) {
                        ((ShapeDrawable) messageViewHolder.bg).getPaint().setColor(Color.parseColor("#FFFFFF"));

                        messageViewHolder.detailsText.setTextColor(context.getResources().getColor( R.color.black));
                        messageViewHolder.messageText.setTextColor(context.getResources().getColor( R.color.black));
                    } else if (messageViewHolder.bg instanceof GradientDrawable) {
                        ((GradientDrawable) messageViewHolder.bg).setColor(Color.parseColor("#FFFFFF"));
                        messageViewHolder.detailsText.setTextColor(context.getResources().getColor( R.color.black));
                        messageViewHolder.messageText.setTextColor(context.getResources().getColor( R.color.black));
                    }
                }

                StringBuilder details = new StringBuilder();

                if(!username.equals(msg_item.getUsername())) {
                    details.append(msg_item.getUsername());
                    details.append(" - ");
                }

                if(msg_item.on_server) {
                    details.append(Utility.getAbbreviatedDateTime(Utility.parseDateAsUTC(msg_item.getDateTimeUTC())));
                }

                messageViewHolder.detailsText.setText(details.toString());

                messageViewHolder.detailsText.setVisibility(View.VISIBLE);

                if(!msg_item.on_server) {
                    messageViewHolder.detailsText.setText("Sending");
                }
                else if(position + 1 < mArrayList.size()) {
                    Object next_msg_item = getItem(position + 1);
                    if (next_msg_item instanceof MessageItem) {
                        if (msg_item.getUsername().equals(((MessageItem) next_msg_item).getUsername())) {
                            messageViewHolder.detailsText.setVisibility(View.GONE);
                        }
                    }
                }


            }


            return convertView_local;
        } catch (Exception ex) {

            ex.printStackTrace();
        }


        return  convertView_local;
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
        private  MessageItem msg;


        public UploadFileToServer_chat(File filePath , String Url_Server_to_upload , String Type_Upload ,
                                       ProgressBar process_upload , ImageView Stop_uploadTask , MessageItem msg) {
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

