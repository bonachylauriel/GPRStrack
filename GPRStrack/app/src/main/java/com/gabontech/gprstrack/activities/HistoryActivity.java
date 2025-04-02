package com.gabontech.gprstrack.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.adapters.AwesomeAdapter;
import com.gabontech.gprstrack.api.API;
import com.gabontech.gprstrack.api.ApiInterface;
import com.gabontech.gprstrack.models.Device;
import com.gabontech.gprstrack.models.HistoryItem;
import com.gabontech.gprstrack.models.HistoryItemCoord;
import com.gabontech.gprstrack.models.HistorySensor;
import com.gabontech.gprstrack.models.HistorySensorData;
import com.gabontech.gprstrack.models.PrecalculatedGraphData;
import com.gabontech.gprstrack.utils.DataSaver;
import com.gabontech.gprstrack.utils.Utils;
import com.github.mikephil.charting.data.Entry;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.picasso.Picasso;

import org.osmdroid.util.GeoPoint;

import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;
public class HistoryActivity extends AppCompatActivity implements OnMapReadyCallback {


    private static final String TAG = "HistoryActivity";
    @Bind(R.id.back)
    ImageButton back;
    @Bind(R.id.search)
    ImageButton search;
    @Bind(R.id.searchLayout)
    LinearLayout search_layout;
    @Bind(R.id.startSearch)
    Button startSearch;

    @Bind(R.id.deviceSpinner)
    Spinner deviceSpinner;
    @Bind(R.id.fromDateTextView)
    EditText fromDateTextView;
    @Bind(R.id.toDateTextView)
    EditText toDateTextView;

    // list layout'as
    @Bind(R.id.list_layout)
    RelativeLayout list_layout;
    @Bind(R.id.list_layout_list)
    ListView list_layout_list;

    // map layoutas
    @Bind(R.id.map_layout)
    RelativeLayout map_layout;
    @Bind(R.id.zoom_in)
    ImageView zoom_in;
    @Bind(R.id.zoom_out)
    ImageView zoom_out;


    // stats layout'as
    @Bind(R.id.stats_layout)
    RelativeLayout stats_layout;
    @Bind(R.id.loading_layout)
    RelativeLayout loading_layout;
    @Bind(R.id.nodata_layout)
    RelativeLayout nodata_layout;

    AwesomeAdapter<HistoryItem> historyLogAdapter;
    ArrayList<HistoryItem> historyItems;
    ApiInterface.GetHistoryResult getHistoryResult;
    ArrayList<HistoryItemCoord> historyItemCoords;
    ArrayList<PrecalculatedGraphData> precalculatedGraphDatas;
    HistoryItemCoord itema;

