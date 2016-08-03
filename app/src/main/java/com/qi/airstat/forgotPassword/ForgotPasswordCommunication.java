package com.qi.airstat.forgotPassword;

import android.content.Context;
import android.support.v4.app.DialogFragment;

import com.qi.airstat.Constants;
import com.qi.airstat.HttpService;

import java.util.ArrayList;

/**
 * Created by JUMPSNACK on 8/2/2016.
 */
public class ForgotPasswordCommunication {

    private static DialogFragment dialogFragment;
    private HttpService httpService;

    private Context context;
    private ForgotPasswordUi forgotPasswordUi;

    private String email;

    public ForgotPasswordCommunication(Context context, ForgotPasswordUi forgotPasswordUi) {
        this.context = context;
        this.forgotPasswordUi = forgotPasswordUi;
        this.dialogFragment = new ForgotPasswordDialog();
        this.email = forgotPasswordUi.edtEmail.getText().toString().trim();

        ArrayList<String> params = new ArrayList<>();
        params.add(Constants.HTTP_MSG_ID);
        params.add(Constants.HTTP_REQUEST_FORGOT_PASSWORD);
        params.add(Constants.HTTP_DATA_FORGOT_PASSWORD_EMAIL);
        params.add(email);

        httpService = new HttpService();
        httpService.excuteConn(context, params, dialogFragment, ForgotPasswordActivity.fragmentManager);
    }
}
