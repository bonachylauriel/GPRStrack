package com.gabontech.gprstrack.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.drawerlayout.widget.DrawerLayout;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.adapters.EventsAdapter;
import com.gabontech.gprstrack.adapters.ObjectsAdapter;
import com.gabontech.gprstrack.api.API;
import com.gabontech.gprstrack.api.ApiInterface;
import com.gabontech.gprstrack.utils.DataSaver;
import com.gabontech.gprstrack.utils.Utils;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DrawerBaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    DrawerLayout drawerLayout, drawerLayouto;
    Context context;
    TextView id_account;
    private final int lastExpandedPosition = -1;
    private EditText searchObject;
    private ObjectsAdapter adapt;
    RelativeLayout layout_balises, layout_evennements, rien_a_voir, rien_a_voir2;
    FloatingActionsMenu mainfloating;
    public FloatingActionButton main_mapactivity, historique, reglageable,floatingButtonLeft,floatingButtonRight;
    ListView listview_evennements;
    ExpandableListView expandable_listview_balises;
    NavigationView navigationViewRight, navigationViewLeft;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer_base);

        listview_evennements = (ListView) findViewById(R.id.listview_evennt);
        expandable_listview_balises = (ExpandableListView) findViewById(R.id.expandable_listview_balises52);
        mainfloating = findViewById(R.id.mainFloatingBtn);
        /* evennementfiltre = (FloatingActionButton)findViewById(R.id.evennements);*/
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

        NavigationView navigationViewLeft = drawerLayout.findViewById(R.id.navigationViewLeft);
        navigationViewLeft.setNavigationItemSelectedListener(this);
        NavigationView navigationViewRight = drawerLayout.findViewById(R.id.navigationViewRight);
        navigationViewRight.setNavigationItemSelectedListener(this);
        navigationViewRight.bringToFront();
        navigationViewLeft.bringToFront();

        //ouverture des portes drawers
        onSetNavigationDrawerEvent();
        //fin de declaration de boutons + affiachege compte utilisateur
        API.getApiInterface(this).getMyAccountData((String) DataSaver.getInstance(this).load("api_key"), getResources().getString(R.string.lang), new Callback<ApiInterface.GetMyAccountDataResult>() {
            @Override
            public void success(ApiInterface.GetMyAccountDataResult dataResult, Response response)
            {
                id_account.setText(dataResult.email);
            }

            @Override
            public void failure(RetrofitError retrofitError) {
                Toast.makeText(DrawerBaseActivity.this,"probleme d'internet", Toast.LENGTH_SHORT).show();
            }
        });
        final String api_key = (String) DataSaver.getInstance(DrawerBaseActivity.this).load("api_key");
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
        // obtention des balises

        refreshObjects();
        // fin affichage evennements.

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
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(DrawerBaseActivity.this, MapActivity.class);
                intent.putExtra("device", new Gson().toJson(listview_evennements.getItemAtPosition(position)));
                startActivity(intent);
                overridePendingTransition(0,0);
                finish();
            }
        });
        // affichage balises
        expandable_listview_balises.setOnChildClickListener(new ExpandableListView.OnChildClickListener(){

            public boolean onChildClick(ExpandableListView parent, View view, int groupPosition, int childPosition, long id) {
                int index = parent.getFlatListPosition(ExpandableListView.getPackedPositionForChild(groupPosition, childPosition));
                parent.setItemChecked(index, true);
                overridePendingTransition(0, 0);

                return true;
            }
        });

        main_mapactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DrawerBaseActivity.this, MapActivity.class));
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
                startActivity(new Intent(DrawerBaseActivity.this, HistoryActivity.class));
            }
        });

        reglageable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DrawerBaseActivity.this, SettingsActivity.class));
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
                drawerLayout.closeDrawer(navigationViewLeft, true);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshObjects();
    }

    protected void onStart() {
        super.onStart();
        refreshObjects();
    }

    protected void onStop() {
        super.onStop();
    }

    private void refreshObjects() {
        String api_key = (String) DataSaver.getInstance(this).load("api_key");
        API.getApiInterface(this).getDevices(api_key, getResources().getString(R.string.lang), new Callback<ArrayList<ApiInterface.GetDevicesItem>>() {
            @Override
            public void success(ArrayList<ApiInterface.GetDevicesItem> getDevicesItems, Response response) {
                adapt = new ObjectsAdapter(DrawerBaseActivity.this, getDevicesItems);
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
    protected void onDestroy() {
        super.onDestroy();
    }
}