package com.qi.airstat;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.support.v4.app.FragmentActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;

import java.util.ArrayList;

public class SensorDataOverviewActivity extends FragmentActivity {
    // It'll be bound to the SensorDataUpdateService.
    // So, this messenger can send message to service.
    private Messenger messageTransmitter = null;
    private Context activityContext = this;

    private boolean isBinded = false;

    // It'll receive message from SensorDataUpdateService and will behave for each pre-defined message.
    // That is, this messenger will be client for service.
    final private Messenger messageReceiver = new Messenger(new SensorDataOverviewHandler());

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

    private class SensorDataOverviewHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            SQLiteDatabase database = null;
            DatabaseManager databaseManager = null;
            Cursor cursor = null;
            ArrayList<Entry> entries = new ArrayList<Entry>();

            switch (msg.what) {
                case Constants.SERVICE_AIR_DATA_UPDATED:
                    // This part is deprecated; will be replaced to use content provider.
                    /*databaseManager = new DatabaseManager(activityContext);
                    database = databaseManager.getReadableDatabase();

                    cursor = database.rawQuery("SELECT * FROM air", null);

                    LineChart lineChart = (LineChart)findViewById(R.id.lic_heart_rate_data_graph);
                    LineData data = lineChart.getLineData();
                    ILineDataSet set = data.getDataSetByIndex(0);

                    if (set == null)
                        set = new LineDataSet(null, "DynChart");

                    if (cursor != null && cursor.moveToFirst()) {
                        do {
                            data.addEntry(new Entry(set.getEntryCount(), cursor.getFloat(cursor.getColumnIndex(Constants.DATABASE_AIR_COLUMN_CO))), 0);
                        } while (cursor.moveToNext());
                    }

                    database.close();

                    data.notifyDataChanged();
                    lineChart.notifyDataSetChanged();*/
                    break;
                case Constants.SERVICE_HEART_RATE_DATA_UPDATED:
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

        DatabaseManager databaseManager = new DatabaseManager(this);
        databaseManager.rebuild();
        databaseManager.close();
        databaseManager = null;
    }

    @Override
    protected void onStart() {
        super.onStart();

        bindService(
                new Intent(this, SensorDataUpdateService.class),
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
}
