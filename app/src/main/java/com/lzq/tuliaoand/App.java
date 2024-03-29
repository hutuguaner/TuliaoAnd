package com.lzq.tuliaoand;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Intent;
import android.os.Environment;
import android.util.Log;

import androidx.room.Room;

import com.blankj.utilcode.util.CrashUtils;
import com.lzq.tuliaoand.common.Const;
import com.lzq.tuliaoand.common.MyDatabase;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.cache.CacheEntity;
import com.lzy.okgo.cache.CacheMode;
import com.lzy.okgo.cookie.CookieJarImpl;
import com.lzy.okgo.cookie.store.DBCookieStore;
import com.lzy.okgo.model.HttpHeaders;
import com.lzy.okgo.model.HttpParams;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.config.Configuration;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;
import okhttp3.OkHttpClient;

public class App extends Application {

    private Socket socket;


    @SuppressLint("MissingPermission")
    @Override
    public void onCreate() {
        super.onCreate();
        initMap();
        //崩溃
        CrashUtils.init(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath() + "/crash");
        //
        okgoInit();
        //
        Logger.addLogAdapter(new AndroidLogAdapter());
        //
        myDatabase = Room.databaseBuilder(getApplicationContext(), MyDatabase.class, "tuliao.db").allowMainThreadQueries().build();
        //
        initYouMeng();
        //
        initSocket();
    }


    public Socket getSocket() {
        return socket;
    }

    private void initSocket() {
        if (socket == null) {
            try {
                socket = IO.socket("http://" + Const.ip() + ":8000");
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    private void initYouMeng() {
        // 初始化SDK
        UMConfigure.init(this, "5ed90c4b167edd9b9b000032", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
        // 选用AUTO页面采集模式
        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }


    private void initMap() {
        //设置 底图引擎 底图 瓦片 缓存 路径
        File dirCache = new File(getExternalCacheDir().getAbsolutePath() + "/tiles");
        if (!dirCache.exists()) dirCache.mkdirs();
        Configuration.getInstance().setOsmdroidTileCache(dirCache);
        //设置离线 影像数据 读取路径
        File dirOfflineImagePath = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/offlineimages");
        if (!dirOfflineImagePath.exists()) dirOfflineImagePath.mkdirs();
        Configuration.getInstance().setOsmdroidBasePath(dirOfflineImagePath);
        //设置缓存大小
        Configuration.getInstance().setTileFileSystemCacheTrimBytes(1024l * 1024 * 1024 * 10);

        //设置初始内存缓存大小瓦片的多少，最少是3*3
        Configuration.getInstance().setCacheMapTileCount((short) 12);
        //设置矢量覆盖物超出的缓存数量
        Configuration.getInstance().setCacheMapTileOvershoot((short) 12);
    }


    private void okgoInit() {
        //okGo网络框架初始化和全局配置
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        //builder.cookieJar(new CookieJarImpl(new SPCookieStore(this)));      //使用sp保持cookie，如果cookie不过期，则一直有效
        builder.cookieJar(new CookieJarImpl(new DBCookieStore(this)));//使用数据库保持cookie，如果cookie不过期，则一直有效
        //builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));      //使用内存保持cookie，app退出后，cookie消失
        //设置请求头
        HttpHeaders headers = new HttpHeaders();
        //headers.put("commonHeaderKey1", "commonHeaderValue1");    //header不支持中文，不允许有特殊字符
        //headers.put("commonHeaderKey2", "commonHeaderValue2");
        //设置请求参数
        HttpParams params = new HttpParams();
        //params.put("commonParamsKey1", "commonParamsValue1");     //param支持中文,直接传,不要自己编码
        //params.put("commonParamsKey2", "这里支持中文参数");
        OkGo.getInstance().init(this)                           //必须调用初始化
                .setOkHttpClient(builder.build())               //建议设置OkHttpClient，不设置会使用默认的
                .setCacheMode(CacheMode.NO_CACHE)               //全局统一缓存模式，默认不使用缓存，可以不传
                .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE)   //全局统一缓存时间，默认永不过期，可以不传
                .setRetryCount(3)                               //全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
                .addCommonHeaders(headers)                      //全局公共头
                .addCommonParams(params);
    }


    public static MyDatabase myDatabase = null;


}


/**
 * Copyright (C) 2017 stfalcon.com
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * https://github.com/stfalcon-studio/ChatKit/blob/master/LICENSE
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p>
 * https://icons8.com/icons/set/bubble
 * <p>
 * https://icons8.com/icons/set/bubble
 * <p>
 * Copyright (C) 2012-2020 Markus Junginger, greenrobot (https://greenrobot.org)
 * <p>
 * EventBus binaries and source code can be used according to the Apache License, Version 2.0.
 */

/**
 * https://icons8.com/icons/set/bubble
 */

/**
 * Copyright (C) 2012-2020 Markus Junginger, greenrobot (https://greenrobot.org)
 *
 * EventBus binaries and source code can be used according to the Apache License, Version 2.0.
 */
