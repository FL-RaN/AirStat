package com.qi.airstat.dataMap;

import android.content.Context;
import android.support.v4.app.DialogFragment;

import com.qi.airstat.HttpService;
import com.qi.airstat.forgotPassword.ForgotPasswordUi;
import com.qi.airstat.iHttpConnection;

/**
 * Created by JUMPSNACK on 8/5/2016.
 */
public class DataMapCommunication implements iHttpConnection {
    private static DialogFragment dialogFragment;
    private HttpService httpService;

    private Context context;
    private ForgotPasswordUi forgotPasswordUi;

    private String email;

    public DataMapCommunication(){

    }

    @Override
    public String executeHttpConn() {
        return null;
    }
}
