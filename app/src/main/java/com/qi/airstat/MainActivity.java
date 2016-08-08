package com.qi.airstat;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.qi.airstat.dataMap.DataMapActivity;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startActivity(new Intent(this, DataMapActivity.class));

        //startService(new Intent(this, FakeDataTransmitService.class));
        //startActivity(new Intent(this, SensorDataOverviewActivity.class));
        finish();
    }
}
