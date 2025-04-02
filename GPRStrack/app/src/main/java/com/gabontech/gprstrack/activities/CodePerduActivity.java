package com.gabontech.gprstrack.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.gabontech.gprstrack.R;

public class CodePerduActivity extends AppCompatActivity {

    TextView txtperduinfo;
    EditText mail;
    Button btn_code, btn_login;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code_perdu);

        mail = findViewById(R.id.Edtxt_email);
        btn_code = findViewById(R.id.btn_codeperdu);
        btn_login = findViewById(R.id.btn_login);
        txtperduinfo = findViewById(R.id.text_perduinfo);

        Typeface typeface = ResourcesCompat.getFont(this, R.font.dancingvariablefont);
        txtperduinfo.setTypeface(typeface);

        btn_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mail ==null){
                    Toast.makeText(CodePerduActivity.this, "Votre mail est obligatoire", Toast.LENGTH_SHORT).show();
                }else{
                    progressDialog = new ProgressDialog(CodePerduActivity.this);
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
                    String ip = getResources().getString(R.string.ip);
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse("https://tracking.gabontech.com/password_reminder/create"));
                    startActivity(browserIntent);
                }
            }
        });
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CodePerduActivity.this, LoginActivity.class));
            }
        });
    }
}