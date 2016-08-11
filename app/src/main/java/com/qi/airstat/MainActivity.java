package com.qi.airstat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qi.airstat.login.LoginBaseActivity;

public class MainActivity extends AppCompatActivity {
    CustomView customView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        startActivity(new Intent(this, DataMapActivity.class));
        startActivity(new Intent(this, LoginBaseActivity.class));
        finish();
    }
}
