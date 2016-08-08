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

    private ClusterManager<DataMapMarker> mClusterManager;
    private DefaultClusterRenderer mRenderer;

    private MarkerTouchDetector markerTouchDetector;
    private MapMovingDetector mapMovingDetector;
    private ClusterTouchDetector clusterTouchDetector;

    private DataMapUi dataMapUi;

    private ArrayList<DataMapMarker> markers;
    private final LatLng START_POINT = new LatLng(32.881265, -117.234139);

    private DataMapReverseGeo reverseGeo;
    private boolean firstCall = true;
    private BackgroundMarkerChanger backgroundMarkerChanger;
    private BackgroundClusterChanger backgroundClusterChanger;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_data_map);
        context = this;

        setPermission();

        mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.fragment_data_map);
        mapFragment.getMapAsync(this);
        markers = new ArrayList<>();
        markerTouchDetector = new MarkerTouchDetector();
        mapMovingDetector = new MapMovingDetector();
        clusterTouchDetector = new ClusterTouchDetector();
        dataMapUi = new DataMapUi();

        setWidgets(dataMapUi);
    }

    private void setPermission() {
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

    private void setWidgets(DataMapUi dataMapUi) {
        dataMapUi.slidingUpPanelLayout = (SlidingUpPanelLayout) findViewById(R.id.slider_data_map_container);
        dataMapUi.tvTitle = (TextView) findViewById(R.id.tv_data_map_title);
        dataMapUi.tvAqiGrade = (TextView) findViewById(R.id.tv_data_map_grade);
        dataMapUi.tvTemp = (TextView) findViewById(R.id.tv_data_map_temp);
        dataMapUi.barTitle = (LinearLayout) findViewById(R.id.bar_data_map_title);

//        dataMapUi.slidingUpPanelLayout.setShadowHeight(0);
        dataMapUi.slidingUpPanelLayout.setAnchorPoint(0.4f);
        dataMapUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        setUpMap(map);

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(START_POINT, 14.0f));

        //Make a ClusterManager
        mClusterManager = new ClusterManager<>(this, map);
        map.setOnCameraChangeListener(mClusterManager);
        map.setOnMarkerClickListener(mClusterManager);
        map.setOnCameraIdleListener(mapMovingDetector);
        map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                if (dataMapUi.slidingUpPanelLayout.getPanelState() == SlidingUpPanelLayout.PanelState.ANCHORED)
                    dataMapUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                else
                    dataMapUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.HIDDEN);
            }
        });


        mRenderer = new DataMapClusterRenderer(this, map, mClusterManager);
        mClusterManager.setRenderer(mRenderer);
        mClusterManager.setOnClusterItemClickListener(markerTouchDetector);
        mClusterManager.setOnClusterClickListener(clusterTouchDetector);

        initMarkers(markers);
    }

    /*
    Set map options
    */
    private void setUpMap(GoogleMap map) {
        try {
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
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
        drawMarkers(markers);

        addMarker("La Jolla shores", 32.858625, -117.256091, 10);
        addMarker("La Jolla Village Square", 32.865494, -117.228736, 200);
        addMarker("WestField UTC", 32.870661, -117.206321, 140);
    }

    /*
    Add single Marker
     */
    private void addMarker(String title, double lat, double lng, float aqiValue) {
        markers.add(new DataMapMarker(title, new LatLng(lat, lng), aqiValue));

        drawMarkers(markers);
    }

    /*
    Refresh Marker data
     */
    private void refreshMarker(String title, double lat, double lng, float aqiValue) {
        for (DataMapMarker marker : markers) {
            if (marker.getTitle().equals(title)) {
                markers.remove(marker);
                markers.add(new DataMapMarker(title, new LatLng(lat, lng), aqiValue));
                break;
            }
        }
        drawMarkers(markers);
    }

    /*
    Delete single Marker
     */
    private void deleteMarker(String title) {
        for (DataMapMarker marker : markers) {
            if (marker.getTitle().equals(title)) {
                markers.remove(marker);
            }
        }
        drawMarkers(markers);
    }

    /*
    Draw/Redraw Marker
     */
    private void drawMarkers(ArrayList<DataMapMarker> markers) {
        mClusterManager.clearItems();

        for (DataMapMarker marker : markers) {
            mClusterManager.addItem(marker);
        }

        mClusterManager.cluster();
    }


    private void renderMarker(DataMapMarker clusterItem) {
        if (clusterItem == null) return;

        DataMapMarker marker = clusterItem;
        dataMapUi.tvTitle.setText(marker.getTitle());

        float aqiValue = marker.getAqiValue();
        String grade;

        if (0 <= aqiValue && aqiValue <= 50) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_GOOD);
            grade = "GOOD";
        } else if (50 < aqiValue && aqiValue <= 100) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_MODERATE);
            grade = "MODERATE";
        } else if (100 < aqiValue && aqiValue <= 150) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_SENSITIVE);
            grade = "SENSITIVE";
        } else if (150 < aqiValue && aqiValue <= 200) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_UNHEALTHY);
            grade = "UNHEALTHY";
        } else if (200 < aqiValue && aqiValue <= 300) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_VERY_UNHEALTHY);
            grade = "VERY UNHEALTHY";
        } else if (300 < aqiValue && aqiValue <= 500) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_HAZARDOUS);
            grade = "HAZARDOUS";
        } else {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_DEFAULT);
            grade = "WAITING...";
        }

        dataMapUi.tvAqiGrade.setText(grade);
        dataMapUi.tvTemp.setText(String.format("%.1f", aqiValue));

    }

    private void renderCluster(Cluster cluster) {
        //Whole items in cluster as COLLECTION
        Collection<DataMapMarker> markers = cluster.getItems();
        //Calc average aqi value
        float avgAqiValue = 0;
        for (DataMapMarker marker : markers) {
            avgAqiValue += marker.getAqiValue();
        }
        avgAqiValue = avgAqiValue / markers.size();

        //Check grade by aqi value
        String grade;
        if (0 <= avgAqiValue && avgAqiValue <= 50) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_GOOD);
            grade = "GOOD";
        } else if (50 < avgAqiValue && avgAqiValue <= 100) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_MODERATE);
            grade = "MODERATE";
        } else if (100 < avgAqiValue && avgAqiValue <= 150) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_SENSITIVE);
            grade = "SENSITIVE";
        } else if (150 < avgAqiValue && avgAqiValue <= 200) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_UNHEALTHY);
            grade = "UNHEALTHY";
        } else if (200 < avgAqiValue && avgAqiValue <= 300) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_VERY_UNHEALTHY);
            grade = "VERY UNHEALTHY";
        } else if (300 < avgAqiValue && avgAqiValue <= 500) {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_HAZARDOUS);
            grade = "HAZARDOUS";
        } else {
            dataMapUi.barTitle.setBackgroundResource(R.drawable.AQI_DEFAULT);
            grade = "WAITING...";
        }

        //UI Set
        dataMapUi.tvAqiGrade.setText(grade);
        dataMapUi.tvTemp.setText("" + avgAqiValue);
    }

    private DataMapMarker findMarker(String title) {
        for (DataMapMarker marker : markers) {
            if (marker.getTitle().equals(title)) {
                return marker;
            }
        }
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();


        refreshMarker("ME", lat, lng, (float) Math.random() * 500);

        DataMapMarker myMarker = findMarker("ME");

        if (firstCall) {
            renderMarker(myMarker);
            map.animateCamera(CameraUpdateFactory.newLatLng(myMarker.getLocation()));
            firstCall = false;
        }

//        if (backgroundClusterChanger==null && backgroundMarkerChanger != null && backgroundMarkerChanger.whoIsHost("ME")) {
//            if (backgroundMarkerChanger != null) {
//                backgroundMarkerChanger.stop();
//                backgroundMarkerChanger.cancel(true);
//            }
////            if(backgroundClusterChanger != null){
////                backgroundClusterChanger.stop();
////                backgroundClusterChanger.cancel(true);
////            }
//            backgroundMarkerChanger = new BackgroundMarkerChanger(myMarker);
//            backgroundMarkerChanger.execute();
//        }
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
            backgroundMarkerChanger = new BackgroundMarkerChanger((DataMapMarker) clusterItem);
            backgroundMarkerChanger.execute();

            map.animateCamera(CameraUpdateFactory.newLatLng(clusterItem.getPosition()));

            dataMapUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            return true;
        }
    }

    private class ClusterTouchDetector implements ClusterManager.OnClusterClickListener {

        @Override
        public boolean onClusterClick(Cluster cluster) {
            // Position info
            double lat = cluster.getPosition().latitude;
            double lng = cluster.getPosition().longitude;

            // Reverse GEOCODING -- USING HTTP

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
            dataMapUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
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
            while (flow) {
                publishProgress(marker);
                SystemClock.sleep(3000);
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

            renderMarker(values[0]);
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
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected Void doInBackground(Cluster... clusters) {
            regionAddress = getRegionAddress();
            while (flow) {
                publishProgress(clusters[0]);
//                SystemClock.sleep(3000);
                try {
                    Thread.sleep(3000);
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
                dataMapUi.slidingUpPanelLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                stop();
                this.cancel(true);
                return;
            }
            renderCluster(clusters[0]);
            dataMapUi.tvTitle.setText(regionAddress);
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            stop();
        }

        public String getRegionAddress() {
            String apiURL = "http://maps.googleapis.com/maps/api/geocode/json?latlng="
                    + latitude + "," + longitude;

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

            regionAddress = result;

            return result;
        }
    }
}
