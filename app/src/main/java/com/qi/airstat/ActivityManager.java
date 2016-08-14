package com.qi.airstat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.qi.airstat.dataMap.DataMapActivity;
import com.qi.airstat.login.LoginBaseActivity;

/**
 * Created by JUMPSNACK on 8/13/2016.
 */
public class ActivityManager {
    public static ActivityManager instance;

    Context context;
    SharedPreferences pref;
    private static final String PREF_NAME = "AirStatActivityPref";
    private static final String IS_LOGIN = "IsLoggedIn";
    SharedPreferences.Editor editor;
    int PRIVATE_MODE = 0;

    public ActivityManager(Context context) {
        this.context = context;
        pref = context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
        instance = this;
    }


    public void createLoginState(){
        editor.putBoolean(IS_LOGIN, true);
        editor.commit();
    }

    public void checkLogin() {
        if (!this.isLoggedIn()) {
            createLoginState();
            Intent intent = new Intent(context, LoginBaseActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
//        else{
//            context.startActivity(new Intent(context, SensorDataOverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
//        }
        else if(CustomView.isPushedDashboard){
            context.startActivity(new Intent(context, SensorDataOverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        } else{
            context.startActivity(new Intent(context, DataMapActivity.class).addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }
    }

    public void logoutUser() {
        editor.clear();
        editor.commit();

//        Intent intent = new Intent(context, LoginBaseActivity.class);
//        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        intent.setFlags(
//                Intent.FLAG_ACTIVITY_CLEAR_TOP |
//                Intent.FLAG_ACTIVITY_CLEAR_TASK |
//                Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent);
    }

    public boolean isLoggedIn() {
        return pref.getBoolean(IS_LOGIN, false);
    }
}
