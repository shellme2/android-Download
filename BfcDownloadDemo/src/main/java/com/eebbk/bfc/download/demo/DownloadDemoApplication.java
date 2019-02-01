package com.eebbk.bfc.download.demo;

import android.Manifest;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.ContextCompat;

import com.eebbk.bfc.download.demo.baseui.AppContext;
import com.eebbk.bfc.download.demo.basic.monitor.DownloadInfoMonitor;
import com.eebbk.bfc.download.demo.util.CrashHandler;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.sdk.behavior.BehaviorCollector;
import com.eebbk.bfc.sdk.download.C;
import com.eebbk.bfc.sdk.download.GlobalConfig;
import com.eebbk.bfc.sdk.download.listener.OnDownloadConnectListener;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.github.moduth.blockcanary.BlockCanary;
import com.squareup.leakcanary.LeakCanary;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-21 16:00
 * Email: jacklulu29@gmail.com
 */

public class DownloadDemoApplication extends Application {

    private static final boolean DEVELOPER_MODE = false;
    public static final boolean SISDEBUG = true;
    private String mSaveLogPath = null;
    private static DownloadDemoApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        CrashHandler.getInstance().init(this);
        initLeakCanary();
        initStrictMode();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            L.e(" no permission granted ");
        } else {
            mSaveLogPath = DemoUtil.getSaveLogPath();
            L.setLog(L.buildLog(SISDEBUG, mSaveLogPath), SISDEBUG);
            L.e(" init save log success " + mSaveLogPath);
        }

        BlockCanary.install(this, new AppContext()).start();
        initDownload();

        BehaviorCollector.getInstance().init(new BehaviorCollector.Builder(this)
            .enable(true)
            .enableCrash(true)
            .enableReport(true)
            .build());
        DownloadInfoMonitor.updateInfoFromSP();
    }

    private void initStrictMode() {
        if (DEVELOPER_MODE) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()   // or .detectAll() for all detectable problems
                .penaltyLog()
                .build());

            StrictMode.setVmPolicy(new
                StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
        }
    }

    private void initLeakCanary() {
        if (LeakCanary.isInAnalyzerProcess(this)) {
            return;
        }
        LeakCanary.install(this);
    }

    public static DownloadDemoApplication getAppContext() {
        return instance;
    }

    public void initDownload() {
        //DownloadController.init(this.getApplicationContext());

        GlobalConfig config = new GlobalConfig.Builder()
            .setDebug(false)
            .setSaveLogPath(mSaveLogPath)
            .setSavePath(this.getCacheDir().getAbsolutePath())
            .setDownloadMode(C.DownloadMode.LOW)
            .build();
        DownloadController.init(this.getApplicationContext(), config);
        DownloadController.getInstance().registerConnectionListener(new OnDownloadConnectListener() {
            @Override
            public void onConnected() {
                L.i("onConnected");
            }

            @Override
            public void onDisconnected() {
                L.i("onDisconnected");
            }
        });
        //DownloadController.getInstance().startService();

        // 添加自定义校验器
        /*GlobalConfig config = new GlobalConfig.Builder()
                // 添加自定义校验器，可以添加多个
                .addValidator(new MyValidator.MyValidatorCreator());
        // 初始化
        DownloadController.init(this.getApplicationContext(), config);*/

        // 添加自定义解压器
        /*GlobalConfig config = new GlobalConfig.Builder()
                // 添加自定义校验器，可以添加多个
                .setUnpacker(new DownloadUnpacker.Creator());
        // 初始化
        DownloadController.init(this.getApplicationContext(), config);*/
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }
}
