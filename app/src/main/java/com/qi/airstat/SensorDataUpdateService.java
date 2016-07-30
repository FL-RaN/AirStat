package com.qi.airstat;

import android.app.Service;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.qi.airstat.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

/*
 *  This class keep updates sensor data from heart rate sensor and
 *  air data in background.
 */
public class SensorDataUpdateService extends Service {
    final private Messenger messageReceiver = new Messenger(new SensorDataUpdateHandler());
    final private Context serviceContext = this;

    private Messenger messageTransmitter = null;
    private ArrayList<Messenger> clients = new ArrayList<Messenger>();
    private SensorDataUpdateHandler handler = null;
    private Runnable runnable = null;

    private boolean isBinded = false;

    private class SensorDataUpdateHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            DatabaseManager databaseManager = null;
            SQLiteDatabase database = null;
            ContentValues values = null;
            String date = null;

            switch (msg.what) {
                case Constants.QUEUED_AIR_DATA:
                    databaseManager = new DatabaseManager(serviceContext);
                    database = databaseManager.getReadableDatabase();
                    values = new ContentValues();

                    date = new SimpleDateFormat("yymmddhhmmss").format(new java.util.Date());
                    values.put(Constants.DATABASE_COMMON_COLUMN_TIME_STAMP, date);
                    values.put(Constants.DATABASE_AIR_COLUMN_CO, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_CO2, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_SO2, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_NO2, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_O3, (float)Math.random() * 500f);
                    values.put(Constants.DATABASE_AIR_COLUMN_PM25, (float)Math.random() * 500f);

                    database.insert(Constants.DATABASE_AIR_TABLE, null, values);

                    database.close();

                    try {
                        for (int i = 0; i < clients.size(); ++i) {
                            clients.get(i).send(Message.obtain(null, Constants.SERVICE_AIR_DATA_UPDATED));
                        }
                    }
                    catch (RemoteException exception) {
                        exception.printStackTrace();
                    }
                    break;
                case Constants.QUEUED_HEART_RATE_DATA:
                    databaseManager = new DatabaseManager(serviceContext);
                    database = databaseManager.getReadableDatabase();
                    values = new ContentValues();

                    date = new SimpleDateFormat("yymmddhhmmss").format(new java.util.Date());
                    values.put(Constants.DATABASE_COMMON_COLUMN_TIME_STAMP, date);
                    values.put(Constants.DATABASE_HEART_RATE_COLUMN_HEART_RATE, (float)Math.random() * 100f);

                    database.insert(Constants.DATABASE_HEART_RATE_TABLE, null, values);

                    database.close();

                    try {
                        for (int i = 0; i < clients.size(); ++i) {
                            clients.get(i).send(Message.obtain(null, Constants.SERVICE_HEART_RATE_DATA_UPDATED));
                        }
                    }
                    catch (RemoteException exception) {
                        exception.printStackTrace();
                    }
                    break;
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

    private ServiceConnection serviceConnection = new ServiceConnection() {
        // Callback function on connect service.
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Bind service with messenger.
            messageTransmitter = new Messenger(service);
            isBinded = true;

            try {
                // Send service to register as new client.
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

    @Override
    public void onCreate() {
        /*handler = new SensorDataUpdateHandler();

        runnable = new Runnable() {
            @Override
            public void run() {
                handler.postDelayed(runnable, Constants.SENSOR_DATA_UPDATE_SERVICE_INTERVAL);
            }
        };

        handler.post(runnable);*/

        bindService(
                new Intent(this, FakeDataTransmitService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE
        );
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (isBinded) {
            unbindService(serviceConnection);
            isBinded = false;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messageReceiver.getBinder();
    }
}
