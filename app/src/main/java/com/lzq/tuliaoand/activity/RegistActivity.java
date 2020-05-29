package com.lzq.tuliaoand.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.blankj.utilcode.util.RegexUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.common.Const;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;

import org.json.JSONException;
import org.json.JSONObject;

public class RegistActivity extends BaseActivity implements View.OnClickListener {

    private EditText etEmail, etCode, etPwd, etPwdAg;
    private ImageView ivBack;
    private TextView tvGetCode;
    private RelativeLayout rlOk;
    //避免频繁获取验证码，做个延时控制
    private final long delayVerifyCode = 1000 * 60;
    private boolean getVerifyCodeBeAllowed = true;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist);

        initView();

    }

    private void initView() {
        ivBack = findViewById(R.id.iv_regist_back);
        tvGetCode = findViewById(R.id.tv_regist_getcode);
        rlOk = findViewById(R.id.rl_regist_ok);
        etEmail = findViewById(R.id.et_regist_email);
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
                this.finish();
                break;
            case R.id.tv_regist_getcode:
                getVerifyCode();
                break;
            case R.id.rl_regist_ok:
                regist();
                break;
        }
    }


    private void regist() {
        String pwd = etPwd.getText().toString();
        if (StringUtils.isEmpty(pwd)) {
            ToastUtils.showLong("请输入密码");
            return;
        }

        String pwdAg = etPwdAg.getText().toString();
        if (StringUtils.isEmpty(pwdAg)) {
            ToastUtils.showLong("请再次输入密码");
            return;
        }
        if (!pwd.equals(pwdAg)) {
            ToastUtils.showLong("两次输入密码不一致，请确认");
            return;
        } else {
            if (pwd.length() < 6) {
                ToastUtils.showLong("密码过短");
                return;
            }
            registDo(etEmail.getText().toString(), etCode.getText().toString(), pwd);
        }
    }

    private void registDo(String email, String verifyCode, String pwd) {
        JSONObject params = new JSONObject();
        try {
            params.put("email", email);
            params.put("verifyCode", verifyCode);
            params.put("password", pwd);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        OkGo.<String>post(Const.REGIST).upJson(params).execute(new StringCallback() {
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
                Log.i("lala", "注册 ： " + response.body());
                try {
                    JSONObject jsonObject = new JSONObject(response.body());
                    int code = jsonObject.getInt("code");
                    if (code == 0) {
                        ToastUtils.showLong("注册成功");
                        RegistActivity.this.finish();
                    } else {
                        String msg = jsonObject.getString("msg");
                        error(msg);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dismiss();
            }
        });
    }

    private void getVerifyCode() {
        if (!getVerifyCodeBeAllowed) {
            ToastUtils.showLong("请勿频繁获取验证码，间隔一分钟，请稍候");
            return;
        }
        String email = etEmail.getText().toString();
        if (StringUtils.isEmpty(email)) {
            ToastUtils.showLong("请输入邮箱");
            return;
        }
        boolean isEmail = RegexUtils.isEmail(email);
        if (!isEmail) {
            ToastUtils.showLong("邮箱格式不正确");
            return;
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", email);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>post(Const.GET_VERIFYCODE_EMAIL).upJson(jsonObject).execute(new StringCallback() {
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
                Log.i("lala", "获取验证码 ： " + response.body());
                try {
                    JSONObject res = new JSONObject(response.body());
                    int code = res.getInt("code");
                    Log.i("lala", "code : " + code);
                    if (code == 0) {
                        resetGetVerifyCodeState();
                        normal("验证码获取成功，请到您邮箱查看");
                    } else {
                        String msg = res.getString("msg");
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

    private Runnable resetGetVerifyCodeStateRunnable = new Runnable() {
        @Override
        public void run() {
            getVerifyCodeBeAllowed = true;
        }
    };

    private void resetGetVerifyCodeState() {
        getVerifyCodeBeAllowed = false;
        handler.postDelayed(resetGetVerifyCodeStateRunnable, delayVerifyCode);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(resetGetVerifyCodeStateRunnable);
    }
}
