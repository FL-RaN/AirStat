package com.qi.airstat.forgotPassword;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.qi.airstat.R;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    public static Activity instance;
    public static FragmentManager fragmentManager;
    private ForgotPasswordCommunication forgotPasswordCommunication;
    private ForgotPasswordUi forgotPasswordUi;

    private String email;

    private Context context = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        instance = ForgotPasswordActivity.this;
        fragmentManager = getSupportFragmentManager();
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
            }
        });
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
}
