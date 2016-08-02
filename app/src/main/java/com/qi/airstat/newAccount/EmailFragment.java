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
public class EmailFragment extends Fragment {

    private NewAccountUi newAccountUi = NewAccountUi.getInstance();

    public static EmailFragment create() {
        EmailFragment fragment = new EmailFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_new_account_email, container, false);

        newAccountUi.edtEmail = (EditText) viewGroup.findViewById(R.id.edt_new_account_email);
        newAccountUi.btnEmailNext = (Button) viewGroup.findViewById(R.id.btn_fragment_email_next);

        setEvent();

        return viewGroup;
    }

    private void setEvent() {
        newAccountUi.edtEmail.addTextChangedListener(new ButtonStateChanger());
        newAccountUi.btnEmailNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NewAccountActivity) getActivity()).setCurrentPagerItem(2);
            }
        });
    }

    private class ButtonStateChanger implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int emailInputSize = newAccountUi.edtEmail.getText().toString().trim().length();
            if (emailInputSize <= 0) {
                newAccountUi.btnEmailNext.setEnabled(false);
            } else {
                newAccountUi.btnEmailNext.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
