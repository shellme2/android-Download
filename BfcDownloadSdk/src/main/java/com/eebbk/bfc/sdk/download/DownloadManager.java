/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eebbk.bfc.sdk.download;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.NetworkInfo;
import android.os.PowerManager;
import android.text.TextUtils;

import com.eebbk.bfc.common.app.SharedPreferenceUtils;
import com.eebbk.bfc.sdk.behavior.utils.ListUtils;
import com.eebbk.bfc.sdk.download.check.BaseValidator;
import com.eebbk.bfc.sdk.download.check.IValidator;
import com.eebbk.bfc.sdk.download.db.DatabaseHelper;
import com.eebbk.bfc.sdk.download.db.DatabaseModeImpl;
import com.eebbk.bfc.sdk.download.db.DownloadDbManager;
import com.eebbk.bfc.sdk.download.db.DownloadTaskColumns;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.message.DownloadBaseMessage;
import com.eebbk.bfc.sdk.download.message.IMessageReceiver;
import com.eebbk.bfc.sdk.download.message.MessageFactory;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.thread.DownloadBaseRunnable;
import com.eebbk.bfc.sdk.download.thread.DownloadRunnable;
import com.eebbk.bfc.sdk.download.thread.DownloadThreadPoolExecutor;
import com.eebbk.bfc.sdk.download.unpack.IDownloadUnpacker;
import com.eebbk.bfc.sdk.download.util.CloseableUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.download.util.NetworkUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;
import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.eebbk.bfc.sdk.download.thread.DownloadBaseRunnable.WAKE_LOCK_HELD_TIME;
import static com.eebbk.bfc.sdk.download.thread.DownloadBaseRunnable.WAKE_LOCK_TAG;

/**
 * The download manager is a system service that handles long-running HTTP downloads. Clients may
 * request that a URL be downloaded to a particular destination file. The download manager will
 * conduct the download in the background, taking care of HTTP interactions and retrying downloads
 * after failures or across connectivity changes and system reboots.
 * <p>
 * Instances of this class should be obtained through
 * {@link Context#getSystemService(String)} by passing
 * {@link Context#DOWNLOAD_SERVICE}.
 * <p>
 * Note that the application must have the {@link android.Manifest.permission#INTERNET}
 * permission to use this class.
 * <p>
 * 下载基础工具 直接与 /module/DownloadSystem 对接
 */
public class DownloadManager implements DownloadQueue.ITaskRunner<DownloadInnerTask>, DownloadBaseRunnable.OnRunnableFinishListener {

    public static final int OPERATION_ADD = 1;
    public static final int OPERATION_DELETE = 2;

    private DownloadDbManager mDbManager;
    private IDownloadUnpacker mUnpacker;
    private Map<String, IValidator> mValidators = new HashMap<>();
    private DownloadQueue mDownloadQueue;
    private DownloadThreadPoolExecutor mThreadPool;
    private final OkHttpClient mClient;
    private boolean mIsDestroy = false;

    private final Object mReloadTaskLock = new Object();
    private boolean mIsReload = false;
    private Runnable mReloadFromDbTask;

    private IMessageReceiver mMsgReceiver;
    private IDownloadManagerListener mManagerListener;

    private DownloadInitHelper mDownloadInitConfig;

    private PowerManager.WakeLock mWakeLock;

    private int mRetryTimes = 0;

    public DownloadManager() {
        mIsDestroy = false;
        mDownloadInitConfig = DownloadInitHelper.getInstance();
        Context context = mDownloadInitConfig.getAppContext();


        mIsReload = false;
        // 数据库操作管理器
        mDbManager = new DownloadDbManager(new DatabaseModeImpl(context));

        // 增加MD5校验器
        GlobalConfig config = mDownloadInitConfig.getGlobalConfig();
        List<BaseValidator.Creator> validators = config.getValidators();
        if (validators != null && !validators.isEmpty()) {
            String type;
            for (BaseValidator.Creator creator : validators) {
                type = creator.getType();
                if (TextUtils.isEmpty(type)) {
                    LogUtil.w(" get validator's creator, but type is null!  ");
                    continue;
                }
                LogUtil.i(" add validator type=" + type);
                addFileValidator(type, creator.create());
            }
        }

        // 增加解压器
        mUnpacker = config.getUnpackCreator() != null ? config.getUnpackCreator().create() : null;
        // 线程池
        mThreadPool = new DownloadThreadPoolExecutor();
        // 下载任务队列
        mDownloadQueue = new DownloadQueue(this,
                mDownloadInitConfig.getGlobalConfig().getDownloadTaskCount(),
                mDownloadInitConfig.getGlobalConfig().getMaxNoNeedQueueTasks());
        // 网络请求
        mClient = new OkHttpClient();
        mClient.setConnectTimeout(2000, TimeUnit.MILLISECONDS);
        mClient.setReadTimeout(20000, TimeUnit.MILLISECONDS);
    }

