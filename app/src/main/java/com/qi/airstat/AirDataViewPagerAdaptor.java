package com.qi.airstat;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/*
 *  This class is adaptor for view pager inside fragment_air_data.
 *  Inside view pager, new fragment_air_data_view_pager will be instantiated in each page.
 */
public class AirDataViewPagerAdaptor extends FragmentStatePagerAdapter {
    public AirDataViewPagerAdaptor(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /*
     * Whenever user slides view pager, AirDataViewPagerFragment will show each fragment instance
     *  by index so user can see new page.
     */
    @Override
    public Fragment getItem(int position) {
        return AirDataViewPagerFragment.getInstanceByIndex(position);
    }

    @Override
    public int getCount() {
        return Constants.AIR_DATA_VIEW_PAGER_MAX_PAGES;
    }
}
