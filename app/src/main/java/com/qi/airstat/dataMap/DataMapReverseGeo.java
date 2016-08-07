package com.qi.airstat.dataMap;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by JUMPSNACK on 8/6/2016.
 */
public class DataMapReverseGeo extends AsyncTask<String, String, String> {
    double latitude;
    double longitude;
    String regionAddress;

    public DataMapReverseGeo(double latitude, double longitude) throws Exception {
        this.latitude = latitude;
        this.longitude = longitude;
    }


    public String getAddress() {
        return regionAddress;
    }

    @Override
    protected String doInBackground(String... strings) {
        String apiURL = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                + latitude + "," + longitude;

        String jsonString = new String();
        String buf;
        URL url = null;
        try {
            url = new URL(apiURL);
            URLConnection conn = url.openConnection();
            BufferedReader br = new BufferedReader(new InputStreamReader(
                    conn.getInputStream(), "UTF-8"));
            while ((buf = br.readLine()) != null) {
                jsonString += buf;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }

        JSONObject jObj = null;
        String result = null;
        try {
            jObj = new JSONObject(jsonString);
            JSONArray jArray =  jObj.getJSONArray("results");
            jObj = (JSONObject) jArray.get(0);
            jArray = jObj.getJSONArray("address_components");
            result = (String) ((JSONObject) jArray.get(3)).get("short_name");
//            result = (String) jObj.get("formatted_address");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        regionAddress = result;

        return result;
    }
}