package com.lzq.tuliaoand;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lzq.tuliaoand.activity.BaseActivity;
import com.lzq.tuliaoand.activity.MainActivity;
import com.lzq.tuliaoand.activity.RegistActivity;
import com.lzq.tuliaoand.activity.ResetPwdActivity;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etEmail;
    private Button btLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initView();

    }

    private void initView() {
        etEmail = findViewById(R.id.et_login_email);
        String emailLogined = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        if (!StringUtils.isEmpty(emailLogined)) {
            etEmail.setText(emailLogined);
        }
        btLogin = findViewById(R.id.bt_login_login);
        //
        btLogin.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_login_login:
                loginCheck();
                break;
        }
    }

    private void loginCheck() {
        String tel = etEmail.getText().toString();
        if (tel == null || tel.trim().equals("")) {
            ToastUtils.showLong("type input tel please!");
            return;
        }
        loginRequest();
    }

    private void loginRequest() {
        String email, pwd;
        email = etEmail.getText().toString();
        if (StringUtils.isEmpty(email)) {
            ToastUtils.showLong("请输入邮箱");
            return;
        }
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkGo.<String>post(Const.LOGIN).upJson(params).execute(new StringCallback() {
            @Override
            public void onStart(Request<String, ? extends Request> request) {
                super.onStart(request);
                show();
            }

            @Override
            public void onError(Response<String> response) {
                super.onError(response);
                error(response.body());
            }

            @Override
            public void onSuccess(Response<String> response) {
                Log.i("lala", "登录 ： " + response.body());
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        String email = jsonObject.getString("data");
                        SPUtils.getInstance().put(SPKey.EMAIL_LOGINED.getUniqueName(), email);
                        LoginActivity.this.finish();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                App.myDatabase.messageDao().delete();
                            }
                        }).start();
                        //startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    } else {
                        String msg = jsonObject.getString("msg");
                        error(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    error(e.getMessage());
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dismiss();
            }
        });

    }
}
