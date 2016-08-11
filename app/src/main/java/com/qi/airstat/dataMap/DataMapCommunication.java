package com.qi.airstat.dataMap;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;
import com.qi.airstat.iHttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/9/2016.
 */
public class DataMapCommunication extends AsyncTask<Void, Void, Void> implements iHttpConnection {

    private HttpService httpService;
    private boolean flow = false;
    private String result = "";
    private DataMapActivity dataMapActivity;
    private String type = "GET";
    private Context context;
    private ArrayList<String> params = null;

    private DataMapCommunication() {
    }

    public DataMapCommunication(Context context, DataMapActivity dataMapActivity) {
        this.context = context;
        this.dataMapActivity = dataMapActivity;
    }

    @Override
    public String executeHttpConn() {
        httpService = new HttpService();
        return httpService.executeConn(DataMapActivity.context, type, Constants.HTTP_STR_URL_ONGOING_SESSION, params);
    }

    public void stopThread() {
        this.flow = false;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        flow = true;
        Looper.prepare();
        while (flow) {
//            onPreExecute();
            try {
                resultHandler(executeHttpConn());
                publishProgress();
                Thread.sleep(Constants.HTTP_DATA_MAP_GET_ONGOING_SESSION_TIME_QUALTUM);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Looper.loop();
        return null;
    }

    public void resultHandler(String result) {
        Log.w("result", result + "");
        JSONObject rcvdData = null;

        try {
            rcvdData = new JSONObject(result);
            parseOngoingSessionData(rcvdData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }
    }

    public void parseOngoingSessionData(JSONObject rcvdData) {
        try {
            int i = 1;
            JSONObject eachData = rcvdData.getJSONObject("" + (i++));

            while (eachData != null) {

                int connectionID = eachData.getInt(Constants.HTTP_DATA_MAP_ONGOING_SESSION_CID);
                long timeStamp = eachData.getLong(Constants.HTTP_DATA_MAP_ONGOING_SESSION_TIME_STAMP);
                JSONObject airData = eachData.getJSONObject(Constants.HTTP_DATA_MAP_ONGOING_SESSION_AIR);

                double temperature = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_TEMP);
                double co = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_CO);
                double so2 = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_SO2);
                double no2 = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_NO2);
                double o3 = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_O3);
                double pm = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_PM);
                double lat = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_LAT);
                double lng = airData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_LNG);

                dataMapActivity.refreshMarker(connectionID, timeStamp, new DataMapDataSet(temperature, co, so2, no2, o3, pm), lat, lng);
                try {
                    eachData = rcvdData.getJSONObject("" + (i++));
                } catch (JSONException e) {
                    eachData = null;
                }
            }
            DataMapCurrentUser.getInstance().setCurrentUserData((float) Math.random() * 400 + 1, (float) Math.random() * 20, (float) Math.random() * 600, (float) Math.random() * 300 + 1700, (float) Math.random() * 100 + 500, (float) Math.random() * 100 + 400);
//            DataMapCurrentUser.getInstance().setCurrentUserData(0,50.4f,1004,2049,604,500);
            dataMapActivity.refreshMarker(DataMapCurrentUser.create());
            for (int j = 1; j < 10; j++) {
                dataMapActivity.refreshMarker(10 * j,
                        162737272727l,
                        new DataMapDataSet((float) Math.random() * 400, (float) Math.random() * 50, (float) Math.random() * 60, (float) Math.random() * 200, (float) Math.random() * 60, (float) Math.random() * 50),
                        dataMapActivity.START_POINT.latitude + (j / 50d), dataMapActivity.START_POINT.longitude + (j / 200d));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        DataMapActivity.mClusterManager.clearItems();

        for (DataMapMarker marker : DataMapActivity.markers) {
            DataMapActivity.mClusterManager.addItem(marker);
        }

        DataMapActivity.mClusterManager.cluster();
    }

    private void makeToast(String msg) {
        if (context != null)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}