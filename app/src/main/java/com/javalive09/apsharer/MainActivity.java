package com.javalive09.apsharer;

import android.Manifest;
import android.app.AlertDialog;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.FileObserver;
import android.os.IBinder;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ipaulpro.afilechooser.utils.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String HOST = "192.168.43.1:8080";
    private static final String PACKAGE_URL_SCHEME = "package:";
    private final int REQUEST_CODE = 123;
    private static int kJobId = 456;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 789;
    private MainService service;
    private boolean mBound;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(MainActivity.this, MainService.class));
        bindService(new Intent(MainActivity.this, MainService.class), serviceConnection, BIND_AUTO_CREATE);
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            service = (MainService) ((MainService.MyBinder) binder).getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            service = null;
            mBound = false;
        }
    };

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (TextUtils.equals(SharedPreferencesUtil.ACTION_AP_STATUS, key)) {
                refreshButton();
            } else if (TextUtils.equals(SharedPreferencesUtil.ACTION_HAVE_CLIENT, key)) {
                refreshHint();
            }
        }
    };

    private void refreshHint() {
        boolean have = SharedPreferencesUtil.haveClient(getApplicationContext());
        String text;
        if (have) {
            text = "扫描所选文件生成的二维码，并下载";
        } else {
            text = "WiFi热点创建成功\n名称:" + WifiApManager.DEFAULT_SSID + "\n密码：" + WifiApManager.DEFAULT_PASSWORD;
        }
        setHint(text);
    }

    private void refreshButton() {
        int status = SharedPreferencesUtil.getApStatus(getApplicationContext());
        switch (status) {
            case SharedPreferencesUtil.ACTION_AP_CLOSED:
                setHint("");
                setButtonTxt(R.string.start);
                break;
            case SharedPreferencesUtil.ACTION_AP_OPENED:
                String text = "WiFi热点创建成功\n名称:" + WifiApManager.DEFAULT_SSID + "\n密码：" + WifiApManager.DEFAULT_PASSWORD;
                setHint(text);
                setButtonTxt(R.string.select);
//                arpFileObserver.startWatching();
//                checkoutArp();
                break;
            case SharedPreferencesUtil.ACTION_AP_OPENING:
                setHint("");
                setButtonTxt(R.string.loading);
                break;
        }
    }

    public void cancelJobs() {
        JobScheduler tm = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
        tm.cancel(kJobId);
    }