    /**
     * 设置下载管理器监听,监听管理器运行情况
     *
     * @param listener 下载器运行状态监听
     */
    public void setOnDownloadManagerListener(IDownloadManagerListener listener) {
        mManagerListener = listener;
    }

    public void registerMsgReceiver(IMessageReceiver receiver) {
        mMsgReceiver = receiver;
    }

    public void unregisterMsgCallback(IMessageReceiver receiver) {
        mMsgReceiver = null;
    }

    /**
     * <pre>开始下载任务，将会根据url和保存地址生成唯一的ID，用于标识当前下载任务
     * 已存在的任务将无法开始下载，请删除后重新调用此方法或者直接调用
     * {@link #restart(int)}
     * </pre>
     *
     * @param taskParam 任务配置信息
     * @return true成功，false失败
     */
    public synchronized boolean start(final TaskParam taskParam) {
        // 根据url和保存地址生成唯一的id
        int generateId = taskParam.getGenerateId();
        if (generateId == ITask.INVALID_GENERATE_ID) {
            LogUtil.e("task= " + taskParam + " generate id is invalid! ");
            return false;
        }
        // 在数据库中查找
        DownloadInnerTask downloadInnerTask = mDbManager.find(this, generateId);
        // 数据库中已存在
        if (downloadInnerTask != null) {
            LogUtil.w("task= " + taskParam.getGenerateId() + " already has in db! ");
            return startOldTask(downloadInnerTask);
        }

        downloadInnerTask = new DownloadInnerTask(taskParam);
        downloadInnerTask
                .setState(Status.DOWNLOAD_WAITING)
                .setTaskPhase(DownloadInnerTask.TASK_PHASE_DOWNLOAD)
                .setErrorCode(null)
                .setException(null);
        mDbManager.insert(downloadInnerTask.getTaskParamInfo(), downloadInnerTask.getTaskStateInfo());
        downloadInnerTask = mDbManager.find(this, downloadInnerTask.getGenerateId());
        if (downloadInnerTask == null) {
            LogUtil.e(" no found task id = " + generateId + " in db! ");
            return false;
        }
        // 加入缓存队列
        mDownloadQueue.add(generateId, downloadInnerTask);
        receiveMessage(MessageFactory.createDownloadWaitingMsg(downloadInnerTask));
        receiveMessage(MessageFactory.createOperationMsg(downloadInnerTask, OPERATION_ADD));
        return true;
    }

    private boolean startOldTask(DownloadInnerTask task) {
        // 任务已完成
        if (task.isTaskFinished()) {
            LogUtil.e("task= " + task + " has already finished! ");
            return false;
        }
        // 在缓存队列中有
        DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(task.getGenerateId());
        if (temp != null) {
            // task already in download queue
            LogUtil.i("task " + task + " already in download queue! ");
            return true;
        } else {
            LogUtil.w("task " + task + " no completed, but not in the download queue! " +
                    " Began to resume the task. ");
            return resume(task.getGenerateId());
        }
    }

