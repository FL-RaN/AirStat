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
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qi.airstat.blc.BluetoothConnector;
import com.qi.airstat.blc.DeviceData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BluetoothClassicService extends Service {
    private final IBinder localBinder = new LocalBinder();
    private final BluetoothHandler bluetoothHandler = new BluetoothHandler();
    private LocationManager locationManager = null;
    private double longitude = 0;
    private double latitude = 0;
    private boolean isReceivingCSV = false;
    private StringBuilder CSV = new StringBuilder();
    private int csvCount = 0;

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

                        /*try {
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                        catch (SecurityException exception) {
                            exception.printStackTrace();
                        }

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("userID", Constants.UID);
                            jsonObject.put("conCreationTime", new SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date()));
                            jsonObject.put("flagValidCon", 1);
                            jsonObject.put("devMAC", "x'" + Constants.MAC_UDOO.replaceAll(":", "") + "'");
                            jsonObject.put("devType", Constants.DEVICE_TYPE_UDOO);
                            jsonObject.put("devPortability", 0x01);
                            jsonObject.put("latitude", "x'" + latitude);
                            jsonObject.put("longitude", "x'" + longitude);
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                        }

                        HttpService httpService = new HttpService();
                        String res = httpService.executeConn(
                                null, "POST",
                                "http://teamc-iot.calit2.net/IOT/public/Connection",
                                jsonObject
                        );

                        try {
                            JSONObject resJson;
                            resJson = new JSONObject(res);
                            Log.d("BLCService", "Connection sent, response was " + res);
                            Constants.CID_BLC = Integer.parseInt(resJson.getString("connectionID"));
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                        }*/

                        BluetoothState.isBLCConnected(true);
                        sendBroadcast(intent);
                    }
                    else if (msg.arg1 == Constants.STATE_NONE) {
                        Intent intent = new Intent(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE);
                        intent.putExtra(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE, msg.arg1);
                        sendBroadcast(intent);

                        BluetoothState.bluetoothConnector = null;
                        BluetoothState.isBLCConnected(false);

                        /*try {
                            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                        }
                        catch (SecurityException exception) {
                            exception.printStackTrace();
                        }

                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("userID", Constants.UID);
                            jsonObject.put("conCreationTime", new SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date()));
                            jsonObject.put("flagValidCon", 0);
                            jsonObject.put("connectionID", Constants.CID_BLC);
                            jsonObject.put("devMAC", "x'" + Constants.MAC_UDOO.replaceAll(":", "") + "'");
                            jsonObject.put("devType", Constants.DEVICE_TYPE_UDOO);
                            jsonObject.put("devPortability", 0x01);
                            jsonObject.put("latitude", "x'" + latitude);
                            jsonObject.put("longitude", "x'" + longitude);
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                        }

                        HttpService httpService = new HttpService();
                        String response = httpService.executeConn(
                                null, "POST",
                                "http://teamc-iot.calit2.net/IOT/public/Disconnection",
                                jsonObject
                        );

                        Constants.MAC_UDOO = null;
                        Constants.CID_BLC = Constants.CID_NONE;

                        Log.d("BLCService DISC RES", response);*/
                    }
                    break;
                case Constants.MESSAGE_READ:
                    if (((String)msg.obj).charAt(0) == '[') {
                        DatabaseManager databaseManager = new DatabaseManager(BluetoothClassicService.this);
                        SQLiteDatabase database = databaseManager.getWritableDatabase();

                        ContentValues values = new ContentValues();

                        try {
                            JSONArray jsonArray = new JSONArray((String) msg.obj);
                            JSONObject jsonObject = jsonArray.getJSONObject(0);

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
                            database.close();

                            Intent intent = new Intent(Constants.BLUETOOTH_MESSAGE_STATE_READ);

                            /*try {
                                Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                            catch (SecurityException exception) {
                                exception.printStackTrace();
                            }

                            JSONObject reformedObject = new JSONObject();
                            JSONArray reformedArray = new JSONArray();

                            try {
                                JSONObject item = new JSONObject();
                                item.put("timeStamp", date);
                                item.put("connectionID", Constants.CID_BLC);
                                item.put("SO2", jsonObject.getDouble("SO2"));
                                item.put("NO2", jsonObject.getDouble("NO2"));
                                item.put("O3", jsonObject.getDouble("O3"));
                                item.put("CO", jsonObject.getDouble("CO"));
                                item.put("PM", jsonObject.getDouble("PM25"));
                                item.put("temperature", jsonObject.getInt("TEMP"));
                                item.put("latitude", latitude);
                                item.put("longitude", longitude);

                                reformedArray.put(item);
                                reformedObject.put("AIR", reformedArray);
                            }
                            catch (JSONException e) {
                                e.printStackTrace();
                            }

                            HttpService httpService = new HttpService();
                            String responseCode = httpService.executeConn(
                                    null,
                                    "POST", "http://teamc-iot.calit2.net/IOT/public/rcv_json_data",
                                    reformedObject
                            );*/

                            sendBroadcast(intent);
                        }
                        catch (JSONException exception) {
                            exception.printStackTrace();
                        }

                        if (database.isOpen()) {
                            database.close();
                        }
                    }
                    else if(((String)msg.obj).contains("end_")) {
                        File file = new File(Environment.getExternalStorageDirectory() + "/dataset" + (csvCount++) + ".csv");

                        try {
                            FileWriter fw = new FileWriter(file, true);
                            fw.write(CSV.toString());
                            fw.flush();
                            fw.close();
                        }
                        catch (IOException exception) {
                            exception.printStackTrace();
                        }

                        InputStream is = null;
                        FileInputStream fileInputStream = null;

                        try {
                            fileInputStream = new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/dataset" + (csvCount - 1) + ".csv"));
                        }
                        catch (IOException exception) {
                            exception.printStackTrace();
                        }

                        try {
                            URL url = new URL("http://155.94.189.29/saveupload.php");
                            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                            conn.setRequestProperty("Content-Type", "text/plain; charset=utf-8");
                            conn.setDoInput(true);
                            conn.setDoOutput(true);
                            conn.setRequestMethod("POST");

                            OutputStream outputStream = new DataOutputStream(conn.getOutputStream());
                            int bytesAvailable, bufferSize, bytesRead, maxBufferSize = 1 * 1024 * 1024;
                            byte[] buffer;

                            bytesAvailable = fileInputStream.available();
                            bufferSize = Math.min(bytesAvailable, maxBufferSize);
                            buffer = new byte[bufferSize];

                            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                            while (bytesRead > 0) {
                                outputStream.write(buffer, 0, bufferSize);
                                bytesAvailable = fileInputStream.available();
                                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                            }
                            // Starts the query
                            /*conn.connect();
                            int response = conn.getResponseCode();
                            Log.d("CSV RESPONSE", "The response is: " + response);
                            is = conn.getInputStream();*/

                            int res = conn.getResponseCode();
                            String reply = conn.getResponseMessage();

                            is.close();
                            fileInputStream.close();
                            outputStream.flush();
                            outputStream.close();
                        }
                        catch (MalformedURLException exception) {
                            Log.d("CSV RESPONSE", "MalformedURLException");
                            exception.printStackTrace();
                        }
                        catch (IOException exception) {
                            Log.d("CSV RESPONSE", "IOException");
                            exception.printStackTrace();
                        }
                        finally {
                            try {
                                if (is != null) {
                                    is.close();
                                }
                            }
                            catch (IOException exception) {
                                exception.printStackTrace();
                            }
                        }

                        Log.d("BLCService", CSV.toString());
                        CSV = null;
                    }
                    else if (((String)msg.obj).contains("start_")) {
                        CSV = new StringBuilder();
                        Log.d("BLCService", "Caught CSV start");
                    }
                    else {
                        String buf = msg.obj.toString();
                        CSV.append(buf.substring(0, buf.length() - 1));
                        Log.d("BLCService", "Caught CSV String: " + buf);
                    }
                    break;
                case Constants.MESSAGE_DEVICE_NAME:
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

        /*try {
            Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        catch (SecurityException exception) {
            exception.printStackTrace();
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("userID", Constants.UID);
            jsonObject.put("conCreationTime", new SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date()));
            jsonObject.put("flagValidCon", 1);
            jsonObject.put("devMAC", "x'" + deviceData.getAddress().replaceAll(":", "") + "'");
            jsonObject.put("devType", Constants.DEVICE_TYPE_UDOO);
            jsonObject.put("devPortability", 0x01);
            jsonObject.put("latitude", latitude);
            jsonObject.put("longitude", longitude);
        }
        catch (JSONException exception) {
            exception.printStackTrace();
        }

        HttpService httpService = new HttpService();
        String res = httpService.executeConn(
                null, "POST",
                "http://teamc-iot.calit2.net/IOT/public/deviceReg",
                jsonObject
        );

        try {
            JSONObject resJson;
            resJson = new JSONObject(res);
            Log.d("BLCService", "DevReg sent, response was " + res);

            if (resJson.getInt("status") == 0) {
                if (BluetoothState.bluetoothConnector == null) {
                    BluetoothState.bluetoothConnector = new BluetoothConnector(deviceData, bluetoothHandler);
                    BluetoothState.bluetoothConnector.connect();
                }
            }
            else {
                Constants.MAC_UDOO = null;
                BluetoothState.isBLCConnected(false);
            }
        }
        catch (JSONException exception) {
            exception.printStackTrace();
        }*/

        if (BluetoothState.bluetoothConnector == null) {
            BluetoothState.bluetoothConnector = new BluetoothConnector(deviceData, bluetoothHandler);
            BluetoothState.bluetoothConnector.connect();
        }
    }

    public void disconnect() {
        if (BluetoothState.bluetoothConnector != null) {
            BluetoothState.bluetoothConnector.stop();
        }
    }
}
