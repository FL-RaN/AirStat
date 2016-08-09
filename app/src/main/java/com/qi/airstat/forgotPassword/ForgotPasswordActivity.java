package com.qi.airstat.forgotPassword;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.qi.airstat.ActivityClosingDialog;
import com.qi.airstat.Constants;
import com.qi.airstat.R;
import com.qi.airstat.iHttpConnection;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private iHttpConnection forgotPasswordCommunication;
    private ForgotPasswordUi forgotPasswordUi;

    private String email;

    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        forgotPasswordUi = new ForgotPasswordUi();

        setWidgets();
    }

    private void setWidgets() {
        forgotPasswordUi.edtEmail = (EditText) findViewById(R.id.edt_forgot_password_email);
        forgotPasswordUi.btnNext = (Button) findViewById(R.id.btn_forgot_password_next);

        setEvent();
    }

    private void setEvent() {
        forgotPasswordUi.edtEmail.addTextChangedListener(new ButtonStateChanger());

        forgotPasswordUi.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*
                Communication Part HERE
                 */
                forgotPasswordCommunication = new ForgotPasswordCommunication(context, forgotPasswordUi);
                String receivedData = forgotPasswordCommunication.executeHttpConn();

                resultHandler(receivedData);
            }
        });
    }

    private void resultHandler(String receivedData) {
        int responseCode = -1;
        try {
            JSONObject jObj = new JSONObject(receivedData);
            responseCode = jObj.getInt(Constants.HTTP_RESPONSE_RESULT);
        } catch (JSONException e) {
            e.printStackTrace();
            makeToast("Sorry, try again later...");
        } catch (Exception e) {
            e.printStackTrace();
        }

        switch (responseCode) {
            case Constants.HTTP_RESPONSE_RESULT_OK:
                new ActivityClosingDialog("Password Reset Email Sent", "Follow the directions in the email to reset your password", this).show(getSupportFragmentManager(), "");
                break;
            default:
        }
    }

    private class ButtonStateChanger implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            email = forgotPasswordUi.edtEmail.getText().toString().trim();
            int emailInputSize = forgotPasswordUi.edtEmail.getText().toString().trim().length();
            if (emailInputSize <= 0 || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                forgotPasswordUi.btnNext.setTextColor(Color.parseColor(forgotPasswordUi.disabledButtonColor));
                forgotPasswordUi.btnNext.setEnabled(false);
            } else {
                forgotPasswordUi.btnNext.setTextColor(Color.parseColor(forgotPasswordUi.enabledButtonColor));
                forgotPasswordUi.btnNext.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }
}
