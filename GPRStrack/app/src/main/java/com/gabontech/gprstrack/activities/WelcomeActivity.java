package com.gabontech.gprstrack.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.gabontech.gprstrack.R;
import com.gabontech.gprstrack.utils.DataSaver;

public class WelcomeActivity extends AppCompatActivity {


    TextView txt_bienvenu, txtversion;
    TextView txt_sbienvenu;
    Button btn_login, btn_code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.lobsterregular);
        Typeface typef = ResourcesCompat.getFont(this, R.font.fjallaoneregular);
        //reconnaissance de bouton
        btn_login = findViewById(R.id.btn_connect);
        btn_code = findViewById(R.id.btn_lostcode);
        txt_bienvenu = findViewById(R.id.text_bienvenue);
        txt_sbienvenu = findViewById(R.id.soustext_bienvenue);
        txtversion = findViewById(R.id.text_version);
        //if there is a api_key then we move to the map activity
        if(DataSaver.getInstance(this).load("api_key") != null)
        {

            startActivity(new Intent(WelcomeActivity.this, MapActivity.class));
            finish();
            return;
        }else if(DataSaver.getInstance(this).load("api_key") == null) /* if null then we can login*/
        {
            startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            finish();
            return;
        }

        txt_bienvenu.setTypeface(typef);
        txt_sbienvenu.setTypeface(typeface);
        txtversion.setTypeface(typef);
        //animation de text
        // code bouton connexion

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(WelcomeActivity.this, LoginActivity.class));
            }
        });

        // code pour bouton code perdu
        btn_code.setOnClickListener(view -> {
            startActivity(new Intent(WelcomeActivity.this, CodePerduActivity.class));
        });
    }
}