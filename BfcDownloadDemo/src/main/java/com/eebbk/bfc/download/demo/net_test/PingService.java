package com.eebbk.bfc.download.demo.net_test;

import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.eebbk.bfc.common.file.FileUtils;
import com.eebbk.bfc.download.demo.util.ToastUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.eebbk.bfc.sdk.download.thread.DownloadBaseRunnable.WAKE_LOCK_HELD_TIME;

public class PingService extends Service {
    private static final String TAG = "PingService";


    private static final int NOTIFICATION_ID = 56845;
    private static final String EXTRA_ACTION_KAY = "ACTION";
    private static final String EXTRA_PING_KAY = "PING_INFO";
    private static final int EXTRA_ACTION_STOP = 1;
    private static final int EXTRA_ACTION_PING = 2;

    List<Thread> mPingThreadList = new ArrayList<>(4);
    private final static String WAKE_LOCK_TAG = "com.eebbk.wakelock.IDLE_ALLOW.ping";
    private PowerManager.WakeLock mWakeLock;


    public static void startPing(Context context, ArrayList<PingInfo> pingInfos) {
        Intent intent = new Intent(context, PingService.class);
        intent.putExtra(EXTRA_ACTION_KAY, EXTRA_ACTION_PING);
        intent.putExtra(EXTRA_PING_KAY, pingInfos);
        context.startService(intent);
    }

    public static void stopServices(Context context) {
        Intent intent = new Intent(context, PingService.class);
        intent.putExtra(EXTRA_ACTION_KAY, EXTRA_ACTION_STOP);
        context.stopService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.acquire(WAKE_LOCK_HELD_TIME);
//        increasePriority();
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent,  int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        if (intent == null) {
            return START_NOT_STICKY;
        }

        switch (intent.getIntExtra(EXTRA_ACTION_KAY, -1)) {
            case -1:
                stopServices(this);
                ToastUtil.showToast(this, "shutdown ping services");
                break;
            case EXTRA_ACTION_STOP:
                stopServices(this);
                break;
            case EXTRA_ACTION_PING:
                ArrayList<PingInfo> pingInfos = intent.getParcelableArrayListExtra(EXTRA_PING_KAY);
                ping(pingInfos);
                break;
        }

        return START_NOT_STICKY;
    }


    private void ping(List<PingInfo> pingInfos) {
        stopPing();

        for (PingInfo pingInfo : pingInfos){
            File file = new File(Environment.getExternalStorageDirectory(), pingInfo.getUrl() + ".csv");
            FileUtils.createFileByDeleteOld(file);
            Log.i(TAG, "file save path: " + file.getAbsolutePath());
            Thread thread = new Thread(new PingRunnable(pingInfo, file.getPath()));
            thread.start();
            mPingThreadList.add(thread);
        }

        ToastUtil.showLongToast(this, "开始ping");
    }

    private void stopPing(){
        for (Thread thread: mPingThreadList){
            if (thread.isAlive()){
                thread.interrupt();
            }
        }
        mPingThreadList.clear();
    }

    @Override
    public void onDestroy() {
        stopPing();
        if (mWakeLock.isHeld()){
            mWakeLock.release();
        }
        super.onDestroy();
    }

    private void increasePriority() {
        try {
            Notification notification = new Notification();
            if (Build.VERSION.SDK_INT < 18) {
                startForeground(NOTIFICATION_ID, notification);
            } else {
                startForeground(NOTIFICATION_ID, notification);
                startService(new Intent(this, InnerService.class));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class InnerService extends Service {
        @Override
        public void onCreate() {
            super.onCreate();
            startForeground(NOTIFICATION_ID, new Notification());
            stopServices(this);
        }

        @Nullable
        @Override
        public IBinder onBind(Intent intent) {
            return null;
        }

        @Override
        public void onDestroy() {
            stopForeground(true);
            super.onDestroy();
        }
    }
}
