package com.qi.airstat.login;

import android.content.Context;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;
import com.qi.airstat.iHttpConnection;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class LoginCommunication implements iHttpConnection {
    /*
    Widget Instance
     */
    private LoginUi loginUi;

    private HttpService httpService;

    /*
    LoginBaseActivity context for Toast
     */
    private Context context;

    private String email;
    private String password;
    private String receivedData;

    ArrayList<String> params;

    /*
    Constructor
     */
    public LoginCommunication(Context context, LoginUi loginUi) {
        this.loginUi = loginUi;
        this.context = context;

        email = loginUi.edtEmail.getText().toString().trim();
        password = loginUi.edtPassword.getText().toString().trim();

        params = new ArrayList<>();
        params.add(Constants.HTTP_DATA_LOGIN_EMAIL);
        params.add(email);
        params.add(Constants.HTTP_DATA_LOGIN_PASSWORD);
        params.add(password);

    }

    public String executeHttpConn(){
        httpService = new HttpService();
        return receivedData = httpService.executeConn(context,"POST", Constants.HTTP_STR_URL_LOGIN, params);
    }
}
