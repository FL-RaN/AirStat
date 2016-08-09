package com.qi.airstat.dataMap;

import android.Manifest;
import android.content.Context;
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

    private ArrayList<DataMapMarker> markers;
    private ClusterManager<DataMapMarker> mClusterManager;

    private DefaultClusterRenderer mRenderer;

    private MarkerTouchDetector markerTouchDetector;
    private MapMovingDetector mapMovingDetector;
    private ClusterTouchDetector clusterTouchDetector;

    private BackgroundMarkerChanger backgroundMarkerChanger;
    private BackgroundClusterChanger backgroundClusterChanger;

    private DataMapCommunication dataMapCommunication;

    private DataMapPanelUi dataMapPanelUi;

    private final LatLng START_POINT = new LatLng(32.881265, -117.234139);
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
                makeToast("Acctected");
            }

            @Override
            public void onPermissionDenied(ArrayList<String> arrayList) {
                makeToast("Rejected");
            }
        };

        new TedPermission(this).setPermissionListener(permissionListener)
                .setRationaleMessage("We need your permission")
                .setDeniedMessage("You make it reject")
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
        dataMapPanelUi.tvAqiGrade = (TextView) findViewById(R.id.tv_data_map_grade);

        dataMapPanelUi.tvAqiValue = (TextView) findViewById(R.id.tv_data_map_panel_aqi);
        dataMapPanelUi.tvTemperature = (TextView) findViewById(R.id.tv_data_map_panel_temp);
        dataMapPanelUi.tvCo = (TextView) findViewById(R.id.tv_data_map_panel_co);
        dataMapPanelUi.tvSo2 = (TextView) findViewById(R.id.tv_data_map_panel_so2);
        dataMapPanelUi.tvNo2 = (TextView) findViewById(R.id.tv_data_map_panel_no2);
        dataMapPanelUi.tvO3 = (TextView) findViewById(R.id.tv_data_map_panel_o3);
        dataMapPanelUi.tvPm = (TextView) findViewById(R.id.tv_data_map_panel_pm2_5);

        dataMapPanelUi.imgPanelArrow = (ImageView) findViewById(R.id.iv_data_map_panel_arrow);

