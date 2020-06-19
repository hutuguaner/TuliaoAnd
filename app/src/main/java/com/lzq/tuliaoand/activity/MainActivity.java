package com.lzq.tuliaoand.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.constant.PermissionConstants;
import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.AppUtils;
import com.blankj.utilcode.util.PermissionUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ShadowUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.VibrateUtils;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Event;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.dialog.DialogVersionToast;
import com.lzq.tuliaoand.dialog.DialogVersionUpdating;
import com.lzq.tuliaoand.services.MyService;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.Callback;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Progress;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.base.Request;
import com.orhanobut.logger.Logger;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.io.File;
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
                myService.sendPositionToServer();
            }
            myService.startGetVersionTask();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        initView();
        initMap();
        initFriendlyToast();

        bindService(new Intent(this, MyService.class), connection, BIND_AUTO_CREATE);

        EventBus.getDefault().register(this);

        if (StringUtils.isTrimEmpty(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            startActivity(new Intent(this, LoginActivity.class));
        } else {
            com.lzq.tuliaoand.subutil.util.LocationUtils.register(1000, 5, this);

        }
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
            RelativeLayout relativeLayout = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.view_marker, null);
            TextView textView = (TextView) relativeLayout.getChildAt(0);
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
        if (!StringUtils.isTrimEmpty(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName())))
            updataLocation();

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
        myService.stopGetVersionTask();
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
            //normal("刚刚安装，地图加载较慢，随着使用，地图加载会越来越快", true);
            ToastUtils.showLong("地图加载速度会越来越快");
        } else {

        }
    }

    //--------------------------------------------------------------------------------------------------------------------------------------------------------

    private void getUsers() {
        String me = SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("email", me);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        OkGo.<String>post(Const.GET_ALL_USERS).upJson(jsonObject).execute(new StringCallback() {
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
                try {
                    JSONObject result = new JSONObject(response.body());
                    int code = result.getInt("code");
                    if (code == 0) {
                        JSONArray data = result.getJSONArray("data");
                        List<User> users = new ArrayList<>();
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject item = data.getJSONObject(i);
                            String email = item.getString("email");
                            String broadcast = item.getString("broadcast");
                            String lng = item.getString("lng");
                            String lat = item.getString("lat");
                            User user = new User();
                            user.setEmail(email);
                            user.setBroadcast(broadcast);
                            if (!StringUtils.isTrimEmpty(lng))
                                user.setLng(Double.parseDouble(lng));
                            if (!StringUtils.isTrimEmpty(lat))
                                user.setLat(Double.parseDouble(lat));
                            users.add(user);
                        }
                        updateUserList(users);

                        updateMarker();


                    } else if (code == 1) {
                        String msg = result.getString("message");
                    } else if (code == 2) {

                    }
                } catch (Exception e) {
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

//-------------------------------------------------------------------------------------------------------------------------------------------------------------


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
                myService.sendPositionToServer();
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
                if (yoYoString != null) {
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
                myService.sendBroadcastToServer(broadMsg);
                etBroadcast.setText("");
                break;
        }
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
                myService.sendPositionToServer();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            this.location = location;
            if (myService != null) {
                myService.sendPositionToServer();
            }
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(Event event) {
        if (event == null) return;
        if (event.type != Event.TYPE_MAIN) return;
        if (event.isConnectTimeOut) {
            ToastUtils.showLong("连接超时，请重新登录");
            SPUtils.getInstance().put(SPKey.EMAIL_LOGINED.getUniqueName(), "");
            startActivity(new Intent(this, LoginActivity.class));
            return;
        } else {
            //
            if (event.users != null && event.users.size() > 0) {

                updateUserList(event.users);

                updateMarker();
            }
            //
            if (event.messageList != null && event.messageList.size() > 0) {
                if (ActivityUtils.getTopActivity() instanceof MainActivity) {
                    Log.i("lala", " 收到消息 ： " + "main");
                    if (yoYoString == null) {
                        yoYoString = YoYo.with(Techniques.Tada)
                                .duration(700)
                                .repeat(YoYo.INFINITE)
                                .playOn(ivMessage);
                    }

                } else if (ActivityUtils.getTopActivity() instanceof CommunicationListActivity) {
                    Log.i("lala", " 收到消息 ： " + "list");

                } else if (ActivityUtils.getTopActivity() instanceof ConversationActivity) {
                    Log.i("lala", " 收到消息 ： " + "conversation");

                    for (int i = 0; i < event.messageList.size(); i++) {
                        Message message = event.messageList.get(i);
                        Log.i("lala", " " + message.getFrom().getEmail() + " " + ConversationActivity.oppositeEmail);
                        if (!message.getFrom().getEmail().equals(ConversationActivity.oppositeEmail)) {
                            if (yoYoString == null) {
                                yoYoString = YoYo.with(Techniques.Tada)
                                        .duration(700)
                                        .repeat(YoYo.INFINITE)
                                        .playOn(ivMessage);
                            }

                            break;
                        }
                    }
                }
            }
            //
            if (event.version != null) {
                int currVersionCode = AppUtils.getAppVersionCode();
                if (event.version.getVersionCode() > currVersionCode) {
                    //更新
                    if (!(ActivityUtils.getTopActivity() instanceof MainActivity)) {
                        return;
                    }
                    if (dialogVersionUpdating != null && dialogVersionUpdating.isShowing()) {
                        return;
                    }
                    if (dialogVersionToast != null && dialogVersionToast.isShowing()) {
                        return;
                    }

                    initUpdatToastDialog();
                    dialogVersionToast.show(event.version.getVersionDesc(), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkForceUpdateStoragePermission();
                        }
                    });
                }
            }
        }

    }

    private DialogVersionToast dialogVersionToast = null;

    private void initUpdatToastDialog() {
        if (dialogVersionToast == null) dialogVersionToast = new DialogVersionToast(this);
    }

    //申请下载安装apk的 存储权限
    private void checkForceUpdateStoragePermission() {
        boolean isGrantedStorage = PermissionUtils.isGranted(PermissionConstants.STORAGE);
        if (!isGrantedStorage) {
            PermissionUtils.permission(PermissionConstants.STORAGE).callback(new PermissionUtils.SimpleCallback() {
                @Override
                public void onGranted() {
                    checkForceUpdateUnknowSourcePermission();
                }

                @Override
                public void onDenied() {
                    Toast.makeText(MainActivity.this, "没有存储权限无法更新，将无法使用", Toast.LENGTH_LONG).show();
                    MainActivity.this.finish();
                }
            }).request();
        } else {
            checkForceUpdateUnknowSourcePermission();
        }


    }

    //申请下载安装apk的 安装未知来源的 权限
    private void checkForceUpdateUnknowSourcePermission() {
        if (Build.VERSION.SDK_INT >= 26) {
            boolean hasRequestPackageInstallPermission = getPackageManager().canRequestPackageInstalls();
            if (hasRequestPackageInstallPermission) {
                initUpdatingDialog();
                downloadApk();
            } else {
                Uri packageUri = Uri.parse("package:" + AppUtils.getAppPackageName());
                Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, packageUri);
                startActivityForResult(intent, 10086);
            }
        } else {
            initUpdatingDialog();
            downloadApk();
        }
    }

    private DialogVersionUpdating dialogVersionUpdating = null;

    private void initUpdatingDialog() {
        if (dialogVersionUpdating == null) dialogVersionUpdating = new DialogVersionUpdating(this);
    }

    private void downloadApk() {
        OkGo.<File>get(Const.APK_URL).execute(new FileCallback() {
            @Override
            public void onStart(Request<File, ? extends Request> request) {
                super.onStart(request);
                dialogVersionToast.dismis();
                dialogVersionUpdating.show();
            }

            @Override
            public void onSuccess(Response<File> response) {
                AppUtils.installApp(response.body().getPath());
            }

            @Override
            public void onError(Response<File> response) {
                super.onError(response);
                ToastUtils.showLong(response.message());
            }

            @Override
            public void onFinish() {
                super.onFinish();
                dialogVersionUpdating.dismis();
            }

            @Override
            public void downloadProgress(Progress progress) {
                super.downloadProgress(progress);
                if (Build.VERSION.SDK_INT >= 24) {
                    dialogVersionUpdating.setProgress((int) ((double) progress.currentSize / progress.totalSize * 100), true);
                } else {
                    dialogVersionUpdating.setProgress((int) ((double) progress.currentSize / progress.totalSize * 100));
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 10086://申请安装未知来源的 app
                //
                if (resultCode == RESULT_OK) {
                    initUpdatingDialog();
                    downloadApk();
                } else if (resultCode == RESULT_CANCELED) {
                    ToastUtils.showLong("请打开“允许安装应用”开关");
                }
                break;
        }
    }

    private void updateUserList(List<User> users) {
        this.userList.clear();
        this.userList.addAll(users);
    }

    private void updateMarker() {

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
            Marker marker = createMarker(user.getLng(), user.getLat(), user.getBroadcast(), user.getEmail());

            markerMap.put(user.getEmail(), marker);
            mapView.getOverlayManager().add(marker);

        }
        mapView.invalidate();
    }


    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        String email = marker.getId();
        if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            ToastUtils.showShort("你自己: " + email);
        } else {

            Intent intent = new Intent(this, ConversationActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);
        }

        return false;
    }


}
