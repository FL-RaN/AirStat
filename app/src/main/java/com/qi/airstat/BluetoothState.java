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

    static private boolean isBLCConnected = false;
    static private boolean isBLEConnected = false;

    static private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    static public BluetoothConnector bluetoothConnector = null;

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

    static public boolean isBluetoothAvailable()    { return bluetoothAdapter.isEnabled();  }
    static public boolean isBLCConnected()          { return isBLCConnected; }
    static public boolean isBLEConnected()          { return isBLEConnected; }

    static public void isBLCConnected(boolean isBLCConnected)   { BluetoothState.isBLCConnected = isBLCConnected; }
    static public void isBLEConnected(boolean isBLEConnected)   { BluetoothState.isBLEConnected = isBLEConnected;         }

    static public void requestBluetoothPermission(Context context) {
        if (bluetoothAdapter != null) {
            if (!bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                ((Activity)context).startActivityForResult(intent, Constants.BLUETOOTH_PERMISSION_REQUEST);
            }
        }
    }

    static public void displayClassicScanner(Context context) {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(context, BluetoothScanActivity.class);
                ((Activity)context).startActivityForResult(intent, Constants.BLUETOOTH_CLASSIC_SCAN_REQEUST);
            }
        }
    }

    static public void displayLightEnergyScanner(Context context) {
        if (bluetoothAdapter != null) {
            if (bluetoothAdapter.isEnabled()) {
                Intent intent = new Intent(context, BluetoothScanActivity.class);
                ((Activity)context).startActivityForResult(intent, Constants.BLUETOOTH_LE_SCAN_REQUEST);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {

    }
}