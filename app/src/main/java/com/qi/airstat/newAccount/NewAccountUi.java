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
    static Button btnWelcomeGetStart;
    static Button btnNameNext;
    static Button btnEmailNext;
    static Button btnPasswordFinish;

    static String disabledButtonColor = "#C1C1C1";
    static String enabledButtonColor = "#009688";

    private NewAccountUi() { /* Prevent for calling as new a instance */
    }

    public static NewAccountUi getInstance() {
        return instance;
    }
}
