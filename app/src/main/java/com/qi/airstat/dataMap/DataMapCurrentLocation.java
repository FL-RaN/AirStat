package com.qi.airstat.dataMap;

/**
 * Created by JUMPSNACK on 8/8/2016.
 */
public class DataMapCurrentLocation {
    private static DataMapCurrentLocation instance = new DataMapCurrentLocation();

    private double lat;
    private double lng;

    private double minLat;
    private double maxLat;
    private double minLng;
    private double maxLng;

    private DataMapCurrentLocation() {
        this.lat = 0;
        this.lng = 0;

        this.minLat = 0;
        this.maxLat = 0;
        this.minLng = 0;
        this.maxLng = 0;
    }

    public static DataMapCurrentLocation getInstance() {
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
