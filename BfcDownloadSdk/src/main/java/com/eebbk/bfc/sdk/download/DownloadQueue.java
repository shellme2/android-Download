package com.eebbk.bfc.sdk.download;

import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Desc: 下载队列管理
 * Author: llp
 * Create Time: 2016-10-08 10:56
 * Email: jacklulu29@gmail.com
 */

public class DownloadQueue<T extends DownloadQueue.DownloadTask> {

    /**
     * Used for generating monotonically-increasing sequence numbers for requests.
     */
    private AtomicInteger mSequenceGenerator = new AtomicInteger();
    private static AtomicInteger sRunQueueCount = new AtomicInteger(0);//线程安全的计数变量,正在运行的任务数量（需要排队的）
    private static AtomicInteger sRunNoQueueCount = new AtomicInteger(0);//线程安全的计数变量，正在运行的任务数量（不需要排队的）

    /**
     * 所有下载任务队列
     */
    private final ConcurrentMap<Integer, T> mAllQueue = new ConcurrentHashMap<>();
    /**
     * <pre>等待网络队列
     * 任务执行过后，发现网络不符合要求，下载任务将会加入此队列
     * 当网络发生变化时，符合当前网络的任务将进入等待执行队列{@link #mWaitQueue}等待执行
     * </pre>
     */
    private final List<T> mWaitNetworkQueue = Collections.synchronizedList( new ArrayList<T>());

    /**
     * 等待执行队列
     */
    private final PriorityBlockingQueue<T> mWaitQueue = new PriorityBlockingQueue<>();

    private ITaskRunner<T> mRunner;
    /**
     * 最大运行任务数量（需要排队的）
     */
    private final int mMaxRunQueueTask;
    /**
     * 最大运行任务数量（不需要排队的）
     */
    private final int mMaxRunNoQueueTask;

    /**
     * Creates the worker pool. Processing will not begin until  is called.
     *
     */
    public DownloadQueue(ITaskRunner<T> runner, int maxRunQueueTask, int maxRunNoQueueTask) {
        this.mMaxRunQueueTask = maxRunQueueTask;
        this.mMaxRunNoQueueTask = maxRunNoQueueTask;
        this.mRunner = runner;
    }

    /**
     * 根据generateId查找任务信息
     * @param generateId 任务id
     * @return 任务
     */
    public T findTaskInfo(int generateId){
        return mAllQueue.get(generateId);
    }

    /**
     * Gets a sequence number.
     */
    public int getSequenceNumber() {
        return mSequenceGenerator.incrementAndGet();
    }

    /**
     * Adds a Request to the dispatch queue.
     *
     * @param id 任务id
     * @param downloadTask 任务
     * @return The passed-in request
     */
    public synchronized T add(final int id, final T downloadTask) {
        // Tag the request as belonging to this queue and add it to the set of current requests.
        mAllQueue.put(id, downloadTask);

        // Process requests in the order they are added.
        downloadTask.setSequence(getSequenceNumber());

        if(!mWaitQueue.contains(downloadTask)){
            mWaitQueue.add(downloadTask);
            LogUtil.i(" add task["+id+"] to wait queue ");
        } else {
            LogUtil.i(" task["+id+"] already in wait queue ");
        }

        scheduleNext();
        return downloadTask;
    }

    public void remove(final T downloadTask){
        if(mAllQueue.containsKey(downloadTask.getGenerateId())){
            mAllQueue.remove(downloadTask.getGenerateId());
            LogUtil.i(" remove "+ downloadTask.getGenerateId()+" from all queue ");
        }

        synchronized (mWaitNetworkQueue){
            if(mWaitNetworkQueue.contains(downloadTask)){
                mWaitNetworkQueue.remove(downloadTask);
                LogUtil.i(" remove "+ downloadTask.getGenerateId()+" from wait network queue ");
            }
        }
        if(mWaitQueue.contains(downloadTask)){
            mWaitQueue.remove(downloadTask);
            LogUtil.i(" remove "+ downloadTask.getGenerateId()+" from wait queue ");
        }
    }

    /**
     * <pre>移动到等待网络队列
     * 某些任务执行后发现网络不符合要求，无法继续进行，暂时移动到等待网络队列，等待网络符合要求然后再进行下载
     * </pre>
     * @param downloadTask 任务
     */
    public void moveToWaitNetworkQueue(final T downloadTask){
        if(mWaitQueue.contains(downloadTask)){
            mWaitQueue.remove(downloadTask);
            LogUtil.i(" remove "+ downloadTask.getGenerateId()+" from wait queue, after add to NetWaitQueue ");
        }
        synchronized (mWaitNetworkQueue){
            mWaitNetworkQueue.add(downloadTask);
        }
    }

    public void removeFromWaitNetworkQueue(final T downloadRequestTask){
        synchronized (mWaitNetworkQueue){
            mWaitNetworkQueue.remove(downloadRequestTask);
        }
    }

    public List<T> getWaitNetworkQueue(){
        return mWaitNetworkQueue;
    }

