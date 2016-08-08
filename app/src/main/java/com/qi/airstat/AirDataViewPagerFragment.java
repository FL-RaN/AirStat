package com.qi.airstat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.LineData;

public class AirDataViewPagerFragment extends Fragment {
    public int currentIndex = 0;
    public LineChart lineChart = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_air_data_view_pager, container, false);

        lineChart = (LineChart)root.findViewById(R.id.lic_air_data_view_pager);
        if (lineChart.getData() == null) {
            lineChart.setData(new LineData());
        }

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Read current index number of generated page.
        currentIndex = getArguments().getInt(Constants.AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX);
    }

    // Whenever user slides page, AirDataViewPagerAdaptor will call this function and
    // will get new page by index for view pager.
    static public AirDataViewPagerFragment getInstanceByIndex(int index) {
        AirDataViewPagerFragment newFragment = new AirDataViewPagerFragment();
        Bundle args = new Bundle();

        // Tell what is next index for new page.
        // This index value will be read whenever generate new instance.
        args.putInt(Constants.AIR_DATA_VIEW_PAGER_BUNDLE_PAGE_INDEX, index);
        newFragment.setArguments(args);

        return newFragment;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public LineChart getLineChart() {
        return lineChart;
    }
}
