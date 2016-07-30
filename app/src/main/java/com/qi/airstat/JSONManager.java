package com.qi.airstat;

import android.content.Context;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class JSONManager {
    ArrayList<JSONObject> dataSet = null;

    public JSONManager() {
        dataSet = new ArrayList<JSONObject>();
    }

    static public String getAssetJSON(Context context, String fileName) {
        int size;
        byte[] buffer;
        String JSON = null;
        InputStream inputStream;

        try {
            inputStream = context.getAssets().open(fileName);
            size = inputStream.available();

            buffer = new byte[size];

            inputStream.read(buffer);
            inputStream.close();

            JSON = new String(buffer, "UTF-8");
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        return JSON;
    }

    public ArrayList<JSONObject> getDataSet() {
        return dataSet;
    }

    public void addData(JSONObject jsonObject) {
        try {
            dataSet.add(jsonObject);
        }
        catch (NullPointerException exception) {
            exception.printStackTrace();
        }
    }

    public void addData(String JSON) {
        try {
            JSONObject jsonObject = new JSONObject(JSON);
            dataSet.add(jsonObject);
        }
        catch (JSONException exception) {
            exception.printStackTrace();
        }
        catch (NullPointerException exception) {
            exception.printStackTrace();
        }
    }
}
