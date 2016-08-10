package com.qi.airstat;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;

import com.qi.airstat.blc.BluetoothConnector;
import com.qi.airstat.blc.DeviceData;

public class BluetoothClassicService extends Service {
    private final IBinder localBinder = new LocalBinder();
    private final BluetoothHandler bluetoothHandler = new BluetoothHandler();

    private class BluetoothHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.MESSAGE_STATE_CHANGE:
                    if (msg.arg1 == Constants.STATE_CONNECTING) {
                    }
                    else if (msg.arg1 == Constants.STATE_CONNECTED) {
                        BluetoothState.isAirSensorAvailable(true);
                    }
                    else if (msg.arg1 == Constants.STATE_NONE) {

                    }

                    Intent intent = new Intent();
                    intent.putExtra(Constants.BLUETOOTH_MESSAGE_STATE_CHANGE, msg.arg1);
                    sendBroadcast(intent);
                    break;
                case Constants.MESSAGE_READ:
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

    @Override
    public void onCreate() {
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
        BluetoothConnector bluetoothConnector = BluetoothState.getBluetoothConnector();
        final int state = bluetoothConnector == null ? Constants.STATE_NONE : bluetoothConnector.getState();

        if (bluetoothConnector == null) {
            bluetoothConnector = new BluetoothConnector(deviceData, bluetoothHandler);
            bluetoothConnector.connect();
        }
        else if (state == Constants.STATE_CONNECTING ||
                  state == Constants.STATE_CONNECTED) {
            return;
        }
    }
}
