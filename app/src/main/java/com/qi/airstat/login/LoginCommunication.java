package com.qi.airstat.login;

import android.content.Context;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class LoginCommunication {
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

    /*
    Constructor
     */
    public LoginCommunication(Context context, LoginUi loginUi) {
        this.loginUi = loginUi;
        this.context = context;

        email = loginUi.edtEmail.getText().toString().trim();
        password = loginUi.edtPassword.getText().toString().trim();

        ArrayList<String> params = new ArrayList<>();
        params.add(Constants.HTTP_MSG_ID);
        params.add(Constants.HTTP_REQUEST_LOGIN);
        params.add(Constants.HTTP_DATA_LOGIN_EMAIL);
        params.add(email);
        params.add(Constants.HTTP_DATA_LOGIN_PASSWORD);
        params.add(password);

        httpService = new HttpService();
        httpService.excuteConn(context, params, null, null);
    }
}