    /**
     * <pre>根据任务id暂停任务，但下载任务状态 < {@link Status#DOWNLOAD_FAILURE}时可以暂停，其他状态不可暂停
     *     校验、解压不可暂停，可以删除任务
     * </pre>
     *
     * @param generateId 任务唯一id
     * @return true成功，false失败
     */
    public synchronized boolean pause(final int generateId) {
        DownloadInnerTask downloadInnerTask;
        // 在数据库中查找
        downloadInnerTask = mDbManager.find(this, generateId);
        if (downloadInnerTask == null) {
            // 数据库中不存在
            LogUtil.e(" no found task id = " + generateId + " in db! ");
            return false;
        }
        // 任务已完成
        if (downloadInnerTask.isTaskFinished()) {
            LogUtil.e("task= " + downloadInnerTask + " has already completed! ");
            return false;
        }
        // 在缓存队列中有
        DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(downloadInnerTask.getGenerateId());
        if (temp != null) {
            downloadInnerTask = temp;
            if (downloadInnerTask.isDownloadComplete()) {
                LogUtil.e(" task id = " + generateId + " has been completed!");
                return false;
            }
            // 正在下载的任务
            if (downloadInnerTask.isBindRunnable() && isInThreadPool(downloadInnerTask)) {
                //downloadInnerTask.cancelDownload();
                mThreadPool.cancel(downloadInnerTask.getGenerateId());
            }
            // 没有进行的下载线程，从缓存队列中移除
            mDownloadQueue.remove(downloadInnerTask);
        }

        if (DownloadUtils.isPauseByUser(downloadInnerTask.getState(), downloadInnerTask.getErrorCode())) {
            LogUtil.d("task= " + downloadInnerTask + " has already paused! ");
            return true;
        }

        // 直接修改状态值
        downloadInnerTask.setState(Status.DOWNLOAD_PAUSE)
                .setErrorCode(ErrorCode.Values.DOWNLOAD_PAUSE_BY_USER)
                .setException(null);
        mDbManager.updateDownloadFailure(downloadInnerTask.getTaskStateInfo());
        receiveMessage(MessageFactory.createDownloadPauseMsg(downloadInnerTask));
        return true;
    }

    public synchronized boolean pauseAll() {
        Collection<DownloadInnerTask> tasks = mDownloadQueue.getAllQueue();
        List<Integer> generateIds = new ArrayList<>(tasks.size());
        for (DownloadInnerTask task : tasks) {
            generateIds.add(task.getGenerateId());
        }
        for (int id : generateIds) {
            pause(id);
        }
        return true;
    }

    /**
     * <pre>根据任务id恢复下载
     * </pre>
     *
     * @param generateId 任务id
     * @return true成功，false失败
     */
    public synchronized boolean resume(final int generateId) {
        DownloadInnerTask downloadInnerTask;
        // 在数据库中查找
        downloadInnerTask = mDbManager.find(this, generateId);
        if (downloadInnerTask == null) {
            // 数据库中不存在
            LogUtil.e(" no found task id = " + generateId + " in db! ");
            return false;
        }

        if (downloadInnerTask.isTaskFinished()) {
            LogUtil.e(" task id = " + generateId + " has been finished!");
            return false;
        }
        // 在缓存队列中查找
        DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(generateId);
        if (temp != null) {
            downloadInnerTask = temp;
            if (downloadInnerTask.isTaskFinished()) {
                LogUtil.e(" task id = " + generateId + " has been finished!");
                return false;
            }
            if (downloadInnerTask.isBindRunnable() && isInThreadPool(downloadInnerTask)) {
                LogUtil.e(" task id = " + generateId + " already running !");
                return false;
            }
            if (downloadInnerTask.isWait()) {
                LogUtil.e(" task id = " + generateId + " already in wait queue !");
                return true;
            }
        }

        // 任务不在等待状态，而且没有运行线程,重新添加到队列
        mDownloadQueue.remove(downloadInnerTask);
        // 直接修改状态值
        downloadInnerTask
                .setState(Status.DOWNLOAD_WAITING)
                .setErrorCode(null)
                .setException(null);
        mDownloadQueue.add(generateId, downloadInnerTask);
        mDbManager.updateDownloadState(downloadInnerTask.getTaskStateInfo());
        receiveMessage(MessageFactory.createDownloadWaitingMsg(downloadInnerTask));
        return true;
    }

    public synchronized boolean restart(final int generateId) {
        DownloadInnerTask downloadInnerTask;
        // 在数据库中查找
        downloadInnerTask = mDbManager.find(this, generateId);
        if (downloadInnerTask == null) {
            // 数据库中不存在
            LogUtil.e(" no found task id = " + generateId + " in db! ");
            return false;
        }
        // 在缓存队列中查找
        DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(generateId);
        if (temp != null) {
            downloadInnerTask = temp;
            if (downloadInnerTask.isBindRunnable()) {
                downloadInnerTask.unbindRunnable();
                if (LogUtil.isDebug()) {
                    LogUtil.d(" stop task to restart!!! ");
                }
                // 停止下载
                mThreadPool.cancelNoNotify(downloadInnerTask.getGenerateId());
                // 恢复初始值
                processRestart(downloadInnerTask);
                // 重新添加下载线程
                downloadQueueRunTask(downloadInnerTask);
            } else {
                processRestart(downloadInnerTask);
                // 加入缓存队列
                mDownloadQueue.add(generateId, downloadInnerTask);
            }
        } else {
            processRestart(downloadInnerTask);
            // 加入缓存队列
            mDownloadQueue.add(generateId, downloadInnerTask);
        }

        receiveMessage(MessageFactory.createDownloadRestartMsg(downloadInnerTask));
        return true;
    }

