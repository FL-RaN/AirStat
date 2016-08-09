package com.qi.airstat.dataMap;

import android.content.Context;
import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;
import com.qi.airstat.iHttpConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/5/2016.
 */
public class DataMapCommunication extends AsyncTask<Void, Void, Void> implements iHttpConnection {
    private HttpService httpService;
    private boolean flow = true;
    private String result = null;
    private DataMapActivity dataMapActivity;

    private Context context;
    ArrayList<String> params;


    public DataMapCommunication(Context context, DataMapActivity dataMapActivity) {
        this.context = context;
        this.dataMapActivity = dataMapActivity;
    }

    public void stop() {
        this.flow = false;
        this.cancel(true);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        params = new ArrayList<>();
        params.add(Constants.HTTP_MSG_ID);
        params.add(Constants.HTTP_REQUEST_ONGOING_SESSION + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MIN_LAT);
        params.add(DataMapCurrentLocation.getInstance().getMinLat() + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MAX_LAT);
        params.add(DataMapCurrentLocation.getInstance().getMaxLat() + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MIN_LNG);
        params.add(DataMapCurrentLocation.getInstance().getMinLng() + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MAX_LNG);
        params.add(DataMapCurrentLocation.getInstance().getMaxLng() + "");

    }

    @Override
    protected Void doInBackground(Void... voids) {
        while (flow) {

            result = executeHttpConn();
            resultHandler(result);

            try {
                Thread.sleep(Constants.HTTP_DATA_MAP_ONGOING_TIME_QUALTUM);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        return null;
    }

    @Override
    public String executeHttpConn() {
        httpService = new HttpService();
        return httpService.executeConn(context, Constants.HTTP_STR_URL_ONGOING_SESSION, params);
    }

    public void resultHandler(String result) {
        JSONObject rcvdData = null;
        int responseCode = -1;

        try {
            rcvdData = new JSONObject(result);
            responseCode = rcvdData.getInt(Constants.HTTP_RESPONSE_RESULT);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        switch (responseCode) {
            case Constants.HTTP_RESPONSE_RESULT_OK:
                parseOngoingSessionData(rcvdData);
                break;
        }
    }

    public void parseOngoingSessionData(JSONObject rcvdData) {
        try {
            int count = rcvdData.getInt(Constants.HTTP_DATA_MAP_ONGOING_SESSION_COUNT);
            JSONArray data = rcvdData.getJSONArray(Constants.HTTP_DATA_MAP_ONGOING_SESSION_DATA);

            dataMapActivity.initMarkerCollection();

            for (int i = 0; i < count; i++) {
                JSONObject eachData = (JSONObject) data.get(i);
                double temperature = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_TEMP);
                double co = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_CO);
                double so2 = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_SO2);
                double no2 = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_NO2);
                double o3 = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_O3);
                double pm = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_PM);
                double lat = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_LAT);
                double lng = eachData.getDouble(Constants.HTTP_DATA_MAP_ONGOING_SESSION_LNG);

                dataMapActivity.addMarker("", new DataMapDataSet(temperature, co, so2, no2, o3, pm), new LatLng(lat, lng));
            }

//            dataMapActivity.removeAndDrawMarker();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
