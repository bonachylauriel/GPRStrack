package com.gabontech.gprstrack;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import com.gabontech.gprstrack.activities.LoginActivity;
import com.gabontech.gprstrack.activities.MapActivity;
import com.gabontech.gprstrack.activities.NoInternetActivity;
import com.gabontech.gprstrack.activities.WelcomeActivity;
import com.gabontech.gprstrack.utils.DataSaver;

public class SplashScreenActivity extends AppCompatActivity {

    public int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
       // if there is not api_key then we can login otherwise the activity goes to the map.
        if(DataSaver.getInstance(this).load("api_key") == null)
        {
            startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
            finish();
            return;
        }
                // we handle the activity for 2 seconds on start
        new Handler().postDelayed(new Runnable(){

            @Override
            public void run(){
                final ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                final NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                if(activeNetwork == null){
                    Intent intent = new Intent(SplashScreenActivity.this, NoInternetActivity.class);
                    startActivity(intent);
                    finish();
                }else{
                    Intent i = new Intent(SplashScreenActivity.this, WelcomeActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, SPLASH_TIME_OUT);
    }
}