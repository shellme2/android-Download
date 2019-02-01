package com.eebbk.bfc.sdk.download;

import android.content.Context;

import com.eebbk.bfc.sdk.download.db.DownloadProviderConfig;
import com.eebbk.bfc.sdk.download.exception.DownloadNoInitException;
import com.eebbk.bfc.sdk.download.util.LogUtil;

/**
 * Desc: 下载初始化帮助类
 * Author: llp
 * Create Time: 2016-10-07 16:45
 * Email: jacklulu29@gmail.com
 */

public class DownloadInitHelper {

    private GlobalConfig mConfig;
    private Context mContext;
    private String mModuleName;
    private DownloadProviderConfig sProviderConfig;

    private DownloadInitHelper() {
        // private construct
    }

    private static class SingleHolder {
        static final DownloadInitHelper sInstanc = new DownloadInitHelper();
    }

    public static DownloadInitHelper getInstance() {
        return SingleHolder.sInstanc;
    }

    private boolean mIsInit = false;


    private void checkInit() {
        if (!mIsInit) {
            throw new DownloadNoInitException();
        }
    }

    public void init(Context appContext, GlobalConfig config) {
        mIsInit = true;

        mContext = appContext.getApplicationContext();
        mModuleName = appContext.getPackageName();
        LogUtil.v(" init package name[" + mModuleName + "] host app id[" + mModuleName + "]");
        sProviderConfig = new DownloadProviderConfig(appContext);

        mConfig = config;
        LogUtil.d(" init config " + config);
    }


    public Context getAppContext() {
        checkInit();
        return mContext;
    }

    public GlobalConfig getGlobalConfig() {
        return mConfig;
    }

    public String getDefaultModuleName() {
        return mModuleName;
    }

    public DownloadProviderConfig getProviderConfig() {
        return sProviderConfig;
    }

    public void onDestroy() {
        mIsInit = false;

        mContext = null;
        mConfig = null;
        mModuleName = null;
        sProviderConfig = null;
    }

}
