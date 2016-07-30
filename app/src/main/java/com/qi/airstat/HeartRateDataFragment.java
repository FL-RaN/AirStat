package com.qi.airstat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HeartRateDataFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_heart_rate_data, container, false);

        ((com.github.mikephil.charting.charts.LineChart)root.findViewById(R.id.lic_heart_rate_data_graph)).setData(new com.github.mikephil.charting.data.LineData());

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
