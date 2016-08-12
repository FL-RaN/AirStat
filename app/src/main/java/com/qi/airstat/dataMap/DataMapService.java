package com.qi.airstat.dataMap;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class DataMapService extends Service {
    private ArrayList<Messenger> clients = new ArrayList<>();
    private Messenger messenger = new Messenger(new IncommingHander());
    private Handler thread = new Handler();
    private Runnable runnable = null;
    private boolean isActivityAlive = false;

    public HttpURLConnection conn;
    String strUrl = Constants.HTTP_STR_URL_ONGOING_SESSION;
    URL url;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    private void getOngoingSession() {
        runnable = new Runnable() {
            @Override
            public void run() {
                HttpService httpService = new HttpService();
                String rcvdData = httpService.executeConn(null, "GET", Constants.HTTP_STR_URL_ONGOING_SESSION, (ArrayList) null);

                try {
                    if (
                            rcvdData.equalsIgnoreCase("MalformedURLException") ||
                                    rcvdData.equalsIgnoreCase("Exception") ||
                                    rcvdData.equalsIgnoreCase("IOException") ||
                                    rcvdData.equalsIgnoreCase("unsuccessful")
                            ) {
                        rcvdData = "";
                    }
                } catch (NullPointerException e) {
                    /*PASS*/
                }

                for (int i = 0; i < clients.size(); i++) {
                    try {
                        clients.get(i).send(Message.obtain(null, Constants.SERVICE_DATA_MAP_DRAW_MAP, rcvdData));
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    // 그림 그리게 메세지 보내기
                }

                if (isActivityAlive) { //flag
                    thread.postDelayed(runnable, Constants.HTTP_DATA_MAP_GET_ONGOING_SESSION_TIME_QUALTUM);
                } else {

                }
            }
        };

        thread.post(runnable);
    }

    class IncommingHander extends Handler {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case Constants.CLIENT_REGISTER:
                    clients.add(msg.replyTo);
                    isActivityAlive = true;
                    getOngoingSession();
                    break;
                case Constants.CLIENT_UNREGISTER:
                    clients.remove(msg.replyTo);
                    isActivityAlive = false;
                    stopSelf();
                    break;
            }
        }
    }
}