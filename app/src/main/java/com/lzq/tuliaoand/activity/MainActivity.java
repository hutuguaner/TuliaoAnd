package com.lzq.tuliaoand.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.AsyncSocket;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.CompletedCallback;
import com.koushikdutta.async.callback.ConnectCallback;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.callback.WritableCallback;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;

import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.w3c.dom.Text;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements View.OnClickListener, View.OnFocusChangeListener {

    private MapView mapView;
    private MapController mapController;
    private ImageView ivLocate, ivSend, ivMessage;
    private EditText etBroadcast;

    private AsyncSocket asyncSocket = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initPermission();
        setContentView(R.layout.activity_main);
        initView();
        initMap();
        initFriendlyToast();

        connectToServer();

    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        locateCurrPosition();
        if (asyncSocket != null) {
            asyncSocket.resume();
        } else {
            connectToServer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        if (asyncSocket != null) {
            asyncSocket.pause();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (asyncSocket != null) asyncSocket.close();
    }

    private void initView() {
        mapView = findViewById(R.id.mv_main);
        ivLocate = findViewById(R.id.iv_main_locate);
        ivLocate.setOnClickListener(this);
        ivSend = findViewById(R.id.iv_main_paperairplane);
        ivSend.setOnClickListener(this);
        ivMessage = findViewById(R.id.iv_main_twobubble);
        ivMessage.setOnClickListener(this);
        etBroadcast = findViewById(R.id.et_main_broad);
        etBroadcast.setOnFocusChangeListener(this);
    }

    private void initMap() {
        Context context = getApplicationContext();
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context));
        mapView.setMinZoomLevel(5d);
        mapView.setMaxZoomLevel(21d);
        mapView.setBuiltInZoomControls(false);
        mapView.setMultiTouchControls(true);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        //mapView.setTileSource(CommonLayers.tianDiTuVecTileSource);
        mapController = new MapController(mapView);
        //
        mapController.setZoom(5);
        mapController.setCenter(new GeoPoint(39.915d, 116.404d));

    }

    private void initFriendlyToast() {
        boolean isJustInstall = SPUtils.getInstance().getBoolean(SPKey.IS_JUST_INSTALL.getUniqueName(), true);
        if (isJustInstall) {
            SPUtils.getInstance().put(SPKey.IS_JUST_INSTALL.getUniqueName(), false);
            normal("刚刚安装，地图加载较慢，随着使用，地图加载会越来越快", true);
        } else {

        }
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------------

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
//-------------------------------------------------------------------------------------------------------------------------------------------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        //super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            if (grantResults[i] != 0) {
                //说明 权限申请 被拒绝
                Toast.makeText(MainActivity.this, "请全部接受权限申请，否则将无法使用", Toast.LENGTH_LONG).show();
                MainActivity.this.finish();
                return;
            }
        }
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
        //拿到权限后 才能设置的一些配置项 放这里

    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------
    //定位到当前位置
    private void locateCurrPosition() {
        show();
        Location location = LocationUtils.getLastKnownLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        if (location == null) {
            dismiss();
            ToastUtils.showLong("定位失败");
            return;
        } else {
            mapZoomTo(location.getLongitude(), location.getLatitude());
            showLocateMarker(location.getLongitude(), location.getLatitude());
            dismiss();
        }
    }

    private void mapZoomTo(double lon, double lat) {
        mapLocateTo(lon, lat);
        mapController.setZoom(18);
    }

    private void mapLocateTo(double lon, double lat) {
        //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
        GeoPoint point = new GeoPoint(lat, lon);
        //设置地图中心点
        mapController.animateTo(point);
    }

    //显示定位标志
    private Marker locateMarker;
    private boolean hasAddLocateMarker = false;

    private void showLocateMarker(GeoPoint geoPoint) {
        if (locateMarker == null) {
            locateMarker = new Marker(mapView);
        }
        if (hasAddLocateMarker) {
            locateMarker.setPosition(geoPoint);
            mapView.invalidate();
        } else {
            locateMarker.setPosition(geoPoint);
            locateMarker.setIcon(getResources().getDrawable(R.mipmap.ic_postion_curr));
            mapView.getOverlayManager().add(locateMarker);
            hasAddLocateMarker = true;
        }
    }

    private void showLocateMarker(double lon, double lat) {
        GeoPoint geoPoint = new GeoPoint(lat, lon);
        showLocateMarker(geoPoint);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_main_locate:
                locateCurrPosition();
                break;
            case R.id.iv_main_twobubble:
                if (isLogin()) {
                    startMsgListActivity();

                } else {
                    startLoginActivity();
                }
                break;
            case R.id.iv_main_paperairplane:
                if (isLogin()) {
                    String broadMsg = etBroadcast.getText().toString();
                    if (!StringUtils.isTrimEmpty(broadMsg)) {
                        if (broadMsg.length() > 20) {
                            ToastUtils.showShort("最多输入20个字符");
                            return;
                        }
                        sendBroadMsg(broadMsg);
                    }
                } else {
                    startLoginActivity();
                }
                break;
        }
    }

    private boolean isLogin() {
        boolean isLogin = SPUtils.getInstance().getBoolean(SPKey.IS_LOGIN.getUniqueName(), false);
        return isLogin;
    }

    private void startLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void startMsgListActivity() {
        Intent msgListIntent = new Intent(this, CommunicationListActivity.class);
        startActivity(msgListIntent);
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        switch (v.getId()) {
            case R.id.et_main_broad:
                Log.i("lala", "is focus : " + hasFocus);
                if (isLogin()) {

                } else {
                    startLoginActivity();
                }
                break;
        }
    }

    private void connectToServer() {
        AsyncServer.getDefault().connectSocket(new InetSocketAddress(Const.ip(), Const.socketPort()), new ConnectCallback() {
            @Override
            public void onConnectCompleted(Exception ex, AsyncSocket socket) {
                if (ex == null) {
                    asyncSocket = socket;
                    asyncSocket.write(new ByteBufferList(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()).getBytes()));
                    asyncSocket.setWriteableCallback(new WritableCallback() {
                        @Override
                        public void onWriteable() {
                            Log.i("lala", "onWriteable");
                        }
                    });
                    asyncSocket.setDataCallback(new DataCallback() {
                        @Override
                        public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                            String msg = new String(bb.getAllByteArray());
                            Log.i("lala", "setDataCallback : " + msg + " " + Thread.currentThread().getName());
                            addMsg(msg);
                        }
                    });
                    asyncSocket.setClosedCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            Log.i("lala", "setClosedCallback : " + ex.getMessage());
                        }
                    });
                    asyncSocket.setEndCallback(new CompletedCallback() {
                        @Override
                        public void onCompleted(Exception ex) {
                            Log.i("lala", "setEndCallback : " + ex.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void sendBroadMsg(String msg) {
        asyncSocket.write(new ByteBufferList(msg.getBytes()));
    }

    private void addMsg(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView textView = new TextView(MainActivity.this);
                textView.setTextColor(getResources().getColor(R.color.txtblack));
                textView.setText(msg);
                //llBroadMsg.addView(textView);

            }
        });
    }
}
