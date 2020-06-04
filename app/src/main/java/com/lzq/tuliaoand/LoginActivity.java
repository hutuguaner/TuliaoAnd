package com.lzq.tuliaoand;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import android.widget.Toast;

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

import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class LoginActivity extends BaseActivity implements View.OnClickListener {

    private EditText etEmail;
    private Button btLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initPermission();
        initView();

    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        if (Build.VERSION.SDK_INT < 23) return;
        String permissions[] = {
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.CAMERA
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(this, perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.

            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(this, toApplyList.toArray(tmpList), 123);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != 0) {
                //说明 权限申请 被拒绝
                Toast.makeText(LoginActivity.this, "请全部接受权限申请，否则将无法使用", Toast.LENGTH_LONG).show();
                LoginActivity.this.finish();
                return;
            }
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        //拿到权限后 才能设置的一些配置项 放这里
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
