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

public class RegistActivity extends BaseActivity implements View.OnClickListener {

    private EditText etTel, etCode, etPwd, etPwdAg;
    private ImageView ivBack;
    private TextView tvGetCode;
    private RelativeLayout rlOk;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        ivBack = findViewById(R.id.iv_regist_back);
        tvGetCode = findViewById(R.id.tv_regist_getcode);
        rlOk = findViewById(R.id.rl_regist_ok);
        etTel = findViewById(R.id.et_regist_tel);
        etCode = findViewById(R.id.et_regist_code);
        etPwd = findViewById(R.id.et_regist_pwd);
        etPwdAg = findViewById(R.id.et_regist_pwd_again);

        //
        ivBack.setOnClickListener(this);
        rlOk.setOnClickListener(this);
        tvGetCode.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_regist_back:

                break;
            case R.id.tv_regist_getcode:

                break;
            case R.id.rl_regist_ok:

                break;
        }
    }
}
