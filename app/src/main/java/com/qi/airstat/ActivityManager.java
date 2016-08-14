package com.qi.airstat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.qi.airstat.dataMap.DataMapActivity;
import com.qi.airstat.login.LoginBaseActivity;

/**
 * Created by JUMPSNACK on 8/13/2016.
 */
/*
Login maintain data and activity managing
 */
public class ActivityManager {
    public static ActivityManager instance;

    private static final String PREF_NAME = "AirStatActivityPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    private static final String IS_VEFIED_UID = "UID";

    private Context context;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;

    private int PRIVATE_MODE = 0;

    public ActivityManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        instance = this;
    }

    /*
    Create login state data to local file
     */
    public void createLoginState() {
        editor.putBoolean(IS_LOGIN, true);
        editor.commit();
    }

    /*
    Set UID to local file
     */
    public void setUID(int uid){
        editor.putInt(IS_VEFIED_UID, uid);
        editor.commit();
    }

    /*
    Get UID from local file
     */
    public int getUID(){
        return pref.getInt(IS_VEFIED_UID, -1);
    }

    /*
    Call when user access this app
     */
    public void checkLogin() {
        if (!this.isLoggedIn()) {   // If didn't logged in
            createLoginState();
            Intent intent = new Intent(context, LoginBaseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } else if (CustomView.isPushedDashboard) {  // Check before state data of dashboard activity
            context.startActivity(new Intent(context, SensorDataOverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } else {  // Check before state data of map activity
            context.startActivity(new Intent(context, DataMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
    }

    /*
    Modify login data to logged out
     */
    public void logoutUser() {
        editor.clear();
        editor.commit();
    }

    /*
    Check login state
     */
    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
