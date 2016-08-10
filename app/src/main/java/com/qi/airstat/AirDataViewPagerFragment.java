package com.qi.airstat;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

public class AirDataViewPagerFragment extends Fragment {
    public LineChart graph = null;
    public int currentIndex = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_air_data_view_pager, container, false);
        LineChart lineChart = (LineChart) root.findViewById(R.id.lic_air_data_view_pager);

        lineChart.getXAxis().setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeWhite));
        lineChart.getAxisRight().setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeWhite));
        lineChart.getAxisLeft().setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeWhite));

        if (lineChart.getData() == null) {
            LineData lineData = new LineData();
            LineDataSet set = new LineDataSet(null, null);

            switch (currentIndex) {
                case Constants.AIR_LABEL_INDEX_PM25:
                    lineChart.setDescription(Constants.AIR_GRAPH_DESCRIPTION_PM25);
                    set.setLabel(Constants.AIR_GRAPH_DESCRIPTION_PM25);
                    break;
                case Constants.AIR_LABEL_INDEX_TEMPERATURE:
                    lineChart.setDescription(Constants.AIR_GRAPH_DESCRIPTION_TEMPERATURE);
                    set.setLabel(Constants.AIR_GRAPH_DESCRIPTION_TEMPERATURE);
                    break;
                case Constants.AIR_LABEL_INDEX_CO:
                    lineChart.setDescription(Constants.AIR_GRAPH_DESCRIPTION_CO);
                    set.setLabel(Constants.AIR_GRAPH_DESCRIPTION_CO);
                    break;
                case Constants.AIR_LABEL_INDEX_SO2:
                    lineChart.setDescription(Constants.AIR_GRAPH_DESCRIPTION_SO2);
                    set.setLabel(Constants.AIR_GRAPH_DESCRIPTION_SO2);
                    break;
                case Constants.AIR_LABEL_INDEX_NO2:
                    lineChart.setDescription(Constants.AIR_GRAPH_DESCRIPTION_NO2);
                    set.setLabel(Constants.AIR_GRAPH_DESCRIPTION_NO2);
                    break;
                case Constants.AIR_LABEL_INDEX_O3:
                    lineChart.setDescription(Constants.AIR_GRAPH_DESCRIPTION_O3);
                    set.setLabel(Constants.AIR_GRAPH_DESCRIPTION_O3);
                    break;
            }

            set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set.setValueTextColor(ColorTemplate.getHoloBlue());
            set.setColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeLemonYellow));
            set.setCircleColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeLemonYellow));
            set.setHighlightEnabled(true);
            set.setValueTextColor(Color.WHITE);
            set.setValueTextSize(9f);
            set.setDrawValues(true);
            lineData.addDataSet(set);

            lineChart.setData(lineData);
        }

        graph = lineChart;

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
}
