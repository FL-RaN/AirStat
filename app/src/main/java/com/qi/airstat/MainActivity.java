package com.qi.airstat;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    public static MainActivity instance;
    private ActivityManager activityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        instance = this;

        activityManager = new ActivityManager(this);
        activityManager.checkLogin();
    }
}