//        dataMapPanelUi.slidingUpPanelLayout.setShadowHeight(0);
        dataMapPanelUi.slidingUpPanelLayout.setAnchorPoint(0.14f);
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

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        setMapEvent();

        initMarkers(markers);

        /*
        Start Communication service
         */
        dataMapCommunication = new DataMapCommunication(this, this);
        dataMapCommunication.executeHttpConn();
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

    private DataMapMarker findMarker(String title) {
        for (DataMapMarker marker : markers) {
            if (marker.getTitle().equals(title)) {
                return marker;
            }
        }
        return null;
    }

    /*
    ************ Marker Setting *************
     */

    private void initMarkers(ArrayList<DataMapMarker> markers) {

        /*
        Initialized user location with FAKE data
         */
        markers.add(new DataMapMarker("ME", START_POINT, 259));
        renderMarker(markers.get(markers.size() - 1));
        map.animateCamera(CameraUpdateFactory.newLatLng(START_POINT));

        for (int i = 1; i < 10; i++) {
            markers.add(new DataMapMarker("test" + i, new LatLng(START_POINT.latitude + (i / 50d), START_POINT.longitude + (i / 200d)), (float) (Math.random() * 300)));
        }
        LatLng test = new LatLng(32.736422, -117.148093);
        for (int i = 0; i < 10; i++) {
            markers.add(new DataMapMarker("testtest" + i, new LatLng(test.latitude + (i / 50d), test.longitude + (i / 200d)), (float) (Math.random() * 300)));
        }
        test = new LatLng(32.513823, -116.962939);
        for (int i = 0; i < 10; i++) {
            markers.add(new DataMapMarker("testtest" + i, new LatLng(test.latitude + (i / 50000d), test.longitude + (i / 200d)), (float) (Math.random() * 300)));
        }
        test = new LatLng(32.829234, -116.526494);
        for (int i = 0; i < 10; i++) {
            markers.add(new DataMapMarker("testtest" + i, new LatLng(test.latitude + (i / 30d), test.longitude + (i / 20d)), 0));
        }
        removeAndDrawMarkers();

        appendMarker("La Jolla shores", 32.858625, -117.256091, 10);
        appendMarker("La Jolla Village Square", 32.865494, -117.228736, 200);
        appendMarker("WestField UTC", 32.870661, -117.206321, 140);
    }

    /*
    Add single Marker
     */
    public void initMarkerCollection() {
        markers.clear();
        mClusterManager.clearItems();
    }// Call First

    public void addMarker(String title, DataMapDataSet dataSet, LatLng location) {
        DataMapMarker marker = new DataMapMarker(title, location, 100);
        marker.setDataSet(dataSet);

        markers.add(marker);
        mClusterManager.addItem(marker);

        mClusterManager.cluster();
    }// Call Second

    private void appendMarker(String title, double lat, double lng, float aqiValue) {
        markers.add(new DataMapMarker(title, new LatLng(lat, lng), aqiValue));
        mClusterManager.addItem(markers.get(markers.size() - 1));
        mClusterManager.cluster();
    }

    /*
    Refresh Marker data
     */
    private void refreshMarker(String title, double lat, double lng, float aqiValue) {
        for (DataMapMarker marker : markers) {
            if (marker.getTitle().equals(title)) {
                marker.setLocation(new LatLng(lat, lng));
                marker.setAqiValue(aqiValue);
                break;
            }
        }
        removeAndDrawMarkers();
    }

    /*
    Remove and Draw Markers
     */
    public void removeAndDrawMarkers() {
        mClusterManager.clearItems();

        for (DataMapMarker marker : markers) {
            mClusterManager.addItem(marker);
        }

        mClusterManager.cluster();
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        DataMapCurrentLocation.getInstance().setLat(lat);
        DataMapCurrentLocation.getInstance().setLng(lng);

        refreshMarker("ME", lat, lng, (float) Math.random() * 500);

        DataMapMarker myMarker = findMarker("ME");
        Log.w("-----------------MARKER", myMarker.toString());

        if (firstCall) {
            renderMarker(myMarker);
            map.animateCamera(CameraUpdateFactory.newLatLng(myMarker.getLocation()));
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
    private void renderMarker(DataMapMarker clusterItem) {
        if (clusterItem == null) return;

        DataMapMarker marker = clusterItem;
        dataMapPanelUi.tvTitle.setText(marker.getTitle());

        float aqiValue = marker.getAqiValue();
        String grade;

        if (0 <= aqiValue && aqiValue <= 50) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_GOOD);
            grade = "GOOD";
        } else if (50 < aqiValue && aqiValue <= 100) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_MODERATE);
            grade = "MODERATE";
        } else if (100 < aqiValue && aqiValue <= 150) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_SENSITIVE);
            grade = "SENSITIVE";
        } else if (150 < aqiValue && aqiValue <= 200) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_UNHEALTHY);
            grade = "UNHEALTHY";
        } else if (200 < aqiValue && aqiValue <= 300) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_VERY_UNHEALTHY);
            grade = "VERY UNHEALTHY";
        } else if (300 < aqiValue && aqiValue <= 500) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_HAZARDOUS);
            grade = "HAZARDOUS";
        } else {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_DEFAULT);
            grade = "WAITING...";
        }

        panelMarkerValueSetter(marker, grade);
    }

    public void panelMarkerValueSetter(DataMapMarker marker, String grade) {
        dataMapPanelUi.tvTitle.setText(marker.getTitle());
        dataMapPanelUi.tvAqiGrade.setText(grade);

        dataMapPanelUi.tvAqiValue.setText("" + marker.getAqiValue());
        dataMapPanelUi.tvTemperature.setText("" + marker.getTemparature());
        dataMapPanelUi.tvCo.setText("" + marker.getCo());
        dataMapPanelUi.tvSo2.setText("" + marker.getSo2());
        dataMapPanelUi.tvNo2.setText("" + marker.getNo2());
        dataMapPanelUi.tvO3.setText("" + marker.getO3());
        dataMapPanelUi.tvPm.setText("" + marker.getPm());
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

        //Check grade by aqi value
        String grade;
        if (0 <= avgAqiValue && avgAqiValue <= 50) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_GOOD);
            grade = "GOOD";
        } else if (50 < avgAqiValue && avgAqiValue <= 100) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_MODERATE);
            grade = "MODERATE";
        } else if (100 < avgAqiValue && avgAqiValue <= 150) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_SENSITIVE);
            grade = "SENSITIVE";
        } else if (150 < avgAqiValue && avgAqiValue <= 200) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_UNHEALTHY);
            grade = "UNHEALTHY";
        } else if (200 < avgAqiValue && avgAqiValue <= 300) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_VERY_UNHEALTHY);
            grade = "VERY UNHEALTHY";
        } else if (300 < avgAqiValue && avgAqiValue <= 500) {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_HAZARDOUS);
            grade = "HAZARDOUS";
        } else {
            dataMapPanelUi.barTitle.setBackgroundResource(R.drawable.AQI_DEFAULT);
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
            DataMapMarker marker = findMarker(((DataMapMarker) clusterItem).getTitle());
            Log.w("TOUCHED", marker.toString());
            backgroundMarkerChanger = new BackgroundMarkerChanger(findMarker(((DataMapMarker) clusterItem).getTitle()));
            backgroundMarkerChanger.execute();
//            renderMarker((DataMapMarker) clusterItem);


            map.animateCamera(CameraUpdateFactory.newLatLng(clusterItem.getPosition()));
            dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
            backgroundClusterChanger = new BackgroundClusterChanger(lat, lng);
            backgroundClusterChanger.execute(cluster);

            map.animateCamera(CameraUpdateFactory.newLatLng(cluster.getPosition()));
            dataMapPanelUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
    }

    private class MapMovingDetector implements GoogleMap.OnCameraIdleListener {
        @Override
        public void onCameraIdle() {
            LatLng northeast = map.getProjection().getVisibleRegion().latLngBounds.northeast;
            LatLng southwest = map.getProjection().getVisibleRegion().latLngBounds.southwest;
            //Requesting Area to DB
            //southwest.latitude < WHAT < northeast.latitude , northeast.longitude < WHAT < southwest.longitude

            DataMapCurrentLocation.getInstance().setMinLat(southwest.latitude);
            DataMapCurrentLocation.getInstance().setMaxLat(northeast.latitude);
            DataMapCurrentLocation.getInstance().setMinLng(northeast.longitude);
            DataMapCurrentLocation.getInstance().setMaxLng(southwest.longitude);

//            makeToast("(" + northeast.latitude + "," + northeast.longitude + "), (" + southwest.latitude + "," + southwest.longitude + ")");
        }
    }


    public void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    public void println(String msg) {
        Log.w("-------------PRINT", msg);
    }

       /* Someone's code*/
