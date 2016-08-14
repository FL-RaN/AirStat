package com.qi.airstat.login;

import android.app.Activity;
import android.content.Intent;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.qi.airstat.ActivityClosingDialog;
import com.qi.airstat.ActivityManager;
import com.qi.airstat.Constants;
import com.qi.airstat.R;
import com.qi.airstat.SensorDataOverviewActivity;
import com.qi.airstat.forgotPassword.ForgotPasswordActivity;
import com.qi.airstat.iHttpConnection;
import com.qi.airstat.newAccount.NewAccountActivity;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JUMPSNACK on 7/29/2016.
 */
public class LoginAdapter {
    private View view;
    private Activity activity;
    private LoginUi loginUi;
    private iHttpConnection loginCommunication;

    public LoginAdapter(View view, Activity activity) {
        this.view = view;
        this.activity = activity;
        this.loginUi = new LoginUi();

        setWidgets(this.loginUi, this.view);
        setEvent(this.loginUi);
    }

    private void setWidgets(LoginUi loginUi, View view) {
        loginUi.edtEmail = (EditText) view.findViewById(R.id.edt_login_mail);
        loginUi.edtPassword = (EditText) view.findViewById(R.id.edt_login_pwd);
        loginUi.btnLogin = (Button) view.findViewById(R.id.btn_login_login);
        loginUi.btnForgotPwd = (Button) view.findViewById(R.id.btn_login_forgot_pwd);
        loginUi.btnCreateAccnt = (Button) view.findViewById(R.id.btn_login_create_account);
    }

    private void setEvent(final LoginUi loginUi) {
        /*
         Login button event handler
         */
        loginUi.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                view.setEnabled(false);
                if (checkFormat(loginUi)) {
                    loginCommunication = new LoginCommunication(view.getContext(), loginUi);
                    String receivedData = loginCommunication.executeHttpConn();

                    resultHandler(receivedData);
                }
            }
        });

        /*
        Create account button event handler
         */
        loginUi.btnCreateAccnt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity.getApplicationContext(), NewAccountActivity.class));
            }
        });

        /*
        Forgot password button event handler
         */
        loginUi.btnForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.startActivity(new Intent(activity.getApplicationContext(), ForgotPasswordActivity.class));
            }
        });
    }

    /*
    Analyze received data
     */
    private void resultHandler(String receivedData) {
        int responseCode = -1;
        JSONObject jObj = null;

        try {
            jObj = new JSONObject(receivedData);
            responseCode = jObj.getInt(Constants.HTTP_RESPONSE_RESULT); // Get response code
        } catch (JSONException e) {
            e.printStackTrace();
            makeToast("Sorry, try again later...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        /*
        Process next step follow as response status code
         */
        switch (responseCode) {
            case Constants.HTTP_RESPONSE_RESULT_OK: // status = 0
                makeToast("Welcome!");

                try {
                    Constants.UID = jObj.getInt(Constants.HTTP_DATA_LOGIN_UID); // Set UID from received data
                    ActivityManager.instance.setUID(Constants.UID);
                } catch (JSONException e) {
                    e.printStackTrace();
                    makeToast("Sorry, try again later...");
                    return ;
                }

                // SensorDataOverviewActivity start
                activity.startActivity(new Intent(activity.getApplicationContext(), SensorDataOverviewActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
                activity.finish();
                break;

            case Constants.HTTP_RESPONSE_RESULT_LOGIN_FAIL: // status = 1
                new ActivityClosingDialog("Failed!", "Please check your email or password", null).show(LoginBaseActivity.fragmentManager, "");
                break;

            case Constants.HTTP_RESPONSE_RESULT_LOGIN_FAIL_ACTIVATION:  // status = 2
                new ActivityClosingDialog("Failed!", "We need your activation." + "\nPlease check your email", null).show(LoginBaseActivity.fragmentManager, "");
                break;

            default:

        }
    }

    /*
    Check inserted user information
     */
    private boolean checkFormat(LoginUi loginUi) {
        String email = loginUi.edtEmail.getText().toString().trim();
        String password = loginUi.edtPassword.getText().toString().trim();

        if (email.getBytes().length <= 0) {
            makeToast("Please enter an email address");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            makeToast("Please enter a valid email address");
            return false;
        } else if (password.getBytes().length <= 0) {
            makeToast("Please enter a password");
            return false;
        }
        return true;
    }

    private void makeToast(String msg) {
        Toast.makeText(view.getContext(), msg, Toast.LENGTH_LONG).show();
    }
}
