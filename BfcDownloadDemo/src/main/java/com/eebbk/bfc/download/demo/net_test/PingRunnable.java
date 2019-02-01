package com.eebbk.bfc.download.demo.net_test;

import android.util.Log;

import com.eebbk.bfc.common.devices.NetUtils;
import com.eebbk.bfc.common.tools.DateUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by Simon on 2017/6/5.
 */

public class PingRunnable implements Runnable {
    private static final String TAG = "PingRunnable";

    PingInfo mPingInfo;
    String mLogSavePath;

    public PingRunnable(PingInfo pingInfo, String logSavePath) {
        mPingInfo = pingInfo;
        mLogSavePath = logSavePath;
    }

    @Override
    public void run() {
        String url = mPingInfo.getUrl();
        int timeOut = mPingInfo.getTimeout();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        File file = new File(mLogSavePath);
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);
        } catch (IOException e) {
        }

        while (true) {
            boolean isConnect = NetUtils.ping(url, 3, timeOut);
            Log.i("ping", "isConnect: " + isConnect);


            try {
                fw.write(String.format("%d,%s,%s\n", System.currentTimeMillis(), DateUtils.getCurTimeString(simpleDateFormat), isConnect));
                fw.flush();
            } catch (IOException e) {
                Log.d(TAG, "写入文件出错");
            }


            try {
                Thread.sleep(mPingInfo.getIntervalTime() * 1000);
            } catch (InterruptedException e) {
            }
        }
    }
}
