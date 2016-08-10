package com.qi.airstat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;

import com.qi.airstat.blc.BluetoothConnector;
import com.qi.airstat.blc.DeviceData;

public class BluetoothState extends BroadcastReceiver {
    static final private BluetoothState instance = new BluetoothState();

    static private boolean isUserSwitchedHeartRateDataOn = false;
    static private boolean isUserSwitchedAirDataOn = false;
    static private boolean isHeartRateSensorAvailable = false;
    static private boolean isAirSensorAvailable = false;

    static private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /*static private BluetoothDevice connectedDevice = null;
    static private Handler handler = null;
    static private String deviceName = null;
    static private String deviceAddress = null;
    static private int deviceState = Constants.STATE_NONE;*/
    static private BluetoothConnector bluetoothConnector = null;

    private BluetoothState() { /* DO NOTHING */ }

    static public BluetoothState getInstance() { return instance; }

    static public boolean isBluetoothSupported(Context context) {
        if (bluetoothAdapter == null) {
            return false;
        }

        if (!context.getPackageManager().
                hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            return false;
        }

        return true;
    }

    static public boolean isBluetoothAvailable()            { return bluetoothAdapter.isEnabled();  }
    static public boolean isUserSwitchedHeartRateDataOn()   { return isUserSwitchedHeartRateDataOn; }
    static public boolean isUserSwitchedAirDataOn()         { return isUserSwitchedAirDataOn;       }
    static public boolean isAirSensorAvailable()            { return isAirSensorAvailable;          }
    static public boolean isHeartRateSensorAvailable()      { return isHeartRateSensorAvailable;    }
    static public BluetoothConnector getBluetoothConnector() { return bluetoothConnector;            }

    static public void isUserSwitchedHeartRateDataOn(boolean isUserSwitchedHeartRateDataOn) { BluetoothState.isUserSwitchedHeartRateDataOn = isUserSwitchedHeartRateDataOn; }
    static public void isUserSwitchedAirDataOn(boolean isUserSwitchedAirDataOn)             { BluetoothState.isUserSwitchedAirDataOn = isUserSwitchedAirDataOn;             }
    static public void isAirSensorAvailable(boolean isAirSensorAvailable)                   { BluetoothState.isAirSensorAvailable = isAirSensorAvailable;                   }
    static public void isHeartRateSensorAvailable(boolean isHeartRateSensorAvailable)       { BluetoothState.isHeartRateSensorAvailable = isHeartRateSensorAvailable;       }

    static public void requestBluetoothPermission(Context context) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity)context).startActivityForResult(intent, Constants.BLUETOOTH_PERMISSION_REQUEST);
            }
        }
        else {
            // Handle if bluetooth is not supported
        }
    }

    static public void displayScanner(Context context) {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(context, BluetoothScanActivity.class);
                ((Activity)context).startActivityForResult(intent, Constants.BLUETOOTH_SCAN_REQEUST);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}
