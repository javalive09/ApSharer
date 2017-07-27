package com.javalive09.apsharer;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.javalive09.apsharer.WifiApManager.WifiStateListener;


import org.nanohttpd.protocols.http.NanoHTTPD;

import java.io.IOException;
import java.util.List;

/**
 * Created by peter on 2017/3/6.
 */

public class MainService extends Service {

    private static final String TAG = MainService.class.getSimpleName();

    private NanoHTTPD simpleWebServer;

    private WifiApManager wifiApManager;

    @Override
    public IBinder onBind(Intent intent) {
        return new MyBinder();
    }

    public class MyBinder extends Binder {
        public Service getService() {
            return MainService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public void startServer() {
        stopServer();
        simpleWebServer = new MainWebServer(8080);
        wifiApManager = new WifiApManager(MainService.this);
        try {
            simpleWebServer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stopServer() {
        if (simpleWebServer != null) {
            simpleWebServer.stop();
        }
    }

    public void startAp() {
        if (simpleWebServer != null) {
            wifiApManager.startWifiAp();
        }
    }

    public void setListener(WifiStateListener listener) {
        if (simpleWebServer != null) {
            wifiApManager.setmWifiStateListener(listener);
        }
    }

    public void stopAp() {
        if (wifiApManager != null) {
            wifiApManager.stopWifiAp();
            wifiApManager.destroy(MainService.this);
        }
    }

    public void closeAp() {
        if(wifiApManager != null) {
            wifiApManager.closeWifiAp();
        }
    }

    public int getWifiApState() {
        if(wifiApManager != null) {
            return wifiApManager.getWifiApState();
        }else {
            return 11;// WifiManager.WIFI_AP_STATE_DISABLED
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public void setForeground() {
        Notification.Builder builder = new Notification.Builder(this.getApplicationContext()); //获取一个Notification构造器
        Intent nfIntent = new Intent(this, MainActivity.class);
        builder.setContentIntent(PendingIntent.getActivity(this, 0, nfIntent, 0)) // 设置PendingIntent
                .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher)) // 设置下拉列表中的图标(大图标)
                .setContentTitle("热点共享服务开启") // 设置下拉列表里的标题
                .setSmallIcon(R.drawable.ic_stat_share) // 设置状态栏内的小图标
                .setContentText("您可以加入热点扫码访问文件") // 设置上下文内容
                .setWhen(System.currentTimeMillis()); // 设置该通知发生的时间
        Notification notification = builder.build(); // 获取构建好的Notification
        notification.defaults = Notification.DEFAULT_SOUND; //设置为默认的声音
        startForeground(110, notification);// 开始前台服务
    }


}
