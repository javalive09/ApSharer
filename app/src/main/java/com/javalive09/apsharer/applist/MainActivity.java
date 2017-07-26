package com.javalive09.apsharer.applist;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.javalive09.apsharer.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * app manager 控制类
 * <p>
 * Created by peter on 2017/3/20.
 */

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private AppAdapter defaultAdapter;
    private AppGridAdapter appGridAdapter;
    private ApplicationInfo clickInfo;
    private RecyclerView recyclerView;
    private static final int NO_SYS = 0;
    private static final int ALL = 1;
    private static final int LIST = 0;
    private static final int GRID = 1;
    private DividerItemDecoration decoration;
    private int showType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_app);
        recyclerView = (RecyclerView) findViewById(R.id.app_list);
        if (recyclerView != null) {
            recyclerView.setItemAnimator(new DefaultItemAnimator());
            appGridAdapter = new AppGridAdapter(MainActivity.this);
            decoration = new DividerItemDecoration(
                    MainActivity.this, DividerItemDecoration.VERTICAL_LIST);
            setDefaultLayout(getDefaultLayout());
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshData(getShowType());
    }

    private String getHeadPackageName() {
        return getSharedPreferences("head_item", MODE_PRIVATE).getString("package_name", "");
    }

    private void refreshData(final int type) {
        setShowType(type);
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> appList = packageManager.getInstalledApplications(0);
        ArrayList<ApplicationInfo> list = new ArrayList<>(appList.size());
        String headPackageName = getHeadPackageName();
        for (ApplicationInfo info : appList) {
            if (info != null && !isSystemApp(info) && !info.packageName.equals(getPackageName())) {
                if (info.packageName.equals(headPackageName)) {
                    list.add(0, info);
                } else {
                    list.add(info);
                }
            }
        }
        appGridAdapter.updataData(list);
        setTitle("AppManager(" + list.size() + ")");
    }

    private boolean isSystemApp(ApplicationInfo appInfo) {
        if (showType == ALL) {
            return false;
        }
        if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) > 0) {// system apps
            return true;
        } else {
            return false;
        }
    }

    private int getShowType() {
        int type = getSharedPreferences("showType", MODE_PRIVATE).getInt("showType", NO_SYS);
        return type;
    }

    private void setShowType(int type) {
        showType = type;
        getSharedPreferences("showType", MODE_PRIVATE).edit().putInt("showType", type).apply();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_app, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                refreshData(showType);
                break;
            case R.id.action_about:
                showAlertDialog(getString(R.string.action_about),
                        getString(R.string.action_about_txt));
                break;

            case R.id.action_feedback:
                sendMailByIntent();
                break;
            case R.id.action_all_app:
                refreshData(ALL);
                break;
            case R.id.action_third_app:
                refreshData(NO_SYS);
                break;
            case R.id.action_list:
                if (getDefaultLayout() != LIST) {
                    setDefaultLayout(LIST);
                }
                break;
            case R.id.action_grid:
                if (getDefaultLayout() != GRID) {
                    setDefaultLayout(GRID);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private int getDefaultLayout() {
        int type = getSharedPreferences("layout", MODE_PRIVATE).getInt("layout", 0);
        return type;
    }

    private AppAdapter setDefaultAdapter(int layout) {
        switch (layout) {
            case 0:
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));
                recyclerView.addItemDecoration(decoration);
                defaultAdapter = appGridAdapter;
                break;
            case 1:
                int itemW = getResources().getDimensionPixelSize(R.dimen.item_cell);
                Resources resources = MainActivity.this.getResources();
                DisplayMetrics dm = resources.getDisplayMetrics();
                int width = dm.widthPixels;
                int count = width / itemW;
                recyclerView.removeItemDecoration(decoration);
                recyclerView.setLayoutManager(new GridLayoutManager(MainActivity.this, count));
                defaultAdapter = appGridAdapter;
                break;
        }
        recyclerView.setAdapter(defaultAdapter);
        return defaultAdapter;
    }

    private void setDefaultLayout(int layout) {
        getSharedPreferences("layout", MODE_PRIVATE).edit().putInt("layout", layout).apply();
        setDefaultAdapter(layout);
    }

    public void sendMailByIntent() {
        Intent data = new Intent(Intent.ACTION_SENDTO);
        data.setData(Uri.parse(getString(R.string.setting_feedback_address)));
        data.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.setting_feedback));
        data.putExtra(Intent.EXTRA_TEXT, getString(R.string.setting_feedback_body));
        startActivity(data);
    }

    public AlertDialog showAlertDialog(String title, String content) {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this).create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.setTitle(title);
        dialog.setMessage(content);
        dialog.show();
        return dialog;
    }

    @Override
    public void onClick(View v) {
        popupMenuNormal(v);
        final ApplicationInfo info = (ApplicationInfo) v.getTag(R.id.appinfo);
        String path = "file:/" + info.publicSourceDir;
        Intent intent = new Intent();
        Uri uri = Uri.parse(path);
        intent.setData(uri);
        setResult(RESULT_OK, intent);
        finish();
    }

    private void addClickCount(ApplicationInfo info) {
        if (info != null) {
            SharedPreferences headSp = getSharedPreferences("head_item", MODE_PRIVATE);
            headSp.edit().putString("package_name", info.packageName).commit();
        }
    }

    @Override
    protected void onDestroy() {
        addClickCount(clickInfo);
        super.onDestroy();
    }

    private void showDetailStopView(ApplicationInfo info) {
        if (info != null) {
            int version = Build.VERSION.SDK_INT;
            Intent intent = new Intent();
            if (version >= 9) {
                intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
                Uri uri = Uri.fromParts("package", info.packageName, null);
                intent.setData(uri);
            } else {
                final String appPkgName = "pkg";
                intent.setAction(Intent.ACTION_VIEW);
                intent.setClassName("com.android.settings",
                        "com.android.settings.InstalledAppDetails");
                intent.putExtra(appPkgName, info.packageName);
            }
            startActivity(intent);
        }
    }

    private void uninstallAPP(ApplicationInfo info) {
        if (info != null) {
            Uri uri = Uri.parse("package:" + info.packageName);
            Intent intent = new Intent(Intent.ACTION_DELETE);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    private Intent getLaunchIntent(ApplicationInfo info) {
        return getPackageManager().getLaunchIntentForPackage(
                info.packageName);
    }

    private void popupMenuNormal(final View anchor) {
        final ApplicationInfo info = (ApplicationInfo) anchor.getTag(R.id.appinfo);
        PopupMenu popup = new PopupMenu(this, anchor);
        final Intent launchIntent = getLaunchIntent(info);
        if (launchIntent != null) {
            popup.getMenuInflater().inflate(R.menu.operate1, popup.getMenu());
        } else {
            popup.getMenuInflater().inflate(R.menu.operate, popup.getMenu());
        }
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_launch:
                        startActivity(launchIntent);
                        break;
                    case R.id.action_detail:
                        showDetailStopView(info);
                        break;
                    case R.id.action_uninstall:
                        uninstallAPP(info);
                        break;
                }
                clickInfo = info;
                return true;
            }
        });
        popup.show();
    }


}
