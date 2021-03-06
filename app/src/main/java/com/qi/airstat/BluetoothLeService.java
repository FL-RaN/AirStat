/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qi.airstat;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing connection and data communication with a GATT server hosted on a
 * given Bluetooth LE device.
 */
public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private LocationManager locationManager = null;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.randomcube.bluetoothlesandbox.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.randomcube.bluetoothlesandbox.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.randomcube.bluetoothlesandbox.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.randomcube.bluetoothlesandbox.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.randomcube.bluetoothlesandbox.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    private double latitude = 0;
    private double longitude = 0;

    @Override
    public void onCreate() {
        try {
            locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 1, locationListener);
        }
        catch (SecurityException exception) {
            exception.printStackTrace();
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(final Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
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

    // Implements callback methods for GATT events that the app cares about.  For example,
    // connection change and services discovered.
    private final BluetoothGattCallback mGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            String intentAction;
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                intentAction = ACTION_GATT_CONNECTED;
                mConnectionState = STATE_CONNECTED;
                broadcastUpdate(intentAction);
                BluetoothState.isBLEConnected(true);
                Log.i(TAG, "Connected to GATT server.");
                // Attempts to discover services after successful connection.
                Log.i(TAG, "Attempting to start service discovery:" +
                        mBluetoothGatt.discoverServices());

                try {
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
                    jsonObject.put("devMAC", "x'" + Constants.MAC_POLAR.replaceAll(":", "") + "'");
                    jsonObject.put("devType", Constants.DEVICE_TYPE_POLAR);
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
                    Constants.CID_BLE = Integer.parseInt(resJson.getString("connectionID"));
                }
                catch (JSONException exception) {
                    exception.printStackTrace();
                }
            }
            else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                intentAction = ACTION_GATT_DISCONNECTED;
                mConnectionState = STATE_DISCONNECTED;
                BluetoothState.isBLEConnected(false);

                try {
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
                    jsonObject.put("userID", Constants.UID);
                    jsonObject.put("connectionID", Constants.CID_BLE);
                    jsonObject.put("devMAC", "x'" + Constants.MAC_POLAR.replaceAll(":", "") + "'");
                    jsonObject.put("devType", Constants.DEVICE_TYPE_POLAR);
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

                Constants.MAC_POLAR = null;
                Constants.CID_BLE = Constants.CID_NONE;

                Log.d("BLEService DISC RES", response);

                Log.i(TAG, "Disconnected from GATT server.");
                broadcastUpdate(intentAction);
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        int signal = 0;

        // This is special handling for the Heart Rate Measurement profile.  Data parsing is
        // carried out as per profile specifications:
        // http://developer.bluetooth.org/gatt/characteristics/Pages/CharacteristicViewer.aspx?u=org.bluetooth.characteristic.heart_rate_measurement.xml
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            }
            else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }

            final int heartRate = characteristic.getIntValue(format, 1);
            signal = heartRate;
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        }
        else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for(byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());

                signal = Integer.parseInt(new String(data));
            }
        }

        DatabaseManager databaseManager = new DatabaseManager(BluetoothLeService.this);
        SQLiteDatabase database = databaseManager.getWritableDatabase();

        ContentValues values = new ContentValues();
        String date = new SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date());

        values.put(Constants.DATABASE_COMMON_COLUMN_TIME_STAMP, date);
        values.put(Constants.DATABASE_HEART_RATE_COLUMN_HEART_RATE, signal);
        database.insert(Constants.DATABASE_HEART_RATE_TABLE, null, values);

        database.close();

        try {
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
            item.put("connectionID", Constants.CID_BLE);
            item.put("heartrate", signal);
            item.put("latitude", latitude);
            item.put("longitude", longitude);

            reformedArray.put(item);
            reformedObject.put("HR", reformedArray);
        }
        catch (JSONException e) {
            e.printStackTrace();
        }

        HttpService httpService = new HttpService();
        String responseCode = httpService.executeConn(
                null,
                "POST", "http://teamc-iot.calit2.net/IOT/public/rcv_json_data",
                reformedObject
        );

        sendBroadcast(intent);
    }

    public class LocalBinder extends Binder {
        BluetoothLeService getService() {
            return BluetoothLeService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // After using a given device, you should make sure that BluetoothGatt.close() is called
        // such that resources are cleaned up properly.  In this particular example, close() is
        // invoked when the UI is disconnected from the Service.
        close();
        BluetoothState.isBLEConnected(false);

        return super.onUnbind(intent);
    }

    private final IBinder mBinder = new LocalBinder();

    /**
     * Initializes a reference to the local Bluetooth adapter.
     *
     * @return Return true if the initialization is successful.
     */
    public boolean initialize() {
        // For API level 18 and above, get a reference to BluetoothAdapter through
        // BluetoothManager.
        if (mBluetoothManager == null) {
            mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            if (mBluetoothManager == null) {
                Log.e(TAG, "Unable to initialize BluetoothManager.");
                return false;
            }
        }

        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Log.e(TAG, "Unable to obtain a BluetoothAdapter.");
            return false;
        }

        return true;
    }

    /**
     * Connects to the GATT server hosted on the Bluetooth LE device.
     *
     * @param address The device address of the destination device.
     *
     * @return Return true if the connection is initiated successfully. The connection result
     *         is reported asynchronously through the
     *         {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     *         callback.
     */
    public boolean connect(final String address) {
        if (mBluetoothAdapter == null || address == null) {
            Log.w(TAG, "BluetoothAdapter not initialized or unspecified address.");
            BluetoothState.isBLEConnected(false);
            return false;
        }

        // Previously connected device.  Try to reconnect.
        if (mBluetoothDeviceAddress != null && address.equals(mBluetoothDeviceAddress)
                && mBluetoothGatt != null) {
            Log.d(TAG, "Trying to use an existing mBluetoothGatt for connection.");
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
                return true;
            } else {
                BluetoothState.isBLEConnected(false);
                return false;
            }
        }

        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found.  Unable to connect.");
            BluetoothState.isBLEConnected(false);
            return false;
        }
        // We want to directly connect to the device, so we are setting the autoConnect
        // parameter to false.
        mBluetoothGatt = device.connectGatt(this, false, mGattCallback);
        Log.d(TAG, "Trying to create a new connection.");
        mBluetoothDeviceAddress = address;
        mConnectionState = STATE_CONNECTING;
        Constants.MAC_POLAR = mBluetoothDeviceAddress;

        try {
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
            jsonObject.put("devMAC", "x'" + Constants.MAC_POLAR.replaceAll(":", "") + "'");
            jsonObject.put("devType", Constants.DEVICE_TYPE_POLAR);
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
                "http://teamc-iot.calit2.net/IOT/public/deviceReg",
                jsonObject
        );

        try {
            JSONObject resJson;
            resJson = new JSONObject(res);
            Log.d("BLEService", "DevReg sent, response was " + res);

            if (resJson.getInt("status") == 0) {
                return true;
            }
            else {
                mConnectionState = STATE_DISCONNECTED;
                Constants.MAC_POLAR = null;
                BluetoothState.isBLEConnected(false);
                return false;
            }
        }
        catch (JSONException exception) {
            exception.printStackTrace();
            return false;
        }
    }

    /**
     * Disconnects an existing connection or cancel a pending connection. The disconnection result
     * is reported asynchronously through the
     * {@code BluetoothGattCallback#onConnectionStateChange(android.bluetooth.BluetoothGatt, int, int)}
     * callback.
     */
    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }

        BluetoothState.isBLEConnected(false);
        mBluetoothGatt.disconnect();
    }

    /**
     * After using a given BLE device, the app must call this method to ensure resources are
     * released properly.
     */
    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;

        BluetoothState.isBLEConnected(false);
    }

    /**
     * Request a read on a given {@code BluetoothGattCharacteristic}. The read result is reported
     * asynchronously through the {@code BluetoothGattCallback#onCharacteristicRead(android.bluetooth.BluetoothGatt, android.bluetooth.BluetoothGattCharacteristic, int)}
     * callback.
     *
     * @param characteristic The characteristic to read from.
     */
    public void readCharacteristic(BluetoothGattCharacteristic characteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * Enables or disables notification on a give characteristic.
     *
     * @param characteristic Characteristic to act on.
     * @param enabled If true, enable notification.  False otherwise.
     */
    public void setCharacteristicNotification(BluetoothGattCharacteristic characteristic,
                                              boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.w(TAG, "BluetoothAdapter not initialized");
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        // This is specific to Heart Rate Measurement.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            BluetoothGattDescriptor descriptor = characteristic.getDescriptor(
                    UUID.fromString(SampleGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            mBluetoothGatt.writeDescriptor(descriptor);
        }
    }

    /**
     * Retrieves a list of supported GATT services on the connected device. This should be
     * invoked only after {@code BluetoothGatt#discoverServices()} completes successfully.
     *
     * @return A {@code List} of supported services.
     */
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }
}
