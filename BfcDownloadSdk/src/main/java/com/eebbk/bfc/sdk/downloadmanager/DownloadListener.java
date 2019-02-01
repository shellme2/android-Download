package com.eebbk.bfc.sdk.downloadmanager;

import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.sdk.download.listener.SimpleDownloadListener;

/**
 * Desc: 事件监听
 * Author: llp
 * Create Time: 2016-10-25 15:49
 * Email: jacklulu29@gmail.com
 */

public class DownloadListener implements IDownloadListener {

    private int id;

    private OnDownloadListener mDownloadListener;
    private OnCheckListener mCheckListener;
    private OnUnpackListener mUnpackListener;

    private boolean mIsCanceled = false;

    public DownloadListener () {
        // do nothing
    }

    public DownloadListener (OnDownloadListener listener) {
        mDownloadListener = listener;
    }

    public DownloadListener (OnDownloadListener listener, OnCheckListener checkListener) {
        mDownloadListener = listener;
        mCheckListener = checkListener;
    }

    public DownloadListener (OnDownloadListener listener, OnCheckListener checkListener, OnUnpackListener unpackListener) {
        mDownloadListener = listener;
        mCheckListener = checkListener;
        mUnpackListener = unpackListener;
    }

    public DownloadListener (SimpleDownloadListener listener) {
        mDownloadListener = listener;
        mCheckListener = listener;
        mUnpackListener = listener;
    }

    @Override
    public boolean isEmpty() {
        return mDownloadListener == null
                && mCheckListener == null
                && mUnpackListener == null;
    }

    @Override
    public int getId() {
        return id;
    }

    public DownloadListener setId(int id) {
        this.id = id;
        return this;
    }

    @Override
    public DownloadListener setOnDownloadListener(OnDownloadListener listener) {
        this.mDownloadListener = listener;
        return this;
    }

    @Override
    public OnDownloadListener getOnDownloadListener() {
        return mDownloadListener;
    }

    @Override
    public DownloadListener setOnCheckListener(OnCheckListener listener) {
        this.mCheckListener = listener;
        return this;
    }

    @Override
    public OnCheckListener getOnCheckListener() {
        return mCheckListener;
    }

    @Override
    public DownloadListener setOnUnpackListener(OnUnpackListener listener) {
        this.mUnpackListener = listener;
        return this;
    }

    @Override
    public OnUnpackListener getOnUnpackListener() {
        return mUnpackListener;
    }

    /**
     * 任务是否被取消，被取消后将收不到回调
     *
     * @return true已取消，false没有取消
     */
    public boolean isCanceled() {
        return mIsCanceled;
    }

    @Override
    public synchronized IDownloadListener setCanceled(boolean canceled) {
        this.mIsCanceled = canceled;
        return this;
    }

}
