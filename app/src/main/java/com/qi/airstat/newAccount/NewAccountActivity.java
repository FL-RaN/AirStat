package com.qi.airstat.newAccount;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.MotionEvent;
import android.view.View;

import com.qi.airstat.R;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class NewAccountActivity extends FragmentActivity {
    public static NewAccountActivity instance;

    private ViewPager createAccountPager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_account);

        instance = NewAccountActivity.this;

        createAccountPager = (ViewPager) findViewById(R.id.vp_new_account_container);
        createAccountPager.setAdapter(new NewAccountPagerAdapter(this.getSupportFragmentManager()));
        createAccountPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return true;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (createAccountPager.getCurrentItem() == 0) {
            super.onBackPressed();
        } else {
            // select the previous step.
            createAccountPager.setCurrentItem(createAccountPager.getCurrentItem() - 1);
        }
    }

    /*
    Change page manually
     */
    public void setCurrentPagerItem(int item) {
        createAccountPager.setCurrentItem(item);
    }
}
