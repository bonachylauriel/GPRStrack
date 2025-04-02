package com.gabontech.gprstrack.activities;

import static java.lang.Double.parseDouble;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.Filter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.adapters.AwesomeAdapter;
import com.gabontech.gprstrack.adapters.EventsAdapter;
import com.gabontech.gprstrack.api.API;
import com.gabontech.gprstrack.api.ApiInterface;
import com.gabontech.gprstrack.api.SharedPrefManager;
import com.gabontech.gprstrack.models.Device;
import com.gabontech.gprstrack.models.DeviceIcon;
import com.gabontech.gprstrack.models.Geofence;
import com.gabontech.gprstrack.models.HistoryItem;
import com.gabontech.gprstrack.models.HistoryItemCoord;
import com.gabontech.gprstrack.models.PolygonWithName;
import com.gabontech.gprstrack.models.Sensor;
import com.gabontech.gprstrack.models.TailItem;
import com.gabontech.gprstrack.utils.DataSaver;
import com.gabontech.gprstrack.utils.Utils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.perf.session.SessionManager;
import com.google.gson.Gson;
import com.google.maps.android.clustering.ClusterManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.osmdroid.util.GeoPoint;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class MapActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener, OnMapReadyCallback {

    DrawerLayout drawerLayouto;
    TextView id_account;
    //private final int lastExpandedPosition = -1;
    private EditText searchObject;
    private ObjectsAdapter adapt;
    RelativeLayout layout_balises, layout_evennements, rien_a_voir, rien_a_voir2;
    FloatingActionsMenu mainfloating;
    public FloatingActionButton main_mapactivity,disconnect, historique, reglageable,floatingButtonLeft,floatingButtonRight;
    ListView listview_evennements;
    ExpandableListView expandable_listview_balises;
    NavigationView navigationViewRight, navigationViewLeft;
    private static final String TAG = "MapActivity";
    private GoogleMap map;
    ImageButton zoomin,zoomout,maptrafic,showtail,showgeofence;
    ImageButton map_layer_icon, maplayer;
    private Timer timer;
    RelativeLayout content_layout, nodata_layout;
    private int autoZoomedTimes = 0;
    private HashMap<Integer, Marker> deviceIdMarkers;
    private HashMap<String, Device> markerIdDevices;
    private HashMap<Integer, Polyline> deviceIdPolyline;
    private HashMap<Integer, LatLng> deviceIdLastLatLng;
    // private HashMap<Integer, Marker> deviceIdSmallMarkerInfo;
    private long lastRefreshTime;
    boolean isAutoZoomEnabled = false;
    boolean isTrafficEnabled = true;
    boolean isShowTailsEnabled = true;
    boolean isShowGeofencesEnabled = true;
    private String stopTime;
    private AsyncTask downloadingAsync;
    private boolean isRefreshLoced = false;
    private boolean isMaptraficactived = true;
    ApiInterface.GetGeofencesResult geofencesResult;
    ArrayList<PolygonWithName> polygonsWithDetails = new ArrayList<>();
    float previousZoomLevel = 0;
    AwesomeAdapter<HistoryItem> historyLogAdapter;
    ArrayList<HistoryItem> historyItems;
    ApiInterface.GetHistoryResult getHistoryResult;
    ArrayList<HistoryItemCoord> historyItemCoords;
    private Object SphericalUtil;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);

        //when user is connected then
        if(DataSaver.getInstance(this).load("api_key") == null)
        {
            startActivity(new Intent(MapActivity.this, LoginActivity.class));
            finish();
            return;
        }

        //fin de declaration de boutons
        deviceIdMarkers = new HashMap<>();
        markerIdDevices = new HashMap<>();
        deviceIdPolyline = new HashMap<>();
        deviceIdLastLatLng = new HashMap<>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        zoomin = findViewById(R.id.zoom_in);
        zoomout = findViewById(R.id.zoom_out);
        showtail = findViewById(R.id.showtails);
        showgeofence = findViewById(R.id.geofences);
        maplayer = findViewById(R.id.map_layer);
        //nodata_layout = (RelativeLayout) findViewById(R.id.nodata_layout);
        content_layout = (RelativeLayout) findViewById(R.id.content_layout);
        map_layer_icon = (ImageButton) findViewById(R.id.map_layer);
        maptrafic = (ImageButton) findViewById(R.id.map_trafic);

        listview_evennements = (ListView) findViewById(R.id.listview_evennt);
        expandable_listview_balises = (ExpandableListView) findViewById(R.id.expandable_listview_balises52);
        mainfloating = findViewById(R.id.mainFloatingBtn);
        disconnect = (FloatingActionButton)findViewById(R.id.disconnect);
        main_mapactivity =(FloatingActionButton)findViewById(R.id.main_mapactivity);
        historique = findViewById(R.id.historiques);
        reglageable = findViewById(R.id.reglages);
        searchObject = (EditText) findViewById(R.id.searchObject);
        rien_a_voir = (RelativeLayout) findViewById(R.id.rien_a_voir);
        rien_a_voir2 = (RelativeLayout) findViewById(R.id.rien_a_voir2);
        id_account = (TextView) findViewById(R.id.id_account);
        ImageButton backRight = (ImageButton) findViewById(R.id.backRigt);
        ImageView backLeft = (ImageView) findViewById(R.id.backLeft);
        layout_evennements = (RelativeLayout) findViewById(R.id.layout_evennements);
        layout_balises = (RelativeLayout) findViewById(R.id.layout_balises);


        navigationViewLeft = (NavigationView) findViewById(R.id.navigationViewLeft);
        navigationViewLeft.setNavigationItemSelectedListener(this);
         navigationViewRight = (NavigationView) findViewById(R.id.navigationViewRight);
        navigationViewRight.setNavigationItemSelectedListener(this);
        navigationViewRight.bringToFront();
        navigationViewLeft.bringToFront();
        //ouverture des portes drawers
        onSetNavigationDrawerEvent();
        //fin de declaration de boutons
        API.getApiInterface(this).getMyAccountData((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), new Callback<ApiInterface.GetMyAccountDataResult>() {
            @Override
            public void success(ApiInterface.GetMyAccountDataResult dataResult, Response response)
            {
                id_account.setText(dataResult.email);
            }
            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(MapActivity.this,"probleme d'internet", Toast.LENGTH_SHORT).show();
            }
        });

        final String api_key = (String) DataSaver.getInstance(MapActivity.this).load("api_key");
        final EventsAdapter adapter = new EventsAdapter(this);
        listview_evennements.setAdapter(adapter);

        //affichage des evennements
        API.getApiInterface(this).getEvents(api_key, getResources().getString(R.string.lang), 0, new Callback<ApiInterface.GetEventsResult>() {
            @Override
            public void success(ApiInterface.GetEventsResult result, Response response)
            {
                adapter.setArray(result.items.data);
                if(result.items.data.size() != 0) {
                    layout_evennements.setVisibility(View.VISIBLE);
                }else {
                    rien_a_voir.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void failure(RetrofitError retrofitError) {
                rien_a_voir.setVisibility(View.VISIBLE);
                layout_evennements.setVisibility(View.GONE);
            }
        });

        refreshObjects();
        maptrafic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isTrafficEnabled = !isTrafficEnabled;
                if (isTrafficEnabled){
                    map.setTrafficEnabled(true);
                    maptrafic.setImageResource(R.drawable.trafficmap_on);
                    //Toast.makeText(MapActivity.this, "Couche traffic activée", Toast.LENGTH_SHORT).show();
                }else{
                    map.setTrafficEnabled(false);
                    maptrafic.setImageResource(R.drawable.trafficmap_off);
                    // Toast.makeText(MapActivity.this, "Couche traffic activée", Toast.LENGTH_SHORT).show();
                }
            }
        });

        showtail.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                isShowTailsEnabled = !isShowTailsEnabled;
                if (isShowTailsEnabled)
                {
                    showtail.setImageResource(R.drawable.tail_active);
                    for (Polyline polyline : deviceIdPolyline.values())
                        polyline.setVisible(true);
                } else
                {
                    showtail.setImageResource(R.drawable.tail_inactive);
                    for (Polyline polyline : deviceIdPolyline.values())
                        polyline.setVisible(false);
                }
            }
        });

        showgeofence.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (isShowGeofencesEnabled)
                {
                    showgeofence.setImageResource(R.drawable.geofence_inactive);
                    for (PolygonWithName polygon : polygonsWithDetails)
                    {
                        polygon.getMarker().setVisible(false);
                        polygon.getPolygon().setVisible(false);
                    }
                    isShowGeofencesEnabled = false;
                } else
                {
                    showgeofence.setImageResource(R.drawable.geofence_active);
                    for (PolygonWithName polygon : polygonsWithDetails)
                    {
                        polygon.getMarker().setVisible(true);
                        polygon.getPolygon().setVisible(true);
                    }
                    isShowGeofencesEnabled = true;
                }
            }
        });

        listview_evennements.setOnScrollListener(new AbsListView.OnScrollListener(){
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState){
                if(scrollState == SCROLL_STATE_IDLE){
                    listview_evennements.bringToFront();
                    layout_evennements.requestLayout();
                }
            }
            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        //recherche des balises
        searchObject.addTextChangedListener(new TextWatcher(){
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                searchObject.setSingleLine(true);
                String text = searchObject.getText().toString();
                adapt.getFilter().filter(text);
            }
        });

        listview_evennements.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int notificationId = new Random().nextInt(100);
                String channelId = "notification_channel_2";

                NotificationManager notificationManager =
                        (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                Intent intent = new Intent(MapActivity.this, EventActivity.class);
                intent.putExtra("event", new Gson().toJson(listview_evennements.getItemAtPosition(position)));
                startActivity(intent);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(),
                        0, intent,PendingIntent.FLAG_CANCEL_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(
                        getApplicationContext(), channelId
                );
                builder.setSmallIcon(R.drawable.dashboard_logo);
                builder.setDefaults(NotificationCompat.DEFAULT_ALL);
                builder.setContentTitle("GPRSTrack");
                builder.setContentText("Evennements disponible ");
                builder.setContentIntent(pendingIntent);
                builder.setAutoCancel(true);
                builder.setPriority(NotificationCompat.PRIORITY_MAX);

                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                    if(notificationManager != null && notificationManager.getNotificationChannel(channelId) ==null){
                        NotificationChannel notificationChannel = new NotificationChannel(
                                channelId, "Notification channel 2", NotificationManager.IMPORTANCE_HIGH
                        );
                        notificationChannel.setDescription("Ceci est pour notifier un nouvel Evennement");
                        notificationChannel.enableLights(true);
                        notificationChannel.enableVibration(true);
                        notificationManager.createNotificationChannel(notificationChannel);
                    }
                }
                Notification notification = builder.build();
                if (notificationManager != null) {
                    notificationManager.notify(notificationId, notification);
                }
                drawerLayouto.closeDrawer(navigationViewRight, true);
            }
        });

        expandable_listview_balises.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
                parent.setItemChecked(index, true);
                overridePendingTransition(0, 0);
                return false;
            }
        });

        main_mapactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapActivity.this, MapActivity.class));
                overridePendingTransition(0,0);
                finish();
            }
        });

        expandable_listview_balises.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return false;
            }
        });

        historique.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapActivity.this, HistoryActivity.class));
            }
        });

        reglageable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapActivity.this, SettingsActivity.class));
            }
        });

        disconnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logoutuser();

            }
        });

        backRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        backLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                drawerLayouto.closeDrawer(navigationViewLeft, true);
            }
        });

    }

    private void logoutuser() {
        Toast.makeText(MapActivity.this, "Vous êtes déconnecté", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MapActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }


    @Override
    protected void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        float timeleft = 10 - Math.round(System.currentTimeMillis() - lastRefreshTime) / 1000f;
                        if (timeleft < 0)
                            timeleft = 0;
                        if (System.currentTimeMillis() - lastRefreshTime >= 10 * 1000)
                            if (map != null)
                                refresh();
                    }
                });
            }
        }, 0, 1000);
        refreshObjects();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        try
        {
            timer.cancel();
            timer.purge();
            downloadingAsync.cancel(true);
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    protected void onStart() {
        super.onStart();
        refreshObjects();
        refresh();
    }

    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
    private void decomposeGeofenceCoordinates() {
        for (int i = 0; i < geofencesResult.items.geofences.size(); i++)
        {
            if (geofencesResult.items.geofences.get(i).active == 1)
            {
                String coordinatesJSON = geofencesResult.items.geofences.get(i).coordinates;
                List<LatLng> coordinatesList = new ArrayList<>();
                List<String> coordsListJSON = new ArrayList<String>();
                try
                {
                    JSONArray jsonArray = new JSONArray(coordinatesJSON);
                    for (int j = 0; j < jsonArray.length(); j++)
                    {
                        coordsListJSON.add(jsonArray.getString(j));
                    }
                    //Log.d("coordsListget0", coordsListJSON.get(0));
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }

                Log.d("coordinates", coordinatesJSON);
                for (String item : coordsListJSON)
                {
                    double lat = 0;
                    double lng = 0;
                    Pattern patternLat = Pattern.compile("(?:(?!\"lat\":).)\"lat\":([+-]?\\d*\\.?\\d+)");
                    Pattern patternLng = Pattern.compile("(?:(?!\"lng\":).)\"lng\":([+-]?\\d*\\.?\\d+)");

                    Matcher matcherLat = patternLat.matcher(item);
                    Matcher matcherLng = patternLng.matcher(item);
                    while (matcherLat.find())
                        lat = parseDouble(matcherLat.group(1));
                    while (matcherLng.find())
                        lng = parseDouble(matcherLng.group(1));

                    coordinatesList.add(new LatLng(lat, lng));
                    geofencesResult.items.geofences.get(i).coordinatesList = coordinatesList;
                }
            }
        }
    }
    private void refreshObjects() {
        String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getDevices(api_key, getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
            @Override
            public void success(ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response) {
                adapt = new ObjectsAdapter(MapActivity.this, getDevicesItems);
                expandable_listview_balises.setAdapter(adapt);
                int count = adapt.getGroupCount();
                for (int i = 0; i < count; i++){
                    expandable_listview_balises.expandGroup(i);
                }
                Utils.setGroupClickListenerToNotify(expandable_listview_balises, adapt);
                if(getDevicesItems.size() != 0)
                    layout_balises.setVisibility(View.VISIBLE);
                else
                    rien_a_voir2.setVisibility(View.VISIBLE);

            }
            @Override
            public void failure(RetrofitError retrofitError) {
                Log.e("DrawerBaseActivity", "failure: " + retrofitError.getMessage());
                layout_balises.setVisibility(View.GONE);
                rien_a_voir2.setVisibility(View.VISIBLE);
            }
        });

    }


    private void onSetNavigationDrawerEvent() {
        //identification et mise en place des elements par id.
        drawerLayouto = (DrawerLayout) findViewById(R.id.drawerLayouto);
        navigationViewRight = (NavigationView) findViewById(R.id.navigationViewRight);
        navigationViewLeft = (NavigationView) findViewById(R.id.navigationViewLeft);
        floatingButtonRight = (FloatingActionButton) findViewById(R.id.floatingButtonRight);
        floatingButtonLeft = (FloatingActionButton) findViewById(R.id.floatingButtonLeft);
        // action des boutons sur click gauche et droit.
        floatingButtonLeft.setOnClickListener(this);
        floatingButtonRight.setOnClickListener(this);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return true;
    }
    // creation des vues sur les contenues on presses buttons action floating pour navigationdrawer en mode focusX.
    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.floatingButtonRight:
                drawerLayouto.openDrawer(navigationViewRight, true);
                break;
            case R.id.floatingButtonLeft:
                drawerLayouto.openDrawer(navigationViewLeft, true);
                break;
            default:
                drawerLayouto.closeDrawer(navigationViewLeft, true);
                drawerLayouto.closeDrawer(navigationViewRight, true);
                break;
        }
    }
    // retour apres ouverture des navigation focusX
    @Override
    public void onBackPressed() {
        if (drawerLayouto.isDrawerOpen(navigationViewLeft)) {
            drawerLayouto.closeDrawer(navigationViewLeft, true);
        }else if(drawerLayouto.isDrawerOpen(navigationViewRight)){
            drawerLayouto.closeDrawer(navigationViewRight, true);
        }
        else{
            super.onBackPressed();
        }
    }
    // a l'arret de l'application pour focusX

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;

        refresh();
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        zoomin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        map.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle()
            {
                if (map.getCameraPosition().zoom < 13 && previousZoomLevel > 13)
                {
                    for (PolygonWithName polygon : polygonsWithDetails)
                    {
                        polygon.getMarker().remove();
                    }
                    putGeofenceNameMarkers(10);
                } else if (previousZoomLevel < 13 && (map.getCameraPosition().zoom > 13))
                {
                    for (PolygonWithName polygon : polygonsWithDetails)
                    {
                        polygon.getMarker().remove();
                        polygon.getPolygon().remove();
                    }
                    putGeofenceNameMarkers(30);
                }

                previousZoomLevel = map.getCameraPosition().zoom;
                //Toast.makeText(MapActivity.this, "zoome level is: " + map.getCameraPosition().zoom, Toast.LENGTH_SHORT).show();
            }
        });

        map_layer_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                if(map.getMapType() == GoogleMap.MAP_TYPE_NORMAL){
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    map_layer_icon.setImageResource(R.drawable.map_layer_change_icon_active);
                }
                else {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    map_layer_icon.setImageResource(R.drawable.map_layer_change_icon_inactive);
                }
            }
        });
        zoomout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
        API.getApiInterface(this).getGeofences((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), new Callback<ApiInterface.GetGeofencesResult>()
        {
            @Override
            public void success(ApiInterface.GetGeofencesResult getGeofencesResult, Response response)
            {
                geofencesResult = getGeofencesResult;
                decomposeGeofenceCoordinates();
                putGeofenceNameMarkers(12);
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                if (retrofitError.getResponse().getStatus() == 403)
                {
                    Toast.makeText(MapActivity.this, "Impossible de génerer les geofences", Toast.LENGTH_SHORT).show();
                } else
                {
                    Toast.makeText(MapActivity.this, /*retrofitError.getResponse().getStatus()*/"Error", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void putGeofenceNameMarkers(int textSize) {
        for (Geofence geofence : geofencesResult.items.geofences)
        {
            if (geofence.active == 1)
            {
                String strText = geofence.name;

                Paint.FontMetrics fm = new Paint.FontMetrics();
                Paint paintText = new Paint();
                paintText.setColor(Color.parseColor(geofence.polygon_color));
                paintText.setTextAlign(Paint.Align.CENTER);
                paintText.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                paintText.getFontMetrics(fm);

                Rect rectText = new Rect();
                paintText.getTextBounds(strText, 0, strText.length(),
                        rectText);
                Bitmap.Config conf = Bitmap.Config.ARGB_8888;
                Bitmap bmpText = Bitmap.createBitmap(rectText.width(),
                        rectText.height(), conf);
                Canvas canvas = new Canvas(bmpText);
                canvas.drawText(strText, canvas.getWidth() / 2,
                        canvas.getHeight() - rectText.bottom, paintText);
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(centroid(geofence.coordinatesList))
                        .icon(BitmapDescriptorFactory.fromBitmap(bmpText))
                        .anchor(0.5f, 1);
                Marker marker = map.addMarker(markerOptions);
                polygonsWithDetails.add(new PolygonWithName(map.addPolygon(new PolygonOptions()
                        .addAll(geofence.coordinatesList)
                        .strokeColor(Color.parseColor(geofence.polygon_color))
                        .fillColor(Color.parseColor("#59" + geofence.polygon_color.substring(1))))
                        , paintText, markerOptions, marker, geofence));
            }
        }
    }

    private LatLng centroid(List<LatLng> points)
    {
        double[] centroid = {0.0, 0.0};
        for (int i = 0; i < points.size(); i++)
        {
            centroid[0] += points.get(i).latitude;
            centroid[1] += points.get(i).longitude;
        }

        int totalPoints = points.size();
        centroid[0] = centroid[0] / totalPoints;
        centroid[1] = centroid[1] / totalPoints;

        return new LatLng(centroid[0], centroid[1]);
    }
    private void refresh()
    {

        if (isRefreshLoced)
            return;
        isRefreshLoced = true;
        lastRefreshTime = System.currentTimeMillis();
        final String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getDevices(api_key, getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>()
        {
            @Override
            public void success(final ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
            {
                Log.d(TAG, "success: loaded devices array");
                final ArrayList<Device> allDevices = new ArrayList<>();
                if (getDevicesItems != null)
                    for (ApiInterface.GetDevicesItem item : getDevicesItems)
                        allDevices.addAll(item.items);
                API.getApiInterface(MapActivity.this).getFieldsDataForEditing(api_key, getResources().getString(R.string.lang), 1, new Callback<ApiInterface.GetFieldsDataForEditingResult>()
                {
                    @Override
                    public void success(final ApiInterface.GetFieldsDataForEditingResult getFieldsDataForEditingResult, Response response)
                    {
                        Log.d(TAG, "success: loaded icons");
                        downloadingAsync = new AsyncTask<Void, Void, Void>()
                        {
                            ArrayList<MarkerOptions> markers;
                            ArrayList<Integer> deviceIds;

                            @Override
                            protected Void doInBackground(Void... params)
                            {
                                // add markers
                                int dp100 = Utils.dpToPx(MapActivity.this, 50);
                                markers = new ArrayList<>();
                                deviceIds = new ArrayList<>();
                                if (getFieldsDataForEditingResult == null || getFieldsDataForEditingResult.device_icons == null)
                                    return null;
                                for (Device item : allDevices)
                                {
                                    if (isCancelled())
                                        break;
                                    // ieškom ikonos masyve
                                    DeviceIcon mapIcon = null;
                                    for (DeviceIcon icon : getFieldsDataForEditingResult.device_icons)
                                        if (item.device_data.icon_id == icon.id)
                                            mapIcon = icon;

                                    String server_base = (String) DataSaver.getInstance(MapActivity.this).load("server_base");

                                    try
                                    {
                                        Log.d("MapActivity", "DOWNLOADING BITMAP: " + server_base + mapIcon.path);
                                        //Bitmap bmp = BitmapFactory.decodeStream(new URL(server_base + mapIcon.path).openConnection().getInputStream());
                                        if (item.online.equals("online")) {
                                            Drawable dr = getResources().getDrawable(R.drawable.icon_online);
                                            Bitmap bmp = ((BitmapDrawable) dr).getBitmap();
                                            int srcWidth = bmp.getWidth();
                                            int srcHeight = bmp.getHeight();

                                            int maxWidth = Utils.dpToPx(MapActivity.this, mapIcon.width);
                                            int maxHeight = Utils.dpToPx(MapActivity.this, mapIcon.height);

                                            float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                                            int dstWidth = (int) (srcWidth * ratio);
                                            int dstHeight = (int) (srcHeight * ratio);

                                            bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);
                                            MarkerOptions m = new MarkerOptions();
                                            LatLng ici = new LatLng(item.lat, item.lng);
                                            m.position(ici);
                                            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                                            markers.add(m);
                                            deviceIds.add(item.id);

                                        }
                                        if (item.online.equals("ack")) {
                                            Bitmap bmp = BitmapFactory.decodeStream(new URL(server_base + mapIcon.path).openConnection().getInputStream());
                                            int srcWidth = bmp.getWidth();
                                            int srcHeight = bmp.getHeight();

                                            int maxWidth = Utils.dpToPx(MapActivity.this, mapIcon.width);
                                            int maxHeight = Utils.dpToPx(MapActivity.this, mapIcon.height);

                                            float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                                            int dstWidth = (int) (srcWidth * ratio);
                                            int dstHeight = (int) (srcHeight * ratio);

                                            bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);
                                            MarkerOptions m = new MarkerOptions();
                                            LatLng ici = new LatLng(item.lat, item.lng);
                                            m.position(ici);
                                            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                                            markers.add(m);
                                            deviceIds.add(item.id);
                                        }
                                        if (item.online.equals("offline")) {
                                            Drawable dr = getResources().getDrawable(R.drawable.icon_offline);
                                            Bitmap bmp = ((BitmapDrawable) dr).getBitmap();
                                            int srcWidth = bmp.getWidth();
                                            int srcHeight = bmp.getHeight();

                                            int maxWidth = Utils.dpToPx(MapActivity.this, mapIcon.width);
                                            int maxHeight = Utils.dpToPx(MapActivity.this, mapIcon.height);

                                            float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                                            int dstWidth = (int) (srcWidth * ratio);
                                            int dstHeight = (int) (srcHeight * ratio);

                                            bmp = bmp.createScaledBitmap(bmp, dp100, dp100, true);
                                            MarkerOptions m = new MarkerOptions();
                                            LatLng ici = new LatLng(item.lat, item.lng);
                                            m.position(ici);
                                            m.title(item.name);
                                            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                                            markers.add(m);
                                            deviceIds.add(item.id);
                                        }
                                    } catch (OutOfMemoryError outOfMemoryError)
                                    {
                                        Toast.makeText(MapActivity.this, "Out of memory! Too many devices are selected to be displayed", Toast.LENGTH_LONG).show();
                                    } catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }
                                }
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid)
                            {
                                ArrayList<GeoPoint> points = new ArrayList<>();

                                if (autoZoomedTimes < 1)
                                {
                                    new Handler().postDelayed(new Runnable()
                                    {
                                        @Override
                                        public void run()
                                        {
                                            runOnUiThread(new Runnable()
                                            {
                                                @Override
                                                public void run()
                                                {
                                                    if (markers.size() > 1)
                                                    {
                                                        try
                                                        {
                                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                                            for (MarkerOptions item : markers)
                                                                builder.include(item.getPosition());
                                                            LatLngBounds bounds = builder.build();
                                                            //                                int padding = 0; // offset from edges of the map in pixels
                                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPx(MapActivity.this, 50));
                                                            map.animateCamera(cu);
                                                        } catch (Exception e)
                                                        {

                                                        }
                                                    } else if (markers.size() > 0)
                                                    {
                                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 15));
                                                    }
                                                    autoZoomedTimes++;
                                                }
                                            });
                                        }
                                    }, 50);
                                } else if (isAutoZoomEnabled)
                                {
                                    if (markers.size() > 1)
                                    {
                                        try
                                        {
                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            for (MarkerOptions item : markers)
                                                builder.include(item.getPosition());
                                            LatLngBounds bounds = builder.build();
                                            //                                int padding = 0; // offset from edges of the map in pixels
                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPx(MapActivity.this, 50));
                                            map.animateCamera(cu);
                                        } catch (Exception e)
                                        {

                                        }
                                    } else if (markers.size() > 0)
                                    {
                                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(markers.get(0).getPosition(), 15));
                                    }
                                    autoZoomedTimes++;
                                }

                                Log.d(TAG, "onPostExecute: icons downloaded and added to map, total markers: " + markers.size());

                                // loading_layout.setVisibility(View.GONE);
                                if (markers.size() != 0)
                                    content_layout.setVisibility(View.VISIBLE);
                                else
                                    nodata_layout.setVisibility(View.VISIBLE);

                                for (int i = 0; i < markers.size(); i++) {
                                    MarkerOptions options = markers.get(i);
                                    int deviceId = deviceIds.get(i);

                                    Marker m;

                                    Polyline polyline;
                                    if (deviceIdMarkers.containsKey(deviceId))
                                    {
                                        Log.d("aa", "moving to" + options.getPosition());
                                        deviceIdMarkers.get(deviceId).setPosition(new LatLng(options.getPosition().latitude, options.getPosition().longitude));
                                        m = deviceIdMarkers.get(deviceId);

                                        polyline = deviceIdPolyline.get(deviceId);
                                    } else
                                    {
                                        Log.d("aa", "putting new");
                                        m = map.addMarker(options);
                                        deviceIdMarkers.put(deviceId, m);
                                        polyline = map.addPolyline(new PolylineOptions());
                                        deviceIdPolyline.put(deviceId, polyline);
                                    }

                                    Device thatonedevice = null;
                                    for (Device device : allDevices)
                                        if (device.id == deviceId)
                                            thatonedevice = device;
                                    markerIdDevices.put(m.getId(), thatonedevice);


                                    // update marker rotation based on driving direction
                                    if (thatonedevice != null && deviceIdLastLatLng.containsKey(deviceId))
                                    {
                                        double dirLat = thatonedevice.lat - deviceIdLastLatLng.get(deviceId).latitude;
                                        double dirLng = thatonedevice.lng - deviceIdLastLatLng.get(deviceId).longitude;

                                        m.setRotation((float) Math.toDegrees(Math.atan2(dirLng, dirLat)));
                                    }
                                    deviceIdLastLatLng.put(deviceId, new LatLng(thatonedevice.lat, thatonedevice.lng));

                                    List<LatLng> polylinePoints = new ArrayList<>();
                                    for (TailItem item : thatonedevice.tail)
                                        polylinePoints.add(new LatLng(Double.valueOf(item.lat), Double.valueOf(item.lng)));
                                    polyline.setPoints(polylinePoints);
                                    polyline.setWidth(Utils.dpToPx(MapActivity.this, 2));
                                    polyline.setColor(Color.parseColor(thatonedevice.device_data.tail_color));
                                }


                                // else

                                map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter()
                                {
                                    @Override
                                    public View getInfoWindow(Marker marker)
                                    {
                                        return null;
                                    }

                                    @Override
                                    public View getInfoContents(final Marker marker)
                                    {
                                        synchronized (this)
                                        {

                                        }
                                        final Device device = markerIdDevices.get(marker.getId());
                                        if (device != null)
                                        {
                                            View view = getLayoutInflater().inflate(R.layout.layout_map_infowindow, null);
                                            view.bringToFront();
                                            view.findViewById(R.id.close).setOnClickListener(new View.OnClickListener()
                                            {
                                                @Override
                                                public void onClick(View v)
                                                {
                                                    marker.hideInfoWindow();
                                                }
                                            });
                                            TextView device_name = (TextView) view.findViewById(R.id.device_name);
                                            device_name.setText(device.name);
                                            TextView altitude = (TextView) view.findViewById(R.id.altitude);
                                            altitude.setText(String.valueOf(device.altitude) + " " + device.unit_of_altitude);
                                            TextView time = (TextView) view.findViewById(R.id.time);
                                            time.setText(device.time);
                                            TextView stopTimeView = (TextView) view.findViewById(R.id.stopTime);
                                            stopTimeView.setText(stopTime);
                                            TextView speed = (TextView) view.findViewById(R.id.speed);
                                            speed.setText(device.speed + " " + device.distance_unit_hour);
                                            TextView address = (TextView) view.findViewById(R.id.address);
                                            address.setText(device.address);

                                            final ArrayList<Sensor> showableSensors = new ArrayList<>();
                                            for (Sensor item : device.sensors)
                                                if (item.show_in_popup > 0)
                                                    showableSensors.add(item);

                                            ListView sensors_list = (ListView) view.findViewById(R.id.sensors_list);
                                            sensors_list.setAdapter(new AwesomeAdapter<Sensor>(MapActivity.this)
                                            {
                                                @Override
                                                public int getCount()
                                                {
                                                    return showableSensors.size();
                                                }

                                                @NonNull
                                                @Override
                                                public View getView(int position, View convertView, @NonNull ViewGroup parent)
                                                {
                                                    if (convertView == null)
                                                        convertView = getLayoutInflater().inflate(R.layout.adapter_map_sensorslist, null);

                                                    Sensor item = showableSensors.get(position);
                                                    TextView name = (TextView) convertView.findViewById(R.id.name);
                                                    name.setText(item.name);
                                                    TextView value = (TextView) convertView.findViewById(R.id.value);
                                                    value.setText(item.value);
                                                    return convertView;
                                                }
                                            });

                                            List<Address> addresses;
                                            try
                                            {
                                                addresses = new Geocoder(MapActivity.this).getFromLocation(device.lat, device.lng, 1);
                                                if (addresses.size() > 0)
                                                    address.setText(addresses.get(0).getAddressLine(0));
                                            } catch (IOException e)
                                            {
                                                e.printStackTrace();
                                            }
                                            return view;
                                        }
                                        return null;
                                    }
                                });
                                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener()
                                {
                                    @Override
                                    public boolean onMarkerClick(final Marker marker)
                                    {
                                        int px = Utils.dpToPx(MapActivity.this, 300);
                                        map.setPadding(0, px, 0, 0);
                                        stopTime = "...";
                                        final Device device = markerIdDevices.get(marker.getId());
                                        if (device != null)
                                        {
                                            API.getApiInterface(MapActivity.this).deviceStopTime((String) DataSaver.getInstance(MapActivity.this).load("api_key"), "en", device.id, new Callback<ApiInterface.DeviceStopTimeResult>()
                                            {
                                                @Override
                                                public void success(ApiInterface.DeviceStopTimeResult result, Response response)
                                                {
                                                    stopTime = result.time;
                                                    marker.showInfoWindow();
                                                }

                                                @Override
                                                public void failure(RetrofitError retrofitError)
                                                {
                                                    Toast.makeText(MapActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                                }
                                            });
                                        }

                                        return false;
                                    }
                                });
                                map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener()
                                {
                                    @Override
                                    public void onInfoWindowClick(Marker marker)
                                    {
                                        marker.hideInfoWindow();
                                    }
                                });

                                map.setOnInfoWindowCloseListener(new GoogleMap.OnInfoWindowCloseListener()
                                {
                                    @Override
                                    public void onInfoWindowClose(Marker marker)
                                    {
                                        map.setPadding(0, 0, 0, 0);
                                    }
                                });
                                isRefreshLoced = false;
                            }
                        }.execute();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError)
                    {
                        Toast.makeText(MapActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                        isRefreshLoced = false;
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(MapActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                isRefreshLoced = false;
            }
        });
    }

    public class ObjectsAdapter extends BaseExpandableListAdapter{
        public Context context;
        public ArrayList<ApiInterface.GetDevicesItem> array;
        public LayoutInflater inflater;
        ApiInterface.GetHistoryResult getHistoryResult;

        public ArrayList<ApiInterface.GetDevicesItem> original;

        public ObjectsAdapter(Context context, ArrayList<ApiInterface.GetDevicesItem> array)
        {
            this.context = context;
            this.array = array;
            this.original = array;
            this.inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        @Override
        public int getGroupCount() {
            return array.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            return array.get(groupPosition).items.size();
        }

        @Override
        public ApiInterface.GetDevicesItem getGroup(int groupPosition) {
            return array.get(groupPosition);
        }

        @Override
        public Device getChild(int groupPosition, int childPosition) {
            return array.get(groupPosition).items.get(childPosition);
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
        {

            if(convertView == null)
                convertView = inflater.inflate(R.layout.adapter_expandable_parent, null);
            ApiInterface.GetDevicesItem item = getGroup(groupPosition);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(item.title + " (" + item.items.size() + ")");
            ImageView expand_indicator = (ImageView) convertView.findViewById(R.id.expand_indicator);
            expand_indicator.setImageResource(isExpanded ? R.drawable.expandable_group_arrow_up : R.drawable.expandable_group_arrow_down);

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
        {
            // checkbox setChecked doesn't work, if we don't reinflate it.
            convertView = inflater.inflate(R.layout.adapter_objects_child, null);

            final Device item = getChild(groupPosition, childPosition);
            DataSaver.getInstance(context).save("unit_of_distance", item.distance_unit_hour);
            DataSaver.getInstance(context).save("unit_of_capacity", item.unit_of_capacity);
            DataSaver.getInstance(context).save("unit_of_altitude", item.unit_of_altitude);

            TextView title = (TextView) convertView.findViewById(R.id.title);
            title.setText(item.name);
            TextView date = (TextView) convertView.findViewById(R.id.date);
            if(item.timestamp != 0)
                date.setText(new SimpleDateFormat("yyyy-MM-dd").format(new Date(item.timestamp * 1000)));
            TextView speed = (TextView) convertView.findViewById(R.id.speed);
            speed.setText(item.speed + " " + item.distance_unit_hour);
            TextView altitudeValue = (TextView) convertView.findViewById(R.id.altitudeValue);
            altitudeValue.setText(item.altitude+ " " + DataSaver.getInstance(context).load("unit_of_altitude"));
            TextView protocolValue = (TextView) convertView.findViewById(R.id.protocolValue);
            protocolValue.setText(item.protocol);
            TextView driverValue = (TextView) convertView.findViewById(R.id.driverValue);
            driverValue.setText(item.driver_data.name != null ? item.driver_data.name : "");
            TextView positionValue = (TextView) convertView.findViewById(R.id.positionValue);
            positionValue.setText(item.lat + "° " + item.lng + "°");

            MaterialCheckBox checkbox = (MaterialCheckBox) convertView.findViewById(R.id.checkbox);
            checkbox.setChecked(item.device_data.active == 1);
            checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
                {
                    API.getApiInterface(context).changeActiveDevice((String) DataSaver.getInstance(context).load("api_key"), context.getResources().getString(R.string.lang), item.id, isChecked, new Callback<ApiInterface.ChangeActiveDeviceResult>() {
                        @Override
                        public void success(ApiInterface.ChangeActiveDeviceResult changeActiveDeviceResult, Response response) {
                          if(!isChecked){
                              item.device_data.active = item.device_data.active == 0 ? 1 : 0;
                          }else{
                              checkbox.setChecked(!isChecked);
                          }
                        }
                        @Override
                        public void failure(RetrofitError retrofitError) {
                            Toast.makeText(context, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            checkbox.setChecked(!isChecked);
                        }
                    });
                }
            });

            ImageView gear = (ImageView) convertView.findViewById(R.id.gear);

            gear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showDialogo();
                }
            });

            if(item.online.equals("online"))
            {
                ImageView onlineImageView = (ImageView) convertView.findViewById(R.id.onlineImageView);
                onlineImageView.setImageResource(R.drawable.button_green);
            }
            else if(item.online.equals("offline"))
            {
                ImageView onlineImageView = (ImageView) convertView.findViewById(R.id.onlineImageView);
                onlineImageView.setImageResource(R.drawable.button_redx);
            }
            else if(item.online.equals("ack"))
            {
                ImageView onlineImageView = (ImageView) convertView.findViewById(R.id.onlineImageView);
                onlineImageView.setImageResource(R.drawable.button_yellow);
            }
            // final View additionalLayout = convertView.findViewById(R.id.additionalLayout);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    LatLng ici = new LatLng(item.lat, item.lng);
                    if (item.id != 0)
                  map.animateCamera(CameraUpdateFactory.newLatLngZoom(ici,14));
                    drawerLayouto.closeDrawer(navigationViewLeft, true);
                }
            });
            return convertView;
        }

        private void showDialogo() {

            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(R.layout.bottomsheetlayout);

            TextView send_command = dialog.findViewById(R.id.envoyer_commande);

            dialog.show();
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            dialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
            dialog.getWindow().setGravity(Gravity.BOTTOM);

            send_command.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    context.startActivity(new Intent(context, SendcommandActivity.class));
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }

        ObjectsAdapter.ItemFilter mFilter = new ObjectsAdapter.ItemFilter();
        public Filter getFilter() {
            return mFilter;
        }

        private class ItemFilter extends Filter {
            @Override
            protected FilterResults performFiltering(CharSequence constraint)
            {
                String filterString = constraint.toString().toLowerCase();
                FilterResults results = new FilterResults();
                final ArrayList<ApiInterface.GetDevicesItem> nlist = new ArrayList<>();
                for(ApiInterface.GetDevicesItem item : original)
                    nlist.add(new ApiInterface.GetDevicesItem(item));

                Iterator<ApiInterface.GetDevicesItem> it = nlist.iterator();
                while (it.hasNext())
                {
                    ApiInterface.GetDevicesItem item = it.next();

                    Iterator<Device> it2 = item.items.iterator();
                    while (it2.hasNext())
                    {
                        Device device = it2.next();
                        if(!device.fitForFilter(filterString))
                            it2.remove();
                    }

                    if(item.items.size() == 0)
                        it.remove();
                }
                results.values = nlist;
                results.count = nlist.size();
                return results;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                array = (ArrayList<ApiInterface.GetDevicesItem>) results.values;
                notifyDataSetChanged();
            }
        }
    }

}