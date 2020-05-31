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

import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends BaseActivity implements View.OnClickListener ,com.lzq.tuliaoand.subutil.util.LocationUtils.OnLocationChangeListener {

    private MapView mapView;
    private MapController mapController;
    private ImageView ivLocate, ivSend, ivMessage;
    private EditText etBroadcast;

    private volatile List<User> users;
    private Location location;

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

    }





    private void receiveUserQuit(String data) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
            if (users == null) users = new ArrayList<>();
            String email = jsonObject.getString("email");
            User user = new User();
            user.setEmail(email);
            if (users.contains(user)) {
                users.remove(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void receiveMessages(String data) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
            if (users == null) users = new ArrayList<>();
            String from = jsonObject.getString("from");
            String content = jsonObject.getString("content");
            long time = jsonObject.getLong("time");
            Message message = new Message();
            message.setEmailFrom(from);
            message.setContent(content);
            message.setTimeStamp(time);

            User user = new User();
            user.setEmail(from);

            if (users.contains(user)) {
                int index = users.indexOf(user);
                Conversation conversation = new Conversation();
                conversation.setOppositeEmail(from);
                if (users.get(index).getConversations().contains(conversation)) {
                    int indexConversation = users.get(index).getConversations().indexOf(conversation);
                    users.get(index).getConversations().get(indexConversation).getMessages().add(message);
                } else {
                    users.get(index).getConversations().add(conversation);
                }

            } else {
                List<Conversation> conversations = new ArrayList<>();
                Conversation conversation = new Conversation();
                conversation.setOppositeEmail(from);
                conversation.setMessages(Arrays.asList(message));
                user.setConversations(conversations);
                users.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void receivePositions(String data) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
            if (users == null) users = new ArrayList<>();
            String email = jsonObject.getString("email");
            double lng = jsonObject.getDouble("lng");
            double lat = jsonObject.getDouble("lat");
            User user = new User();
            user.setEmail(email);

            if (email.equals(SPUtils.getInstance().getString(SPKey.EMAIL_LOGINED.getUniqueName()))) {
                locateMarker.setPosition(new GeoPoint(lat, lng));
                mapView.invalidate();
                return;
            }

            if (users.contains(user)) {
                int index = users.indexOf(user);
                users.get(index).setLat(lat);
                users.get(index).setLng(lng);
                users.get(index).getMarker().setPosition(new GeoPoint(lat, lng));
                mapView.invalidate();
            } else {
                user.setLng(lng);
                user.setLat(lat);
                Marker marker = new Marker(mapView);
                marker.setPosition(new GeoPoint(lat, lng));
                marker.setIcon(getResources().getDrawable(R.mipmap.ic_postion_curr));
                mapView.getOverlayManager().add(marker);
                mapView.invalidate();
                user.setMarker(marker);
                users.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    private void receiveBroadcasts(String data) {
        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(data);
            if (users == null) users = new ArrayList<>();
            String email = jsonObject.getString("email");
            String content = jsonObject.getString("broadcast");
            User user = new User();
            user.setEmail(email);
            user.setBroadcast(content);
            if (users.contains(user)) {
                int index = users.indexOf(user);
                users.get(index).setBroadcast(content);
                users.get(index).getMarker().setTitle(content);
                users.get(index).getMarker().showInfoWindow();
                mapView.invalidate();
            } else {
                users.add(user);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
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

                    sendBroadCastToServer(broadMsg);
                }
                break;
        }
    }


    private void startLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }

    private void startMsgListActivity() {
        Intent msgListIntent = new Intent(this, CommunicationListActivity.class);
        startActivity(msgListIntent);
    }


    private void sendPostionToServer(double lng, double lat) {


    }

    private void sendBroadCastToServer(String broadcast) {


    }



    //---------------------------------
    @Override
    public void getLastKnownLocation(Location location) {
        Logger.d(location);
    }

    @Override
    public void onLocationChanged(Location location) {
        Logger.d(location);
        this.location = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Logger.d(status);
    }


}
