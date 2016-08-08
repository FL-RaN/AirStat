package com.qi.airstat;

import android.app.Activity;
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
public class ActivityClosingDialog extends DialogFragment {

    private String title;
    private String content;
    private Activity activity;

    public ActivityClosingDialog() {
    }

    public ActivityClosingDialog(String title, String content, Activity activity) {
        this.title = title;
        this.content = content;
        this.activity = activity;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        TextView tvTitle = new TextView(getContext());
        tvTitle.setText(title);
        tvTitle.setTextSize(20);
        tvTitle.setTextColor(Color.BLACK);
        tvTitle.setTypeface(null, Typeface.BOLD);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setPadding(10, 10, 10, 10);

        builder.setCustomTitle(tvTitle)
                .setMessage(content)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (activity != null) activity.finish();
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}