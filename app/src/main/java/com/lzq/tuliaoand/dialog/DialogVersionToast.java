package com.lzq.tuliaoand.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.lzq.tuliaoand.R;


public class DialogVersionToast {

    private Context cxt;

    private AlertDialog alertDialog = null;


    public DialogVersionToast(Context cxt) {
        this.cxt = cxt;
    }

    public void show(String versionDesc, View.OnClickListener onClickListener) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
            alertDialog = builder.create();
            alertDialog.setCancelable(false);
        } else {
            if (alertDialog.isShowing()) {
                return;
            }
        }


        View view = LayoutInflater.from(cxt).inflate(R.layout.dialog_version_toast, null);

        TextView tvVersionDesc = view.findViewById(R.id.tv_dialog_version_toast_desc);
        TextView tvOk = view.findViewById(R.id.tv_dialog_version_toast_ok);

        tvVersionDesc.setText(versionDesc);
        tvOk.setOnClickListener(onClickListener);

        alertDialog.setView(view);
        alertDialog.show();

    }


    public void dismis() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = null;
    }


    public boolean isShowing() {
        if (alertDialog != null && alertDialog.isShowing()) {
            return true;
        }
        return false;
    }

}