    public boolean isInWaitNetworkQueue(final T task){
        synchronized (mWaitNetworkQueue){
            return mWaitNetworkQueue.contains(task);
        }
    }

    public Collection<T> getAllQueue(){
        return mAllQueue.values();
    }

    /**
     * 下载任务执行完毕回调方法
     *
     * @param generateId 任务id
     * @param removeFromAll 是否要把任务从所有队列中移除（某些情况下，比如等待网络连接，任务会暂时存放在等待网路队列，此时不应该移除）
     */
    public void taskFinished(final int generateId, final boolean needQueue, final boolean removeFromAll){
        if(mAllQueue.containsKey(generateId)){
            if(removeFromAll){
                mAllQueue.remove(generateId);
            }
        } else {
            LogUtil.w(" task ["+generateId+"] finished, but no found in the queue!");
        }

        if(needQueue){
            sRunQueueCount.decrementAndGet();
        } else {
            sRunNoQueueCount.decrementAndGet();
        }
        LogUtil.i(DownloadUtils.formatString(
                " task [%s] needQueue[%s] finished, (queueCount[%s] MAX[%s]) (noQueueCount[%s] MAX[%s])",
                generateId,
                needQueue,
                sRunQueueCount,
                mMaxRunQueueTask,
                sRunNoQueueCount,
                mMaxRunNoQueueTask));
        // 抽取下一轮任务
        scheduleNext();
    }

    /**
     * 开始下一轮任务
     */
    public synchronized void scheduleNext(){
        LogUtil.i(DownloadUtils.formatString(
                " schedule next now (queueCount[%s] Max[%s]) , (noQueueCount[%s],Max[%s])",
                sRunQueueCount.get(),
                mMaxRunQueueTask,
                sRunNoQueueCount,
                mMaxRunNoQueueTask));

        if (sRunQueueCount.get() > mMaxRunQueueTask && sRunNoQueueCount.get() > mMaxRunNoQueueTask) {
            LogUtil.i(" schedule next running task is full[" + mMaxRunQueueTask + ", " + mMaxRunNoQueueTask + "]");
            return;
        }
        List<T> tempList = new ArrayList<>();
        while (sRunQueueCount.get() < mMaxRunQueueTask || sRunNoQueueCount.get() < mMaxRunNoQueueTask) {
            T downloadRequestTask = mWaitQueue.poll();
            if (downloadRequestTask == null) {
                LogUtil.d(" schedule next task is null");
                break;
            }
            // need queue task
            if (downloadRequestTask.isNeedQueue()) {
                if (sRunQueueCount.get() >= mMaxRunQueueTask) {
                    tempList.add(downloadRequestTask);
                    downloadRequestTask = null;
                    continue;
                }
            }
            // no need queue task
            else {
                if (sRunNoQueueCount.get() >= mMaxRunNoQueueTask) {
                    tempList.add(downloadRequestTask);
                    downloadRequestTask = null;
                    continue;
                }
            }

            if (downloadRequestTask.isNeedQueue()) {
                sRunQueueCount.incrementAndGet();
            } else {
                sRunNoQueueCount.incrementAndGet();
            }
            LogUtil.i(DownloadUtils.formatString(
                    " schedule next task[%s] needQueue[%s], (queueCount[%s] Max[%s]) , (noQueueCount[%s],Max[%s])",
                    downloadRequestTask.getGenerateId(),
                    downloadRequestTask.isNeedQueue(),
                    sRunQueueCount.get(),
                    mMaxRunQueueTask,
                    sRunNoQueueCount,
                    mMaxRunNoQueueTask));
            mRunner.downloadQueueRunTask(downloadRequestTask);
        }

        if (!tempList.isEmpty()) {
            for (T t : tempList) {
                mWaitQueue.add(t);
            }
        }

        if (mWaitQueue.isEmpty() && isIdle()) {
            mRunner.downloadQueueIdle();
        }
    }

    /**
     * 停止队列
     */
    public void stop(){
        mAllQueue.clear();
        mWaitQueue.clear();
        mWaitNetworkQueue.clear();
        LogUtil.d(" clear cache queue! ");
    }

    /**
     * 是否空闲
     * @return true空闲，false否
     */
    public boolean isIdle(){
        return sRunQueueCount.get() <= 0
                && sRunNoQueueCount.get() <= 0
                //&& mWaitNetworkQueue.size() <= 0
                ;
    }


    public interface ITaskRunner<T extends DownloadQueue.DownloadTask> {
        /**
         * 从等待队列抽取出来，开始运行任务
         * @param task 任务
         */
        void downloadQueueRunTask(T task);

        /**
         * 空闲
         */
        void downloadQueueIdle();
    }

    public static abstract class DownloadTask {
        /** Sequence number of this request, used to enforce FIFO ordering. */
        private Integer mSequence;

        public abstract int getGenerateId();

        public void setSequence(Integer sequence) {
            this.mSequence = sequence;
        }

        public Integer getSequence() {
            return mSequence;
        }

        public abstract boolean isNeedQueue();
    }
}
