package com.qi.airstat;

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

public class HeartRateDataFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_heart_rate_data, container, false);
        LineChart lineChart = (LineChart)root.findViewById(R.id.lic_heart_rate_data_graph);

        lineChart.getXAxis().setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeWhite));
        lineChart.getAxisRight().setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeWhite));
        lineChart.getAxisLeft().setTextColor(ContextCompat.getColor(this.getContext(), R.color.colorThemeWhite));

        if (lineChart.getData() == null) {
            LineData lineData = new LineData();
            LineDataSet set = new LineDataSet(null, "Heart Rate");

            set.setAxisDependency(YAxis.AxisDependency.RIGHT);
            set.setValueTextColor(ColorTemplate.getHoloBlue());
            set.setColor(ContextCompat.getColor(this.getContext(), R.color.colorHeartRed));
            set.setCircleColor(ContextCompat.getColor(this.getContext(), R.color.colorHeartRed));
            set.setHighlightEnabled(true);
            set.setValueTextColor(Color.WHITE);
            set.setValueTextSize(9f);
            set.setDrawValues(true);
            lineData.addDataSet(set);

            lineChart.setData(lineData);
        }

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
