package com.android.gudana.MoD;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.gudana.R;
import com.android.gudana.chat.ChatApplication;
import com.android.gudana.chat.activities.ChatActivity;
import com.android.gudana.chat.model.User;
import com.android.gudana.gpslocationtracking.LocationTrack;
import com.android.gudana.hify.utils.RoundBitmap;
import com.android.gudana.hify.utils.database.UserHelper;
import com.android.gudana.hify.utils.database.live_location_sharing_db;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFrame;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import es.dmoral.toasty.Toasty;
import hani.momanii.supernova_emoji_library.Helper.EmojiconEditText;
import io.github.sac.Ack;
import io.github.sac.BasicListener;
import io.github.sac.Emitter;
import io.github.sac.ReconnectStrategy;
import io.github.sac.Socket;
import io.socket.client.IO;

//import com.firebase.geofire.GeoQuery;


public class MoD_Live_Location_receiver_Activity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        com.google.android.gms.location.LocationListener {

    MapView mMapView;
    private UserHelper userHelper;
    private live_location_sharing_db live_location;
    // socket cluster attribut

    String SocketClusterChannel ;
    Socket socket;
    Handler Typinghandler=new Handler();
    String Username="Demo";
    String UserType = "sub"; // That means connected als subscriber (sub) or publischer (pub)


    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    private Button start_aharing;
    private String usernam = "Username";

    private Button option_15 , option_60 , option_8 , option_custom_time;
    private String Selected_options = "option_15";

    PlaceAutocompleteFragment Destination_autocompleteFragment;
    Drawable select_backgrond, normal_select_backgrond;
    private BitmapDescriptor drawable_icon_dsescritor;
    private Bitmap Icon_User = null;
    private MarkerOptions MyPosition;
    private boolean marker_ok = false; // to tell tthat the mar was correctlly initialised   ..
    private ImageView imgView;
    private static final LatLng MELBOURNE = new LatLng(-37.813, 144.962);
    boolean  repeat = true;
    private TextView appBarName, appBarSeen;
    private EmojiconEditText Live_Titel;

    private CircleImageView messageImageRight = null;
    LocationTrack locationTrack;
    private LatLng RT_gps_position;
    private Context context;
    private ImageView buttonEmoji;
    private String json_data;
    JSONObject Location_data;
    private boolean already_init = false;
    private boolean Map_Ready = false;

    private FirebaseFirestore mFirestore;
    private FirebaseUser currentUser;
    private String id,friend_name, friend_email, friend_image, friend_token;


    private String User_Id_Sender = ""  , Event = "" ;
    private Double longitude;
    private Double latitude ;
    private JSONObject info_gps_location;
    private io.socket.client.Socket mSocket_gps_location;
    HashMap<String, io.socket.emitter.Emitter.Listener> eventListeners = new HashMap<>();




    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsInitializer.initialize(getApplicationContext());
        setContentView(R.layout.live_location_receiver);
        // live_location_receiver


        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setTitle("");
        context = MoD_Live_Location_receiver_Activity.this;