//    private void setMarkers(JSONObject response) {
//
//        mClusterManager.clearItems();
//
//        try {
//            JSONArray venues = response.getJSONArray("venues");
//
//            for (int i = 0; i < venues.length(); i++) {
//                Venue venue = new Gson().fromJson(venues.getJSONObject(i).toString(), Venue.class);
//                MarkerItem marker = new MarkerItem(venue.getLat(), venue.getLng(), venue, R.drawable.pin_quente);
//                mClusterManager.addItem(marker);
//            }
//
//            mClusterManager.cluster();
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }

    private class BackgroundMarkerChanger extends AsyncTask<DataMapMarker, DataMapMarker, Void> {
        private boolean flow = true;
        private DataMapMarker marker;
        String regionAddress = null;

        public void stop() {
            this.flow = false;
        }

        public boolean whoIsHost(String title) {
            if (marker.getTitle().equals(title)) return true;
            return false;
        }

        public BackgroundMarkerChanger(DataMapMarker marker) {
            this.marker = marker;
        }

        @Override
        protected Void doInBackground(DataMapMarker... values) {
            if (!marker.getTitle().equals("ME"))
                regionAddress = getRegionAddress(marker.getLocation().latitude, marker.getLocation().longitude);
            while (flow) {
                publishProgress(marker);
                SystemClock.sleep(400);
//                try {
//                    Thread.sleep(3000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(DataMapMarker... values) {
            super.onProgressUpdate(values);
            Log.w("---------OnTHREAD", values[0].toString());
            renderMarker(values[0]);
            if (regionAddress != null)
                dataMapPanelUi.tvTitle.setText(regionAddress);
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
        String regionAddress;

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
//                SystemClock.sleep(3000);
                try {
                    Thread.sleep(600);
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
            dataMapPanelUi.tvTitle.setText(regionAddress);
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
        dataMapCommunication.stop();

    }

    public String getRegionAddress(double lat, double lng) {
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
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        JSONObject jObj = null;
        String result = null;
        try {
            jObj = new JSONObject(jsonString);
            JSONArray jArray = jObj.getJSONArray("results");
            jObj = (JSONObject) jArray.get(0);
            jArray = jObj.getJSONArray("address_components");
            result = (String) ((JSONObject) jArray.get(3)).get("short_name");
//            result = (String) jObj.get("formatted_address");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return result;
    }

}
