package com.qi.airstat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class SensorDataOverviewActivity extends FragmentActivity {
    static private int graphCountHeartRate = 0;
    static private int graphCountAir = 0;

    // It'll be binded to the SensorDataUpdateService.
    // So, this messenger can send message to service.
    private Messenger messageTransmitter = null;

    private boolean isBinded = false;

    private RelativeLayout locationDisabledLayout = null;
    private RelativeLayout bluetoothDisabledLayout = null;
    private RelativeLayout bluetoothUnsupportedLayout = null;

    // It'll receive message from SensorDataUpdateService and will behave for each pre-defined message.
    // That is, this messenger will be client for service.
    final private Messenger messageReceiver = new Messenger(new SensorDataOverviewHandler());

    private ServiceConnection serviceConnection = new ServiceConnection() {
        // Callback function on connect service.
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Bind service using messenger.
            messageTransmitter = new Messenger(service);
            isBinded = true;

            try {
                // Send message to service for register this activity as new client.
                Message message = Message.obtain(null, Constants.CLIENT_REGISTER);
                message.replyTo = messageReceiver;
                messageTransmitter.send(message);
            }
            catch (RemoteException exception) {
                exception.printStackTrace();
            }
        }

        // Callback function on disconnect service.
        public void onServiceDisconnected(ComponentName className) {
            messageTransmitter = null;
            isBinded = false;
        }
    };

    private class SensorDataOverviewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.QUEUED_AIR_DATA:
                    updateAirData();
                    break;
                case Constants.QUEUED_HEART_RATE_DATA:
                    updateHeartRateData();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data_overview);

        locationDisabledLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_location_disabled);
        bluetoothDisabledLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_bluetooth_disabled);
        bluetoothUnsupportedLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_bluetooth_not_supported);

        DatabaseManager databaseManager = new DatabaseManager(this);
        databaseManager.rebuild();
        databaseManager.close();

        if (!BluetoothState.isBluetoothSupported(this)) {
            locationDisabledLayout.setVisibility(View.GONE);
            bluetoothDisabledLayout.setVisibility(View.GONE);
            bluetoothUnsupportedLayout.setVisibility(View.VISIBLE);
            return;
        }
        else {
            bluetoothUnsupportedLayout.setVisibility(View.GONE);
        }

        if (!LocationState.isLocationAvailable(this)) {
            locationDisabledLayout.setVisibility(View.VISIBLE);
        }
        else {
            locationDisabledLayout.setVisibility(View.GONE);

            if (!BluetoothState.isBluetoothAvailable()) {
                bluetoothDisabledLayout.setVisibility(View.VISIBLE);
            }
            else {
                bluetoothDisabledLayout.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(
                new Intent(this, FakeDataTransmitService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isBinded) {
            unbindService(serviceConnection);
            isBinded = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.BLUETOOTH_PERMISSION_REQUEST:
                if (resultCode == RESULT_OK) {
                    bluetoothDisabledLayout.setVisibility(View.GONE);

                    if (!LocationState.isLocationAvailable(this)) {
                        locationDisabledLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        locationDisabledLayout.setVisibility(View.GONE);
                    }
                }
                break;
            case Constants.LOCATION_PERMISSION_REQUEST:
                if (resultCode == RESULT_OK) {
                    locationDisabledLayout.setVisibility(View.GONE);

                    if (!BluetoothState.isBluetoothAvailable()) {
                        bluetoothDisabledLayout.setVisibility(View.VISIBLE);
                    }
                    else {
                        bluetoothDisabledLayout.setVisibility(View.GONE);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void updateHeartRateData() {
        DatabaseManager databaseManager = new DatabaseManager(this);
        SQLiteDatabase database = databaseManager.getReadableDatabase();
        Cursor cursor = null;

        LineChart lineChart = (LineChart)findViewById(R.id.lic_heart_rate_data_graph);
        if (lineChart != null) {
            LineData data = lineChart.getData();

            if (data != null) {
                ILineDataSet set = data.getDataSetByIndex(0);

                if (set == null) {
                    set = new LineDataSet(null, "DynChart");
                    set.setAxisDependency(YAxis.AxisDependency.LEFT);
                    set.setValueTextColor(ColorTemplate.getHoloBlue());
                    set.setHighlightEnabled(true);
                    set.setValueTextColor(Color.WHITE);
                    set.setValueTextSize(9f);
                    set.setDrawValues(true);
                    data.addDataSet(set);
                }

                if (data.getEntryCount() >= 5) {
                    database.execSQL("DELETE FROM heart_rate WHERE id IN (SELECT id FROM heart_rate ORDER BY id ASC LIMIT 1)");
                    data.removeEntry(0, 0);
                }

                cursor = database.rawQuery("SELECT * FROM heart_rate", null);
                int lastValue = 0;

                if (cursor != null && cursor.moveToLast()) {
                    lastValue = cursor.getInt(cursor.getColumnIndex(Constants.DATABASE_HEART_RATE_COLUMN_HEART_RATE));

                    do {
                        //Entry newEntry = new Entry(set.getEntryCount(), lastValue);
                        Entry newEntry = new Entry(graphCountHeartRate++, lastValue);
                        data.addEntry(newEntry, 0);
                    } while (cursor.moveToNext());
                }

                database.close();

                ((TextView) findViewById(R.id.tv_heart_rate_data_bpm_value)).setText("" + lastValue);

                data.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }
        }
    }

    private void updateAirData() {
        DatabaseManager databaseManager = new DatabaseManager(this);
        SQLiteDatabase database = databaseManager.getReadableDatabase();
        Cursor cursor = null;

            LineChart lineChart = (LineChart)findViewById(R.id.lic_air_data_view_pager);

            if (lineChart != null) {
                LineData data = lineChart.getData();

                if (data != null) {
                    ILineDataSet set = data.getDataSetByIndex(0);

                    if (set == null) {
                        set = new LineDataSet(null, "DynChart");
                        set.setAxisDependency(YAxis.AxisDependency.LEFT);
                        set.setValueTextColor(ColorTemplate.getHoloBlue());
                        set.setHighlightEnabled(true);
                        set.setValueTextColor(Color.WHITE);
                        set.setValueTextSize(9f);
                        set.setDrawValues(true);
                        data.addDataSet(set);
                    }

                    if (data.getEntryCount() >= 5) {
                        database.execSQL("DELETE FROM air WHERE id IN (SELECT id FROM air ORDER BY id ASC LIMIT 1)");
                        data.removeEntry(0, 0);
                    }

                    cursor = database.rawQuery("SELECT * FROM air", null);
                    float co2 = 0f, co = 0f, so2 = 0f, no2 = 0f, o3 = 0f, pm25 = 0f;

                    if (cursor != null && cursor.moveToLast()) {
                        do {
                            Entry newEntry = null;
                            co2 = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_CO2));
                            co = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_CO));
                            so2 = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_SO2));
                            no2 = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_NO2));
                            o3 = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_O3));
                            pm25 = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_PM25));
                            switch (0) {
                                case 0:
                                    newEntry = new Entry(graphCountHeartRate++, co2);
                                    break;
                                case 1:
                                    newEntry = new Entry(graphCountHeartRate++, co);
                                    break;
                                case 2:
                                    newEntry = new Entry(graphCountHeartRate++, so2);
                                    break;
                                case 3:
                                    newEntry = new Entry(graphCountHeartRate++, no2);
                                    break;
                                case 4:
                                    newEntry = new Entry(graphCountHeartRate++, o3);
                                    break;
                                case 5:
                                    newEntry = new Entry(graphCountHeartRate++, pm25);
                                    break;
                            }
                            data.addEntry(newEntry, 0);
                        } while (cursor.moveToNext());
                    }

                    database.close();

                    ((TextView) findViewById(R.id.tv_air_data_brief_co2_value)).setText(String.format("%.2f", co2));
                    ((TextView) findViewById(R.id.tv_air_data_brief_co_value)).setText(String.format("%.2f", co));
                    ((TextView) findViewById(R.id.tv_air_data_brief_so2_value)).setText(String.format("%.2f", so2));
                    ((TextView) findViewById(R.id.tv_air_data_brief_no2_value)).setText(String.format("%.2f", no2));
                    ((TextView) findViewById(R.id.tv_air_data_brief_o3_value)).setText(String.format("%.2f", o3));
                    ((TextView) findViewById(R.id.tv_air_data_brief_pm2_5_value)).setText(String.format("%.2f", pm25));

                    data.notifyDataChanged();
                    lineChart.notifyDataSetChanged();
                    lineChart.invalidate();
            }
        }
    }

    public void onClickRequestLocationPermission(View view) {
        LocationState.requestLocationPermission(this);
    }

    public void onClickRequestBluetoothPermission(View view) {
        BluetoothState.requestBluetoothPermission(this);
    }
}