    private void processRestart(DownloadInnerTask downloadInnerTask) {
        downloadInnerTask.deleteAllFiles();
        downloadInnerTask.setState(Status.DOWNLOAD_WAITING)
                .setTaskPhase(DownloadInnerTask.TASK_PHASE_DOWNLOAD)
                .setErrorCode(null)
                .setException(null)
                .setFinishSize(0)
                .setLastTime(-1)
                .setRetryTime(0)
                .setSpeed(0)
                .setTotalSize(0)
                .setDownloadFinishTime(-1)
                .setCheckFinishTime(-1)
                .setUnpackFinishTime(-1);
        mDbManager.updateDownloadState(downloadInnerTask.getTaskStateInfo());
    }

    /**
     * Pause all running task
     */
    /*public void pauseAll() {
        Collection<DownloadInnerTask> collection = mDownloadQueue.getAllQueue();
        if(collection != null && !collection.isEmpty()){
            DownloadInnerTask[] queue = new DownloadInnerTask[collection.size()];
            queue = collection.toArray(queue);
            LogUtil.d(" pause download queue size: " + queue.length);
            for (DownloadInnerTask downloadRequestTask : queue) {
                pause(downloadRequestTask.getGenerateId());
            }
        }
    }*/
    public boolean deleteTaskAndAllFile(final int generateId) {
        return delete(generateId, true, true);
    }

    public boolean deleteTasksAndAllFile(final int[] generateIds) {
        return delete(generateIds, true, true);
    }

    public boolean deleteTaskWithoutFile(final int generateId) {
        return delete(generateId, true, false);
    }

    public boolean deleteTasksWithoutFile(final int[] generateIds) {
        return delete(generateIds, true, false);
    }

    public boolean deleteTask(final int generateId) {
        return delete(generateId, false, true);
    }

    public boolean deleteTasks(final int[] generateIds) {
        return delete(generateIds, false, true);
    }

    /**
     * 删除任务
     *
     * @param generateId       任务id
     * @param deleteAllFile    是否删除所有文件
     * @param deleteFileEnable 删除文件开关，只有为true时才能删除文件
     * @return true成功，false失败
     */
    private synchronized boolean delete(final int generateId, final boolean deleteAllFile, final boolean deleteFileEnable) {
        // 在数据库中查找
        DownloadInnerTask downloadInnerTask = mDbManager.find(this, generateId);
        if (downloadInnerTask == null) {
            // 数据库中不存在
            LogUtil.e(" no found task id = " + generateId + " in db! ");
            return false;
        }

        // 在缓存队列中查找
        DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(generateId);
        if (temp != null) {
            downloadInnerTask = temp;
            // 标记任务为
            downloadInnerTask.setTaskIsDeleted();
            if (downloadInnerTask.isBindRunnable() && isInThreadPool(downloadInnerTask)) {
                // 取消线程
                mThreadPool.cancel(downloadInnerTask.getGenerateId());
            }
            mDownloadQueue.remove(downloadInnerTask);
        }

        if (deleteFileEnable) {
            if (deleteAllFile) {
                downloadInnerTask.deleteAllFiles();
            } else {
                downloadInnerTask.deleteFiles();
            }
        }

        mDbManager.delete(downloadInnerTask.getTaskParamInfo(), downloadInnerTask.getTaskStateInfo());
        receiveMessage(MessageFactory.createOperationMsg(downloadInnerTask, OPERATION_DELETE));
        return true;
    }

