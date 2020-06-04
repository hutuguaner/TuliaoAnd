package com.lzq.tuliaoand.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.services.MyService;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements View.OnClickListener, com.lzq.tuliaoand.subutil.util.LocationUtils.OnLocationChangeListener, Marker.OnMarkerClickListener {

    private MapView mapView;
    private MapController mapController;
    private ImageView ivLocate, ivSend, ivMessage;
    private EditText etBroadcast;

    private Location location;

    private YoYo.YoYoString yoYoString;

    private Map<String, Marker> markerMap = new HashMap<>();

    //private Queue<User> userQueue;
    private List<User> userList = new ArrayList<>();

    private MyService myService = null;

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            MyService.MyBinder myBinder = (MyService.MyBinder) iBinder;
            myService = myBinder.getService();

            if (location != null) {
                myService.uploadPosition(location.getLongitude(), location.getLatitude());
            }
            myService.startGetUserTask();
            myService.startGetMessageTask();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initPermission();
        setContentView(R.layout.activity_main);
        initView();
        initMap();
        initFriendlyToast();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        com.lzq.tuliaoand.subutil.util.LocationUtils.register(1000, 5, this);

        bindService(new Intent(this, MyService.class), connection, BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);
    }


    private Marker createMarker(double lng, double lat, String broadcast, String email) {
        Marker marker = new Marker(mapView);
        marker.setPosition(new GeoPoint(lat, lng));
        marker.setOnMarkerClickListener(this);
        marker.setId(email);
        marker.setInfoWindow(null);
        if (StringUtils.isTrimEmpty(broadcast)) {
            if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                marker.setIcon(getResources().getDrawable(R.mipmap.ic_locate_blue));
            } else {
                marker.setIcon(getResources().getDrawable(R.mipmap.ic_postion_curr));
            }
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        } else {
            TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.view_marker, null);

            if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                textView.setBackground(getResources().getDrawable(R.mipmap.ic_bubble_blue));
                textView.setTextColor(getResources().getColor(R.color.white));
            } else {
                textView.setBackground(getResources().getDrawable(R.mipmap.ic_bubble_green));
                textView.setTextColor(getResources().getColor(R.color.black));
            }
            marker.setAnchor(Marker.ANCHOR_LEFT, Marker.ANCHOR_BOTTOM);
            textView.setTextSize(15f);
            textView.setText(broadcast);

            marker.setView(textView);
        }
        return marker;
    }


    private void updataBroadcastAndPostion(final List<User> users) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (userList) {

                    userList.clear();

                    userList.addAll(users);
                }
            }
        }).start();
    }

    private String getCurrLoginEmail() {
        return SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        if (StringUtils.isTrimEmpty(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        com.lzq.tuliaoand.subutil.util.LocationUtils.unregister();
        if (myService != null)
            unbindService(connection);
        EventBus.getDefault().unregister(this);
        myService.stopGetMessageTask();
        myService.stopGetUserTask();
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
        updataLocation();
    }

    //-------------------------------------------------------------------------------------------------------------------------------------------------------------
    //定位到当前位置
    private void updataLocation() {
        show();

        Location location = LocationUtils.getLastKnownLocation((LocationManager) getSystemService(Context.LOCATION_SERVICE));
        if (location == null) {
            dismiss();
            ToastUtils.showLong("定位失败");
            return;
        } else {
            this.location = location;
            if (myService != null) {
                myService.uploadPosition(this.location.getLongitude(), this.location.getLatitude());
            }
            mapZoomTo(this.location.getLongitude(), this.location.getLatitude());

            dismiss();
        }
    }


    private void mapZoomTo(double lon, double lat) {
        mapLocateTo(lon, lat);
        //mapController.setZoom(18);
    }

    private void mapLocateTo(double lon, double lat) {
        //用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
        GeoPoint point = new GeoPoint(lat, lon);
        //设置地图中心点
        mapController.animateTo(point);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.iv_main_locate:
                updataLocation();
                break;
            case R.id.iv_main_twobubble:
                startMsgListActivity();

                if (yoYoString != null && yoYoString.isRunning()) {
                    yoYoString.stop();
                    yoYoString = null;
                }
                break;
            case R.id.iv_main_paperairplane:
                String broadMsg = etBroadcast.getText().toString();
                if (StringUtils.isTrimEmpty(broadMsg)) return;
                if (broadMsg.length() > 20) {
                    ToastUtils.showShort("最多输入20个字符");
                    return;
                }
                myService.uploadBroadCast(broadMsg);
                etBroadcast.setText("");
                break;
        }
    }


    private void startLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void startMsgListActivity() {
        Intent conversationListIntent = new Intent(this, CommunicationListActivity.class);
        startActivity(conversationListIntent);
    }


    //---------------------------------
    @Override
    public void getLastKnownLocation(Location location) {
        if (location != null) {
            this.location = location;
            if (myService != null) {
                myService.uploadPosition(this.location.getLongitude(), this.location.getLatitude());
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.location = location;
            if (myService != null) {
                myService.uploadPosition(this.location.getLongitude(), this.location.getLatitude());
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (event == null) return;
        if (event.isConnectTimeOut) {
            ToastUtils.showLong("连接超时，请重新登录");
            SPUtils.getInstance().put(SPKey.EMAIL_LOGINED.getUniqueName(), "");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        } else {
            if (event.users != null && event.users.size() > 0) {

                updateUserList(event.users);

                updateMarker();
            }
            if (event.messageList != null && event.messageList.size() > 0) {
                if (ActivityUtils.getTopActivity() instanceof MainActivity) {
                    if (yoYoString == null) {
                        yoYoString = YoYo.with(Techniques.Tada)
                                .duration(700)
                                .repeat(YoYo.INFINITE)
                                .playOn(ivMessage);
                    } else {

                    }
                }
            }
        }

    }

    private void updateUserList(List<User> users) {
        this.userList.clear();
        this.userList.addAll(users);
    }

    private void updateMarker() {



        for (int i=0;i<userList.size();i++){
            User user = userList.get(i);
            Log.i("lala"," 获取到的当前用户 ： "+user.getEmail()+" "+user.getBroadcast()+" "+user.getLng()+" "+user.getLat());
        }

        //先删掉 用户列表已经不存在 但是在地图上显示的 marker
        Set<String> keySet = markerMap.keySet();
        Iterator<String> iteratorKey = keySet.iterator();
        while (iteratorKey.hasNext()) {
            String key = iteratorKey.next();
            User user = new User();
            user.setEmail(key);
            if (!userList.contains(user)) {
                markerMap.remove(key);
                mapView.getOverlayManager().remove(markerMap.get(key));
            }
        }

        //遍历 用户列表，如果地图上已经显示 则更新位置 如果地图上没有，则添加
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            if (markerMap.containsKey(user.getEmail())) {
                //先删除已有再 重新添加
                mapView.getOverlayManager().remove(markerMap.get(user.getEmail()));
                markerMap.remove(user.getEmail());

            } else {

            }
            Marker marker =  createMarker(user.getLng(), user.getLat(), user.getBroadcast(), user.getEmail());

            markerMap.put(user.getEmail(), marker);
            mapView.getOverlayManager().add(marker);

        }
        mapView.invalidate();
    }


    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        String email = marker.getId();
        if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            ToastUtils.showShort("你自己");
        } else {

            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        }

        return false;
    }


}
