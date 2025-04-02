package com.gabontech.gprstrack.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.adapters.SetupAdapter;
import com.gabontech.gprstrack.api.API;
import com.gabontech.gprstrack.api.ApiInterface;
import com.gabontech.gprstrack.models.CustomEvent;
import com.gabontech.gprstrack.models.Driver;
import com.gabontech.gprstrack.models.SetupData;
import com.gabontech.gprstrack.models.UserGprsTemplate;
import com.gabontech.gprstrack.models.UserSmsTemplate;
import com.gabontech.gprstrack.utils.DataSaver;
import com.gabontech.gprstrack.utils.Utils;
import com.google.gson.Gson;

import butterknife.ButterKnife;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class SettingsActivity extends AppCompatActivity {


    ExpandableListView expandable_list11;
    LinearLayout saveChanges11;
    RelativeLayout content_layout11;
    RelativeLayout loading_layout11;
    private SetupAdapter adapter;
    DataSaver dataSaver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        ButterKnife.bind(this);
        dataSaver = DataSaver.getInstance(this);

        expandable_list11 = (ExpandableListView) findViewById(R.id.expandable_list11);
        content_layout11 = (RelativeLayout) findViewById(R.id.content_layout11);
        loading_layout11 = (RelativeLayout) findViewById(R.id.loading_layout11);
        saveChanges11 = (LinearLayout) findViewById(R.id.saveChanges11);

        loading_layout11.setVisibility(View.VISIBLE);
        final String api_key = (String) dataSaver.load("api_key");
        // setup data
        API.getApiInterface(this).getSetupData(api_key, getResources().getString(R.string.lang), new Callback<ApiInterface.SetupDataResult>()
        {
            @Override
            public void success(final ApiInterface.SetupDataResult setupDataResult, Response response)
            {

                // get drivers
                API.getApiInterface(SettingsActivity.this).getUserDrivers(api_key, getResources().getString(R.string.lang), 0, new Callback<ApiInterface.GetUserDriversResult>()
                {
                    @Override
                    public void success(final ApiInterface.GetUserDriversResult getUserDriversResult, Response response)
                    {

                        // get events
                        API.getApiInterface(SettingsActivity.this).getCustomEvents(api_key, getResources().getString(R.string.lang), new Callback<ApiInterface.GetCustomEventsResult>()
                        {
                            @Override
                            public void success(final ApiInterface.GetCustomEventsResult getCustomEventsResult, Response response)
                            {
                                // get sms templates
                                API.getApiInterface(SettingsActivity.this).getUserSmsTemplates(api_key, getResources().getString(R.string.lang), new Callback<ApiInterface.GetUserSmsTemplatesResult>()
                                {
                                    @Override
                                    public void success(final ApiInterface.GetUserSmsTemplatesResult getUserSmsTemplatesResult, Response response)
                                    {
                                        // get gprs templates
                                        API.getApiInterface(SettingsActivity.this).getUserGprsTemplates(api_key, getResources().getString(R.string.lang), new Callback<ApiInterface.GetUserGprsTemplatesResult>()
                                        {
                                            @Override
                                            public void success(ApiInterface.GetUserGprsTemplatesResult getUserGprsTemplatesResult, Response response)
                                            {
                                                adapter = new SetupAdapter(SettingsActivity.this, expandable_list11, setupDataResult,
                                                        getUserDriversResult.items.drivers.data,
                                                        getCustomEventsResult.items.events.data,
                                                        getUserSmsTemplatesResult.items.user_sms_templates.data,
                                                        getUserGprsTemplatesResult.items.user_gprs_templates.data);
                                                expandable_list11.setAdapter(adapter);
                                                Utils.setGroupClickListenerToNotify(expandable_list11, adapter);

                                                loading_layout11.setVisibility(View.GONE);
                                                content_layout11.setVisibility(View.VISIBLE);
                                            }

                                            @Override
                                            public void failure(RetrofitError retrofitError)
                                            {
                                                Toast.makeText(SettingsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }

                                    @Override
                                    public void failure(RetrofitError retrofitError)
                                    {
                                        Toast.makeText(SettingsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {
                                Toast.makeText(SettingsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }

                    @Override
                    public void failure(RetrofitError retrofitError)
                    {
                        Toast.makeText(SettingsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void failure(RetrofitError retrofitError)
            {
                Toast.makeText(SettingsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
            }
        });

        saveChanges11.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (adapter == null) return; // not loaded yet
                final SetupData data = adapter.getSetupData();
                if (data == null) return; // not loaded yet
                String groups_array = new Gson().toJson(adapter.getObjectGroups());
                API.getApiInterface(SettingsActivity.this).saveEditedSetup(api_key, getResources().getString(R.string.lang),
                        data.unit_of_distance,
                        data.unit_of_capacity,
                        data.unit_of_altitude,
                        data.timezone_id,
                        groups_array,
                        data.sms_gateway,
                        data.sms_gateway_params.request_method,
                        data.sms_gateway_params.encoding,
                        data.sms_gateway_params.authentication,
                        data.sms_gateway_params.username,
                        data.sms_gateway_params.password,
                        data.sms_gateway_url,
                        data.sms_gateway_params.auth_id,
                        data.sms_gateway_params.auth_token,
                        data.sms_gateway_params.senders_phone,
                        new Callback<ApiInterface.AddUserDriverResult>()
                        {
                            @Override
                            public void success(ApiInterface.AddUserDriverResult addUserDriverResult, Response response)
                            {
                                Toast.makeText(SettingsActivity.this, "Enregistré avec succès!", Toast.LENGTH_SHORT).show();
                                dataSaver.save("unit_of_distance", data.unit_of_distance);
                                dataSaver.save("unit_of_distance_hour", "mph");
                                if(data.unit_of_distance.equals("km"))
                                {
                                    dataSaver.save("unit_of_distance_hour", "km/h");
                                }
                                dataSaver.save("unit_of_altitude", data.unit_of_altitude);
                                dataSaver.save("unit_of_capacity", data.unit_of_capacity);
                                finish();
                            }

                            @Override
                            public void failure(RetrofitError retrofitError)
                            {
                                Toast.makeText(SettingsActivity.this, R.string.errorHappened, Toast.LENGTH_SHORT).show();
                            }
                        });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) // new sms template
        {
            UserSmsTemplate item = new Gson().fromJson(data.getStringExtra("item"), UserSmsTemplate.class);
            adapter.addSmsTemplate(item);
        } else if (requestCode == 2 && resultCode == RESULT_OK) // new gprs template
        {
            UserGprsTemplate item = new Gson().fromJson(data.getStringExtra("item"), UserGprsTemplate.class);
            adapter.addGprsTemplate(item);
        } else if (requestCode == 3 && resultCode == RESULT_OK) // new driver
        {
            Driver item = new Gson().fromJson(data.getStringExtra("item"), Driver.class);
            adapter.addDriver(item);
        } else if (requestCode == 4 && resultCode == RESULT_OK) // new custom template
        {
            CustomEvent item = new Gson().fromJson(data.getStringExtra("item"), CustomEvent.class);
            adapter.addCustomEvent(item);
        }
    }
}