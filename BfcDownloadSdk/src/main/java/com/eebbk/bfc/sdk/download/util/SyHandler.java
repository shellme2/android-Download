package com.eebbk.bfc.sdk.download.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Desc: 自定义Handler，解决Handler在某些情况下空指针造成崩溃
 * Author: llp
 * Create Time: 2016-04-20 11:18
 * Email: jacklulu29@gmail.com
 */
public class SyHandler extends Handler {
	
	/**
     * Default constructor associates this handler with the {@link Looper} for the
     * current thread.
     *
     * If this thread does not have a looper, this handler won't be able to receive messages
     * so an exception is thrown.
     */
    public SyHandler() {
    	super();
    }

    /**
     * Constructor associates this handler with the {@link Looper} for the
     * current thread and takes a callback interface in which you can handle
     * messages.
     *
     * If this thread does not have a looper, this handler won't be able to receive messages
     * so an exception is thrown.
     *
     * @param callback The callback interface in which to handle messages, or null.
     */
    public SyHandler(Callback callback) {
    	super(callback);
    }

    /**
     * Use the provided {@link Looper} instead of the default one.
     *
     * @param looper The looper, must not be null.
     */
    public SyHandler(Looper looper) {
    	super(looper);
    }

    /**
     * Use the provided {@link Looper} instead of the default one and take a callback
     * interface in which to handle messages.
     *
     * @param looper The looper, must not be null.
     * @param callback The callback interface in which to handle messages, or null.
     */
    public SyHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    @Override
    public void dispatchMessage(Message msg) {
        try {
            super.dispatchMessage(msg);
        } catch (Exception e) {
            LogUtil.e(e, " dispatchMessage error: ");
        }
    }

    @Override
    public void handleMessage(Message msg) {
        try {
            super.handleMessage(msg);
        } catch (Exception e) {
        	LogUtil.e(e, " handleMessage error: ");
        }
    }
}
