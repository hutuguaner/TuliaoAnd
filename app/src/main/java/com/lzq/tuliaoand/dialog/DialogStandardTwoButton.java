package com.lzq.tuliaoand.dialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.lzq.tuliaoand.R;


public class DialogStandardTwoButton {

    private Context cxt;

    private AlertDialog alertDialog = null;


    public DialogStandardTwoButton(Context cxt) {
        this.cxt = cxt;
    }

    public void show(String content, String leftButtonText, String rightButtonText, View.OnClickListener leftButtonOnClickLis, View.OnClickListener rightButtonOnClickLis) {
        if (alertDialog == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(cxt);
            alertDialog = builder.create();
        } else {
            if (alertDialog.isShowing()) {
                return;
            }
        }


        View view = LayoutInflater.from(cxt).inflate(R.layout.dialog_standard_twobutton, null);

        TextView tvContent = view.findViewById(R.id.tv_dialog_standard_twobutton_content);
        TextView tvLeftButton = view.findViewById(R.id.tv_dialog_standard_twobutton_leftbutton);
        TextView tvRightButton = view.findViewById(R.id.tv_dialog_standard_twobutton_rightbutton);

        tvContent.setText(content);
        tvLeftButton.setText(leftButtonText);
        tvRightButton.setText(rightButtonText);

        tvLeftButton.setOnClickListener(leftButtonOnClickLis);
        tvRightButton.setOnClickListener(rightButtonOnClickLis);

        alertDialog.setView(view);
        alertDialog.show();

    }


    public void dismis() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
        alertDialog = null;
    }


}