    /**
     * 批量删除任务
     *
     * @param generateIds       任务id数组
     * @param deleteAllFile    是否删除所有文件
     * @param deleteFileEnable 删除文件开关，只有为true时才能删除文件
     * @return true成功，false失败
     */
    private synchronized boolean delete(final int[] generateIds, final boolean deleteAllFile, final boolean deleteFileEnable) {
        // 在数据库中查找
        List<DownloadInnerTask> downloadInnerTaskList = mDbManager.find(this, generateIds);
        if (ListUtils.isEmpty(downloadInnerTaskList)) {
            // 数据库中不存在
            LogUtil.e(" no found taskList in db! ");
            return false;
        }

        for (DownloadInnerTask innerTask : downloadInnerTaskList) {
            // 在缓存队列中查找
            DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(innerTask.getGenerateId());
            if (temp != null) {
                innerTask = temp;
                // 标记任务为
                innerTask.setTaskIsDeleted();
                if (innerTask.isBindRunnable() && isInThreadPool(innerTask)) {
                    // 取消线程
                    mThreadPool.cancel(innerTask.getGenerateId());
                }
                mDownloadQueue.remove(innerTask);
            }

            if (deleteFileEnable) {
                if (deleteAllFile) {
                    innerTask.deleteAllFiles();
                } else {
                    innerTask.deleteFiles();
                }
            }
        }

        mDbManager.delete(downloadInnerTaskList);

        for (DownloadInnerTask innerTask : downloadInnerTaskList) {
            receiveMessage(MessageFactory.createOperationMsg(innerTask, OPERATION_DELETE));
        }

        return true;
    }

    /**
     * 动态修改某个任务的网络类型
     *
     * @param networkTypes 网络类型
     * @param generateId   任务id
     * @return true成功，false失败
     */
    public synchronized boolean setNetworkTypes(int networkTypes, int generateId) {
        // 如果使用了移动数据网络，默认可以使用Wifi网络
        if (NetworkParseUtil.containsMobile(networkTypes)) {
            networkTypes = NetworkParseUtil.addNetworkType(networkTypes, NetworkType.NETWORK_WIFI);
        }

        DownloadInnerTask downloadInnerTask;
        // 在数据库中查找
        downloadInnerTask = mDbManager.find(this, generateId);
        if (downloadInnerTask == null) {
            // 数据库中不存在
            LogUtil.e(" no found task id = " + generateId + " in db! ");
            // 任务已被删除，无法通知
            //receiveMessage(MessageFactory.createConfigChangedMsg(generateId, downloadInnerTask.getModuleName(), -1));
            return false;
        }

        // 在缓存队列中查找
        DownloadInnerTask temp = (DownloadInnerTask) mDownloadQueue.findTaskInfo(generateId);
        if (temp != null) {
            downloadInnerTask = temp;
        }
        // 修改缓存数据，任务运行时会动态检查此值
        downloadInnerTask.editNetworkTypes(networkTypes);
        // 插入数据库
        mDbManager.updateDownloadNetworkTypes(downloadInnerTask.getTaskParamInfo());
        receiveMessage(MessageFactory.createConfigChangedMsg(generateId, downloadInnerTask.getModuleName(), networkTypes));

        checkWaitNetworkTask(downloadInnerTask);
        return true;
    }

    private void checkWaitNetworkTask(DownloadInnerTask task) {
        if (mDownloadQueue.isInWaitNetworkQueue(task)) {
            NetworkInfo info = NetworkUtil.getActiveNetworkInfo(mDownloadInitConfig.getAppContext());
            if (info == null || !info.isConnected()) {
                LogUtil.d(" network changed, but no connected ! ");
                return;
            }
            int flag = NetworkUtil.checkNetwork(
                    info,
                    task.getTotalSize(),
                    task.getNetworkTypes(),
                    task.isAllowRoaming(),
                    mDownloadInitConfig.getGlobalConfig().isAllowMobile2g());
            if (flag == NetworkUtil.NETWORK_OK) {
                mDownloadQueue.removeFromWaitNetworkQueue(task);
                resume(task.getGenerateId());
            }
        }
    }

    /**
     * 网络发生了变化
     */
    public synchronized void networkChanged() {
        if (LogUtil.isDebug()) {
            LogUtil.v(" network changed! ");
        }
        if (mDownloadInitConfig.getAppContext() == null) {
            LogUtil.e(" must init application context ! ");
            return;
        }
        NetworkInfo info = NetworkUtil.getActiveNetworkInfo(mDownloadInitConfig.getAppContext());
        if (info == null || !info.isConnected()) {
            LogUtil.d(" network changed, but no connected ! ");
            return;
        }
        // 网络发生改变，重置网络检测
        NetworkUtil.networkChanged();

        mRetryTimes = 0;

        resumeWaitNetworkTasks(info);
    }

