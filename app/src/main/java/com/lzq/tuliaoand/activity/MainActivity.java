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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
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
import org.w3c.dom.Text;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements View.OnClickListener, com.lzq.tuliaoand.subutil.util.LocationUtils.OnLocationChangeListener, MainModel, Marker.OnMarkerClickListener {

    private MapView mapView;
    private MapController mapController;
    private ImageView ivLocate, ivSend, ivMessage;
    private EditText etBroadcast;

    private volatile List<User> users;
    private Location location;

    private MainPresenter mainPresenter;

    private TimerTask timerTask;

    private Timer timer;

    private ArrayList<Conversation> conversations;

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
                        Thread.sleep(1000 * 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //
                    mainPresenter.getUsers();

                    //
                    try {
                        Thread.sleep(1000 * 2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    //
                    mainPresenter.getMsgs();

                }
            };
        if (timer == null) timer = new Timer();

        timer.schedule(timerTask, 1000, 1000 * 5);
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
            conversation.setOppositeEmail(message.getEmailFrom());

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
        if (this.users == null) this.users = new ArrayList<>();
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            if (this.users.contains(user)) {
                int index = this.users.indexOf(user);
                this.users.get(index).setBroadcast(user.getBroadcast());
                this.users.get(index).getMarker().setTitle(user.getBroadcast());
                this.users.get(index).getMarker().showInfoWindow();
                mapView.invalidate();
            } else {
                Marker marker = new Marker(mapView);
                if (user.getEmail().equals(getCurrLoginEmail())) {
                    marker.setIcon(getResources().getDrawable(R.mipmap.ic_locate_blue));
                } else {
                    marker.setIcon(getResources().getDrawable(R.mipmap.ic_postion_curr));
                }
                marker.setTitle(user.getBroadcast());
                marker.setId(user.getEmail());
                marker.setOnMarkerClickListener(this);
                mapView.getOverlayManager().add(marker);
                mapView.invalidate();
                user.setMarker(marker);
                this.users.add(user);
            }
        }
    }

    private String getCurrLoginEmail() {
        return SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName());
    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        resumeTask();
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
            locateMarker.setIcon(getResources().getDrawable(R.mipmap.ic_locate_blue));

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
                updataLocation();
                showLocateMarker(location.getLongitude(), location.getLatitude());
                mapZoomTo(location.getLongitude(), location.getLatitude());
                break;
            case R.id.iv_main_twobubble:
                startMsgListActivity();
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


    //--------------------------------
    @Override
    public void getUsersStart() {

    }

    @Override
    public void getUsersError(String err) {

    }

    @Override
    public void getUsersSuccess(List<User> users) {
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
        receiveMessages(messages);
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
    public void uploadMsgStart() {

    }

    @Override
    public void uploadMsgError(String msg) {

    }

    @Override
    public void uploadMsgSuccess() {

    }

    @Override
    public void uploadMsgFinish() {

    }

    @Override
    public boolean onMarkerClick(Marker marker, MapView mapView) {
        String email = marker.getId();

        return false;
    }
}