//    private void checkoutArp() {
//        ComponentName serviceComponent = new ComponentName(this, ArpService.class);
//        JobInfo.Builder builder = new JobInfo.Builder(kJobId, serviceComponent);
//        builder.setPeriodic(2000);
////        builder.setMinimumLatency(100); // wait at least 延时执行时间
////        builder.setOverrideDeadline(2000);// maximum delay 设置最大延时
////        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY); // require unmetered network
////        builder.setRequiresDeviceIdle(false); // device should be idle
////        builder.setRequiresCharging(false); // we don't care if the device is charging or not
//        JobScheduler jobScheduler = (JobScheduler) getApplication().getSystemService(Context.JOB_SCHEDULER_SERVICE);
//        jobScheduler.schedule(builder.build());
//    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                showAlertDialog(getString(R.string.action_about),
                        getString(R.string.action_about_txt));
                break;
            case R.id.action_exit:
                exit();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public AlertDialog showAlertDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.show();
        return dialog;
    }


    private void setButtonTxt(int resId) {
        TextView view = (TextView) findViewById(R.id.start);
        switch (resId) {
            case R.string.loading:
                view.setEnabled(false);
                break;
            default:
                view.setEnabled(true);
                break;
        }
        String txt = getString(resId);
        view.setText(txt);
    }

    private int getButtonType() {
        TextView view = (TextView) findViewById(R.id.start);
        String txt = view.getText().toString();
        if (TextUtils.equals(txt, getString(R.string.loading))) {
            return R.string.loading;
        } else if (TextUtils.equals(txt, getString(R.string.start))) {
            return R.string.start;
        } else if (TextUtils.equals(txt, getString(R.string.select))) {
            return R.string.select;
        }
        return -1;
    }

    protected void onResume() {
        super.onResume();
        if (!android.provider.Settings.System.canWrite(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
            intent.setData(Uri.parse("package:" + getPackageName()));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }
        refreshButton();
        SharedPreferencesUtil.addListener(getApplicationContext(), listener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        SharedPreferencesUtil.removeListener(getApplicationContext(), listener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mBound) {
            unbindService(serviceConnection);
        }
    }

    private boolean passPermission() {
        int hasExternalStoragePermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (hasExternalStoragePermission != PackageManager.PERMISSION_GRANTED) {
            if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showMessageOKCancel("需要允许读取sd卡的权限来分享sd卡中的文件", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQUEST_CODE_ASK_PERMISSIONS);
                    }
                });
            } else {
                startAppSettings();
            }

            return false;
        }
        return true;
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE_URL_SCHEME + getPackageName()));
        startActivity(intent);
    }

    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    showChooser();
                } else {
                    // Permission Denied
                    Toast.makeText(MainActivity.this, "读取SD卡权限被拒绝!", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void exit() {
        if (service != null) {
            service.stopAp();
            service.stopForeground(true);
        }
        if (mBound) {
            stopService(new Intent(MainActivity.this, MainService.class));
        }
        cancelJobs();
        SharedPreferencesUtil.setApStatus(getApplicationContext(), SharedPreferencesUtil.ACTION_AP_CLOSED);
        finish();
    }

    public void onClick(View view) {
        switch (getButtonType()) {
            case R.string.select:
                if (passPermission()) {
                    showChooser();
                }
                break;
            case R.string.start:
                if (service != null) {
                    service.setForeground();
                    service.startAp();
                    service.startServer();
                }
                SharedPreferencesUtil.setApStatus(getApplicationContext(), SharedPreferencesUtil.ACTION_AP_OPENING);
                setButtonTxt(R.string.loading);
//                checkoutArp();
                break;
        }
    }

    private void setHint(String hint) {
        ((TextView) findViewById(R.id.hint)).setText(hint);
    }

    private void showChooser() {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent();
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, getString(R.string.chooser_title));
        try {
            startActivityForResult(intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE:
                // If the file selection was successful
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        // Get the URI of the selected file
                        final Uri uri = data.getData();
                        Log.i(TAG, "Uri = " + uri.toString());
                        try {
                            generalQR(uri);
                        } catch (Exception e) {
                            Log.i(TAG, "File select error = " + e);
                        }
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void generalQR(Uri uri) throws IOException {
        // Get the file path from the URI
        final String path = FileUtils.getPath(this, uri);
        final String mineType = FileUtils.getMimeType(MainActivity.this, uri);
        Bitmap image;
        if (mineType.contains("image/")) {
            image = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
        } else if (mineType.contains("video/")) {
            image = getVideoThumbnail(path, 96, 96, MediaStore.Images.Thumbnails.MICRO_KIND);
        } else if (mineType.contains("audio/")) {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_audio);
        } else if (mineType.contains("text/")) {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_txt);
        } else if (mineType.contains("application/")) {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_apk);
        } else {
            image = BitmapFactory.decodeResource(getResources(), R.drawable.ic_txt);
        }

        if (image == null) {
            image = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        }

        setTitle(path);
        Bitmap bitmap = QRCode.createQRCodeWithLogo5(HOST + path, getResources().getDimensionPixelSize(R.dimen.qr_cell), image);
        ((ImageView) findViewById(R.id.qr)).setImageBitmap(bitmap);
    }

    /**
     * @param videoPath 视频路径
     * @param width     图片宽度
     * @param height    图片高度
     * @param kind      eg:MediaStore.Video.Thumbnails.MICRO_KIND   MINI_KIND: 512 x 384，MICRO_KIND: 96 x 96
     * @return
     */
    private Bitmap getVideoThumbnail(String videoPath, int width, int height, int kind) {
        // 获取视频的缩略图
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        //extractThumbnail 方法二次处理,以指定的大小提取居中的图片,获取最终我们想要的图片
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
        return bitmap;
    }

}
