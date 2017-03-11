package com.javalive09.apsharer;

import android.app.job.JobParameters;
import android.app.job.JobService;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * Created by peter on 2017/3/10.
 */

public class ArpService extends JobService {

    private static final String ARP = "/proc/net/arp";

    @Override
    public boolean onStartJob(JobParameters params) {
        boolean result = arpNotEmpty();
        SharedPreferencesUtil.setHaveClient(getApplicationContext(), result);
        jobFinished(params, true);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return true;
    }

    private boolean arpNotEmpty() {
        int lineCount = 0;
        try {
            BufferedReader localBufferdReader = new BufferedReader(new FileReader(new File(ARP)));

            while (localBufferdReader.readLine() != null) {
                lineCount++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return lineCount > 1;
    }

}
