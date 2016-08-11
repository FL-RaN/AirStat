package com.qi.airstat.dataMap;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
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

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class DataMapActivity extends FragmentActivity implements OnMapReadyCallback, LocationListener {

    public static Context context;

    private GoogleMap map;
    private MapFragment mapFragment;

    public static ArrayList<DataMapMarker> markers;
    public static ClusterManager<DataMapMarker> mClusterManager;

    private DefaultClusterRenderer mRenderer;

    private MarkerTouchDetector markerTouchDetector;
    private MapMovingDetector mapMovingDetector;
    private ClusterTouchDetector clusterTouchDetector;

    private BackgroundMarkerChanger backgroundMarkerChanger;
    private BackgroundClusterChanger backgroundClusterChanger;
//    public RefreshMap refreshMap;

    private DataMapCommunication dataMapCommunication;

    private DataMapPanelUi dataMapPanelUi;

    public final LatLng START_POINT = new LatLng(32.881265, -117.234139);
    private boolean firstCall = true;


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
                        Manifest.permission.ACCESS_COARSE_LOCATION,
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
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        setMapEvent();

//        initMarkers(markers);

        /*
        Start Communication service
         */
        dataMapCommunication = new DataMapCommunication(null, this);
        dataMapCommunication.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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

        mRenderer = new DataMapClusterRenderer(this, map, mClusterManager);
        mClusterManager.setRenderer(mRenderer);
        mClusterManager.setOnClusterItemClickListener(markerTouchDetector);
        mClusterManager.setOnClusterClickListener(clusterTouchDetector);
    }

    public static DataMapMarker findMarker(int cid) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == cid) {
                return marker;
            }
        }
        return null;
    }

    /*
    ************ Marker Setting *************
     */

    /*
    Add single Marker
     */
    public void initMarkerCollection() {
        markers.clear();
        mClusterManager.clearItems();
    }// Call First

    public void addMarker(int cid, long timeStamp, DataMapDataSet dataSet, LatLng location) {
        DataMapMarker marker = new DataMapMarker(cid, timeStamp, dataSet, location);
        markers.add(marker);
    }// Call Second


//    public void refreshMapThread() {
//        refreshMap = new RefreshMap(mClusterManager);
//        refreshMap.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }
//
//    private class RefreshMap extends AsyncTask<Void, Void, Void> {
//        private boolean flow = true;
//        private ClusterManager<DataMapMarker> mClusterManager;
//
//        public void stop() {
//            flow = false;
//            this.cancel(true);
//        }
//
//        private RefreshMap(ClusterManager<DataMapMarker> mClusterManager) {
//            this.mClusterManager = mClusterManager;
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//            removeAndDrawMarkers();
//            mClusterManager.cluster();
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//            flow = true;
//            while (flow) {
//                publishProgress();
//                try {
//                    Thread.sleep(Constants.HTTP_DATA_MAP_GET_ONGOING_SESSION_TIME_QUALTUM);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//            return null;
//        }
//    }


    public void addMarker(DataMapMarker marker) {
        markers.add(marker);
        mClusterManager.addItem(marker);

//        mClusterManager.cluster();
    }// Call Second

    private void appendMarker(int cid, long timeStamp, DataMapDataSet dataSet, double lat, double lng) {
        markers.add(new DataMapMarker(cid, timeStamp, dataSet, new LatLng(lat, lng)));
        mClusterManager.addItem(markers.get(markers.size() - 1));
        mClusterManager.cluster();
    }

    /*
    Refresh Marker data
     */
    public boolean refreshMarker(int cid, long timeStamp, DataMapDataSet dataSet, double lat, double lng) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == cid) {
                marker.setDataSet(dataSet);
                marker.setLocation(new LatLng(lat, lng));
                marker.setTimeStamp(timeStamp);
                return true;
            }
        }
        addMarker(cid, timeStamp, dataSet, new LatLng(lat, lng));
        return false;
//        removeAndDrawMarkers();
    }

    public boolean refreshMarker(DataMapMarker newMarker) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == newMarker.getConnectionID()) {
                if (marker.getConnectionID() == -1) {
                    Log.w("FIND!!", "THISISUSER, " + newMarker.getLocation().latitude + "//" + newMarker.getLocation().longitude);
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
    Remove and Draw Markers
     */
//    public void removeAndDrawMarkers() {
//        mClusterManager.clearItems();
////        (Collection<DataMapMarker>)((List<DataMapMarker>) markers);
////        for (DataMapMarker marker : markers) {
////            mClusterManager.addItem(marker);
////        }
//        mClusterManager.addItems((Collection<DataMapMarker>) ((List<DataMapMarker>) markers));
//
////        refreshMapThread();
//    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        DataMapCurrentUser.getInstance().setLat(lat);
        DataMapCurrentUser.getInstance().setLng(lng);

        Log.w("RealTime", "" + lat + "/" + lng);


        if (firstCall) {
            addMarker(DataMapCurrentUser.create());
            panelValueChanger(findMarker(-1));
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
    Rendering Marker
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

        for (DataMapMarker marker : markers) {
            avgAqiValue += marker.getAqiValue();
            avgTemperature += marker.getTemparature();
            avgCo += marker.getCo();
            avgSo2 += marker.getSo2();
            avgNo2 += marker.getNo2();
            avgO3 += marker.getO3();
            avgPm += marker.getPm();
        }

        avgAqiValue /= markers.size();
        avgTemperature /= markers.size();
        avgCo /= markers.size();
        avgSo2 /= markers.size();
        avgNo2 /= markers.size();
        avgO3 /= markers.size();
        avgPm /= markers.size();

        ValueAnimator colorAnimator = null;
        int priviousBackground = ((ColorDrawable) (dataMapPanelUi.barTitle.getBackground())).getColor();
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
    }

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
//            backgroundMarkerChanger.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            backgroundMarkerChanger.execute();
            dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            map.animateCamera(CameraUpdateFactory.newLatLng(clusterItem.getPosition()));
            return true;
        }
    }

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

    public void println(String msg) {
        Log.w("-------------PRINT", msg);
    }

    private class BackgroundMarkerChanger extends AsyncTask<DataMapMarker, DataMapMarker, Void> {
        private boolean flow = true;
        private DataMapMarker marker;
        String[] regionAddress = null;

        public void stop() {
            this.flow = false;
        }

        public boolean whoIsHost(int cid) {
            if (marker.getConnectionID() == cid) return true;
            return false;
        }

        public BackgroundMarkerChanger(DataMapMarker marker) {
            this.marker = marker;
        }

        @Override
        protected Void doInBackground(DataMapMarker... values) {
            if (marker.getConnectionID() != -1)
                regionAddress = getRegionAddress(marker.getLocation().latitude, marker.getLocation().longitude);
            while (flow) {
                publishProgress(marker);
                try {
//                    Thread.sleep(200);
                    SystemClock.sleep(200);
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
//                SystemClock.sleep(600);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
//        dataMapCommunication.stopThread();
//        backgroundMarkerChanger.stop();
//        backgroundClusterChanger.stop();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dataMapCommunication.stopThread();
        backgroundMarkerChanger.stop();
        backgroundClusterChanger.stop();
    }

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
