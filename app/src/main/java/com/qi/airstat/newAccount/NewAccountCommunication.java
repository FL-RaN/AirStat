package com.qi.airstat.newAccount;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/3/2016.
 */
public class NewAccountCommunication {
    private static DialogFragment dialogFragment;
    private HttpService httpService;

    private Context context;
    private NewAccountUi newAccountUi;

    private String firstName;
    private String lastName;
    private String email;
    private String password;

    public NewAccountCommunication(Context context, NewAccountUi newAccountUi, FragmentManager fragmentManager){
        this.context = context;
        this.newAccountUi = newAccountUi;

        firstName = newAccountUi.edtFirstName.getText().toString().trim();
        lastName = newAccountUi.edtLastName.getText().toString().trim();
        email = newAccountUi.edtEmail.getText().toString().trim();
        password = newAccountUi.edtPassword.getText().toString().trim();

        ArrayList<String> params = new ArrayList<>();
        params.add(Constants.HTTP_MSG_ID);
        params.add(Constants.HTTP_REQUEST_CREATE_NEW_ACCOUNT);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_FIRST_NAME);
        params.add(firstName);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_LAST_NAME);
        params.add(lastName);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_EMAIL);
        params.add(email);
        params.add(Constants.HTTP_DATA_CREATE_NEW_ACCOUNT_PASSWORD);
        params.add(password);

        httpService = new HttpService();
        httpService.excuteConn(context, params, new NewAccountDialog(), fragmentManager);
    }

}