    private GoogleMap map;
    private SimpleDateFormat dateFormat= new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
           //identify buttons and ellements on xml file
        back = (ImageButton) findViewById(R.id.back);
        search = (ImageButton) findViewById(R.id.search);
        fromDateTextView = (EditText) findViewById(R.id.fromDateTextView);
        toDateTextView = (EditText) findViewById(R.id.toDateTextView);
        startSearch = (Button) findViewById(R.id.startSearch);
        list_layout_list = (ListView)findViewById(R.id.list_layout_list);
        search_layout =(LinearLayout)findViewById(R.id.searchLayout);
        deviceSpinner = (Spinner) findViewById(R.id.deviceSpinner);
        list_layout= (RelativeLayout) findViewById(R.id.list_layout);
        map_layout = (RelativeLayout) findViewById(R.id.map_layout);
        zoom_in = (ImageView) findViewById(R.id.zoom_in);
        zoom_out = (ImageView) findViewById(R.id.zoom_out);
        stats_layout =(RelativeLayout) findViewById(R.id.stats_layout);
        loading_layout = (RelativeLayout) findViewById(R.id.loading_layout);
        nodata_layout = (RelativeLayout) findViewById(R.id.nodata_layout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        back.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });

        search.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null)
                    return;
                if (search_layout.getVisibility() != View.VISIBLE) {
                    map_layout.setVisibility(View.GONE);
                    search_layout.setVisibility(View.VISIBLE);
                }else{
                    search_layout.setVisibility(View.GONE);
                    map_layout.setVisibility(View.VISIBLE);
                }
            }
        });

        fromDateTextView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
                showDateTimeDialog(fromDateTextView);
            }
        });


        toDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                showTimeDateDialog(toDateTextView);
            }
        });
          //then we get instance of the request gettings all history related to the selected device
        API.getApiInterface(this).getDevices((String) DataSaver.getInstance(HistoryActivity.this).load("api_key"), getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>()
        {
            @Override
            public void success(ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response)
            {
                ArrayList<Device> totalDevices = new ArrayList<>();
                for (ApiInterface.GetDevicesItem item : getDevicesItems)
                    totalDevices.addAll(item.items);
                final ArrayAdapter<Device> devicesAdapter = new ArrayAdapter<>(HistoryActivity.this, R.layout.spinner_item, totalDevices);
                deviceSpinner.setAdapter(devicesAdapter);
            }
            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(HistoryActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });
                 //on search button clicked to launch the request
        startSearch.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                try
                {
                    Date fromDate = dateFormat.parse(fromDateTextView.getText().toString());
                    String from_date = new SimpleDateFormat("yyyy-MM-dd").format(fromDate);
                    String from_time = new SimpleDateFormat("HH:mm").format(fromDate);

                    Date toDate = dateFormat.parse(toDateTextView.getText().toString());
                    String to_date = new SimpleDateFormat("yyyy-MM-dd").format(toDate);
                    String to_time = new SimpleDateFormat("HH:mm").format(toDate);

                    long diffInMillies = toDate.getTime() - fromDate.getTime();
                    long days = TimeUnit.DAYS.convert(diffInMillies, TimeUnit.MILLISECONDS);

                    if (days > 31)
                    {
                        Toast.makeText(HistoryActivity.this, "Oops! /n Interval Maxi: un mois SVP.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    nodata_layout.setVisibility(View.GONE);
                    list_layout.setVisibility(View.VISIBLE);
                    map_layout.setVisibility(View.GONE);
                    stats_layout.setVisibility(View.GONE);
                    loading_layout.setVisibility(View.VISIBLE);
                    int device_id = ((Device) deviceSpinner.getSelectedItem()).id;

                    API.getApiInterface(HistoryActivity.this).getHistory((String) DataSaver.getInstance(HistoryActivity.this).load("api_key"), getResources().getString(R.string.lang),
                            device_id, from_date, from_time, to_date, to_time, false,
                            new Callback<ApiInterface.GetHistoryResult>()
                            {
                                @Override
                                public void success(ApiInterface.GetHistoryResult result, Response response)
                                {
                                    if (result.items == null)
                                    {
                                        loading_layout.setVisibility(View.INVISIBLE);
                                        list_layout.setVisibility(View.GONE);
                                        map_layout.setVisibility(View.GONE);
                                        nodata_layout.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    if (result.items.size() != 0)
                                    {
                                        loading_layout.setVisibility(View.INVISIBLE);
                                        list_layout.setVisibility(View.VISIBLE);
                                        map_layout.setVisibility(View.VISIBLE);
                                        //search_layout.setVisibility(View.GONE);
                                    } else
                                    {
                                        loading_layout.setVisibility(View.INVISIBLE);
                                        list_layout.setVisibility(View.GONE);
                                        nodata_layout.setVisibility(View.VISIBLE);
                                        return;
                                    }
                                    historyItems = result.items;
                                    search_layout.setVisibility(View.GONE);
                                    getHistoryResult = result;
                                    historyLogAdapter.setArray(getHistoryResult.items);
                                    initMap(result.items);

                                    if (getHistoryResult.sensors == null)
                                        getHistoryResult.sensors = new ArrayList<HistorySensor>();

                                    getHistoryResult.sensors.add(0, new HistorySensor("speed", "Speed", " " + DataSaver.getInstance(HistoryActivity.this).load("unit_of_distance")));
                                    getHistoryResult.sensors.add(1, new HistorySensor("altitude", "Altitude", " " + DataSaver.getInstance(HistoryActivity.this).load("unit_of_altitude")));

                                    historyItemCoords = new ArrayList<>();
                                    for (HistoryItem item : getHistoryResult.items)
                                        historyItemCoords.addAll(item.items);
                                    Collections.sort(historyItemCoords, new Comparator<HistoryItemCoord>()
                                    {
                                        @Override
                                        public int compare(HistoryItemCoord lhs, HistoryItemCoord rhs)
                                        {
                                            long t1 = rhs.getTimestamp();
                                            long t2 = lhs.getTimestamp();
                                            if (t2 > t1)
                                                return 1;
                                            else if (t1 > t2)
                                                return -1;
                                            else
                                                return 0;
                                        }
                                    });

                                    precalculatedGraphDatas = new ArrayList<>();
                                    for (HistorySensor sensor : getHistoryResult.sensors)
                                    {
                                        ArrayList<Float> sensorDataValues = new ArrayList<>();
                                        ArrayList<Long> sensorDataTimestamps = new ArrayList<>();
                                        for (HistoryItemCoord item : historyItemCoords)
                                        {
                                            if (item.sensors_data != null)
                                                for (HistorySensorData data : item.sensors_data)
                                                    if (data.id.equals(sensor.id))
                                                    {
                                                        sensorDataValues.add(data.value);
                                                        long timestamp = item.getTimestamp();
                                                        sensorDataTimestamps.add(timestamp);
                                                    }
                                        }

                                        PrecalculatedGraphData object = new PrecalculatedGraphData();
                                        object.sensor_id = sensor.id;
                                        object.sensorDataValues = sensorDataValues;
                                        object.sensorDataTimestamps = sensorDataTimestamps;

                                        ArrayList<String> xVals = new ArrayList<>();
                                        for (int i = 0; i < sensorDataTimestamps.size(); i++)
                                            xVals.add(new SimpleDateFormat("MM-dd HH:mm:ss").format(sensorDataTimestamps.get(i)));
                                        object.xVals = xVals;

                                        ArrayList<Entry> yVals = new ArrayList<>();
                                        for (int i = 0; i < sensorDataValues.size(); i++)
                                            yVals.add(new Entry(sensorDataValues.get(i), i));
                                        object.yVals = yVals;

                                        precalculatedGraphDatas.add(object);
                                    }

                                    loading_layout.setVisibility(View.GONE);
                                }

                                @Override
                                public void failure(RetrofitError retrofitError)
                                {
                                    loading_layout.setVisibility(View.GONE);
                                    Log.d(TAG, "get history failure: " + retrofitError.getMessage());
                                    if (retrofitError.getKind() == RetrofitError.Kind.UNEXPECTED)
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.tooMuchData, Toast.LENGTH_LONG).show();
                                    }
                                    else if(retrofitError.getKind() == RetrofitError.Kind.NETWORK)
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.networkError, Toast.LENGTH_LONG).show();
                                    }
                                    else if (retrofitError.getResponse() != null && retrofitError.getResponse().getStatus() == 403)
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.dontHavePermission, Toast.LENGTH_SHORT).show();
                                    } else
                                    {
                                        Toast.makeText(HistoryActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        });
        // History log list
        historyLogAdapter = new AwesomeAdapter<HistoryItem>(this)
        {
            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                if (convertView == null)
                    convertView = getLayoutInflater().inflate(R.layout.adapter_historylog, null);
                HistoryItem item = getItem(position);
                TextView device_name = (TextView) convertView.findViewById(R.id.device_name);
                device_name.setText(getHistoryResult.device.name);
                TextView date = (TextView) convertView.findViewById(R.id.date);

                TextView text_speed = (TextView) convertView.findViewById(R.id.text_speed);
                text_speed.setText(getHistoryResult.top_speed);

                TextView text_distance = (TextView) convertView.findViewById(R.id.distance_text);
                text_distance.setText(getHistoryResult.distance_sum);

                TextView time_made = (TextView) convertView.findViewById(R.id.time_made);
                time_made.setText(item.time);

                String dateText = item.raw_time;
                TextView hint = (TextView) convertView.findViewById(R.id.hint);
                String hintString = item.getHint(getHistoryResult.item_class);
                switch (hintString){
                    case "drive":
                        dateText += " ";
                        hint.setText(R.string.driving);
                        break;
                    case "stop":
                        dateText += " ";
                        hint.setText(R.string.stopped);
                        break;
                    case "start":
                        dateText += " ";
                        hint.setText(R.string.route_begin);
                        break;
                    case "end":
                        dateText += " ";
                        hint.setText(R.string.route_end);

                        break;
                    case "event":
                        dateText += " ";
                        hint.setText(R.string.event);
                        break;
                }
                String dteTxte = item.raw_time;
                DateFormat iputFormat = new SimpleDateFormat("yyyy-MM-dd");
                try{

                    Date dte =iputFormat.parse(dteTxte);
                    date.setText(iputFormat.format(dte));
                }catch (ParseException e) {
                    e.printStackTrace();
                }

                TextView timing_time = (TextView) convertView.findViewById(R.id.timing_path);
                DateFormat outputFormat = new SimpleDateFormat("HH:mm");
                DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String datTxt = item.raw_time;
                try {
                    Date dat = inputFormat.parse(datTxt);
                    timing_time.setText(outputFormat.format(dat));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


                ImageView icon = (ImageView) convertView.findViewById(R.id.icon);
                Picasso.with(HistoryActivity.this).load(item.getImageUrl(getHistoryResult.images)).into(icon);
                return convertView;
            }
        };
        //set adapter on listview
        list_layout_list.setAdapter(historyLogAdapter);

         // when item selected by item on history list devices
        list_layout_list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                HistoryItem item = (HistoryItem) list_layout_list.getItemAtPosition((int) id);
                String hintString = item.getHint(getHistoryResult.item_class);
                MarkerOptions mo = new MarkerOptions();
                mo.position(new LatLng(Double.parseDouble(item.items.get(0).lat), Double.parseDouble(item.items.get(0).lng)));
                mo.title(item.items.get(0).lat + ", " + item.items.get(0).lng);
                Marker m = map.addMarker(mo);
                m.showInfoWindow();
                map.addMarker(mo);
                try {/*ici pour faire dessiner les directions par intervalles de temps de parcours*/
                    if ("start".equals(hintString)) {

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mo.getPosition(), 15));
                        final List<GeoPoint> points = new ArrayList<>();
                        long previousCoordTime = historyItems.get(0).getTimestamp();
                        int loopId = 0;

                            if (loopId == 0 || (loopId > 0 && previousCoordTime != item.getTimestamp()))
                            {
                                GeoPoint point = new GeoPoint(Double.parseDouble(item.items.get(0).lat), Double.parseDouble(item.items.get(0).lng));
                                points.add(point);
                            }
                            previousCoordTime = item.getTimestamp();
                            loopId++;

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#819afc"));
                        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
                        for (GeoPoint point : points)
                        {

                            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));

                        }
                        map.addPolyline(polylineOptions);
                    } else if ("stop".equals(hintString)) {

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mo.getPosition(), 15));
                        final List<GeoPoint> points = new ArrayList<>();
                        long previousCoordTime = historyItems.get(0).getTimestamp();
                        int loopId = 0;

                            if (loopId == 0 || (loopId > 0 && previousCoordTime != item.getTimestamp()))
                            {
                                GeoPoint point = new GeoPoint(Double.parseDouble(item.items.get(0).lat), Double.parseDouble(item.items.get(0).lng));
                                points.add(point);
                            }
                            previousCoordTime = item.getTimestamp();
                            loopId++;

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#819afc"));
                        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
                        for (GeoPoint point : points)
                        {
                            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
                        map.addPolyline(polylineOptions);
                    } else if ("end".equals(hintString)) {

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mo.getPosition(), 15));
                        final List<GeoPoint> points = new ArrayList<>();
                        long previousCoordTime = historyItems.get(0).getTimestamp();
                        int loopId = 0;

                            if (loopId == 0 || (loopId > 0 && previousCoordTime != item.getTimestamp()))
                            {
                                GeoPoint point = new GeoPoint(Double.parseDouble(item.items.get(0).lat), Double.parseDouble(item.items.get(0).lng));
                                points.add(point);
                            }
                            previousCoordTime = item.getTimestamp();
                            loopId++;

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#819afc"));
                        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
                        for (GeoPoint point : points)
                        {

                            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));

                        }
                        map.addPolyline(polylineOptions);
                    } else if ("event".equals(hintString)) {

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mo.getPosition(), 15));
                        final List<GeoPoint> points = new ArrayList<>();
                        long previousCoordTime = historyItems.get(0).getTimestamp();
                        int loopId = 0;

                            if (loopId == 0 || (loopId > 0 && previousCoordTime != item.getTimestamp()))
                            {
                                GeoPoint point = new GeoPoint(Double.parseDouble(item.items.get(0).lat), Double.parseDouble(item.items.get(0).lng));
                                points.add(point);
                            }
                            previousCoordTime = item.getTimestamp();
                            loopId++;

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#819afc"));
                        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
                        for (GeoPoint point : points)
                        {

                            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));

                        }
                        map.addPolyline(polylineOptions);
                    } else if ("drive".equals(hintString)) {

                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(mo.getPosition(), 15));
                        final List<GeoPoint> points = new ArrayList<>();
                        long previousCoordTime = historyItems.get(0).getTimestamp();
                        int loopId = 0;

                            if (loopId == 0 || (loopId > 0 && previousCoordTime != item.getTimestamp()))
                            {
                                GeoPoint point = new GeoPoint(Double.parseDouble(item.items.get(0).lat), Double.parseDouble(item.items.get(0).lng));
                                points.add(point);
                            }
                            previousCoordTime = item.getTimestamp();
                            loopId++;

                        PolylineOptions polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.parseColor("#819afc"));
                        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
                        for (GeoPoint point : points)
                        {
                            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
                        }
                        map.addPolyline(polylineOptions);
                    }
                } catch (OutOfMemoryError outOfMemoryError) {
                    Toast.makeText(HistoryActivity.this, "Eviter de choisir trop de lignes", Toast.LENGTH_LONG).show();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
             //map clicked otherwise set map view visible
        View.OnClickListener mapClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                list_layout.setVisibility(View.GONE);
                stats_layout.setVisibility(View.GONE);
                map_layout.setVisibility(View.VISIBLE);

                new Handler().post(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        for (HistoryItemCoord coord : historyItemCoords)
                            builder.include(new LatLng(Double.parseDouble(coord.lat), Double.parseDouble(coord.lng)));
                        map.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 12));
                    }
                });
            }
        };
                //history log view 
        View.OnClickListener historyLogClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                map_layout.setVisibility(View.GONE);
                stats_layout.setVisibility(View.GONE);
                list_layout.setVisibility(View.VISIBLE);
            }
        };
        
        //get stat view displayed or masked;

        View.OnClickListener statisticsClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (getHistoryResult == null) return;
                map_layout.setVisibility(View.GONE);
                stats_layout.setVisibility(View.VISIBLE);
                list_layout.setVisibility(View.GONE);
            }
        };
        //list_layout_statistics.setOnClickListener(statisticsClick);
        //list_layout_statistics2.setOnClickListener(statisticsClick);


        zoom_in.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomIn());
            }
        });
        zoom_out.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                map.animateCamera(CameraUpdateFactory.zoomOut());
            }
        });
    }
    private void showTimeDateDialog(EditText toDateTextView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                        calendar.set(Calendar.MINUTE, minute);

                        toDateTextView.setText(dateFormat.format(calendar.getTime()));
                    }
                };
                new TimePickerDialog(HistoryActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
            }
        };
        new DatePickerDialog(HistoryActivity.this, dateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void showDateTimeDialog(EditText fromDateTextView) {
        Calendar calendar = Calendar.getInstance();
        DatePickerDialog.OnDateSetListener dateSetListener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR,year);
                calendar.set(Calendar.MONTH,month);
                calendar.set(Calendar.DAY_OF_MONTH,dayOfMonth);

                TimePickerDialog.OnTimeSetListener timeSetListener=new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
                        calendar.set(Calendar.HOUR_OF_DAY,hourOfDay);
                        calendar.set(Calendar.MINUTE,minute);

                        fromDateTextView.setText(dateFormat.format(calendar.getTime()));
                    }
                };
                new TimePickerDialog(HistoryActivity.this,timeSetListener,calendar.get(Calendar.HOUR_OF_DAY),calendar.get(Calendar.MINUTE),true).show();
            }
        };
        new DatePickerDialog(HistoryActivity.this, dateSetListener, calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }



    @SuppressLint("StaticFieldLeak")
    private void initMap(final ArrayList<HistoryItem> items)
    {
        map.clear();
        ArrayList<HistoryItemCoord> historyItemCoords = new ArrayList<>();
        for (HistoryItem item : items)
        {
            for (HistoryItemCoord coord : item.items)
            {
                historyItemCoords.add(coord);
            }
        }
        Collections.sort(historyItemCoords, new Comparator<HistoryItemCoord>()
        {
            @Override
            public int compare(HistoryItemCoord lhs, HistoryItemCoord rhs)
            {
                long t1 = lhs.getTimestamp();
                long t2 = rhs.getTimestamp();
                if (t2 > t1)
                    return 1;
                else if (t1 > t2)
                    return -1;
                else
                    return 0;
            }
        });
        final List<GeoPoint> points = new ArrayList<>();

        Collections.sort(historyItemCoords, new Comparator<HistoryItemCoord>()
        {
            @Override
            public int compare(HistoryItemCoord lhs, HistoryItemCoord rhs)
            {
                if (lhs.getTimestamp() == rhs.getTimestamp())
                    return 0;
                else if (lhs.getTimestamp() < rhs.getTimestamp())
                    return -1;
                return 1;
            }
        });

        long previousCoordTime = historyItemCoords.get(0).getTimestamp();
        int loopId = 0;
        for (HistoryItemCoord coord : historyItemCoords)
        {
            if (loopId == 0 || (loopId > 0 && previousCoordTime != coord.getTimestamp()))
            {
                GeoPoint point = new GeoPoint(Double.parseDouble(coord.lat), Double.parseDouble(coord.lng));
                points.add(point);
            }
            previousCoordTime = coord.getTimestamp();
            loopId++;
        }
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.color(Color.parseColor("#819afc"));
        polylineOptions.width(Utils.dpToPx(HistoryActivity.this, 3));
        for (GeoPoint point : points)
        {

            polylineOptions.add(new LatLng(point.getLatitude(), point.getLongitude()));
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(point.getLatitude(), point.getLongitude()),14));
        }
        map.addPolyline(polylineOptions);
        // Create markers
        new AsyncTask<Void, Void, Void>()
        {
            ArrayList<MarkerOptions> items;

            @Override
            protected Void doInBackground(Void... params)
            {
                int markerSize = Utils.dpToPx(HistoryActivity.this, 25);
                items = new ArrayList<>();
                for (HistoryItem item : getHistoryResult.items)
                {
                    if (item.status == 1) continue;
                    try
                    {
                        Bitmap bmp = BitmapFactory.decodeStream(new URL(item.getImageUrl(getHistoryResult.images)).openConnection().getInputStream());

                        int srcWidth = bmp.getWidth();
                        int srcHeight = bmp.getHeight();

                        int maxWidth = markerSize;
                        int maxHeight = markerSize;

                        float ratio = Math.min((float) maxWidth / (float) srcWidth, (float) maxHeight / (float) srcHeight);
                        int dstWidth = (int) (srcWidth * ratio);
                        int dstHeight = (int) (srcHeight * ratio);

                        bmp = bmp.createScaledBitmap(bmp, markerSize, markerSize, true);
                        MarkerOptions opt = new MarkerOptions();
                        opt.position(new LatLng(Float.valueOf(item.items.get(0).lat), Float.valueOf(item.items.get(0).lng)));
                        opt.icon(BitmapDescriptorFactory.fromBitmap(Bitmap.createScaledBitmap(bmp, dstWidth, dstHeight, true)));
                        items.add(opt);

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
                for (MarkerOptions opt : items)
                    map.addMarker(opt);

                Log.d(TAG, "onPostExecute: icons downloaded and added to map, total markers: " + items.size());
            }
        }.execute();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap  googleMap)
    {
        map = googleMap;
        map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

    }

}