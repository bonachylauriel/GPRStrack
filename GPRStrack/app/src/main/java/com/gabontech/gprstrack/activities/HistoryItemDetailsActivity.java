package com.gabontech.gprstrack.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.adapters.AwesomeAdapter;
import com.gabontech.gprstrack.models.Device;
import com.gabontech.gprstrack.models.HistoryItem;
import com.gabontech.gprstrack.models.HistoryItemClass;
import com.gabontech.gprstrack.models.HistoryItemCoord;
import com.gabontech.gprstrack.models.HistoryItemImage;
import com.gabontech.gprstrack.models.HistorySensorData;
import com.gabontech.gprstrack.utils.Utils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HistoryItemDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {


    @Bind(R.id.listview)
    ListView listview;
    @Bind(R.id.zoom_in)
    ImageView zoom_in;
    @Bind(R.id.zoom_out)
    ImageView zoom_out;
    GoogleMap map;
    HistoryItem item;
    Device device;
    ArrayList<HistoryItemClass> historyItemClasses;
    private SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_item_details);
        ButterKnife.bind(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        listview = (ListView) findViewById(R.id.listview);
         zoom_in = (ImageView) findViewById(R.id.zoom_in);
         zoom_out = (ImageView) findViewById(R.id.zoom_out);

        item = new Gson().fromJson(getIntent().getStringExtra("item"), HistoryItem.class);
        device = new Gson().fromJson(getIntent().getStringExtra("device"), Device.class);
        historyItemClasses = new Gson().fromJson(getIntent().getStringExtra("historyItemClasses"), new TypeToken<ArrayList<HistoryItemClass>>(){}.getType());
        ArrayList<HistoryItemImage> historyItemImages = new Gson().fromJson(getIntent().getStringExtra("historyItemImages"), new TypeToken<ArrayList<HistoryItemImage>>(){}.getType());


        ArrayList<Pair> array = new ArrayList<>();
        array.add(new Pair<>(getString(R.string.type)+":", item.getHint(historyItemClasses)));
        array.add(new Pair<>(getString(R.string.time)+":", item.raw_time));

        if(item.items.size() > 0) {
            array.add(new Pair<>(getString(R.string.latitude) + ":", item.items.get(0).lat));
            array.add(new Pair<>(getString(R.string.longitude) + ":", item.items.get(0).lng));
            try {
                Geocoder geocoder = new Geocoder(this);
                List<Address> addresses = geocoder.getFromLocation(Double.valueOf(item.items.get(0).lat), Double.valueOf(item.items.get(0).lng), 1);
                if(addresses.size() > 0)
                {
                    array.add(new Pair<>(getString(R.string.address) + ":", addresses.get(0).getAddressLine(0)));
                }
            } catch (IOException e) { }
        }

        if(item.driver == null)
            array.add(new Pair<>(getString(R.string.driver) + ":", "-"));
        else
            array.add(new Pair<>(getString(R.string.driver) + ":", item.driver.name));

        float topSpeed = 0;
        for(HistoryItemCoord coord : item.items)
        {
            if (coord.sensors_data == null) {
                topSpeed = coord.speed;
            }
            else {
                for(HistorySensorData sensor : coord.sensors_data)
                    if(sensor.id.equals("speed"))
                        if(sensor.value > topSpeed)
                            topSpeed = sensor.value;
            }
        }
        array.add(new Pair<>(getString(R.string.topSpeed) + ":", topSpeed));

        float topAltitude = 0;
        for(HistoryItemCoord coord : item.items)
        {
            if (coord.sensors_data != null) {
                for(HistorySensorData sensor : coord.sensors_data)
                    if(sensor.id.equals("altitude"))
                        if(sensor.value > topAltitude)
                            topAltitude = sensor.value;
            }
        }
        array.add(new Pair<>(getString(R.string.topAltitude) + ":", topAltitude));

        listview.setAdapter(new AwesomeAdapter<Pair>(this, array)
        {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if(convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_info_list, null);
                Pair item = getItem(position);
                TextView left = (TextView) convertView.findViewById(R.id.left);
                TextView right = (TextView) convertView.findViewById(R.id.right);

                left.setText(item.first != null ? String.valueOf(item.first) : "");
                right.setText(item.second != null ? String.valueOf(item.second) : "");
                return convertView;
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        map = googleMap;
        setUpMap();
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
    }

    private void setUpMap()
    {
        zoom_in.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });

        if(item.items.size() > 0) {
            LatLng geopoint = new LatLng(Double.valueOf(item.items.get(0).lat), Double.valueOf(item.items.get(0).lng));
            map.animateCamera(CameraUpdateFactory.newCameraPosition(CameraPosition.fromLatLngZoom(geopoint, 14)));

            Drawable dr = getResources().getDrawable(R.drawable.icon_offline);
            Bitmap bitmap = ((BitmapDrawable) dr).getBitmap();

            MarkerOptions m = new MarkerOptions();
            m.position(geopoint);
            m.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bitmap, Utils.dpToPx(this, 15), Utils.dpToPx(this, 15), true)));

            map.addMarker(m);


            if(item.getHint(historyItemClasses).equals("online")) {
                PolylineOptions polylineOptions = new PolylineOptions();
                polylineOptions.color(Color.parseColor("#819afc"));
                polylineOptions.width(Utils.dpToPx(HistoryItemDetailsActivity.this, 3));

                final LatLngBounds.Builder builder = new LatLngBounds.Builder();
                for (HistoryItemCoord coord : item.items)
                {
                    LatLng latlng = new LatLng(Double.valueOf(coord.lat), Double.valueOf(coord.lng));
                    polylineOptions.add(latlng);
                    builder.include(latlng);
                }
                map.addPolyline(polylineOptions);
                listview.setVisibility(View.VISIBLE);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Utils.dpToPx(HistoryItemDetailsActivity.this, 5)));
                    }
                }, 100);
            }
        }
    }
}