package com.qi.airstat.dataMap;

import com.google.android.gms.maps.model.LatLng;
import com.qi.airstat.Constants;

/**
 * Created by JUMPSNACK on 8/8/2016.
 */

/*
Single tone class for notifying my location to server
 */
public class DataMapCurrentUser {
    private static DataMapCurrentUser instance = new DataMapCurrentUser();

    private static int connectionID;
    private static long timeStamp;
    private static DataMapDataSet dataSet;

    private static double lat;
    private static double lng;

    private static double minLat;
    private static double maxLat;
    private static double minLng;
    private static double maxLng;

    private static float currentZoom;
    private static float currentMaxZoom;

    private DataMapCurrentUser() {
        this.connectionID = Constants.CID_BLC;
        dataSet = new DataMapDataSet();

        this.lat = 0;
        this.lng = 0;

        this.minLat = 0;
        this.maxLat = 0;
        this.minLng = 0;
        this.maxLng = 0;
    }

    public static DataMapMarker create() {
        DataMapMarker currnetUser = new DataMapMarker(connectionID, timeStamp, new LatLng(lat, lng));
        currnetUser.setDataSet(dataSet);
        return currnetUser;
    }

    public static void setCurrentUserData(float temparature, float co, float so2, float no2, float o3, float pm) {
        dataSet.dataReset(temparature, co, so2, no2, o3, pm);
    }

    public static int getConnectionID() {
        return connectionID;
    }

    public static void setConnectionID(int connectionID) {
        DataMapCurrentUser.connectionID = connectionID;
    }

    public static long getTimeStamp() {
        return timeStamp;
    }

    public static void setTimeStamp(long timeStamp) {
        DataMapCurrentUser.timeStamp = timeStamp;
    }

    public static float getCurrentZoom() {
        return currentZoom;
    }

    public static void setCurrentZoom(float currentZoom) {
        DataMapCurrentUser.currentZoom = currentZoom;
    }

    public static float getCurrentMaxZoom() {
        return currentMaxZoom;
    }

    public static void setCurrentMaxZoom(float currentMaxZoom) {
        DataMapCurrentUser.currentMaxZoom = currentMaxZoom;
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
