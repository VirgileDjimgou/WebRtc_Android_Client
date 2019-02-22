

package com.android.gudana.service;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.android.gudana.R;
import com.android.gudana.chat.ChatApplication;
import com.android.gudana.chat.model.User;
import com.android.gudana.gpslocationtracking.LocationTrack;
import com.android.gudana.hify.utils.Config;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import io.github.sac.Ack;
import io.github.sac.BasicListener;
import io.github.sac.Emitter;
import io.github.sac.ReconnectStrategy;
import io.github.sac.Socket;
import io.socket.client.IO;

import static com.android.gudana.hify.utils.Config.UID_EVENT_LOCATION_LIVE_CHANNEL;


// normal socket


/**
 * Created by fabio on 30/01/2016.
 */
public class SensorService extends Service {


    private String User_Id_Sender = ""  , Event = "" ;
    private Double longitude;
    private Double latitude ;
    private JSONObject info_gps_location;
    private io.socket.client.Socket mSocket_gps_location;
    private HashMap<String, io.socket.emitter.Emitter.Listener> eventListeners = new HashMap<>();



    public static  int currentNotificationID = 0;
    public static  String notificationText;
    public static  Bitmap icon;


    public static NotificationManager notificationManager = null ;
    public static NotificationCompat.Builder notificationBuilder;
    public static Boolean NotifierExist = false;

    // gps share
    private LatLng RT_gps_position;
    LocationTrack locationTrack;
    //private live_location_sharing_db live_location;
    String url="ws://35.237.206.152:8000/socketcluster/"; // default Adresse
    //String SocketClusterChannel = "GuDana-Location-Sharing+random_name";
    Socket socket_cluster;

    JSONObject LiveLoc;

    public int counter=0;
    public SensorService( Context applicationContext ) {
        super();
        try{
            if(NotifierExist != true) {
                NotifierExist = true;
                // to avoid  more than one time intialisation  of notification manager    ... on instance of notification
                notificationManager = (NotificationManager) applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);
            }

            if(icon == null){
                icon = BitmapFactory.decodeResource(applicationContext.getResources(),
                        R.mipmap.ic_launcher);
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    public SensorService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Bundle extras = intent.getExtras();

        try{

            if(extras == null) {
                Log.d("Service","null");
            } else {
                Log.d("Service","not null");
                String data = (String) extras.get("From");
                try {

                    JSONObject obj = new JSONObject(data);
                    LiveLoc = obj;
                    try {
                        //startTimer();
                        InitSocketIo(); // to share your current location  ...
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Throwable t) {
                    Log.e("My App", "Could not parse malformed JSON: \"" + "data " + "\"");
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }


        return START_STICKY;
    }


    public void initService(JSONObject LiveLocation){
        LiveLoc = LiveLocation;
        //System.out.println(LiveLoc.getCONTACTS_TABLE_MATRITCULE_LIVE());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent(this, SensorRestarterBroadcastReceiver.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();
    }

    private Timer timer;
    private TimerTask timerTask;
    long oldTime=0;

    // Init  web socket    with remote server
    public void InitSocketIo() throws  InterruptedException {

        try {
            mSocket_gps_location = IO.socket(((ChatApplication) getApplication()).getURL());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            ChatApplication chatApplication = (ChatApplication) this.getApplication();
            User user = chatApplication.getUser();

            mSocket_gps_location.connect();
            info_gps_location = user.serializeToJSON();

            info_gps_location.put("emitter_user_id", FirebaseAuth.getInstance().getUid());
            info_gps_location.put("room_id", UID_EVENT_LOCATION_LIVE_CHANNEL);


            try {

                eventListeners.put("location_listener", onLive_location_Receive);
                setListeningToEvents(true);


            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mSocket_gps_location.emit("room_share_location", info_gps_location);
            System.out.println("hello  comment va tu ");
            startTimer();



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    //  socket listerners  ...
    private void setListeningToEvents(boolean start_listening) {

        try{

            for(Map.Entry eventListener: eventListeners.entrySet()) {
                if(start_listening) {
                    mSocket_gps_location.on((String) eventListener.getKey(), (io.socket.emitter.Emitter.Listener) eventListener.getValue());
                } else {
                    mSocket_gps_location.off((String) eventListener.getKey(), (io.socket.emitter.Emitter.Listener) eventListener.getValue());
                }
            }

        }catch(Exception ex){
            ex.printStackTrace();
        }
    }

    public void startTimer() throws InterruptedException {
        //set a new Timer
        timer = new Timer();
        try{

            //live_location = new live_location_sharing_db(ServiceContext);
            locationTrack = new LocationTrack(this);

            //initialize the TimerTask's job
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            //initializeTimerTask();
            send_location_TimerTask();

            //schedule the timer, to wake up every 1 second
            timer.schedule(timerTask, 2000, 2000); //
        }catch (Exception ex){
            ex.printStackTrace();
        }

    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void send_location_TimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                //  send gps location to  all client for reatime  purpose  ...
                // chehck all the client to broacst  adress   and the client must be local persisted   ...

                try {
                    JSONObject location_object=new JSONObject();

                    location_object.put("Event", UID_EVENT_LOCATION_LIVE_CHANNEL);
                    location_object.put("User_Id_Sender", FirebaseAuth.getInstance().getUid());
                    location_object.put("is_LiveLocation",true);
                    location_object.put("latitude",locationTrack.getLatitude());
                    location_object.put("longitude", locationTrack.getLongitude());

                    new SendMessageTask_location(location_object).execute();

                } catch (Exception e) {
                    e.printStackTrace();
                    //Toasty.error(ServiceContext, "Live location sharing error ", Toast.LENGTH_SHORT).show();
                }
                try {
                    Log.i("in timer", UID_EVENT_LOCATION_LIVE_CHANNEL+ "  in timer ++++  "+ (counter++));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }



    private final io.socket.emitter.Emitter.Listener onLive_location_Receive = new io.socket.emitter.Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject json;
            try {

                json = (JSONObject) args[0];
                System.out.println(json.toString());

                User_Id_Sender = json.getString("User_Id_Sender");
                latitude = json.getDouble("latitude");
                longitude = json.getDouble("longitude");
                Event = json.getString("Event");

                // save message  on local  database  for offline use  ...
                // new Save_offline_MessageTask (json).execute();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    private class SendMessageTask_location extends AsyncTask<String, String, Void> {
        private final JSONObject message_contents;
        //private final String message_raw;

        public SendMessageTask_location(JSONObject message_contents) {
            this.message_contents =  message_contents;
            // this.message_raw = message_contents;
        }

        @Override
        protected Void doInBackground(String... args) {
            JSONObject inputJson;

            try {
                //inputJson = new JSONObject(info_gps_location.toString());
                //inputJson.put("live_location", message_contents);

                mSocket_gps_location.emit("live_location", this.message_contents);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void a) {
            Log.i("socket", "sent message to server");

            //Play_Song_out_message();
            // send  firebase cloud notification
        }
    }


}