package com.qi.airstat.newAccount;

import android.content.Context;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;
import com.qi.airstat.iHttpConnection;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class NewAccountCommunication implements iHttpConnection {
    private HttpService httpService;

    private Context context;
    private NewAccountUi newAccountUi;

    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private String cpassword;

    ArrayList<String> params;

    public NewAccountCommunication(Context context, NewAccountUi newAccountUi) {
        this.context = context;
        this.newAccountUi = newAccountUi;

        firstName = newAccountUi.edtFirstName.getText().toString().trim();
        lastName = newAccountUi.edtLastName.getText().toString().trim();
        email = newAccountUi.edtEmail.getText().toString().trim();
        password = newAccountUi.edtPassword.getText().toString().trim();
        cpassword = newAccountUi.edtConfirmPassword.getText().toString().trim();


        /*
        Compose a message for send to server
         */
        params = new ArrayList<>();
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_FIRST_NAME);
        params.add(firstName);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_LAST_NAME);
        params.add(lastName);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_EMAIL);
        params.add(email);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_PASSWORD);
        params.add(password);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_CONFIRM_PASSWORD);
        params.add(cpassword);

    }

    @Override
    public String executeHttpConn() {
        httpService = new HttpService();    // Communucates with server
        return httpService.executeConn(context,"POST", Constants.HTTP_STR_URL_CREATE_NEW_ACCOUNT, params);
    }
}
