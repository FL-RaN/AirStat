package com.qi.airstat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static MainActivity instance;
    private ActivityManager activityManager;
    int backPressCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        activityManager = new ActivityManager(this);
        activityManager.checkLogin();

//        startActivity(new Intent(this, DataMapActivity.class));
//        startActivity(new Intent(this, LoginBaseActivity.class ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY));
//        startActivityForResult(new Intent(this, SensorDataOverviewActivity.class), 200);
//        finish();
    }
}
