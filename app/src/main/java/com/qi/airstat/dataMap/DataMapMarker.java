package com.qi.airstat.dataMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class DataMapMarker implements ClusterItem {

    private LatLng location;
    private String title;
    private float aqiValue;

    public DataMapMarker(String title, LatLng location, float aqiValue) {
        this.location = location;
        this.title = title;
        this.aqiValue = aqiValue;
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public float getAqiValue() {
        return aqiValue;
    }

    public void setAqiValue(float aqiValue){ this.aqiValue = aqiValue;}


    @Override
    public LatLng getPosition() {
        return location;
    }
}
