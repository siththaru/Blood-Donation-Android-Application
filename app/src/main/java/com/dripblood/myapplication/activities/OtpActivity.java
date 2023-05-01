package com.dripblood.myapplication.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.dripblood.myapplication.R;
import com.facebook.login.Login;
import com.google.firebase.auth.AuthCredential;

public class OtpActivity extends AppCompatActivity {

    private String email;
    private String password;
    public AuthCredential googleCred;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        //Toolbar
        this.setTitle("Phone Verification");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent i = getIntent();
        email = i.getStringExtra("EMAIL");
        password = i.getStringExtra("PASSWORD");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}