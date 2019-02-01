package com.eebbk.bfc.sdk.download.thread;

import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.HashMap;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Simon on 2017/6/7.
 */

public class DownloadThreadPoolExecutor extends ThreadPoolExecutor {

    private HashMap<Integer, DownloadBaseRunnable> mRunnablePool = new HashMap<>();

    public DownloadThreadPoolExecutor(){
        super(0, Integer.MAX_VALUE,60L, TimeUnit.SECONDS,new SynchronousQueue<Runnable>());
    }

    public DownloadThreadPoolExecutor(DownloadWorkCallback downloadWorkCallback){
        this();
        this.mWorkCallback = downloadWorkCallback;
    }

    public void execute(final int taskId, final DownloadRunnable runnable) {
        if(mRunnablePool.get(taskId) != null){
            LogUtil.w(" already has runnable["+mRunnablePool.get(taskId).getRunnableId()+"] in the pool! ");
        }
        mRunnablePool.put(taskId, runnable);
        LogUtil.d(" add runnable["+runnable.getRunnableId()+"] to the pool! ");
        this.execute(runnable);
        LogUtil.d(" execute runnableId["+runnable.getRunnableId()+"]  runnable["+mRunnablePool.get(taskId)+"]! ");
    }

    public boolean isInThreadPool(final int taskId){
        DownloadBaseRunnable runnable = mRunnablePool.get(taskId);
        boolean isValid = runnable != null && runnable.isValid();
        if(LogUtil.isDebug()){
            LogUtil.d(" runnableId: ["+(runnable==null?taskId:runnable.getRunnableId())+"] isValid: " + isValid);
        }
        return isValid;
    }

    public void cancel(final int taskId){
        DownloadBaseRunnable r = mRunnablePool.get(taskId);
        if (r != null) {
            r.cancelRunnable();
            LogUtil.i("cancel runnable =" + taskId + " ");
        }
    }

    public void cancelNoNotify(final int taskId){
        DownloadBaseRunnable r = mRunnablePool.get(taskId);
        if (r != null) {
            r.cancelRunnableNoNotify();
            LogUtil.i("cancel no notify  runnable =" + taskId + " ");
        }
    }

    public DownloadBaseRunnable getRunnable(int taskId){
        return mRunnablePool.get(taskId);
    }

    public void remove(int taskId) {
        DownloadBaseRunnable r = mRunnablePool.get(taskId);
        if (r != null) {
            mRunnablePool.remove(taskId);
            this.remove(r);
            LogUtil.i(" remove task[" + r.getRunnableId() + "] from runnable thread pool");
        } else {
            LogUtil.i(" remove task[" + taskId + "], but not found runnable");
        }
    }


    @Override
    protected void beforeExecute(Thread t, Runnable r) {
        super.beforeExecute(t, r);
        if (mWorkCallback != null){
            mWorkCallback.beforeDownloadWork();
        }
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        super.afterExecute(r, t);
        if (mWorkCallback != null){
            mWorkCallback.afterDownloadWork();
        }
    }


    @Override
    protected void terminated() {
        super.terminated();
    }

    private DownloadWorkCallback mWorkCallback;
    public interface DownloadWorkCallback{
        void beforeDownloadWork();
        void afterDownloadWork();
    }
}
