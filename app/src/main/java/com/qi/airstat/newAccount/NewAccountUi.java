package com.qi.airstat.newAccount;

import android.widget.Button;
import android.widget.EditText;

/**
 * Created by JUMPSNACK on 8/1/2016.
 */
public class NewAccountUi {
    private static NewAccountUi instance = new NewAccountUi();

    static EditText edtFirstName;
    static EditText edtLastName;
    static EditText edtEmail;
    static EditText edtPassword;
    static EditText edtConfirmPassword;
    static Button btnNameNext;
    static Button btnEmailNext;
    static Button btnPasswordFinish;

    private NewAccountUi() {
    }

    public static NewAccountUi getInstance() {
        return instance;
    }
}
