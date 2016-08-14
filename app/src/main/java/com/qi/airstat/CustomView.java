package com.qi.airstat;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.qi.airstat.dataMap.DataMapActivity;

/**
 * Created by JUMPSNACK on 8/10/2016.
 */
/*
Bottom navigation bar
 */
public class CustomView extends LinearLayout implements View.OnClickListener {
    private Button btnDashboard;
    private Button btnMap;

    public static boolean isButtonPushed = false;
    public static boolean isPushedDashboard = true;

    private final String SELECTED_COLOR = "#F23434";
    private final String DEFAULT_COLOR = "#88ffffff";

    public CustomView(Context context) {
        super(context);
        initView(context);
    }

    public CustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public void initView(Context context) {
        String infService = Context.LAYOUT_INFLATER_SERVICE;
        LayoutInflater li = (LayoutInflater) getContext().getSystemService(infService);
        View v = li.inflate(R.layout.view_custom, this, false);
        addView(v);

        btnDashboard = (Button) findViewById(R.id.btn_custom_view_left);
        btnMap = (Button) findViewById(R.id.btn_custom_view_right);

        if (isPushedDashboard) {    // Click dashboard button
            btnImgChanger(SELECTED_COLOR, DEFAULT_COLOR);
        } else {    // Click map button
            btnImgChanger(DEFAULT_COLOR, SELECTED_COLOR);
        }
        isButtonPushed = false; // Flag about clicking any button on the bottom navigation bar

        btnDashboard.setOnClickListener(this);
        btnMap.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        Button btnClicked = (Button) view;
        isButtonPushed = true;

        if (btnClicked.equals(btnDashboard)) {  // Click dashboard button
            MainActivity.instance.finishActivity(300);
            MainActivity.instance.startActivityForResult(new Intent(getContext(), SensorDataOverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 200);
            isPushedDashboard = true;
        } else if (btnClicked.equals(btnMap)) { // Click map button
            MainActivity.instance.finishActivity(200);
            MainActivity.instance.startActivityForResult(new Intent(getContext(), DataMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION), 300);
            isPushedDashboard = false;
        }
    }

    /*
    Selected button image changer
     */
    private void btnImgChanger(String target, String revers) {
        Drawable img = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_bottom_dashboard, null);

        img.setColorFilter(Color.parseColor(target), PorterDuff.Mode.MULTIPLY);
        btnDashboard.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
        btnDashboard.setTextColor(Color.parseColor(target));

        img = ResourcesCompat.getDrawable(getResources(), R.drawable.icon_bottom_map, null);

        img.setColorFilter(Color.parseColor(revers), PorterDuff.Mode.MULTIPLY);
        btnMap.setCompoundDrawablesWithIntrinsicBounds(null, img, null, null);
        btnMap.setTextColor(Color.parseColor(revers));
    }
}
