package com.qi.airstat.newAccount;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Created by JUMPSNACK on 8/2/2016.
 */
public class NewAccountDialog extends DialogFragment {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        TextView title = new TextView(getContext());
        title.setText("Congraturation!");
        title.setTextSize(20);
        title.setTextColor(Color.BLACK);
        title.setTypeface(null, Typeface.BOLD);
        title.setGravity(Gravity.CENTER);
        title.setPadding(10,10,10,10);

        builder.setCustomTitle(title)
                .setMessage("Your account is ready to go")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        NewAccountActivity.instance.finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}