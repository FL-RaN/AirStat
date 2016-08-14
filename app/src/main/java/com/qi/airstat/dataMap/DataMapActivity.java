package com.qi.airstat.dataMap;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;
import com.qi.airstat.ActivityManager;
import com.qi.airstat.Constants;
import com.qi.airstat.R;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class DataMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {
    public final LatLng START_POINT = new LatLng(32.881265, -117.234139);

    public static Context context;

    private GoogleMap map;
    private MapFragment mapFragment;
    private DataMapPanelUi dataMapPanelUi;

    /*
    Marker and cluster
     */
    public static ArrayList<DataMapMarker> markers;
    public ClusterManager<DataMapMarker> mClusterManager;

    /*
    Cluster renderer
     */
    private DefaultClusterRenderer mRenderer;

    /*
    Event Listener
     */
    private MarkerTouchDetector markerTouchDetector;
    private MapMovingDetector mapMovingDetector;
    private ClusterTouchDetector clusterTouchDetector;
    private LocationManager locationManager;

    /*
    Event Handler
     */
    private BackgroundMarkerChanger backgroundMarkerChanger;
    private BackgroundClusterChanger backgroundClusterChanger;

    private boolean firstCall = true;
    private int backPressCount = 0;

    /*
    Service
     */
    private Messenger messageReceiver = new Messenger(new IncommingHandler());
    private Messenger messageSender = null;
    private boolean isDataMapServiceBound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            messageSender = new Messenger(iBinder);

            try {
                // Send message to service for register this activity as new client.
                Message message = Message.obtain(null, Constants.CLIENT_REGISTER);
                message.replyTo = messageReceiver;
                messageSender.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }

            isDataMapServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            try {
                // Send message to service for register this activity as new client.
                Message message = Message.obtain(null, Constants.CLIENT_UNREGISTER);
                message.replyTo = messageReceiver;
                messageSender.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }

            isDataMapServiceBound = false;
        }
    };

    /*
    Back button event for logout process
     */
    @Override
    public void onBackPressed() {
        backPressCount++;

        if (backPressCount < 2) {
            makeToast("Please press again to exit.");
        } else {
            ActivityManager.instance.logoutUser(); //essential for logout
            System.gc();
            System.runFinalization();
            System.exit(0);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (isDataMapServiceBound) {    // Prevent duplicated service call
            try {
                // Send message to service for register this activity as new client.
                Message message = Message.obtain(null, Constants.CLIENT_UNREGISTER);
                message.replyTo = messageReceiver;
                messageSender.send(message);
            } catch (RemoteException exception) {
                exception.printStackTrace();
            }
            messageSender = null;
            isDataMapServiceBound = false;
        }

        try {
            if (locationManager != null)    // Close location manager when app restarted
                locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        if (backgroundMarkerChanger != null) {  // Kill Asynctask
            backgroundMarkerChanger.stop();
            backgroundMarkerChanger.cancel(true);
        }
        if (backgroundClusterChanger != null) { // Kill Asynctask
            backgroundClusterChanger.stop();
            backgroundClusterChanger.cancel(true);
        }

        dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);

        unbindService(serviceConnection);
    }

    @Override
    protected void onResume() {
        super.onResume();
        backPressCount = 0;
        if (!isDataMapServiceBound) {
            isDataMapServiceBound = true;
            bindService(
                    new Intent(this, DataMapService.class),
                    serviceConnection,
                    Context.BIND_AUTO_CREATE
            );
        }
    }

    /*
    Service handler
     */
    class IncommingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Constants.SERVICE_DATA_MAP_DRAW_MAP:
                    String rcvdData = (String) msg.obj;
                    resultHandler(rcvdData);
                    break;
            }
        }
    }

    public void resultHandler(String result) {
        Log.d("result", result + "");
        JSONObject rcvdData = null;

        try {
            rcvdData = new JSONObject(result);
            parseOngoingSessionData(rcvdData);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*
    Process received data
     */
    public void parseOngoingSessionData(JSONObject rcvdData) {
        try {
            Iterator<String> it = rcvdData.keys();

            while (it.hasNext()) {
                JSONObject eachData = rcvdData.getJSONObject(it.next());

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

                if (connectionID == Constants.CID_BLC) {    // Process host user data
                    DataMapCurrentUser.setCurrentUserData((float) temperature, (float) co, (float) so2, (float) no2, (float) o3, (float) pm);
                    DataMapCurrentUser.setConnectionID(connectionID);
                    DataMapCurrentUser.setTimeStamp(timeStamp);
                    refreshMarker(DataMapCurrentUser.create());
                } else {    // Process other users data
                    refreshMarker(connectionID, timeStamp, new DataMapDataSet(temperature, co, so2, no2, o3, pm), lat, lng);
                }
            }
            refreshMap();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void refreshMap() {
        mClusterManager.clearItems();

        for (DataMapMarker marker : markers) {
            mClusterManager.addItem(marker);
        }

        mClusterManager.cluster();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_data_map);

        context = this;

        /*
        Request permission to user
         */
        checkPermission();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_data_map);
        mapFragment.getMapAsync(this);

        markers = new ArrayList<>();

        markerTouchDetector = new MarkerTouchDetector();
        mapMovingDetector = new MapMovingDetector();
        clusterTouchDetector = new ClusterTouchDetector();

        dataMapPanelUi = new DataMapPanelUi();

        setWidgets(dataMapPanelUi);

    }

    /*
    Permission checker
    */
    private void checkPermission() {
        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                /*DO NOTHING*/
            }

            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                makeToast("Please check your permission setting");
            }
        };

        new TedPermission(this).setPermissionListener(permissionListener)
                .setRationaleMessage("We need your permission")
                .setDeniedMessage("You make it rejected")
                .setPermissions(Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.INTERNET)
                .check();
    }

    /*
    Initializing widgets
     */
    private void setWidgets(final DataMapPanelUi dataMapPanelUi) {
        dataMapPanelUi.slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slider_data_map_container);
        dataMapPanelUi.barTitle = (LinearLayout) findViewById(R.id.bar_data_map_title);
        dataMapPanelUi.tvTitle = (TextView) findViewById(R.id.tv_data_map_title);
        dataMapPanelUi.tvSubTitle = (TextView) findViewById(R.id.tv_data_map_sub_title);
        dataMapPanelUi.tvAqiGrade = (TextView) findViewById(R.id.tv_data_map_grade);

        dataMapPanelUi.tvAqiValue = (TextView) findViewById(R.id.tv_data_map_panel_aqi);
        dataMapPanelUi.tvTemperature = (TextView) findViewById(R.id.tv_data_map_panel_temp);
        dataMapPanelUi.tvCo = (TextView) findViewById(R.id.tv_data_map_panel_co);
        dataMapPanelUi.tvSo2 = (TextView) findViewById(R.id.tv_data_map_panel_so2);
        dataMapPanelUi.tvNo2 = (TextView) findViewById(R.id.tv_data_map_panel_no2);
        dataMapPanelUi.tvO3 = (TextView) findViewById(R.id.tv_data_map_panel_o3);
        dataMapPanelUi.tvPm = (TextView) findViewById(R.id.tv_data_map_panel_pm2_5);

        dataMapPanelUi.imgPanelArrow = (ImageView) findViewById(R.id.iv_data_map_panel_arrow);

        dataMapPanelUi.slidingUpPanelLayout.setAnchorPoint(0.16f);
        dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
        dataMapPanelUi.slidingUpPanelLayout.addPanelSlideListener(new SlidingUpPanelLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {
            }

            @Override
            public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                if (newState == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    dataMapPanelUi.imgPanelArrow.setBackgroundResource(R.drawable.icon_page_up);
                } else if (newState == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    dataMapPanelUi.imgPanelArrow.setBackgroundResource(R.drawable.icon_page_down);
                }
            }
        });
    }

    /*
    Initializing GoogleMap
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        setUpMap(map);

        mClusterManager = new ClusterManager<>(this, map);
        initMarkerCollection();
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        setMapEvent();

    }

    /*
    Set map options
    */
    private void setUpMap(GoogleMap map) {
        try {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
                    return false;
                }
            });
            map.getUiSettings().setZoomControlsEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void setMapEvent() {
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POINT, 14.0f));
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        map.setOnCameraIdleListener(mapMovingDetector);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (dataMapPanelUi.slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
                    dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                else
                    dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });

        mRenderer = new DataMapClusterRenderer(this, map, mClusterManager, markers);
        mClusterManager.setRenderer(mRenderer);
        mClusterManager.setOnClusterItemClickListener(markerTouchDetector);
        mClusterManager.setOnClusterClickListener(clusterTouchDetector);
    }

    /*
    Find marker from list
     */
    public static DataMapMarker findMarker(int cid) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == cid) {
                return marker;
            }
        }
        return null;
    }

    /*
    Add single Marker
     */
    public void initMarkerCollection() {
        markers.clear();
        mClusterManager.clearItems();
    }

    /*
    Add marker as seperated data
     */
    public void addMarker(int cid, long timeStamp, DataMapDataSet dataSet, LatLng location) {
        DataMapMarker marker = new DataMapMarker(cid, timeStamp, dataSet, location);
        markers.add(marker);
    }

    /*
    Add marker as seted data
     */
    public void addMarker(DataMapMarker marker) {
        markers.add(marker);
        mClusterManager.addItem(marker);

    }

    /*
    Refresh Marker's data as each data
     */
    public boolean refreshMarker(int cid, long timeStamp, DataMapDataSet dataSet, double lat, double lng) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == cid) {
                marker.setDataSet(dataSet);
                marker.setTimeStamp(timeStamp);
                marker.setLocation(new LatLng(lat, lng));
                return true;
            }
        }
        addMarker(cid, timeStamp, dataSet, new LatLng(lat, lng));
        return false;
    }

    /*
    Refresh Marker's data as data set
     */
    public boolean refreshMarker(DataMapMarker newMarker) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == newMarker.getConnectionID()) {
                if (marker.getConnectionID() == Constants.CID_BLC) {
                }
                marker.setDataSet(newMarker.getDataSet());
                marker.setLocation(newMarker.getLocation());
                marker.setTimeStamp(newMarker.getTimeStamp());
                return true;
            }
        }
        return false;
    }

    /*
    Continuously check user location
     */
    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // Set current location
        DataMapCurrentUser.getInstance().setLat(lat);
        DataMapCurrentUser.getInstance().setLng(lng);

        // Set current zoom leven
        DataMapCurrentUser.getInstance().setCurrentZoom(map.getCameraPosition().zoom);
        DataMapCurrentUser.getInstance().setCurrentMaxZoom(map.getMaxZoomLevel());

        if (firstCall) {
            addMarker(DataMapCurrentUser.create());
            panelValueChanger(findMarker(Constants.CID_BLC));
            map.animateCamera(CameraUpdateFactory.newLatLng(new LatLng(lat, lng)));
            firstCall = false;
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {
    }

    @Override
    public void onProviderEnabled(String s) {
    }

    @Override
    public void onProviderDisabled(String s) {
    }

    /*
    Rendering Marker as AQI Level
     */
    private void panelValueChanger(DataMapMarker clusterItem) {
        if (clusterItem == null) return;

        DataMapMarker marker = clusterItem;

        float aqiValue = marker.getAqiValue();
        String grade;

        if (0 <= aqiValue && aqiValue <= 50) {
            backgroundAnimator(Constants.AQI_LEVEL_GOOD);
            grade = "GOOD";
        } else if (50 < aqiValue && aqiValue <= 100) {
            backgroundAnimator(Constants.AQI_LEVEL_MODERATE);
            grade = "MODERATE";
        } else if (100 < aqiValue && aqiValue <= 150) {
            backgroundAnimator(Constants.AQI_LEVEL_SENSITIVE);
            grade = "SENSITIVE";
        } else if (150 < aqiValue && aqiValue <= 200) {
            backgroundAnimator(Constants.AQI_LEVEL_UNHEALTHY);
            grade = "UNHEALTHY";
        } else if (200 < aqiValue && aqiValue <= 300) {
            backgroundAnimator(Constants.AQI_LEVEL_VERY_UNHEALTHY);
            grade = "VERY UNHEALTHY";
        } else if (300 < aqiValue && aqiValue <= 500) {
            backgroundAnimator(Constants.AQI_LEVEL_HAZARDOUS);
            grade = "HAZARDOUS";
        } else {
            backgroundAnimator(Constants.AQI_LEVEL_DEFAULT);
            grade = "WAITING...";
        }

        panelMarkerValueSetter(marker, grade);
    }

    /*
    Rendering Marker's data to panel
     */
    public void panelMarkerValueSetter(DataMapMarker marker, String grade) {
        dataMapPanelUi.tvAqiGrade.setText(grade);

        if (grade.equals("WAITING...")) {
            dataMapPanelUi.slidingUpPanelLayout.setTouchEnabled(false);
        } else {
            dataMapPanelUi.slidingUpPanelLayout.setTouchEnabled(true);
            dataMapPanelUi.tvAqiValue.setText("" + marker.getAqiValue());
            dataMapPanelUi.tvTemperature.setText("" + marker.getTemparature());
            dataMapPanelUi.tvCo.setText("" + marker.getCo());
            dataMapPanelUi.tvSo2.setText("" + marker.getSo2());
            dataMapPanelUi.tvNo2.setText("" + marker.getNo2());
            dataMapPanelUi.tvO3.setText("" + marker.getO3());
            dataMapPanelUi.tvPm.setText("" + marker.getPm());
        }
    }

    /*
    Rendering Cluster
     */
    private void renderCluster(Cluster cluster) {
        //Whole items in cluster as COLLECTION
        Collection<DataMapMarker> markers = cluster.getItems();

        //Calc average aqi value
        float avgAqiValue = 0;
        float avgTemperature = 0;
        float avgCo = 0;
        float avgSo2 = 0;
        float avgNo2 = 0;
        float avgO3 = 0;
        float avgPm = 0;

        int countMarker = 0;
        for (DataMapMarker marker : markers) {
            if (marker.getAqiValue() < 0)
                continue;
            avgAqiValue += marker.getAqiValue();
            avgTemperature += marker.getTemparature();
            avgCo += marker.getCo();
            avgSo2 += marker.getSo2();
            avgNo2 += marker.getNo2();
            avgO3 += marker.getO3();
            avgPm += marker.getPm();
            countMarker++;
        }

        avgAqiValue /= countMarker;
        avgTemperature /= countMarker;
        avgCo /= countMarker;
        avgSo2 /= countMarker;
        avgNo2 /= countMarker;
        avgO3 /= countMarker;
        avgPm /= countMarker;

        //Check grade by aqi value
        String grade;
        if (0 <= avgAqiValue && avgAqiValue <= 50) {
            backgroundAnimator(Constants.AQI_LEVEL_GOOD);
            grade = "GOOD";
        } else if (50 < avgAqiValue && avgAqiValue <= 100) {
            backgroundAnimator(Constants.AQI_LEVEL_MODERATE);
            grade = "MODERATE";
        } else if (100 < avgAqiValue && avgAqiValue <= 150) {
            backgroundAnimator(Constants.AQI_LEVEL_SENSITIVE);
            grade = "SENSITIVE";
        } else if (150 < avgAqiValue && avgAqiValue <= 200) {
            backgroundAnimator(Constants.AQI_LEVEL_UNHEALTHY);
            grade = "UNHEALTHY";
        } else if (200 < avgAqiValue && avgAqiValue <= 300) {
            backgroundAnimator(Constants.AQI_LEVEL_VERY_UNHEALTHY);
            grade = "VERY UNHEALTHY";
        } else if (300 < avgAqiValue && avgAqiValue <= 500) {
            backgroundAnimator(Constants.AQI_LEVEL_HAZARDOUS);
            grade = "HAZARDOUS";
        } else {
            backgroundAnimator(Constants.AQI_LEVEL_DEFAULT);
            grade = "WAITING...";
        }

        //UI Set
        dataMapPanelUi.tvAqiGrade.setText(grade);
        dataMapPanelUi.tvAqiValue.setText(String.format("%.1f", avgAqiValue));
        dataMapPanelUi.tvTemperature.setText(String.format("%.1f", avgTemperature));
        dataMapPanelUi.tvCo.setText(String.format("%.1f", avgCo));
        dataMapPanelUi.tvSo2.setText(String.format("%.1f", avgSo2));
        dataMapPanelUi.tvNo2.setText(String.format("%.1f", avgNo2));
        dataMapPanelUi.tvO3.setText(String.format("%.1f", avgO3));
        dataMapPanelUi.tvPm.setText(String.format("%.1f", avgPm));
        dataMapPanelUi.slidingUpPanelLayout.setTouchEnabled(true);
    }

    /*
    Make smooth moving at change value
     */
    public void backgroundAnimator(String color) {
        int from = ((ColorDrawable) (dataMapPanelUi.barTitle.getBackground())).getColor();
        int to = Color.parseColor(color);
        ValueAnimator colorAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), from, to);
        if (colorAnimator != null) {
            colorAnimator.setDuration(100);
            colorAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    dataMapPanelUi.barTitle.setBackgroundColor((int) valueAnimator.getAnimatedValue());
                }
            });
            colorAnimator.start();
        }
    }

    /*
    Event listener for marker
     */
    private class MarkerTouchDetector implements ClusterManager.OnClusterItemClickListener {
        @Override
        public boolean onClusterItemClick(ClusterItem clusterItem) {
            if (backgroundMarkerChanger != null) {
                backgroundMarkerChanger.stop();
                backgroundMarkerChanger.cancel(true);
            }
            if (backgroundClusterChanger != null) {
                backgroundClusterChanger.stop();
                backgroundClusterChanger.cancel(true);
            }
            dataMapPanelUi.tvTitle.setText("");
            dataMapPanelUi.tvSubTitle.setText("");
            dataMapPanelUi.tvAqiGrade.setText("");
            DataMapMarker marker = (DataMapMarker) clusterItem;
            backgroundMarkerChanger = new BackgroundMarkerChanger(marker);
            backgroundMarkerChanger.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            map.animateCamera(CameraUpdateFactory.newLatLng(clusterItem.getPosition()));
            return true;
        }
    }

    /*
    Event listener for cluster
     */
    private class ClusterTouchDetector implements ClusterManager.OnClusterClickListener {

        @Override
        public boolean onClusterClick(Cluster cluster) {
            // Position info
            double lat = cluster.getPosition().latitude;
            double lng = cluster.getPosition().longitude;

            if (backgroundMarkerChanger != null) {
                backgroundMarkerChanger.stop();
                backgroundMarkerChanger.cancel(true);
            }
            if (backgroundClusterChanger != null) {
                backgroundClusterChanger.stop();
                backgroundClusterChanger.cancel(true);
            }
            dataMapPanelUi.tvTitle.setText("");
            dataMapPanelUi.tvSubTitle.setText("");
            dataMapPanelUi.tvAqiGrade.setText("");
            backgroundClusterChanger = new BackgroundClusterChanger(lat, lng);
            backgroundClusterChanger.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, cluster);

            dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            map.animateCamera(CameraUpdateFactory.newLatLng(cluster.getPosition()));

            return true;
        }
    }

    /*
    Event listener for map moving
     */
    private class MapMovingDetector implements GoogleMap.OnCameraIdleListener {
        @Override
        public void onCameraIdle() {
            LatLng northeast = map.getProjection().getVisibleRegion().latLngBounds.northeast;
            LatLng southwest = map.getProjection().getVisibleRegion().latLngBounds.southwest;

            DataMapCurrentUser.getInstance().setMinLat(southwest.latitude);
            DataMapCurrentUser.getInstance().setMaxLat(northeast.latitude);
            DataMapCurrentUser.getInstance().setMinLng(northeast.longitude);
            DataMapCurrentUser.getInstance().setMaxLng(southwest.longitude);
        }
    }


    public void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    /*
    Event handler for marker
     */
    private class BackgroundMarkerChanger extends AsyncTask<DataMapMarker, DataMapMarker, Void> {
        private boolean flow = true;
        private DataMapMarker marker;
        String[] regionAddress = null;

        public void stop() {
            this.flow = false;
        }

        public BackgroundMarkerChanger(DataMapMarker marker) {
            this.marker = marker;
        }

        @Override
        protected Void doInBackground(DataMapMarker... values) {
            if (marker.getConnectionID() != Constants.CID_BLC)
                regionAddress = getRegionAddress(marker.getLocation().latitude, marker.getLocation().longitude);
            while (flow) {
                publishProgress(marker);
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(DataMapMarker... values) {
            super.onProgressUpdate(values);
            panelValueChanger(values[0]);
            if (regionAddress != null) {
                dataMapPanelUi.tvTitle.setText(regionAddress[0]);
                dataMapPanelUi.tvSubTitle.setText(regionAddress[1]);
            } else {
                dataMapPanelUi.tvTitle.setText("ME");
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stop();
        }
    }

    /*
    Event handler for Cluster
     */
    private class BackgroundClusterChanger extends AsyncTask<Cluster, Cluster, Void> {
        private boolean flow = true;
        double latitude;
        double longitude;
        String[] regionAddress;

        public void stop() {
            this.flow = false;
        }

        public BackgroundClusterChanger(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Void doInBackground(Cluster... clusters) {
            regionAddress = getRegionAddress(latitude, longitude);
            while (flow) {
                publishProgress(clusters[0]);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Cluster... clusters) {
            super.onProgressUpdate(clusters);
            if (clusters[0] == null) {
                dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                stop();
                this.cancel(true);
                return;
            }
            renderCluster(clusters[0]);
            dataMapPanelUi.tvTitle.setText(regionAddress[0]);
            dataMapPanelUi.tvSubTitle.setText(regionAddress[1]);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stop();
        }
    }

    /*
    Reverse geocoding to display on panel
     */
    public String[] getRegionAddress(double lat, double lng) {
        String apiURL = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                + lat + "," + lng;

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

            br.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jObj = null;
        String[] result = new String[2];
        try {
            jObj = new JSONObject(jsonString);
            JSONArray jArray = jObj.getJSONArray("results");
            jObj = (JSONObject) jArray.get(0);
            jArray = jObj.getJSONArray("address_components");
            Object checker;
            for (int i = 2; i < 4; i++) {
                checker = ((JSONObject) jArray.get(i)).get("short_name");
                if (checker != null)
                    result[i - 2] = checker + " ";
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return result;
    }
}
