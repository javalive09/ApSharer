package com.javalive09.apsharer.applist;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by peter on 16/7/12.
 */
public class Manager extends Application {

    private List<ApplicationInfo> list;

    public void init() {
        long start = System.currentTimeMillis();
        PackageManager packageManager = getPackageManager();
        List<ApplicationInfo> appList = packageManager.getInstalledApplications(0);
        list = new ArrayList<>(appList.size());
        String headPackageName = getHeadPackageName();
        for (ApplicationInfo info : appList) {
//            if (isUserApp(info)) {
            if (info.packageName.equals(headPackageName)) {
                list.add(0, info);
            } else {
                list.add(info);
            }
//            }
        }
//        Collections.sort(list, new ApplicationInfo.DisplayNameComparator(packageManager));
        Log.i("peter", "time = " + (System.currentTimeMillis() - start));
    }

    private String getHeadPackageName() {
        return getSharedPreferences("head_item", MODE_PRIVATE).getString("package_name", "");
    }

    public List<ApplicationInfo> getData() {
        return list;
    }

    boolean isUserApp(ApplicationInfo info) {
        int mask = ApplicationInfo.FLAG_SYSTEM | ApplicationInfo.FLAG_UPDATED_SYSTEM_APP;
        return (info.flags & mask) == 0;
    }

    public static class DisplayNameComparator implements Comparator<ApplicationInfo> {

        private final Collator sCollator = Collator.getInstance();
        private PackageManager mPM;

        public DisplayNameComparator(PackageManager pm) {
            mPM = pm;
        }

        public final int compare(ApplicationInfo aa, ApplicationInfo ab) {
            CharSequence sa = mPM.getApplicationLabel(aa);
            if (sa == null) {
                sa = aa.packageName;
            }
            CharSequence sb = mPM.getApplicationLabel(ab);
            if (sb == null) {
                sb = ab.packageName;
            }
            return sCollator.compare(sa.toString(), sb.toString());
        }

    }

}