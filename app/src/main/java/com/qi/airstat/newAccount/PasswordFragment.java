package com.qi.airstat.newAccount;

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

import com.qi.airstat.R;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class PasswordFragment extends Fragment {

    private NewAccountUi newAccountUi = NewAccountUi.getInstance();

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
                /*NOT YET :D*/
            }
        });
    }

    private class ButtonStateChanger implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int passwordInputSize = newAccountUi.edtPassword.getText().toString().trim().length();
            int comfirmInputSize = newAccountUi.edtConfirmPassword.getText().toString().trim().length();
            if (passwordInputSize <= 0 || comfirmInputSize <= 0 || comparePassword()) {
                newAccountUi.btnPasswordFinish.setEnabled(false);
            } else {
                newAccountUi.btnPasswordFinish.setEnabled(true);
            }
        }

        private boolean comparePassword() {
            return !newAccountUi.edtPassword.getText().equals(newAccountUi.edtConfirmPassword.getText());
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
