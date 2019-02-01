package com.eebbk.bfc.sdk.download.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.eebbk.bfc.sdk.download.BfcDownload;
import com.eebbk.bfc.sdk.download.util.LogUtil;

/**
 * Desc: 网络变化广播监听
 * Author: llp
 * Create Time: 2016-11-06 15:33
 * Email: jacklulu29@gmail.com
 */

public class DownloadReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LogUtil.i("DownloadReceiver: receive network changed");

        final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = connManager.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            LogUtil.i("DownloadReceiver: network connected");
            BfcDownload download = BfcDownload.getImpl();
            if (download != null) {
                download.networkChanged();
            }
        }
    }

}
