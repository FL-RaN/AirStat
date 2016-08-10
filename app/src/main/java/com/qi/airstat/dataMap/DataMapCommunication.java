package com.qi.airstat.dataMap;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.qi.airstat.Constants;
import com.qi.airstat.iHttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/9/2016.
 */
public class DataMapCommunication extends Thread implements iHttpConnection {

    private HttpThread httpThread;
    private boolean flow = false;
    private String result = "";
    private DataMapActivity dataMapActivity;

    private Context context;
    private ArrayList<String> params;

    private DataMapCommunication() {
    }

    public DataMapCommunication(Context context, DataMapActivity dataMapActivity) {
        this.context = context;
        this.dataMapActivity = dataMapActivity;
    }

    public void wake() {
        synchronized (this) {

            if (this.flow && getState() == State.TIMED_WAITING) {
                try {
//                    Log.w("STATUS", this.getState() + "");
                    this.interrupt();
                } catch (Exception e) {
//                    Log.w("WAKE", "FAIL");
                }
            }
        }
    }

    public void stopThread() {
        this.flow = false;
    }

    private ArrayList onPreExecute() {
        params = new ArrayList<>();
        params.add(Constants.HTTP_MSG_ID);
        params.add(Constants.HTTP_REQUEST_ONGOING_SESSION + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MIN_LAT);
        params.add(DataMapCurrentUser.getInstance().getMinLat() + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MAX_LAT);
        params.add(DataMapCurrentUser.getInstance().getMaxLat() + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MIN_LNG);
        params.add(DataMapCurrentUser.getInstance().getMinLng() + "");
        params.add(Constants.HTTP_DATA_MAP_RANGE_MAX_LNG);
        params.add(DataMapCurrentUser.getInstance().getMaxLng() + "");

        return params;
    }

    @Override
    public void run() {
        super.run();

        flow = true;
        while (flow) {
//            onPreExecute();
            try {
                resultHandler(executeHttpConn());
                Thread.sleep(Constants.HTTP_DATA_MAP_GET_ONGOING_SESSION_TIME_QUALTUM);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resultHandler(String result) {
        Log.w("result", result + "");
        JSONObject rcvdData = null;

        try {
            rcvdData = new JSONObject(result);
        } catch (JSONException e) {
            e.printStackTrace();
            onPostExecute(result);
            return;
        }
        if (onPostExecute(result)) {
            parseOngoingSessionData(rcvdData);
        }
    }

    @Override
    public String executeHttpConn() {
        httpThread = new HttpThread();
        return httpThread.executeConn(DataMapActivity.context, Constants.HTTP_STR_URL_ONGOING_SESSION, params);
    }

    public void parseOngoingSessionData(JSONObject rcvdData) {
        try {
            dataMapActivity.initMarkerCollection();

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

                dataMapActivity.addMarker(connectionID, timeStamp, new DataMapDataSet(temperature, co, so2, no2, o3, pm), new LatLng(lat, lng));
                try {
                    eachData = rcvdData.getJSONObject("" + (i++));
                } catch (JSONException e) {
                    eachData = null;
                }
            }
            dataMapActivity.addMarker(DataMapCurrentUser.create());
            dataMapActivity.refreshMap();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    protected boolean onPostExecute(String result) {
        /* Err CASE*/
        if (result.equalsIgnoreCase("MalformedURLException") || result.equalsIgnoreCase("Exception") || result.equalsIgnoreCase("IOException") || result.equalsIgnoreCase("unsuccessful")) {
            makeToast("Something went wrong. Connection Problem");
            return false;
        } else { /* Success CASE */
            return true;
        }
    }


    private class HttpThread {

        private Context context;
        private ArrayList<String> params;
        HttpURLConnection conn;
        String strUrl;
        URL url;
        String rcvdData;

        public String executeConn(Context context, String strUrl, ArrayList<String> params) {
            this.context = context;
            this.params = params;
            this.strUrl = strUrl;

            rcvdData = this.connection(strUrl);
            return rcvdData;
        }

        private String connection(String strUrl) {
            try {
                url = new URL(strUrl);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String sendingResult = sendToServer();
            if (sendingResult != null) return sendingResult;

            String receivingResult = receiveFromServer();
            if (receivingResult != null) return receivingResult;

            return null;
        }


        private String sendToServer() {
            try {
                /*
                Set HttpURLConnection to send and receive data from php and mysql
                 */
                conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(Constants.HTTP_CONNECT_TIMEOUT);
                conn.setReadTimeout(Constants.HTTP_READ_TIMEOUT);
                conn.setRequestMethod("GET");

                Log.w("URL", strUrl);

                conn.connect();

            } catch (IOException e) {
                e.printStackTrace();
                return "SND IOException";
            } catch (Exception e) {
                e.printStackTrace();
                return "SND Exception";
            }
            return null;
        }


        private String receiveFromServer() {
            try {
                int responseCode = conn.getResponseCode();
                Log.w("RSP CODE", String.valueOf(responseCode));

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    InputStream inputStream = conn.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    StringBuilder result = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        result.append(line);
                    }

                    String strResult = result.toString();

                    return strResult;
                } else {
                    return "unsuccessful";
                }
            } catch (IOException e) {
                e.printStackTrace();
                return "RCV IOException";
            } catch (Exception e) {
                e.printStackTrace();
                return "RCV Exception";
            } finally {
                conn.disconnect();
            }
        }
    }

    private void makeToast(String msg) {
        if (context != null)
            Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
