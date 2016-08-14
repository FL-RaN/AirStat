package com.qi.airstat.forgotPassword;

import android.content.Context;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;
import com.qi.airstat.iHttpConnection;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/2/2016.
 */
public class ForgotPasswordCommunication implements iHttpConnection {

    private HttpService httpService;

    private Context context;

    private String email;

    ArrayList<String> params;

    public ForgotPasswordCommunication(Context context, ForgotPasswordUi forgotPasswordUi) {
        this.context = context;
        this.email = forgotPasswordUi.edtEmail.getText().toString().trim();

        /*
        Compose a message
         */
        params = new ArrayList<>();
        params.add(Constants.HTTP_DATA_FORGOT_PASSWORD_EMAIL);
        params.add(email);
    }

    /*
    Communicates with server
     */
    @Override
    public String executeHttpConn() {
        httpService = new HttpService();
        return httpService.executeConn(context,"POST", Constants.HTTP_STR_URL_FORGOT_PASSWORD, params);
    }
}
