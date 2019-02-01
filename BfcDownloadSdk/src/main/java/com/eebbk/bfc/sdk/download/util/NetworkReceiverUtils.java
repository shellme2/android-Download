package com.eebbk.bfc.sdk.download.util;

import android.content.ComponentName;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import com.eebbk.bfc.sdk.download.receiver.DownloadReceiver;

/**
 * Created by Simon on 2017/6/30.
 */

public class NetworkReceiverUtils {
    private final static String[] NEED_MONITOR_NET_CHANGE_PACKAGES = new String[]{
            "com.eebbk.syncenglish",
            "com.eebbk.synmath",
            "com.eebbk.synchinese",
            "com.eebbk.synstudy",
            "com.eebbk.ancientprose",
            "com.eebbk.bbkmiddlemarket",
            "com.eebbk.vtraining"
    };
    private static DownloadReceiver mDownloadReceiver;


    public static void registerOrEnableNetChangeComponent(Context context){
        if (isNeedMonitorNetChange(context)){
            enableReceiver(context);
        }else {
            registerNetChangeReceiver(context);
        }
    }

    public static void unregisterNetChangeComponent(Context context){
        unregisterNetChangeReceiver(context);
    }


    private static boolean isNeedMonitorNetChange(Context context) {
        String packageName = context.getPackageName();
        for (String needMonitorPackageName : NEED_MONITOR_NET_CHANGE_PACKAGES) {
            if (needMonitorPackageName.equals(packageName)) {
                return true;
            }
        }

        return false;
    }

    private static void enableReceiver(Context context) {
        ComponentName componentName = new ComponentName(context, DownloadReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
    }

    /**
     * 禁用当前app某个组件
     */
    private static void disableReceiver(Context context) {
        ComponentName componentName = new ComponentName(context, DownloadReceiver.class);
        PackageManager packageManager = context.getPackageManager();
        packageManager.setComponentEnabledSetting(componentName, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }


    private static void registerNetChangeReceiver(Context context) {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        if (mDownloadReceiver == null) {
            mDownloadReceiver = new DownloadReceiver();
        }
        context.registerReceiver(mDownloadReceiver, intentFilter);
    }

    private static void unregisterNetChangeReceiver(Context context) {
        if (mDownloadReceiver != null) {
            context.unregisterReceiver(mDownloadReceiver);
        }
        mDownloadReceiver = null;
    }
}
