package com.lzq.tuliaoand.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.widget.ContentLoadingProgressBar;

import com.lzq.tuliaoand.R;


public class DialogVersionUpdating {

    private Context cxt;

    private AlertDialog alertDialog = null;
    ContentLoadingProgressBar contentLoadingProgressBar;

    public DialogVersionUpdating(Context cxt) {
        this.cxt = cxt;
    }

    public void show() {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
        } else {
            if (alertDialog.isShowing()) {
                return;
            }
        }


        View view = LayoutInflater.from(cxt).inflate(R.layout.dialog_version_updating, null);

        contentLoadingProgressBar = view.findViewById(R.id.clpb_dialog_version_updating);

        alertDialog.setView(view);
        alertDialog.show();

    }

    public void setProgress(int progress) {
        contentLoadingProgressBar.setProgress(progress);
    }

    public void setProgress(int progress, boolean isAnimate) {
        contentLoadingProgressBar.setProgress(progress, isAnimate);
    }


    public void dismis() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = null;
    }

    public boolean isShowing() {
        if (alertDialog != null && alertDialog.isShowing())
            return true;
        return false;
    }


}
