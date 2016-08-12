package com.qi.airstat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.qi.airstat.blc.DeviceData;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SensorDataOverviewActivity extends FragmentActivity {
    final private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private RelativeLayout locationDisabledLayout = null;
    private RelativeLayout bluetoothDisabledLayout = null;
    private RelativeLayout bluetoothUnsupportedLayout = null;
    private RelativeLayout polarDisabledLayout = null;
    private RelativeLayout udooDisabledLayout = null;
    private TextView hrValue = null;
    // Amount of labels are same as pages inside view pager.
    private TextView[] airLabels = new TextView[Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES];
    private LineChart[] airGraphs = new LineChart[Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES];
    private ViewPager airViewPager = null;

    // It'll be binded to the SensorDataUpdateService.
    // So, this messenger can send message to service.
    private Messenger messageTransmitter = null;

    private boolean isBinded = false;

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
                    updateAirDataGraph();
                    break;
                case Constants.QUEUED_HEART_RATE_DATA:
                    updateHeartRateData();
                    break;
                default:
                    break;
            }
        }
    }

    private BroadcastReceiver bluetoothBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            switch (action) {
                // Broadcast messages for Bluetooth Classic
                case Constants.BLUETOOTH_MESSAGE_MESSAGE_DEVICE_NAME:
                    break;
                case Constants.BLUETOOTH_MESSAGE_MESSAGE_TOAST:
                    break;
                case Constants.BLUETOOTH_MESSAGE_MESSAGE_WRITE:
                    break;
                case Constants.BLUETOOTH_MESSAGE_STATE_READ:
                    break;
                case Constants.BLUETOOTH_MESSAGE_STATE_CHANGE:
                    Log.d("SensorDataActivity", "Handler caught 'BLUETOOTH_MESSAGE_STATE_CHANGE'.");
                    int state = intent.getIntExtra(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE, Constants.STATE_NONE);

                    if (state == Constants.STATE_CONNECTED) {
                        udooDisabledLayout.setVisibility(View.GONE);
                        String startMessage = "start," + (long)(System.currentTimeMillis() / 1000L);
                        BluetoothState.bluetoothConnector.write(startMessage.getBytes());
                    }
                    break;
                // Broadcast messages for Bluetooth Light Energy
                case BluetoothLeService.ACTION_GATT_CONNECTED:
                    isBLEConnected = true;
                    break;
                case BluetoothLeService.ACTION_GATT_DISCONNECTED:
                    polarDisabledLayout.setVisibility(View.VISIBLE);
                    BLEService.disconnect();
                    isBLEConnected = false;
                    break;
                case BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED:
                    displayGattServices(BLEService.getSupportedGattServices());
                    polarDisabledLayout.setVisibility(View.GONE);
                    break;
                case BluetoothLeService.ACTION_DATA_AVAILABLE:
                    //displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    //val.setText(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    int signal = Integer.parseInt(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    Log.d("POLAR DATA", "" + signal);

                    String date = new SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date());
                    JSONObject dataset = new JSONObject();
                    JSONArray jsonArray = new JSONArray();

                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("timeStamp", date);
                        jsonObject.put("connectionID", 1);
                        jsonObject.put("heartrate", signal);
                        jsonObject.put("latitude", 111.11f);
                        jsonObject.put("longitude", 222.22f);

                        jsonArray.put(jsonObject);
                        dataset.put("HR", jsonArray);
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Log.d("JSON DATA", dataset.toString());

                    /*HttpService httpService = new HttpService();
                    String responseCode = httpService.executeConn(
                            SensorDataOverviewActivity.this,
                            "POST", "http://teamc-iot.calit2.net/IOT/public/rcv_json_data",
                            dataset
                    );*/

                    break;
                default:
                    break;
            }
        }
    };

    private BluetoothClassicService BLCService = null;
    private boolean isBLCServiceBound = false;
    private ServiceConnection BLCServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            BLCService = ((BluetoothClassicService.LocalBinder) iBinder).getService();
            isBLCServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            BLCService = null;
            isBLCServiceBound = false;
        }
    };

    private BluetoothLeService BLEService = null;
    private boolean isBLEServiceBound = false;
    private boolean isBLEConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    private final ServiceConnection BLEServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            BLEService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!BLEService.initialize()) {
                finish();
            }

            // Automatically connects to the device upon successful start-up initialization.
            //BLEService.connect(mDeviceAddress);
            isBLEServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            BLEService = null;
            isBLEServiceBound = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_data_overview);

        initialize();
        checkPermissions();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.BLUETOOTH_MESSAGE_MESSAGE_DEVICE_NAME);
        intentFilter.addAction(Constants.BLUETOOTH_MESSAGE_MESSAGE_TOAST);
        intentFilter.addAction(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE);
        intentFilter.addAction(Constants.BLUETOOTH_MESSAGE_STATE_READ);
        intentFilter.addAction(Constants.BLUETOOTH_MESSAGE_MESSAGE_WRITE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        registerReceiver(bluetoothBroadcastReceiver, intentFilter);

        if (BluetoothAdapter.getDefaultAdapter() != null) {
            bindService(
                    new Intent(this, BluetoothClassicService.class),
                    BLCServiceConnection,
                    Context.BIND_AUTO_CREATE
            );

            bindService(
                    new Intent(this, BluetoothLeService.class),
                    BLEServiceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }

        bindService(
                new Intent(this, FakeDataTransmitService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isBLEConnected) {
            BLEService.disconnect();
        }

        /*if (BluetoothState.getBluetoothConnector().getState() == Constants.STATE_CONNECTED) {
            //BLCService.de
        }*/
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isBinded) {
            unbindService(serviceConnection);
            isBinded = false;
        }

        if (isBLCServiceBound) {
            unbindService(BLCServiceConnection);
            isBLCServiceBound = false;
        }

        if (isBLEServiceBound) {
            unbindService(BLEServiceConnection);
            isBLEServiceBound = false;
        }

        unregisterReceiver(bluetoothBroadcastReceiver);
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
            case Constants.BLUETOOTH_CLASSIC_SCAN_REQEUST:
                if (resultCode == RESULT_OK) {
                    String mac = data.getStringExtra(Constants.BLUETOOTH_SCAN_RESULT_MAC);
                    //String dev = data.getStringExtra(Constants.BLC_SCAN_RESULT_DEV);

                    BluetoothDevice bluetoothDevice = bluetoothAdapter.getRemoteDevice(mac);
                    DeviceData deviceData = new DeviceData(bluetoothDevice, "Unknown Device");
                    BLCService.connect(deviceData);
                }
                break;
            case Constants.BLUETOOTH_LE_SCAN_REQUEST:
                if (resultCode == RESULT_OK) {
                    String mac = data.getStringExtra(Constants.BLUETOOTH_SCAN_RESULT_MAC);

                    BLEService.connect(mac);
                }
                break;
            default:
                break;
        }
    }

    private void initialize() {
        locationDisabledLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_location_disabled);
        bluetoothDisabledLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_bluetooth_disabled);
        bluetoothUnsupportedLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_bluetooth_not_supported);
        polarDisabledLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_heart_rate_disabled);
        udooDisabledLayout = (RelativeLayout)findViewById(R.id.rel_sensor_data_overview_air_data_disabled);

        udooDisabledLayout.setVisibility(View.VISIBLE);

        airViewPager = (ViewPager)findViewById(R.id.vp_air_data_graph);
        AirDataViewPagerAdaptor adaptor = (AirDataViewPagerAdaptor)airViewPager.getAdapter();

        for (int i = 0; i < Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES; ++i) {
            airGraphs[i] = ((AirDataViewPagerFragment)adaptor.getItem(i)).graph;
        }

        hrValue = (TextView)findViewById(R.id.tv_heart_rate_data_bpm_value);

        airLabels[Constants.AIR_LABEL_INDEX_PM25] = (TextView)findViewById(R.id.tv_air_data_brief_pm2_5_value);
        airLabels[Constants.AIR_LABEL_INDEX_TEMPERATURE] = (TextView)findViewById(R.id.tv_air_data_brief_temperature_value);
        airLabels[Constants.AIR_LABEL_INDEX_CO] = (TextView)findViewById(R.id.tv_air_data_brief_co_value);
        airLabels[Constants.AIR_LABEL_INDEX_SO2] = (TextView)findViewById(R.id.tv_air_data_brief_so2_value);
        airLabels[Constants.AIR_LABEL_INDEX_NO2] = (TextView)findViewById(R.id.tv_air_data_brief_no2_value);
        airLabels[Constants.AIR_LABEL_INDEX_O3] = (TextView)findViewById(R.id.tv_air_data_brief_o3_value);

        DatabaseManager databaseManager = new DatabaseManager(this);
        databaseManager.rebuild();
        databaseManager.close();
    }

    private void checkPermissions() {
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

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = "<UNKNOWN SERVICE>";
        String unknownCharaString = "<UNKNOWN CHARACTERISTIC>";
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    Constants.LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(Constants.LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        Constants.LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(Constants.LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

        if (mGattCharacteristics != null) {
            final BluetoothGattCharacteristic characteristic =
                    mGattCharacteristics.get(3).get(0);
            final int charaProp = characteristic.getProperties();

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    BLEService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                BLEService.readCharacteristic(characteristic);
            }

            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                BLEService.setCharacteristicNotification(
                        characteristic, true);
            }
        }
    }

    private void updateHeartRateData() {
        DatabaseManager databaseManager = new DatabaseManager(this);
        SQLiteDatabase database = databaseManager.getReadableDatabase();
        Cursor cursor = null;

        int signal = 0;

        LineChart lineChart = (LineChart)findViewById(R.id.lic_heart_rate_data_graph);

        if (lineChart != null) {
            LineData lineData = lineChart.getData();

            if (lineData != null) {
                cursor = database.rawQuery("SELECT * FROM heart_rate", null);

                if (cursor != null && cursor.moveToLast()) {
                    signal = cursor.getInt(cursor.getColumnIndex(Constants.DATABASE_HEART_RATE_COLUMN_HEART_RATE));

                    do {
                        if (lineData != null) {
                            if (lineData.getEntryCount() > Constants.SENSOR_REALTIME_RECORD_LIMIT) {
                                database.execSQL("DELETE FROM heart_rate WHERE id IN (SELECT id FROM heart_rate ORDER BY id ASC LIMIT 1)");
                                ILineDataSet set = lineData.getDataSetByIndex(0);
                                set.removeFirst();

                                for (int j = 0; j < set.getEntryCount(); ++j) {
                                    Entry entry = set.getEntryForIndex(j);
                                    entry.setX(entry.getX() - 1);
                                }
                            }

                            lineData.addEntry(new Entry(lineData.getEntryCount(), signal), 0);
                        }
                    } while (cursor.moveToNext());
                }

                database.close();

                hrValue.setText(String.valueOf(signal));

                lineData.notifyDataChanged();
                lineChart.notifyDataSetChanged();
                lineChart.invalidate();
            }
        }
    }

    private void updateAirDataGraph() {
        LineData[] lineData = new LineData[Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES];
        float[] data = new float[Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES];

        AirDataViewPagerAdaptor adaptor = (AirDataViewPagerAdaptor)airViewPager.getAdapter();

        for (int i = 0; i < Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES; ++i) {
            airGraphs[i] = ((AirDataViewPagerFragment)adaptor.getItem(i)).graph;
        }

        for (int i = 0; i < Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES; ++i) {
            if (airGraphs[i] != null) {
                lineData[i] = airGraphs[i].getData();
            }
        }

        DatabaseManager databaseManager = new DatabaseManager(this);
        SQLiteDatabase database = databaseManager.getReadableDatabase();
        Cursor cursor = null;

        cursor = database.rawQuery("SELECT * FROM air", null);

        if (cursor != null && cursor.moveToLast()) {
            do {
                data[Constants.AIR_LABEL_INDEX_PM25] = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_PM25));
                data[Constants.AIR_LABEL_INDEX_TEMPERATURE] = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_TEMPERATURE));
                data[Constants.AIR_LABEL_INDEX_CO] = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_CO));
                data[Constants.AIR_LABEL_INDEX_SO2] = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_SO2));
                data[Constants.AIR_LABEL_INDEX_NO2] = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_NO2));
                data[Constants.AIR_LABEL_INDEX_O3] = cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_O3));

                for (int i = 0; i < Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES; ++i) {
                    if (lineData[i] != null) {
                        if (lineData[i].getEntryCount() > Constants.SENSOR_REALTIME_RECORD_LIMIT) {
                            database.execSQL("DELETE FROM air WHERE id IN (SELECT id FROM air ORDER BY id ASC LIMIT 1)");
                            ILineDataSet set = lineData[i].getDataSetByIndex(0);
                            set.removeFirst();

                            for (int j = 0; j < set.getEntryCount(); ++j) {
                                Entry entry = set.getEntryForIndex(j);
                                entry.setX(entry.getX() - 1);
                            }
                        }

                        lineData[i].addEntry(new Entry(lineData[i].getEntryCount(), data[i]), 0);
                    }
                }
            } while(cursor.moveToNext());
        }

        cursor.close();
        database.close();

        for (int i = 0; i < Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES; ++i) {
            airLabels[i].setText(String.format("%.2f", data[i]));

            if (lineData[i] != null) {
                lineData[i].notifyDataChanged();
            }

            if (airGraphs[i] != null) {
                airGraphs[i].notifyDataSetChanged();
                airGraphs[i].invalidate();
            }
        }
    }

    public void onClickRequestLocationPermission(View view) {
        LocationState.requestLocationPermission(this);
    }

    public void onClickRequestBluetoothPermission(View view) {
        BluetoothState.requestBluetoothPermission(this);
    }

    public void onClickAirDataItem(View view) {
        int index = 0;

        switch (view.getId()) {
            case R.id.lil_air_data_brief_pm2_5:
                index = Constants.AIR_LABEL_INDEX_PM25;
                break;
            case R.id.lil_air_data_brief_temperature:
                index = Constants.AIR_LABEL_INDEX_TEMPERATURE;
                break;
            case R.id.lil_air_data_brief_co:
                index = Constants.AIR_LABEL_INDEX_CO;
                break;
            case R.id.lil_air_data_brief_so2:
                index = Constants.AIR_LABEL_INDEX_SO2;
                break;
            case R.id.lil_air_data_brief_no2:
                index = Constants.AIR_LABEL_INDEX_NO2;
                break;
            case R.id.lil_air_data_brief_o3:
                index = Constants.AIR_LABEL_INDEX_O3;
                break;
        }

        airViewPager.setCurrentItem(index);
    }

    public void onClickConnectAirDevice(View view) {
        BluetoothState.displayClassicScanner(this);
    }

    public void onClickConnectHeartRateDevice(View view) {
        BluetoothState.displayLightEnergyScanner(this);
    }
}
