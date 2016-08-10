package com.qi.airstat.dataMap;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by JUMPSNACK on 8/8/2016.
 */

/*
Single tone class for notifying my location to server
 */
public class DataMapCurrentUser {
    private static DataMapCurrentUser instance = new DataMapCurrentUser();

private static int id;
    private static long timeStamp;
    private static DataMapDataSet dataSet;

    private static double lat;
    private static double lng;

    private static double minLat;
    private static double maxLat;
    private static double minLng;
    private static double maxLng;

    private DataMapCurrentUser() {
        this.id = -1;
        dataSet = new DataMapDataSet();

        this.lat = 0;
        this.lng = 0;

        this.minLat = 0;
        this.maxLat = 0;
        this.minLng = 0;
        this.maxLng = 0;
    }
    
    public static DataMapMarker create(){
        Log.w("Stored data", ""+lat+"..."+lng);
        DataMapMarker currnetUser = new DataMapMarker(id, timeStamp, new LatLng(lat, lng));
        currnetUser.setDataSet(dataSet);
        return currnetUser;
    }

    public void setCurrentUserData(float temparature, float co, float so2, float no2, float o3, float pm) {
        this.dataSet.dataReset(temparature, co, so2, no2, o3, pm);
    }

    public static DataMapCurrentUser getInstance() {
        return instance;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }

    public double getMinLat() {
        return minLat;
    }

    public void setMinLat(double minLat) {
        this.minLat = minLat;
    }

    public double getMaxLat() {
        return maxLat;
    }

    public void setMaxLat(double maxLat) {
        this.maxLat = maxLat;
    }

    public double getMinLng() {
        return minLng;
    }

    public void setMinLng(double minLng) {
        this.minLng = minLng;
    }

    public double getMaxLng() {
        return maxLng;
    }

    public void setMaxLng(double maxLng) {
        this.maxLng = maxLng;
    }
}
