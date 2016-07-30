package com.qi.airstat;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;

public class FakeDataTransmitService extends Service {
    final private Messenger messageReceiver = new Messenger(new FakeDataTransmitServiceHandler());

    private ArrayList<Messenger> clients = new ArrayList<Messenger>();
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

        runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    for (int i = 0; i < clients.size(); ++i) {
                        clients.get(i).send(Message.obtain(null, Constants.QUEUED_AIR_DATA));
                        clients.get(i).send(Message.obtain(null, Constants.QUEUED_HEART_RATE_DATA));
                    }
                }
                catch (RemoteException exception) {
                    exception.printStackTrace();
                }

                handler.postDelayed(runnable, Constants.SENSOR_DATA_UPDATE_SERVICE_INTERVAL);
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
