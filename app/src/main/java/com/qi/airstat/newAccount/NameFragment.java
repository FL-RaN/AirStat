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
public class NameFragment extends Fragment {

    private NewAccountUi newAccountUi = NewAccountUi.getInstance();

    public static NameFragment create() {
        NameFragment fragment = new NameFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_new_account_name, container, false);

        newAccountUi.edtFirstName = (EditText) viewGroup.findViewById(R.id.edt_new_account_first_name);
        newAccountUi.edtLastName = (EditText) viewGroup.findViewById(R.id.edt_new_account_last_name);
        newAccountUi.btnNameNext = (Button) viewGroup.findViewById(R.id.btn_fragment_name_next);

        setEvent();

        return viewGroup;
    }

    public void setEvent() {
        ButtonStateChanger buttonStateChanger = new ButtonStateChanger();

        newAccountUi.edtFirstName.addTextChangedListener(buttonStateChanger);
        newAccountUi.edtLastName.addTextChangedListener(buttonStateChanger);
        newAccountUi.btnNameNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NewAccountActivity) getActivity()).setCurrentPagerItem(1);
            }
        });
    }

    private class ButtonStateChanger implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            int firstNameInputSize = newAccountUi.edtFirstName.getText().toString().trim().length();
            int lastNameInputSize = newAccountUi.edtLastName.getText().toString().trim().length();
            if (firstNameInputSize <= 0 || lastNameInputSize <= 0) {
                newAccountUi.btnNameNext.setEnabled(false);
            } else {
                newAccountUi.btnNameNext.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable editable) {
        }
    }
}