    /**
     * 恢复因网络导致暂停的下载任务
     *
     * @param info
     */
    private synchronized void resumeWaitNetworkTasks(NetworkInfo info) {
        LogUtil.i("resume wait network tasks");
        if (mDownloadQueue == null) {
            return;
        }
        List<DownloadInnerTask> tasks = mDownloadQueue.getWaitNetworkQueue();
        List<DownloadInnerTask> networkOkList = new ArrayList<>();

        int flag;
        for (DownloadInnerTask task : tasks) {
            if (task == null) {
                continue;
            }
            flag = NetworkUtil.checkNetwork(
                    info,
                    task.getTotalSize(),
                    task.getNetworkTypes(),
                    task.isAllowRoaming(),
                    mDownloadInitConfig.getGlobalConfig().isAllowMobile2g());
            if (flag == NetworkUtil.NETWORK_OK) {
                networkOkList.add(task);
            }
        }

        for (DownloadInnerTask task : networkOkList) {
            mDownloadQueue.removeFromWaitNetworkQueue(task);
            resume(task.getGenerateId());
        }
    }

    /**
     * 判断是否有线程在运行
     *
     * @param task 任务
     * @return true在运行，false没有运行
     */
    private boolean isInThreadPool(DownloadInnerTask task) {
        return task != null && task.getGenerateId() != ITask.INVALID_GENERATE_ID
                && mThreadPool.isInThreadPool(task.getGenerateId());
    }

    /**
     * 根据校验类型获取校验器
     *
     * @param checkType 校验类型，see {@link com.eebbk.bfc.sdk.downloadmanager.ITask.CheckType}
     *                  或者用户自定义类型
     * @return 校验器
     */
    private IValidator getFileValidator(String checkType) {
        if (mValidators.containsKey(checkType)) {
            return mValidators.get(checkType);
        }
        return null;
    }

    /**
     * 添加文件校验器。如果校验器类型同名的话，新添加的校验器将会覆盖旧的
     *
     * @param checkType 校验类型
     * @param validator 校验器
     */
    public void addFileValidator(String checkType, IValidator validator) {
        if (checkType != null && !TextUtils.isEmpty(checkType) && validator != null) {
            mValidators.put(checkType, validator);
        }
    }


    @Override
    public void onRunnableFinished(DownloadBaseRunnable baseRunnable) {
        if (isDestroy()) {
            return;
        }
        if (baseRunnable == null) {
            return;
        }

        int taskId = baseRunnable.getTaskId();

        // 从线程缓存队列中移除
        if (mThreadPool != null) {
            DownloadBaseRunnable runnable = mThreadPool.getRunnable(taskId);
            if (runnable == null) {
                LogUtil.w(" runnable finished, but no in pool! ");
                return;
            }
            if (runnable.getRunnableId() != null && runnable.getRunnableId().equals(baseRunnable.getRunnableId())) {
                mThreadPool.remove(taskId);
            } else {
                LogUtil.w(" runnable[" + baseRunnable.getRunnableId() + "] finished, but in the pool is different runnable[" +
                        runnable.getRunnableId() + "]");
            }
        }

        // 从缓存任务队列中移除
        if (mDownloadQueue != null) {
            DownloadInnerTask task = (DownloadInnerTask) mDownloadQueue.findTaskInfo(taskId);
            if (task != null) {
                // 解绑
                task.unbindRunnable();

                // 因为网络原因暂停的任务将其移到等待网络的缓存队列中
                if (DownloadUtils.isPauseByNetwork(task.getState(), task.getErrorCode())) {
                    LogUtil.i(" move task[" + task.getGenerateId() + "] to wait network queue ");
                    mDownloadQueue.moveToWaitNetworkQueue(task);
                    mDownloadQueue.taskFinished(task.getGenerateId(), task.isNeedQueue(), false);
                } else {
                    mDownloadQueue.taskFinished(task.getGenerateId(), task.isNeedQueue(), true);
                }

            } else {
                LogUtil.e(" runnable finished, but no found task[" + taskId + "] in the queue! ");
                // 在下载的回调监听中删除任务, 可能会造成任务找不到, 无法继续其他的下载任务;  为了恢复下载, 将调用流程走下去
                // TODO: 2017/6/23 这个方法不太合理, 有时间的话要修改下载的逻辑结构
                mDownloadQueue.taskFinished(-1, true, false);
            }
        }

    }

