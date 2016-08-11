package com.qi.airstat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class LocationState {
    static final private LocationState instance = new LocationState();

    private LocationState() { /* DO NOTHING */ }

    static public LocationState getInstance() { return instance; }

    static public boolean isLocationAvailable(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return false;
        }

        return true;
    }

    static public void requestLocationPermission(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(
                        (Activity)context,
                        new String[] {
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                        }, Constants.LOCATION_PERMISSION_REQUEST
                );
            }
        }
    }

    static public void getCurrentLocation(Context context) {
        LocationManager locationManager = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
    }
}
