package com.lzq.tuliaoand.activity;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableDecoder;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableEncoder;
import com.bumptech.glide.load.resource.bitmap.BitmapDrawableResource;
import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
import com.lzq.tuliaoand.App;
import com.lzq.tuliaoand.LoginActivity;
import com.lzq.tuliaoand.R;
import com.lzq.tuliaoand.bean.Conversation;
import com.lzq.tuliaoand.bean.Message;
import com.lzq.tuliaoand.bean.User;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.SPKey;
import com.lzq.tuliaoand.model.MainModel;
import com.lzq.tuliaoand.presenter.MainPresenter;
import com.orhanobut.logger.Logger;
import com.pranavpandey.android.dynamic.utils.DynamicUnitUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.LocationUtils;
import org.osmdroid.util.TileSystem;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.infowindow.MarkerInfoWindow;
import org.w3c.dom.Text;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements View.OnClickListener, com.lzq.tuliaoand.subutil.util.LocationUtils.OnLocationChangeListener, MainModel, Marker.OnMarkerClickListener {

    private MapView mapView;
    private MapController mapController;
    private ImageView ivLocate, ivSend, ivMessage;
    private EditText etBroadcast;

    private Location location;

    private MainPresenter mainPresenter;

    private TimerTask timerTask;

    private Timer timer;

    private ArrayList<Conversation> conversations;

    private YoYo.YoYoString yoYoString;

    private Map<String, Marker> markerMap;

    //private Queue<User> userQueue;
    private List<User> userList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainPresenter = new MainPresenter(this);

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


    }


    private void resumeTask() {

        if (timerTask == null)
            timerTask = new TimerTask() {
                @Override
                public void run() {
                    //
                    if (location != null) {
                        mainPresenter.uploadPosition(location.getLongitude(), location.getLatitude());
                    }

                    //
                    try {
                        Thread.sleep(1000 * 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //
                    mainPresenter.getUsers();

                    //
                    try {
                        Thread.sleep(1000 * 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //清理下线用户 的地图marker
                    if (userList != null && !userList.isEmpty()) {
                        if (markerMap != null && !markerMap.isEmpty()) {
                            Set<String> emails = markerMap.keySet();
                            Iterator<String> iterator = emails.iterator();
                            while (iterator.hasNext()) {
                                String email = iterator.next();
                                User user = new User();
                                user.setEmail(email);
                                if (!userList.contains(user)) {
                                    Marker marker = markerMap.get(email);
                                    mapView.getOverlayManager().remove(marker);
                                    mapView.postInvalidate();
                                    iterator.remove();
                                }
                            }
                        }
                    } else {
                        if (markerMap != null && !markerMap.isEmpty()) {
                            Set<String> emails = markerMap.keySet();
                            Iterator<String> iterator = emails.iterator();
                            while (iterator.hasNext()) {
                                String email = iterator.next();
                                Marker marker = markerMap.get(email);
                                mapView.getOverlayManager().remove(marker);
                                mapView.postInvalidate();
                            }
                            markerMap.clear();
                        }
                    }

                    //
                    for (final User user:userList){
                        //
                        if (markerMap == null) markerMap = new HashMap<>();
                        if (markerMap.containsKey(user.getEmail())) {
                            setViewOrIconOnMarker(user.getBroadcast(), user.getEmail(), markerMap.get(user.getEmail()));
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {

                                    if (MapView.getTileSystem().isValidLatitude(user.getLat()) && MapView.getTileSystem().isValidLongitude(user.getLng())) {
                                        markerMap.get(user.getEmail()).setPosition(new GeoPoint(user.getLat(), user.getLng()));
                                        mapView.invalidate();
                                    }

                                }
                            });

                        } else {
                            final Marker marker = new Marker(mapView);
                            marker.setId(user.getEmail());
                            marker.setOnMarkerClickListener(MainActivity.this);
                            marker.setAnchor(Marker.ANCHOR_LEFT, Marker.ANCHOR_BOTTOM);
                            marker.setInfoWindow(null);
                            setViewOrIconOnMarker(user.getBroadcast(), user.getEmail(), marker);

                            if (MapView.getTileSystem().isValidLatitude(user.getLat()) && MapView.getTileSystem().isValidLongitude(user.getLng())) {
                                marker.setPosition(new GeoPoint(user.getLat(), user.getLng()));

                                mapView.getOverlayManager().add(marker);
                                mapView.postInvalidate();
                                markerMap.put(user.getEmail(), marker);


                            }

                        }
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }


                    //
                    mainPresenter.getMsgs();

                }
            };
        if (timer == null) timer = new Timer();

        timer.schedule(timerTask, 500, 1000 * 5);
    }


    private void setViewOrIconOnMarker(String broadcast, String email, Marker marker) {
        if (StringUtils.isTrimEmpty(broadcast)) {
            if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                marker.setIcon(getResources().getDrawable(R.mipmap.ic_locate_blue));
            } else {
                marker.setIcon(getResources().getDrawable(R.mipmap.ic_postion_curr));
            }
        } else {
            TextView textView = (TextView) LayoutInflater.from(this).inflate(R.layout.view_marker, null);

            if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                textView.setBackground(getResources().getDrawable(R.mipmap.ic_bubble_blue));
                textView.setTextColor(getResources().getColor(R.color.white));
            } else {
                textView.setBackground(getResources().getDrawable(R.mipmap.ic_bubble_green));
                textView.setTextColor(getResources().getColor(R.color.black));
            }
            textView.setTextSize(15f);
            textView.setText(broadcast);

            marker.setView(textView);
        }
    }


    private void pauseTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) timer.purge();
    }


    private void receiveMessages(List<Message> messages) {
        if (conversations == null) conversations = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            Message message = messages.get(i);
            Conversation conversation = new Conversation();

            if (conversations.contains(conversation)) {
                int index = conversations.indexOf(conversation);
                conversations.get(index).getMessages().add(message);
            } else {
                ArrayList<Message> msgs = new ArrayList<>();
                msgs.add(message);
                conversation.setMessages(msgs);
                conversations.add(conversation);
            }

        }
    }


    private void updataBroadcastAndPostion(List<User> users) {
        if (userList == null) userList = new ArrayList<>();

        userList.clear();

        userList.addAll(users);

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
            resumeTask();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        pauseTask();
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
            dismiss();
        }
    }


    private void uploadPostion() {
        Location location = LocationUtils.getLastKnownLocation((LocationManager) getSystemService(LOCATION_SERVICE));
        sendPostionToServer(location.getLongitude(), location.getLatitude());
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

    private void showLocateMarker(GeoPoint geoPoint) {
        if (markerMap == null) markerMap = new HashMap<>();
        if (markerMap.containsKey(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            markerMap.get(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName())).setPosition(geoPoint);
            mapView.invalidate();
        } else {
            Marker marker = new Marker(mapView);
            marker.setInfoWindow(null);
            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            marker.setPosition(geoPoint);
            marker.setIcon(getResources().getDrawable(R.mipmap.ic_locate_blue));
            mapView.getOverlayManager().add(marker);
            mapView.invalidate();
            markerMap.put(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()), marker);
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
                updataLocation();
                showLocateMarker(location.getLongitude(), location.getLatitude());
                mapZoomTo(location.getLongitude(), location.getLatitude());
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
                if (!StringUtils.isTrimEmpty(broadMsg)) {
                    if (broadMsg.length() > 20) {
                        ToastUtils.showShort("最多输入20个字符");
                        return;
                    }

                    mainPresenter.uploadBroadCast(broadMsg);
                }
                break;
        }
    }


    private void startLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void startMsgListActivity() {
        if (conversations == null) conversations = new ArrayList<>();
        Intent conversationListIntent = new Intent(this, CommunicationListActivity.class);
        conversationListIntent.putExtra("data", conversations);
        startActivity(conversationListIntent);
    }


    private void sendPostionToServer(double lng, double lat) {


    }

    private void sendBroadCastToServer(String broadcast) {


    }


    //---------------------------------
    @Override
    public void getLastKnownLocation(Location location) {

    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }


    @Override
    public void connectTimeOut() {
        ToastUtils.showLong("连接断开，请重新登录");
        SPUtils.getInstance().put(SPKey.EMAIL_LOGINED.getUniqueName(), "");
        startActivity(new Intent(this, LoginActivity.class));
    }

    //--------------------------------
    @Override
    public void getUsersStart() {

    }

    @Override
    public void getUsersError(String err) {

    }

    @Override
    public void getUsersSuccess(List<User> users) {
        if (users == null || users.size() < 1) return;
        updataBroadcastAndPostion(users);


    }

    @Override
    public void getUsersFinish() {

    }

    @Override
    public void getMsgsStart() {

    }

    @Override
    public void getMsgsError(String msg) {

    }

    @Override
    public void getMsgsSuccess(List<Message> messages) {
        if (messages == null || messages.size() < 1) return;
        receiveMessages(messages);

        if (yoYoString == null) {
            yoYoString = YoYo.with(Techniques.Tada)
                    .duration(700)
                    .repeat(YoYo.INFINITE)
                    .playOn(ivMessage);
        } else {

        }

    }

    @Override
    public void getMsgsFinish() {

    }

    @Override
    public void uploadPositionStart() {

    }

    @Override
    public void uploadPositionError(String msg) {

    }

    @Override
    public void uploadPositionSuccess() {

    }

    @Override
    public void uploadPositionFinish() {

    }

    @Override
    public void uploadBroadcastStart() {

    }

    @Override
    public void uploadBroadcastError(String msg) {

    }

    @Override
    public void uploadBroadcastSuccess() {

    }

    @Override
    public void uploadBroadcastFinish() {

    }


    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        String email = marker.getId();
        if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
            ToastUtils.showShort("你自己");
        } else {
            if (conversations == null) conversations = new ArrayList<>();
            Conversation conversation = new Conversation();


        }

        return false;
    }
}
