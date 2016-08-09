package com.qi.airstat.dataMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class DataMapMarker implements ClusterItem {

    private LatLng location;
    private String title;

//    private float aqiValue;
//    private float temparature = 75.3f;
//    private float co = 10f;
//    private float so2 = 30f;
//    private float no2 = 72.2f;
//    private float o3 = 14.3f;
//    private float pm = 123.3f;

    private DataMapDataSet dataSet = new DataMapDataSet();

    public DataMapMarker(String title, LatLng location, float aqiValue) {
        this.location = location;
        this.title = title;
        dataSet.setAqiValue(aqiValue);
    }

    public void setData(float temp, float co, float so2, float no2, float o3, float pm) {
        setTemparature(temp);
        setCo(co);
        setSo2(so2);
        setNo2(no2);
        setO3(o3);
        setPm(pm);
    }

    public void setDataSet(DataMapDataSet dataSet){
        this.dataSet = dataSet;
    }


    public float getTemparature() {
        return Float.parseFloat(String.format("%.1f", dataSet.getTemperature()));
    }

    public void setTemparature(float temparature) {
        dataSet.setTemperature(temparature);
    }

    public float getCo() {
        return Float.parseFloat(String.format("%.1f", dataSet.getCo()));
    }

    public void setCo(float co) {
        dataSet.setCo(co);
    }

    public float getSo2() {
        return Float.parseFloat(String.format("%.1f", dataSet.getSo2()));
    }

    public void setSo2(float so2) {
        dataSet.setSo2(so2);
    }

    public float getNo2() {
        return Float.parseFloat(String.format("%.1f", dataSet.getNo2()));
    }

    public void setNo2(float no2) {
        dataSet.setNo2(no2);
    }

    public float getO3() {
        return Float.parseFloat(String.format("%.1f", dataSet.getO3()));
    }

    public void setO3(float o3) {
        dataSet.setO3(o3);
    }

    public float getPm() {
        return Float.parseFloat(String.format("%.1f", dataSet.getPm()));
    }

    public void setPm(float pm) {
        dataSet.setPm(pm);
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
        return Float.parseFloat(String.format("%.1f", dataSet.getAqiValue()));
    }

    public void setAqiValue(float aqiValue) {
        dataSet.setAqiValue(aqiValue);
    }


    @Override
    public LatLng getPosition() {
        return location;
    }
}
