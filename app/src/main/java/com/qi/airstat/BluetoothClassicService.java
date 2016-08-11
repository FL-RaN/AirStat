package com.qi.airstat;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qi.airstat.blc.BluetoothConnector;
import com.qi.airstat.blc.DeviceData;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BluetoothClassicService extends Service {
    private final IBinder localBinder = new LocalBinder();
    private final BluetoothHandler bluetoothHandler = new BluetoothHandler();
    private LocationManager locationManager = null;
    private float longitude = 0f;
    private float latitude = 0f;

    private class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if (msg.arg1 == Constants.STATE_CONNECTING) {

                    }
                    else if (msg.arg1 == Constants.STATE_CONNECTED) {
                        Intent intent = new Intent(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE);
                        intent.putExtra(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE, msg.arg1);
                        sendBroadcast(intent);

                        BluetoothState.isBLCConnected(true);
                    }
                    else if (msg.arg1 == Constants.STATE_NONE) {
                        Intent intent = new Intent(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE);
                        intent.putExtra(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE, msg.arg1);
                        sendBroadcast(intent);

                        BluetoothState.isBLCConnected(false);
                    }
                    break;
                case Constants.MESSAGE_READ:
                    switch ((String)msg.obj) {
                        case "a":
                            break;
                        default:
                            break;
                    }

                    DatabaseManager databaseManager = new DatabaseManager(BluetoothClassicService.this);
                    SQLiteDatabase database = databaseManager.getWritableDatabase();

                    ContentValues values = new ContentValues();

                    try {
                        JSONObject jsonObject = new JSONObject((String) msg.obj);

                        Date unixTime = new Date(jsonObject.getLong("C_TIME") * 1000L);
                        String date = new SimpleDateFormat("yyMMddHHmmss").format(unixTime);

                        values.put(Constants.DATABASE_AIR_COLUMN_CO, jsonObject.getDouble("CO"));
                        values.put(Constants.DATABASE_AIR_COLUMN_TEMPERATURE, jsonObject.getInt("TEMP"));
                        values.put(Constants.DATABASE_COMMON_COLUMN_TIME_STAMP, date);
                        values.put(Constants.DATABASE_AIR_COLUMN_SO2, jsonObject.getDouble("SO2"));
                        values.put(Constants.DATABASE_AIR_COLUMN_PM25, jsonObject.getDouble("PM25"));
                        values.put(Constants.DATABASE_AIR_COLUMN_O3, jsonObject.getDouble("O3"));
                        values.put(Constants.DATABASE_AIR_COLUMN_NO2, jsonObject.getDouble("NO2"));
                        values.put(Constants.DATABASE_AIR_COLUMN_LAT, latitude);
                        values.put(Constants.DATABASE_AIR_COLUMN_LON, longitude);

                        database.insert(Constants.DATABASE_AIR_TABLE, null, values);

                        Intent intent = new Intent(Constants.BLUETOOTH_MESSAGE_STATE_READ);
                        sendBroadcast(intent);
                    }
                    catch (JSONException exception) {
                        exception.printStackTrace();
                    }

                    database.close();
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
                    // Means connected
                    break;
                case Constants.MESSAGE_TOAST:
                    break;
                case Constants.MESSAGE_WRITE:
                    break;
                default:
                    break;
            }
        }
    }

    private BroadcastReceiver backgroundBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            latitude = (float)location.getLatitude();
            longitude = (float)location.getLongitude();
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    public void onCreate() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
        }
        catch (SecurityException exception) {
            exception.printStackTrace();
        }

        registerReceiver(backgroundBroadcastReceiver, getIntentFilter());
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(backgroundBroadcastReceiver);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return localBinder;
    }

    public class LocalBinder extends Binder {
        BluetoothClassicService getService() {
            return BluetoothClassicService.this;
        }
    }

    private IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();

        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);

        return intentFilter;
    }

    public void connect(DeviceData deviceData) {
        final int state = BluetoothState.bluetoothConnector == null ? Constants.STATE_NONE : BluetoothState.bluetoothConnector.getState();

        if (BluetoothState.bluetoothConnector == null) {
            BluetoothState.bluetoothConnector = new BluetoothConnector(deviceData, bluetoothHandler);
            BluetoothState.bluetoothConnector.connect();
        }
    }

    public void disconnect() {
        if (BluetoothState.bluetoothConnector != null) {
            BluetoothState.bluetoothConnector.stop();
        }

        BluetoothState.bluetoothConnector = null;
    }
}
