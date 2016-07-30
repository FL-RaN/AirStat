package com.qi.airstat.login;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qi.airstat.R;

/**
 * Created by JUMPSNACK on 7/29/2016.
 */
public class LoginAdapter {
    private View view;
    private LoginUi loginUi;

    public LoginAdapter(View view){
        this.view = view;
        this.loginUi = new LoginUi();

        setWidgets(this.loginUi, this.view);
    }

    private void setWidgets(LoginUi loginUi, View view){
        loginUi.edtEmail = (EditText) view.findViewById(R.id.edt_login_mail);
        loginUi.edtPassword = (EditText) view.findViewById(R.id.edt_login_pwd);
        loginUi.btnLogin = (Button) view.findViewById(R.id.btn_login_login);
        loginUi.btnForgotPwd = (Button) view.findViewById(R.id.btn_login_forgot_pwd);
        loginUi.btnCreateAccnt = (Button) view.findViewById(R.id.btn_login_create_account);
    }
}