        locationTrack = new LocationTrack(MoD_Live_Location_receiver_Activity.this);

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.ca_chat_bar, null);

        appBarName = actionBarView.findViewById(R.id.chat_bar_name);
        appBarSeen = actionBarView.findViewById(R.id.chat_bar_seen);
        messageImageRight = actionBarView.findViewById(R.id.icon_image);

        actionBar.setCustomView(actionBarView);

        // init Socket Cluster
        select_backgrond = getResources().getDrawable(R.drawable.rounded_edittext__3);
        normal_select_backgrond = getResources().getDrawable(R.drawable.rounded_edittext);

        // get Value Intent   ...
        Bundle extras = getIntent().getExtras();
        json_data = extras.getString("data");
        if(json_data != null){
            // extract dat json   and start socket   channel

                try {

                    Location_data= new JSONObject(json_data);
                    SocketClusterChannel = Location_data.getString(live_location_sharing_db.CONTACTS_TABLE_MATRITCULE_LIVE);

                } catch (Exception e) {
                    e.printStackTrace();
                }


        }

        // check if  SocketChannel  exist

        if(SocketClusterChannel == null ||  SocketClusterChannel.isEmpty()){
            // something wrong with the Initialisation of your Socket  ...

            Toasty.error(context, "Something with  Live Location   Cluster on Server  !", Toast.LENGTH_SHORT).show();

            try {
                Thread.sleep(2000);
                this.finish();
            }catch (Exception ex){
                ex.printStackTrace();
            }

        }




        // init socket live locationreceiver  ...
        try {
            InitSocketIo();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }




        // location startegies  ...

        mMapView = (MapView) findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }else{
            mMapView.getMapAsync(this);
        }

        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }



        // mMapView.onResume(); // needed to get the map to display immediately

        if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }else{
            mMapView.getMapAsync(this);
        }

        try {
            MapsInitializer.initialize(this.getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        //  action to start  a  configuration (price , personne , ...etc )
        start_aharing = (Button) findViewById(R.id.Share_your_ownlocation);
        start_aharing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // create new Live Trackingobje

                Intent LiveLocationSharing = new Intent(MoD_Live_Location_receiver_Activity.this , MoD_Live_Location_sharing_Activity.class);
                startActivityForResult(LiveLocationSharing, ChatActivity.CodeLiveLocation);
                MoD_Live_Location_receiver_Activity.this.finish();

            }
        });



        Destination_autocompleteFragment = (PlaceAutocompleteFragment)
                this.getFragmentManager().findFragmentById(R.id.fragment_destination);
        Destination_autocompleteFragment.setHint("Destination ...");
        Destination_autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                //destination = place.getName().toString();
            }
            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
            }
        });

        mFirestore = FirebaseFirestore.getInstance();
        live_location = new live_location_sharing_db(MoD_Live_Location_receiver_Activity.this);
        // new MyAsyncTask(MoD_Live_Location_receiver_Activity.this, "live sharing ").execute();
    }



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

            // always with two device  ....
            info_gps_location.put("emitter_user_id", FirebaseAuth.getInstance().getUid());
            info_gps_location.put("room_id", SocketClusterChannel);


            try {

                eventListeners.put("location_listener", onLive_location_Receive);
                setListeningToEvents(true);


            } catch (Exception ex) {
                ex.printStackTrace();
            }

            mSocket_gps_location.emit("room_share_location", info_gps_location);
            System.out.println("receiver  live location from   ");
            //startTimer();



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


    private final io.socket.emitter.Emitter.Listener onLive_location_Receive = new io.socket.emitter.Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject object;
            try {

                object = (JSONObject) args[0];
                System.out.println(object.toString());

                try {

                    User_Id_Sender = object.getString("User_Id_Sender");
                    latitude = object.getDouble("latitude");
                    longitude = object.getDouble("longitude");
                    Event = object.getString("Event");


                    //String Latitude = object.getString("latitude");
                    //String longitude = object.getString("longitude");
                    String id_sender = object.getString("User_Id_Sender");
                    String Event_name = object.getString("Event");
                    id = id_sender;

                    // convertion ...
                    final double latitude_convert = latitude;
                    final double longitude_convert = longitude;

                    try{
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    try {
                                        if(Map_Ready && already_init == false){
                                            InitMarker_andUserInfos();
                                            Map_Ready = true;
                                            // you  don't need to  Init user infos again ...
                                            already_init = true;
                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    if(marker_ok){
                                        LatLng latLng = new LatLng(latitude_convert,longitude_convert);
                                        MyPosition.position(new LatLng(latitude_convert, longitude_convert));
                                        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                                        //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                                        //mMap.addMarker(MyPosition);
                                    }

                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                        //mMap.addMarker(MyPosition);

                    }catch (Exception ex){
                        ex.printStackTrace();
                    }



                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };



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
            Log.i("socket", "sent  receiver");

            //Play_Song_out_message();
            // send  firebase cloud notification
        }
    }


    public void InitMarker_andUserInfos(){
        try {

            userHelper = new UserHelper(MoD_Live_Location_receiver_Activity.this);

            // get users info ....
            // get Users Informations
            mFirestore.collection("Users")
                    .document(id)
                    .get()
                    .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                        @Override
                        public void onSuccess(DocumentSnapshot documentSnapshot) {

                            friend_name=documentSnapshot.getString("name");
                            friend_email=documentSnapshot.getString("email");
                            friend_image=documentSnapshot.getString("image");
                            friend_token=documentSnapshot.getString("token_id");


                            usernam=friend_name;
                            String nam = friend_name;
                            final String imag_link = friend_image;

                            try{

                                if(usernam!=null){
                                    appBarName.setText(nam);
                                    appBarSeen.setText("Live Location GuDana");
                                }

                            }catch (Exception ex){
                                ex.printStackTrace();
                            }

                            // get Gps Position  ..
                            if (locationTrack.canGetLocation()) {
                                RT_gps_position = new LatLng(locationTrack.getLatitude(), locationTrack.getLongitude());
                                // Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();
                            } else {
                                locationTrack.showSettingsAlert();
                            }

                            // try to check if all variable  ist not null ...
                            if(RT_gps_position != null && usernam != null ){
                                Picasso.with(MoD_Live_Location_receiver_Activity.this).load(imag_link).into(target);

                            }
                        }
                    });



        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            MyPosition =  new MarkerOptions()
                    .draggable(true)
                    .title(usernam)
                    .snippet("your realtime position ")
                    //.icon(BitmapDescriptorFactory.fromBitmap( Icon_User))
                    .icon(BitmapDescriptorFactory.fromBitmap(RoundBitmap.getRoundedShape(bitmap,100,100)))
                    .position(RT_gps_position);

            mMap.addMarker(MyPosition);
            repeat = false;
            marker_ok = true; // to tell that the marker was correcttly initialised
            already_init = true;
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            try{

                Bitmap icon = BitmapFactory.decodeResource(getResources(),R.drawable.user);
                MyPosition =  new MarkerOptions()
                        .draggable(true)
                        .title(usernam)
                        .snippet("your realtime position ")
                        //.icon(BitmapDescriptorFactory.fromBitmap( Icon_User))
                        .icon(BitmapDescriptorFactory.fromBitmap(RoundBitmap.getRoundedShape(icon,100,100)))
                        .position(RT_gps_position);
                mMap.addMarker(MyPosition);
                repeat = false;
                marker_ok = true; // to tell that the marker was correcttly initialised
                already_init = true;
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    };


    @Override
    public void onBackPressed() {

        JSONObject object=new JSONObject();
        try {
            object.put("ismessage",false);
            object.put("data","<b><gray>"+Username+"</b></gray> leaved the group");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        super.onBackPressed();
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) // Press Back Icon
        {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }



    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {

        Picasso.with(this).cancelRequest(target);
        super.onDestroy();

        JSONObject object=new JSONObject();
        try {
            object.put("ismessage",false);
            object.put("data","<b><gray>"+Username+"</b></gray> leaved the group");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        }
        buildGoogleApiClient();
        Map_Ready = true;
        // mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
        mMap.setMyLocationEnabled(true);
    }



    protected synchronized void buildGoogleApiClient(){
        mGoogleApiClient = new GoogleApiClient.Builder(MoD_Live_Location_receiver_Activity.this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        if(getApplicationContext()!=null){
            mLastLocation = location;

            try{
                if(marker_ok){
                    // LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());
                    //MyPosition.position(new LatLng(location.getLatitude(), location.getLongitude()));
                    //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                    //mMap.animateCamera(CameraUpdateFactory.zoomTo(11));
                    // mMap.addMarker(MyPosition);
                }

            }catch (Exception ex){
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

        try {

            mLocationRequest = new LocationRequest();
            mLocationRequest.setInterval(1000);
            mLocationRequest.setFastestInterval(1000);
            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
            }
            if(mGoogleApiClient.isConnected()){
                LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);

            }

        }catch(Exception ex){

            ex.printStackTrace();

        }

    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    final int LOCATION_REQUEST_CODE = 1;
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    //mapFragment.getMapAsync(this);


                } else {
                    Toast.makeText(getApplicationContext(), "Please provide the permission", Toast.LENGTH_LONG).show();
                }
                break;
            }
        }
    }


    // to start some task in background
    public class MyAsyncTask extends AsyncTask<Void, Void, String> {

        private Context context;
        private Map<String, String> datal;
        private String url_icon_local;

        MyAsyncTask(Context context, String  Map_Info) {
            this.context = context;
            this.url_icon_local = Map_Info;
        }

        @Override
        protected String doInBackground(Void... params) {
            //StartLiveLocationLoop();
            return  "hello";

        }

        @Override
        protected void onPostExecute(String bitmap) {
            super.onPostExecute(bitmap);


        }
    }


}

