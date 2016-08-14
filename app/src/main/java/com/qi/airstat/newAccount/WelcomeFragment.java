package com.qi.airstat.newAccount;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.qi.airstat.R;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
/*
Create new account welcome page
 */
public class WelcomeFragment extends Fragment {

    private NewAccountUi newAccountUi = NewAccountUi.getInstance();

    public static WelcomeFragment create() {
        WelcomeFragment fragment = new WelcomeFragment();

        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.fragment_new_account_welcome, container, false);

        newAccountUi.btnWelcomeGetStart = (Button) viewGroup.findViewById(R.id.btn_fragment_welcome_get_start);
        newAccountUi.btnWelcomeGetStart.setTextColor(Color.parseColor(newAccountUi.enabledButtonColor));
        setEvent();

        return viewGroup;
    }

    public void setEvent() {
        newAccountUi.btnWelcomeGetStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ((NewAccountActivity) getActivity()).setCurrentPagerItem(1);
            }
        });
    }
}
