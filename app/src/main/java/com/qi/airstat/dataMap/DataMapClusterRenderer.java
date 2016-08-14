package com.qi.airstat.dataMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.Cluster;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;
import com.google.maps.android.ui.IconGenerator;
import com.qi.airstat.Constants;
import com.qi.airstat.R;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by JUMPSNACK on 8/5/2016.
 */
public class DataMapClusterRenderer extends DefaultClusterRenderer<DataMapMarker> {

    private View view;
    private ImageView imgMarker;
    private TextView tvMarker;
    private Context context;
    private ArrayList<DataMapMarker> markers;
    private GoogleMap map;

    private final int AT_LEAST_NUMBER_OF_MARKER_FOR_CLUSTER = 3;


    @Override
    protected boolean shouldRenderAsCluster(Cluster<DataMapMarker> cluster) {
        float currentZoom = DataMapCurrentUser.getCurrentZoom();
        float currrentMaxZoom = DataMapCurrentUser.getCurrentMaxZoom();
        return currentZoom < currrentMaxZoom-4 && cluster.getSize() >= AT_LEAST_NUMBER_OF_MARKER_FOR_CLUSTER;
    }

    public DataMapClusterRenderer(Context context, GoogleMap map, ClusterManager<DataMapMarker> clusterManager, ArrayList<DataMapMarker> markers) {
        super(context, map, clusterManager);
        this.context = context;
        this.map = map;
        view = LayoutInflater.from(context).inflate(R.layout.marker_custom, null);
        imgMarker = (ImageView) view.findViewById(R.id.img_marker);
        tvMarker = (TextView) view.findViewById(R.id.tv_marker);
        this.markers = markers;
    }

    @Override
    protected void onBeforeClusterRendered(Cluster<DataMapMarker> cluster, MarkerOptions markerOptions) {
        super.onBeforeClusterRendered(cluster, markerOptions);

        Collection<DataMapMarker> clusteredMarkers = cluster.getItems();
        IconGenerator clusterIconGenerator = new IconGenerator(context);

        float avgAqiValue = 0;
        int countClusterItem = 0;
        for (DataMapMarker marker : clusteredMarkers) {
            if (marker.getAqiValue() < 0)
                continue;

            avgAqiValue += marker.getAqiValue();
            countClusterItem++;
        }

        avgAqiValue = avgAqiValue / countClusterItem;

        int clusterColor = 0;
        if (0 <= avgAqiValue && avgAqiValue <= 50) {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_GOOD);
        } else if (50 < avgAqiValue && avgAqiValue <= 100) {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_MODERATE);
        } else if (100 < avgAqiValue && avgAqiValue <= 150) {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_SENSITIVE);
        } else if (150 < avgAqiValue && avgAqiValue <= 200) {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_UNHEALTHY);
        } else if (200 < avgAqiValue && avgAqiValue <= 300) {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_VERY_UNHEALTHY);
        } else if (300 < avgAqiValue && avgAqiValue <= 500) {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_HAZARDOUS);
        } else {
            clusterColor = Color.parseColor(Constants.AQI_LEVEL_DEFAULT);
        }

        Drawable clusterIcon = ResourcesCompat.getDrawable(context.getResources(), R.drawable.cluster_background, null);
        clusterIcon.setColorFilter(clusterColor, PorterDuff.Mode.SRC_ATOP);

        clusterIconGenerator.setBackground(clusterIcon);

        Bitmap icon = clusterIconGenerator.makeIcon();
        icon = Bitmap.createScaledBitmap(icon, icon.getWidth() * 2 / 3, icon.getHeight() * 2 / 3, false);
        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    @Override
    protected void onClusterItemRendered(DataMapMarker clusterItem, Marker marker) {
        super.onClusterItemRendered(clusterItem, marker);

        double aqiValue = clusterItem.getAqiValue();

        if (0 <= aqiValue && aqiValue <= 50) {
            imgMarker.setBackgroundResource(R.drawable.marker_good);
        } else if (50 < aqiValue && aqiValue <= 100) {
            imgMarker.setBackgroundResource(R.drawable.marker_moderate);
        } else if (100 < aqiValue && aqiValue <= 150) {
            imgMarker.setBackgroundResource(R.drawable.marker_sensitive);
        } else if (150 < aqiValue && aqiValue <= 200) {
            imgMarker.setBackgroundResource(R.drawable.marker_unhealthy);
        } else if (200 < aqiValue && aqiValue <= 300) {
            imgMarker.setBackgroundResource(R.drawable.marker_very_unhealthy);
        } else if (300 < aqiValue && aqiValue <= 500) {
            imgMarker.setBackgroundResource(R.drawable.marker_hazardous);
        } else {
            imgMarker.setBackgroundResource(R.drawable.marker_default);
        }

        int scale; String label;
        if (clusterItem.getConnectionID() == Constants.CID_BLC) {
            label = "ME";
            scale = 2;
        } else {
            label = "";
            scale = 3;
        }
        Bitmap icon = createDrawableFromView(DataMapActivity.context, view);
        icon = Bitmap.createScaledBitmap(icon, icon.getWidth() * 2 / scale, icon.getHeight() * 2 / scale, false);

        marker.setIcon(BitmapDescriptorFactory.fromBitmap(icon));

        marker.setPosition(findMarker(clusterItem.getConnectionID()).getLocation());
        tvMarker.setText(label);

    }

    public DataMapMarker findMarker(int cid) {
        for (DataMapMarker marker : markers) {
            if (marker.getConnectionID() == cid) {
                return marker;
            }
        }
        return null;
    }

    @Override
    protected void onBeforeClusterItemRendered(DataMapMarker item, MarkerOptions markerOptions) {
        super.onBeforeClusterItemRendered(item, markerOptions);

//        double aqiValue = item.getAqiValue();
//
//        if (0 <= aqiValue && aqiValue <= 50) {
//            imgMarker.setBackgroundResource(R.drawable.marker_good);
//        } else if (50 < aqiValue && aqiValue <= 100) {
//            imgMarker.setBackgroundResource(R.drawable.marker_moderate);
//        } else if (100 < aqiValue && aqiValue <= 150) {
//            imgMarker.setBackgroundResource(R.drawable.marker_sensitive);
//        } else if (150 < aqiValue && aqiValue <= 200) {
//            imgMarker.setBackgroundResource(R.drawable.marker_unhealthy);
//        } else if (200 < aqiValue && aqiValue <= 300) {
//            imgMarker.setBackgroundResource(R.drawable.marker_very_unhealthy);
//        } else if (300 < aqiValue && aqiValue <= 500) {
//            imgMarker.setBackgroundResource(R.drawable.marker_hazardous);
//        } else {
//            imgMarker.setBackgroundResource(R.drawable.marker_default);
//        }
//
//        int scale;
//        if (item.getConnectionID() == -1) {
//            tvMarker.setText("ME");
//            scale = 2;
//        } else {
//            tvMarker.setText("");
//            scale = 3;
//        }
//        Bitmap icon = createDrawableFromView(DataMapActivity.context, view);
//        icon = Bitmap.createScaledBitmap(icon, icon.getWidth() * 2 / scale, icon.getHeight() * 2 / scale, false);
//
//        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(icon));
    }

    // Convert view to bmp
    private Bitmap createDrawableFromView(Context context, View view) {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        view.measure(displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.layout(0, 0, displayMetrics.widthPixels, displayMetrics.heightPixels);
        view.buildDrawingCache();
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        return bitmap;
    }
}
