package com.qi.airstat;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class FakeDataTransmitService extends Service {
    final private Context serviceContext = this;
    final private Messenger messageReceiver = new Messenger(new FakeDataTransmitServiceHandler());

    private BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private boolean isBluetoothSupported = false;

    private ArrayList<Messenger> clients = new ArrayList<>();
    private FakeDataTransmitServiceHandler handler = null;
    private Runnable runnable = null;

    private class FakeDataTransmitServiceHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.CLIENT_REGISTER:
                    clients.add(msg.replyTo);
                    break;
                case Constants.CLIENT_UNREGISTER:
                    clients.remove(msg.replyTo);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onCreate() {
        handler = new FakeDataTransmitServiceHandler();
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        runnable = new Runnable() {
            @Override
            public void run() {
                DatabaseManager databaseManager = new DatabaseManager(serviceContext);
                SQLiteDatabase database = databaseManager.getWritableDatabase();

                if (bluetoothAdapter == null) {
                    isBluetoothSupported = false;

                    for (int i = 0; i < clients.size(); ++i) {
                        try {
                            clients.get(i).send(Message.obtain(null, Constants.SERVICE_BLUETOOTH_NOT_SUPPORTED));
                        }
                        catch (RemoteException exception) {
                            exception.printStackTrace();
                        }
                    }
                }
                else {
                    isBluetoothSupported = true;

                    ContentValues values = new ContentValues();
                    String date = new SimpleDateFormat("yyMMddHHmmss").format(new java.util.Date());
                    values.put(Constants.DATABASE_COMMON_COLUMN_TIME_STAMP, date);
                    values.put(Constants.DATABASE_AIR_COLUMN_LAT, 0.0f);
                    values.put(Constants.DATABASE_AIR_COLUMN_LON, 0.0f);
                    values.put(Constants.DATABASE_AIR_COLUMN_CO, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_TEMPERATURE, (float)Math.random() * 38f);
                    values.put(Constants.DATABASE_AIR_COLUMN_SO2, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_NO2, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_O3, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_PM25, (float)Math.random() * 500f);

                    database.insert(Constants.DATABASE_AIR_TABLE, null, values);

                    values = new ContentValues();
                    values.put(Constants.DATABASE_COMMON_COLUMN_TIME_STAMP, date);
                    values.put(Constants.DATABASE_HEART_RATE_COLUMN_HEART_RATE, (int)(Math.random() * (135 - 70) + 70));

                    database.insert(Constants.DATABASE_HEART_RATE_TABLE, null, values);

                    database.close();

                    try {
                        for (int i = 0; i < clients.size(); ++i) {
                            clients.get(i).send(Message.obtain(null, Constants.QUEUED_AIR_DATA));
                            clients.get(i).send(Message.obtain(null, Constants.QUEUED_HEART_RATE_DATA));
                        }
                    }
                    catch (RemoteException exception) {
                        exception.printStackTrace();
                    }

                    handler.postDelayed(runnable, Constants.FAKE_DATA_SERVICE_UPDATE_INTERVAL);
                }
            }
        };

        handler.post(runnable);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messageReceiver.getBinder();
    }
}
