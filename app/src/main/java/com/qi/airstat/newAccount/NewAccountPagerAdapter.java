package com.qi.airstat.newAccount;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/2/2016.
 */
public class NewAccountPagerAdapter extends FragmentStatePagerAdapter {

    private NewAccountUi newAccountUi = NewAccountUi.getInstance();
    private ArrayList<Fragment> creatingAccountFragments;

    public NewAccountPagerAdapter(FragmentManager fm) {
        super(fm);

        creatingAccountFragments = new ArrayList<>();
        creatingAccountFragments.add(NameFragment.create());
        creatingAccountFragments.add(EmailFragment.create());
        creatingAccountFragments.add(PasswordFragment.create());
    }


    @Override
    public Fragment getItem(int position) {
       return creatingAccountFragments.get(position);

    }

    @Override
    public int getCount() {
        return creatingAccountFragments.size();
    }


}
