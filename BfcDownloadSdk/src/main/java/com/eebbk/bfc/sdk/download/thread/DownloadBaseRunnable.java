package com.eebbk.bfc.sdk.download.thread;

import android.content.Context;
import android.os.PowerManager;
import android.os.Process;

import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.exception.DownloadNoInitException;
import com.eebbk.bfc.sdk.download.util.LogUtil;

/**
 * Desc: 下载基础Runnable
 * Author: llp
 * Create Time: 2016-10-10 8:37
 * Email: jacklulu29@gmail.com
 */

public abstract class DownloadBaseRunnable implements IDownloadRunnable {
    // 系统要求使用"com.eebbk.wakelock.IDLE_ALLOW"的tag, 防止app被杀
    public final static String WAKE_LOCK_TAG = "com.eebbk.wakelock.IDLE_ALLOW.downloadServiceWakeLock";
    public final static long WAKE_LOCK_HELD_TIME = 11 * 60 * 60 * 1000;   // Services持有的最长时间, 防止未知异常造成Services长时间无法退出
    private PowerManager.WakeLock mWakeLock;

    protected int mTaskId;
    protected String mRunnableId;

    protected volatile boolean mIsCanceled = false;
    protected volatile boolean mIsFinished = false;

    private OnRunnableFinishListener mFinishListener;

    public DownloadBaseRunnable(OnRunnableFinishListener finishListener, String runnableId, int taskId){
        mIsCanceled = false;
        mIsFinished = false;
        mFinishListener = finishListener;

        mRunnableId = runnableId;
        mTaskId = taskId;
    }

    @Override
    public int getTaskId() {
        return mTaskId;
    }

    @Override
    public String getRunnableId(){
        return mRunnableId;
    }

    @Override
    public boolean isValid(){
        if(LogUtil.isDebug()){
            LogUtil.d(" canceled: " + mIsCanceled + " finished: " + mIsFinished);
        }
        return !mIsCanceled && !mIsFinished;
    }

    @Override
    public void cancelRunnable(){
        this.mIsCanceled = true;
        LogUtil.d(" set cancel runnableID["+mRunnableId+"] true! ");
        onCancel();
        runnableFinished(true);
    }

    public void cancelRunnableNoNotify(){
        this.mIsCanceled = true;
        LogUtil.d(" set cancel runnableID["+mRunnableId+"] true! ");
        runnableFinished(false);
    }

    @Override
    public boolean isCanceled() {
        return mIsCanceled;
    }

    @Override
    public void run() {
        try {
            //持有WakeLock锁，防止被系统强杀（系统识别到持有该锁的应用不会被杀），频繁持有和释放锁不会造成性能问题
            initWakeLock();
            onStart();
            onDownload();
        } finally {
            runnableFinished(true);
        }
    }

    /**
     * 任务开始，调用setThreadPriority设置为THREAD_PRIORITY_BACKGROUND
     */
    public void onStart(){
        Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
    }

    /**
     * 用户取消时调用
     */
    public abstract void onCancel();

    /**
     * 下载执行时调用
     */
    public abstract void onDownload();

    /**
     * 执行结束时调用
     */
    public abstract void onFinish();

    public interface OnRunnableFinishListener {
        void onRunnableFinished(DownloadBaseRunnable baseRunnable);
    }

    private void runnableFinished(boolean notify){
        if(this.mIsFinished){
            return;
        }
        this.mIsFinished = true;
        onFinish();
        releaseWakeLock();
        if (notify && mFinishListener != null) {
            mFinishListener.onRunnableFinished(this);
        }
        mFinishListener = null;
    }

    private void initWakeLock() {
        LogUtil.i("WakeLock-initWakeLock()");
        Context context = DownloadInitHelper.getInstance().getAppContext();
        if (context == null) {
            throw new DownloadNoInitException();
        }
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.setReferenceCounted(false);
        mWakeLock.acquire(WAKE_LOCK_HELD_TIME);// wakeLock已经取消引用计数, 可以多次获取,最后一次释放
    }

    private void releaseWakeLock() {
        if (mWakeLock != null && mWakeLock.isHeld()) {
            mWakeLock.release();
            LogUtil.i("WakeLock-releaseWakeLock");
        }
    }
}
