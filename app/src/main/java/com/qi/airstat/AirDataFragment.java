package com.qi.airstat;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/*
 *  This fragment is child view of AirDataActivity.
 *  and also having a view pager inside.
 */
public class AirDataFragment extends Fragment {
    // To display fragment_air_data_view_pager, it'll use view pager inside fragment_air_data.
    private ViewPager viewPager = null;
    // And also adaptor will be attached to view pager.
    private PagerAdapter pagerAdapter = null;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup)inflater.inflate(R.layout.fragment_air_data, container, false);

        // Find view pager which will be attached by pager adapter.
        viewPager = (ViewPager)root.findViewById(R.id.vp_air_data_graph);
        // Get view pager adapter.
        pagerAdapter = new AirDataViewPagerAdaptor(getFragmentManager());
        // Attach pager adapter to view pager.
        viewPager.setAdapter(pagerAdapter);

        return root;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
