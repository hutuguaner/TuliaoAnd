package com.lzq.tuliaoand.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lzq.tuliaoand.R;

public class ResetPwdActivity extends BaseActivity implements View.OnClickListener {
    private EditText etTel, etCode, etPwd, etPwdAg;
    private ImageView ivBack;
    private TextView tvGetCode;
    private RelativeLayout rlOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_pwd);

        ivBack = findViewById(R.id.iv_resetpwd_back);
        tvGetCode = findViewById(R.id.tv_resetpwd_getcode);
        rlOk = findViewById(R.id.rl_resetpwd_ok);
        etTel = findViewById(R.id.et_resetpwd_tel);
        etCode = findViewById(R.id.et_resetpwd_code);
        etPwd = findViewById(R.id.et_resetpwd_pwd);
        etPwdAg = findViewById(R.id.et_resetpwd_pwd_again);

        ivBack.setOnClickListener(this);
        tvGetCode.setOnClickListener(this);
        rlOk.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_resetpwd_back:
                this.finish();
                break;
            case R.id.tv_resetpwd_getcode:

                break;
            case R.id.rl_resetpwd_ok:

                break;
        }
    }
}