    @Override
    public void downloadQueueRunTask(DownloadInnerTask task) {
        initWakeLock();
        // wakeLock已经取消引用计数, 可以多次获取,最后一次释放
        mWakeLock.acquire(WAKE_LOCK_HELD_TIME);

        if (isDestroy()) {
            return;
        }
        if (task == null) {
            LogUtil.e(" run task is null! ");
            return;
        }
        // 创建新的runnableId，并绑定，如果已经绑定了且id不同，将会直接替换
        task.bindRunnable(task.generateRunnableId());
        // 创建runnable
        DownloadRunnable runnable = new DownloadRunnable(this, task, mClient, mDbManager, mUnpacker, getFileValidator(task.getCheckType()));
        LogUtil.i(" start bind task runnable[" + task.getRunnableId() + "] needQueue[" + task.isNeedQueue() + "] from wait queue ");
        // 添加到线程池执行
        mThreadPool.execute(task.getGenerateId(), runnable);
        // 通知下载器处于运行状态
        if (null != mManagerListener) {
            mManagerListener.onDownloadManagerRunTask(task);
        }
    }

    @Override
    public void downloadQueueIdle() {

        // 该方法调用时, 会持有DownloadQueue的对象锁, 为了避免死锁, 单独开个线程, 后期需要重构
        // FIXME: 2017/12/4 这里如果尝试去恢复任务，会导致在网络有问题的时候循环去访问一个固定资源，产生大量访问
        new Thread(){
            @Override
            public void run() {
                super.run();

                // 由于公司机器的缘故, 很多由于网络不好而导致暂停的任务, 在网络变好后, 机器不会发出网络变化的广播, 而无法恢复
                // 主动尝试恢复
                NetworkInfo info = NetworkUtil.getActiveNetworkInfo(mDownloadInitConfig.getAppContext());
                if (info != null && info.isConnected() && mRetryTimes < 10) {
                    LogUtil.i(" resume wait network tasks when stop services; ");
                    resumeWaitNetworkTasks(info);
                    mRetryTimes++;
                }
            }
        }.start();


        if (mWakeLock != null) {
            mWakeLock.release();
            LogUtil.i("WakeLock-mWakeLock.release2");
        }

        if (isDestroy()) {
            return;
        }
        LogUtil.i(" downloader is idle ");
        if (null != mManagerListener) {
            mManagerListener.onDownloadManagerIdle();
        }
    }

    /**
     * 判断下载器是否处于空闲状态
     *
     * @return true空闲, false不空闲
     */
    public boolean isDownloadManagerIdle() {
        return !isDestroy() && mDownloadQueue.isIdle();
    }

    public boolean isDestroy() {
        return mIsDestroy;
    }

    /**
     * 接收到下载线程发送过来的消息
     *
     * @param message 消息
     */
    public void receiveMessage(DownloadBaseMessage message) {
        if (message == null) {
            return;
        }
        if (mMsgReceiver != null) {
            mMsgReceiver.onMessageReceive(message);
        }
    }

    public synchronized void onDestroy() {
        LogUtil.i(" manager is destroy! ");
        mIsDestroy = true;
        if (mDbManager != null) {
            mDbManager.destroy();
            mDbManager = null;
        }
        if (mDownloadQueue != null) {
            mDownloadQueue.stop();
            mDownloadQueue = null;
        }
        if (mThreadPool != null) {
            mThreadPool.shutdown();
            mThreadPool = null;
        }
    }

    /**
     * 从数据库中重新加载下载任务，下载器重启时使用
     */
    public void reloadAllTasksFromDb() {
        synchronized (mReloadTaskLock) {
            if (!mIsReload && null == mReloadFromDbTask) {
                LogUtil.i(" create a thread to reload all download tasks ");
                mIsReload = true;
                mReloadFromDbTask = new ReloadTask();
                new Thread(mReloadFromDbTask).start();
            } else {
                if (LogUtil.isDebug()) {
                    LogUtil.d(" reload tasks from db but already running! ");
                }
            }
        }
    }

