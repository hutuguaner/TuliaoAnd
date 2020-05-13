package com.lzq.tuliaoand.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.blankj.utilcode.util.ActivityUtils;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.dialog.DialogStandardTwoButton;

import java.util.PriorityQueue;
import java.util.Queue;

import cn.pedant.SweetAlert.SweetAlertDialog;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
            } else {
                //hideSystemUI();
            }
        }

    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);
    }

    // Shows the system bars by removing all the flags
    // except for the ones that make the content appear under the system bars.
    private void showSystemUI() {
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    //获取状态栏高度
    protected int getStatusBarHeight(){
        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        return rectangle.top;
    }

    private SweetAlertDialog progressDialog = null;

    private Queue<Object> queue = new PriorityQueue<>();

    public void show() {
        if (progressDialog == null) {
            progressDialog = new SweetAlertDialog(this, SweetAlertDialog.PROGRESS_TYPE);
            progressDialog.getProgressHelper().setBarColor(Color.parseColor("#2d75e0"));
            progressDialog.setTitleText("加载中...");
            progressDialog.setCancelable(false);
        }
        if (progressDialog.isShowing()) {
            queue.add(0);
        } else {
            progressDialog.show();
        }
    }

    public void dismiss() {
        if (!queue.isEmpty()) {
            queue.poll();
        } else {
            if (progressDialog != null && progressDialog.isShowing()) {
                progressDialog.dismiss();
            }
        }
    }

    public void warn(String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.WARNING_TYPE)
                .setTitleText("警告")
                .setContentText(msg)
                .show();
    }


    public void error(String msg) {
        if (!ActivityUtils.isActivityAlive(this)) return;
        new SweetAlertDialog(this, SweetAlertDialog.ERROR_TYPE)
                .setTitleText("错误")
                .setContentText(msg)
                .show();
    }

    public void normal(String msg, boolean cancelable) {

        SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("提示")
                .setContentText(msg);
        sweetAlertDialog.setCancelable(cancelable);
        sweetAlertDialog.show();
    }

    public void normal(String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE)
                .setTitleText("提示")
                .setContentText(msg)
                .show();
    }

    public void success(String msg) {
        new SweetAlertDialog(this, SweetAlertDialog.SUCCESS_TYPE).setTitleText("成功").setContentText(msg).show();
    }

    public void yesOrNo(String content, String noText, String yesText, SweetAlertDialog.OnSweetClickListener noClickLis, SweetAlertDialog.OnSweetClickListener yesClickLis) {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE).setTitleText("提示").setContentText(content).setCancelText(noText).setCancelClickListener(noClickLis).setConfirmText(yesText).setConfirmClickListener(yesClickLis).show();
    }

    public void yesOrNo(String title, String content, String noText, String yesText, SweetAlertDialog.OnSweetClickListener noClickLis, SweetAlertDialog.OnSweetClickListener yesClickLis) {
        new SweetAlertDialog(this, SweetAlertDialog.CUSTOM_IMAGE_TYPE).setTitleText(title).setContentText(content).setCancelText(noText).setCancelClickListener(noClickLis).setConfirmText(yesText).setConfirmClickListener(yesClickLis).show();
    }

    //国测星绘 样式的 弹框
    private DialogStandardTwoButton dialogStandardTwoButton = null;

    public void yesOrNo2(String content, String leftText, String rightText, View.OnClickListener leftLis, View.OnClickListener rightLis) {
        if (dialogStandardTwoButton == null) {
            dialogStandardTwoButton = new DialogStandardTwoButton(this);
        }
        dialogStandardTwoButton.show(content, leftText, rightText, leftLis, rightLis);
    }

    public void yesOrNo2Dismiss() {
        if (dialogStandardTwoButton != null) {
            dialogStandardTwoButton.dismis();
        }
    }
}
