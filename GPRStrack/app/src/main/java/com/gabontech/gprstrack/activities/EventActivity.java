package com.gabontech.gprstrack.activities;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.models.Event;
import com.gabontech.gprstrack.models.HistoryItem;
import com.gabontech.gprstrack.models.HistoryItemClass;
import com.gabontech.gprstrack.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;


import java.util.ArrayList;
import java.util.Objects;

import butterknife.ButterKnife;

public class EventActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap map;
    ImageButton zoomin, zoomout;
    ImageView back;
    ImageButton map_layer_icon;
    TextView actionbar_title;
    RelativeLayout content_layout;
    HistoryItem item;
    ArrayList<HistoryItemClass> historyItemClasses;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event);
        ButterKnife.bind(this);


        zoomin = findViewById(R.id.zoom_in);
        zoomout = findViewById(R.id.zoom_out);
        back = (ImageView) findViewById(R.id.back);
        actionbar_title = findViewById(R.id.actionbar_title);
        map_layer_icon = (ImageButton) findViewById(R.id.map_layer);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        Event event = new Gson().fromJson(getIntent().getStringExtra("event"), Event.class);
        actionbar_title.setText(event.device_name);

    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        map_layer_icon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (map.getMapType() == GoogleMap.MAP_TYPE_NORMAL) {
                    map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    map_layer_icon.setImageResource(R.drawable.map_layer_change_icon_active);
                } else {
                    map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    map_layer_icon.setImageResource(R.drawable.map_layer_change_icon_inactive);
                }
            }
        });

    }

    @SuppressLint("PotentialBehaviorOverride")
    private void setUpMap()
    {
        zoomin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoomout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        Event event = new Gson().fromJson(getIntent().getStringExtra("event"), Event.class);
            LatLng geopoint = new LatLng(Double.valueOf(event.latitude), Double.valueOf(event.longitude));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(geopoint, 14)));

            Drawable dr = getResources().getDrawable(R.drawable.normal_event);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();

            MarkerOptions m = new MarkerOptions();
            m.position(geopoint);
            m.title(event.device_name);
            m.snippet(event.message +""+ "-"+ event.time);
            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, Utils.dpToPx(this, 40), Utils.dpToPx(this, 40), true)));
            Objects.requireNonNull(map.addMarker(m)).showInfoWindow();

    }

}