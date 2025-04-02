package com.gabontech.gprstrack.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.api.API;
import com.gabontech.gprstrack.api.ApiInterface;
import com.gabontech.gprstrack.utils.DataSaver;

import java.util.Random;

import butterknife.Bind;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "EditObjectActivity";
    @Bind(R.id.username)
    EditText username;
    @Bind(R.id.password)
    EditText password;
    @Bind(R.id.signin)
    Button signin;
    @Bind(R.id.register)
    Button pwreset;
    private String UrlPrefix;
    private String customServerAddress="https://tracking.gabontech.com/";
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if(activeNetwork == null) {
            Intent intent = new Intent(LoginActivity.this, NoInternetActivity.class);
            startActivity(intent);
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BATTERY_STATS}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_WIFI_STATE}, PackageManager.PERMISSION_GRANTED);
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, PackageManager.PERMISSION_GRANTED);

        progressDialog = new ProgressDialog(this);
        signin = (Button) findViewById(R.id.signin);
        pwreset = (Button) findViewById(R.id.register);
        password = findViewById(R.id.password);
        username = findViewById(R.id.username);


        String ip = getResources().getString(R.string.ip);
        String httpsOn = getResources().getString(R.string.https_on);

        if (httpsOn.equals("on")) {
            UrlPrefix = "https://";
        }
        else {
            UrlPrefix = "http://";
        }

        if (ip.isEmpty()) {

        }
        else {
            String url = UrlPrefix + ip + "/";
        }

        String server_base = customServerAddress;
        if(!server_base.startsWith("http://") && !server_base.startsWith("https://"))
            server_base = UrlPrefix + server_base;
        if(!server_base.endsWith("/"))
            server_base += "/";
        DataSaver.getInstance(LoginActivity.this).save("server_base", server_base);
        DataSaver.getInstance(LoginActivity.this).save("server", server_base + "api/");

        if (ip.isEmpty()) {
            enableRegistration();
        }
        else {
            API.getApiInterface(LoginActivity.this).registrationStatus("en", new Callback<ApiInterface.RegistrationStatusResult>()
            {
                @Override
                public void success(ApiInterface.RegistrationStatusResult result, Response response)
                {
                    if (result.status == 1) {
                        enableRegistration();
                    }
                }

                @Override
                public void failure(RetrofitError retrofitError) {
                    Log.e(TAG, "failure: retrofitError" + retrofitError.getMessage());
                    Toast.makeText(LoginActivity.this, getString(R.string.errorHappened), Toast.LENGTH_SHORT).show();
                }
            });
        }
        signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(customServerAddress != null) {
                    String server_base = customServerAddress;
                    if(!server_base.startsWith("http://") && !server_base.startsWith("https://"))
                        server_base = UrlPrefix + server_base;
                    if(!server_base.endsWith("/"))
                        server_base += "/";
                    DataSaver.getInstance(LoginActivity.this).save("server_base", server_base);
                    DataSaver.getInstance(LoginActivity.this).save("server", server_base + "api/");
                }
                API.getApiInterface(LoginActivity.this).login(username.getText().toString(), password.getText().toString(), new Callback<ApiInterface.LoginResult>()
                {
                    @Override
                    public void success(ApiInterface.LoginResult loginResult, Response response)
                    {   // progress dialogue
                        progressDialog = new ProgressDialog(LoginActivity.this);
                        progressDialog.setMessage("En cours..."); // Setting Message
                        progressDialog.setTitle("Connexion au serveur"); // Setting Title
                        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER); // Progress Dialog Style Spinner
                        progressDialog.show(); // Display Progress Dialog
                        progressDialog.setCancelable(false);
                        new Thread(new Runnable() {
                            public void run() {
                                try {
                                    Thread.sleep(10000);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                progressDialog.dismiss();
                            }
                        }).start();
                        //end progress dialogue

                        DataSaver.getInstance(LoginActivity.this).save("api_key", loginResult.user_api_hash);
                        Log.d("LoginActivity", "api_key: " + loginResult.user_api_hash);

                        if(customServerAddress == null)
                        {
                            String url = "https://tracking.gabontech.com/";
                            DataSaver.getInstance(LoginActivity.this).save("server_base", url);
                            DataSaver.getInstance(LoginActivity.this).save("server", url + "api/");
                        }

                        showNotification();
                        finish();
                    }

                    @Override
                    public void failure(RetrofitError retrofitError)
                    {
                        Toast.makeText(LoginActivity.this, getString(R.string.wrongLogin), Toast.LENGTH_SHORT).show();
                    }
                });
            }

        });
    }

    private void showNotification() {
        int notificationId = new Random().nextInt(100);
        String channelId = "notification_channel_1";

        NotificationManager notificationManager =
                 (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent intent = new Intent(LoginActivity.this, MapActivity.class);
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
        builder.setContentText("Nouvelle connexion détectée \n Vous êtes connecté en tant que" + ""+ ""+ username.getText().toString());
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
          if(notificationManager != null && notificationManager.getNotificationChannel(channelId) ==null){
              NotificationChannel notificationChannel = new NotificationChannel(
                      channelId, "Notification channel 1", NotificationManager.IMPORTANCE_HIGH
              );
              notificationChannel.setDescription("Ceci est pour notifier une nouvelle connexion");
              notificationChannel.enableLights(true);
              notificationChannel.enableVibration(true);
              notificationManager.createNotificationChannel(notificationChannel);
          }
        }
        Notification notification = builder.build();
        if (notificationManager != null) {
            notificationManager.notify(notificationId, notification);
        }
    }

    private void enableRegistration() {

        pwreset.setBackgroundResource(R.drawable.button_blue);
        pwreset.setEnabled(false);
        pwreset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, CodePerduActivity.class));
            }
        });
    }
}