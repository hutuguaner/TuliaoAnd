package com.lzq.tuliaoand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.blankj.utilcode.util.ToastUtils;
import com.lzq.tuliaoand.activity.BaseActivity;
import com.lzq.tuliaoand.activity.MainActivity;
import com.lzq.tuliaoand.activity.RegistActivity;
import com.lzq.tuliaoand.activity.ResetPwdActivity;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etTel, etPwd;
    private Button btLogin;
    private TextView tvRegist, tvResetPwd;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etTel = findViewById(R.id.et_login_tel);
        etPwd = findViewById(R.id.et_login_pwd);
        btLogin = findViewById(R.id.bt_login_login);
        tvRegist = findViewById(R.id.tv_login_regist);
        tvResetPwd = findViewById(R.id.tv_login_resetpwd);

        //
        btLogin.setOnClickListener(this);
        tvRegist.setOnClickListener(this);
        tvResetPwd.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login_login:
                loginCheck();
                break;
            case R.id.tv_login_regist:
                startActivity(new Intent(this, RegistActivity.class));
                break;
            case R.id.tv_login_resetpwd:
                startActivity(new Intent(this, ResetPwdActivity.class));
                break;
        }
    }

    private void loginCheck() {
        String tel = etTel.getText().toString();
        String pwd = etPwd.getText().toString();
        if (tel == null || tel.trim().equals("")) {
            ToastUtils.showLong("type input tel please!");
            return;
        }
        if (pwd == null || pwd.trim().equals("")) {
            ToastUtils.showLong("type input pwd please!");
            return;
        }
        loginRequest();
    }

    private void loginRequest() {
        String tel, pwd;
        tel = etTel.getText().toString();
        pwd = etPwd.getText().toString();

        startActivity(new Intent(this, MainActivity.class));
    }
}