    public class ReloadTask implements Runnable {
        @Override
        public void run() {
            LogUtil.i(" exec runnable to reload all download tasks ");
            if (null == mDownloadInitConfig.getAppContext()) {
                LogUtil.w(" exec runnable but application context = null! ");
                return;
            }
            ContentResolver resolver = mDownloadInitConfig.getAppContext().getContentResolver();
            Cursor cursor = null;
            try {
                // 查询未完成的任务，未完成状态判断查考DownloadTask.isFinished()方法
                String state = DownloadTaskColumns.STATE;
                String selectionStr = "NOT (" +
                        " (" + state + "==? or " + state + "==? or " + state + "==? or " + state + "==?) " +
                        /*" or (" + state + "==? and (" + DownloadTaskColumns.ERROR_CODE + "==? or "
                        + DownloadTaskColumns.ERROR_CODE + "==?))" +*/
                        " or (" + state + "==? and (" + DownloadTaskColumns.CHECK_ENABLE + "==0 or "
                        + DownloadTaskColumns.CHECK_TYPE + " is null or "
                        + DownloadTaskColumns.CHECK_CODE + " is null or "
                        + DownloadTaskColumns.CHECK_CODE + "==?) and "
                        + DownloadTaskColumns.AUTO_UNPACK + "==0) " +
                        " or (" + state + "==? and " + DownloadTaskColumns.AUTO_UNPACK + " == 0)" +
                        ")";
                String[] args = {
                        "" + Status.UNPACK_SUCCESS,
                        "" + Status.DOWNLOAD_FAILURE,
                        "" + Status.CHECK_FAILURE,
                        "" + Status.UNPACK_FAILURE,

                        /*"" + Status.DOWNLOAD_PAUSE,
                        ErrorCode.Values.DOWNLOAD_PAUSE_BY_USER,
                        ErrorCode.Values.DOWNLOAD_OUT_OF_SPACE,*/

                        "" + Status.DOWNLOAD_SUCCESS,
                        ITask.CheckType.NON,

                        "" + Status.CHECK_SUCCESS
                };
                cursor = resolver.query(mDownloadInitConfig.getProviderConfig().taskUri, DatabaseHelper.getTasksColumnsProjection(), selectionStr, args, null);
                if (cursor == null) {
                    LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_RELOAD_ALL_TASKS_CURSOR_IS_NULL,
                            " reload all tasks but cursor = null"));
                    return;
                }
                DownloadInnerTask task;
                //第一次的时候将旧任务添加埋点进行回传
                List<String> stringArrayList = null;
                boolean isFirst = SharedPreferenceUtils.getInstance(mDownloadInitConfig.getAppContext()).get("isFirst", true);
                if(isFirst){
                    stringArrayList = new ArrayList<>();
                }

                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    do {
                        task = new DownloadInnerTask(cursor);

                        //添加进入埋点
                        if(null != stringArrayList){
                            stringArrayList.add(task.getUrl());
                        }

                        if (task.getGenerateId() == ITask.INVALID_GENERATE_ID
                                || DownloadUtils.isPauseByUser(task.getState(), task.getErrorCode())
                                || DownloadUtils.isPauseByOutOfSpace(task.getState(), task.getErrorCode())
                                || task.isTaskFinished()) {
                            continue;
                        }
                        // auto recover tasks from db
                        resume(task.getGenerateId());

                    } while (cursor.moveToNext());
                }

                //进行埋点添加
                if(isFirst){
                    SharedPreferenceUtils.getInstance(mDownloadInitConfig.getAppContext()).put("isFirst", false);
                }
            } catch (Throwable throwable) {
                LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                        " An exception occurred while trying to get task by all tasks"));
            } finally {
                CloseableUtil.close(cursor);
                synchronized (mReloadTaskLock) {
                    mReloadFromDbTask = null;
                }
            }
        }
    }

    private void initWakeLock() {
        if(mWakeLock != null) {
            return;
        }
        Context context = mDownloadInitConfig.getAppContext();
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, WAKE_LOCK_TAG);
        mWakeLock.setReferenceCounted(false);
        LogUtil.i("WakeLock-initWakeLock2");
    }

    public interface IDownloadManagerListener {
        void onDownloadManagerRunTask(DownloadInnerTask task);

        void onDownloadManagerIdle();
    }
}
