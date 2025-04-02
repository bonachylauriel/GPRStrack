package com.gabontech.gprstrack.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.SplashScreenActivity;

public class NoInternetActivity extends AppCompatActivity {

    public Button btn_nodata;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_internet);

        btn_nodata = (Button) findViewById(R.id.btn_nodata);

        btn_nodata.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(NoInternetActivity.this, SplashScreenActivity.class);
                startActivity(intent);
                finish();

            }
        });
    }
}