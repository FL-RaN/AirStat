package com.qi.airstat.dataMap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class DataMapMarker implements ClusterItem {

    private LatLng location;
    private int connectionID;
    private long timeStamp;

    private DataMapDataSet dataSet = new DataMapDataSet();

    public DataMapMarker(int connectionID, long timeStamp, LatLng location) {
        this.location = location;
        this.connectionID = connectionID;
        this.timeStamp = timeStamp;
    }

    public DataMapMarker(int connectionID, long timeStamp, DataMapDataSet dataSet, LatLng location){
        this.connectionID = connectionID;
        this.timeStamp = timeStamp;
        setDataSet(dataSet);
        this.location = location;
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

    public DataMapDataSet getDataSet(){
        return dataSet;
    }

    public int getConnectionID() {
        return connectionID;
    }

    public void setConnectionID(int connectionID) {
        this.connectionID = connectionID;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
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

    public LatLng getLocation() {
        return location;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public float getAqiValue() {
        return Float.parseFloat(String.format("%.1f", dataSet.getAqiValue()));
    }

    @Override
    public LatLng getPosition() {
        return location;
    }
}
