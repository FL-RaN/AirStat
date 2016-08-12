package com.qi.airstat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qi.airstat.login.LoginBaseActivity;

public class MainActivity extends AppCompatActivity {
public static MainActivity instance;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;
//        startActivity(new Intent(this, DataMapActivity.class));
        startActivity(new Intent(this, LoginBaseActivity.class ));
//        startActivityForResult(new Intent(this, SensorDataOverviewActivity.class), 200);
        finish();
    }
}
