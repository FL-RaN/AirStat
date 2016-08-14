package com.qi.airstat.newAccount;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.qi.airstat.ActivityClosingDialog;
import com.qi.airstat.Constants;
import com.qi.airstat.R;
import com.qi.airstat.iHttpConnection;

import org.json.JSONObject;

import java.util.regex.Matcher;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class PasswordFragment extends Fragment {

    private NewAccountUi newAccountUi = NewAccountUi.getInstance();

    private iHttpConnection newAccountCommunication;

    private String passwordInput;
    private String confirmPassword;

    public static PasswordFragment create() {
        PasswordFragment fragment = new PasswordFragment();
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_new_account_password, container, false);

        newAccountUi.edtPassword = (EditText) viewGroup.findViewById(R.id.edt_new_account_password);
        newAccountUi.edtConfirmPassword = (EditText) viewGroup.findViewById(R.id.edt_new_account_confirm_password);
        newAccountUi.btnPasswordFinish = (Button) viewGroup.findViewById(R.id.btn_fragment_password_finish);

        setEvent();

        return viewGroup;
    }

    private void setEvent() {
        ButtonStateChanger buttonStateChanger = new ButtonStateChanger();

        newAccountUi.edtPassword.addTextChangedListener(buttonStateChanger);
        newAccountUi.edtConfirmPassword.addTextChangedListener(buttonStateChanger);
        newAccountUi.btnPasswordFinish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!passwordInput.equals(confirmPassword)) {
                    makeToast("Please check your password");
                    return;
                }
                newAccountCommunication = new NewAccountCommunication(getContext(), newAccountUi);
                String receivedData = newAccountCommunication.executeHttpConn();

                resultHandler(receivedData);
            }
        });
    }

    private void resultHandler(String receivedData) {
        int responseCode = -1;
        try {
            JSONObject jObj = new JSONObject(receivedData);
            responseCode = jObj.getInt(Constants.HTTP_RESPONSE_RESULT);
        } catch (Exception e) {
            e.printStackTrace();
            makeToast("Sorry, try again later...");
        }

        /*
        Process next step follow as response code
         */
        switch (responseCode) {
            case Constants.HTTP_RESPONSE_RESULT_OK:
                new ActivityClosingDialog("Congraturation!", "Check your email\n\n" + newAccountUi.edtEmail.getText(), NewAccountActivity.instance).show(getFragmentManager(), "");
                break;

            case Constants.HTTP_RESPONSE_RESULT_CREATE_NEW_ACCOUNT_FAIL_DUP:
                new ActivityClosingDialog("Failed!", "You are already registered :(", NewAccountActivity.instance).show(getFragmentManager(), "");
                break;

            case Constants.HTTP_RESPONSE_RESULT_CREATE_NEW_ACCOUNT_FAIL_INCORRECT_FORMAT_PASSWORD:
                new ActivityClosingDialog("Failed!", "Incorrect password format", null).show(getFragmentManager(), "");
                break;

            case Constants.HTTP_RESPONSE_RESULT_CREATE_NEW_ACCOUNT_FAIL_MISMATCH_PASSWORD:
                new ActivityClosingDialog("Failed!", "Password mismatched", null).show(getFragmentManager(), "");
                break;
        }
    }

    private void makeToast(String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_LONG).show();
    }

    private class ButtonStateChanger implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            passwordInput = newAccountUi.edtPassword.getText().toString().trim();
            confirmPassword = newAccountUi.edtConfirmPassword.getText().toString().trim();

            int passwordInputSize = passwordInput.length();
            int comfirmInputSize = confirmPassword.length();

            if (passwordInputSize <= 0 || comfirmInputSize <= 0 || !pwdFormatChecker(passwordInput) || !pwdFormatChecker(confirmPassword)) {
                newAccountUi.btnPasswordFinish.setTextColor(Color.parseColor(newAccountUi.disabledButtonColor));
                newAccountUi.btnPasswordFinish.setEnabled(false);
            } else {
                newAccountUi.btnPasswordFinish.setTextColor(Color.parseColor(newAccountUi.enabledButtonColor));
                newAccountUi.btnPasswordFinish.setEnabled(true);
            }
        }

        private boolean pwdFormatChecker(String password) {
            Matcher matcher = Constants.VALID_PASSWOLD_REGEX_ALPHA_NUM.matcher(password);
            return matcher.matches();
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
