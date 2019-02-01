package com.eebbk.bfc.sdk.download.services;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.BfcDownload;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.DownloadManager;
import com.eebbk.bfc.sdk.download.GlobalConfig;
import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.service.IDownloadCallback;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkReceiverUtils;
import com.eebbk.bfc.sdk.download.util.SyHandler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Desc: 下载服务
 * Author: llp
 * Create Time: 2016-10-07 20:26
 * Email: jacklulu29@gmail.com
 */

public class DownloadService extends Service implements IDownloadServiceStub, DownloadManager.IDownloadManagerListener {

    private DownloadServiceStub mServiceStub;

    private static final int MSG_STOP_SERVICES = 100;

    private DownloadManager mManager;
    private Handler mHandler;
    private DownloadInitHelper mDownloadInitConfig;

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceStub = new DownloadServiceStub(this);

        mDownloadInitConfig = DownloadInitHelper.getInstance();

        mHandler = new SyHandler(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                if (msg.what == MSG_STOP_SERVICES) {
                    stopService();
                    return true;
                }
                return false;
            }
        });

        try {
            // 版本更新库使用是类似于异步初始化, 初始化配置没好之前可能报错; 以后的操作需要对Manager判null
            // 这个问题理论上下载库不应该处理的, 先做一下容错
            mManager = new DownloadManager();
        } catch (Exception e) {
            return;
        }

        mManager.setOnDownloadManagerListener(this);

        NetworkReceiverUtils.registerOrEnableNetChangeComponent(getApplicationContext());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mManager == null){
            stopService();
            return START_NOT_STICKY;
        }

        // TODO: 2017/7/3 可能需要修改成 bindServices 调用,  跨进程的时候, 是拿不到BfcDownload对象的
        BfcDownload.onServiceConnected(mServiceStub);

        mManager.reloadAllTasksFromDb();
        if (intent != null && intent.hasExtra(IDownloadServiceStub.KEY_ACTION)) {
            processAction(intent);
        }
        stopServiceDelayedIfIdle();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mServiceStub;
    }

    @Override
    public void onDestroy() {

        NetworkReceiverUtils.unregisterNetChangeComponent(getApplicationContext());

        //noinspection ConstantConditions
        BfcDownload.onServiceDisConnected();
        removeStopServiceMessage();
        if (mManager != null){
            mManager.onDestroy();
        }

        super.onDestroy();
    }

    public void registerCallback(IDownloadCallback callback) throws RemoteException {
        // service不支持跨进程，所有这里为节约开销，直接注册消息接收器，不使用IDownloadCallback
        // 跨进程时才注册IDownloadCallback，通过aidl通信
        mManager.registerMsgReceiver(BfcDownload.getImpl().getMessageReceiver());
    }

    public void unregisterCallback(IDownloadCallback callback) throws RemoteException {
        mManager.unregisterMsgCallback(BfcDownload.getImpl().getMessageReceiver());
    }

    public boolean start(TaskParam taskParam) throws RemoteException {
        checkAndInitWithConfig(taskParam, mDownloadInitConfig.getGlobalConfig());
        return mManager.start(taskParam);
    }

    private void checkAndInitWithConfig(TaskParam taskParam, GlobalConfig config) {
        if (taskParam == null || config == null) {
            return;
        }
        if (TextUtils.isEmpty(taskParam.getSavePath())) {
            taskParam.setSavePath(config.getSavePath());
        }

        if (taskParam.getNetworkTypes() == 0) {
            taskParam.setNetworkTypes(config.getNetWorkType());
        }

        if (TextUtils.isEmpty(taskParam.getModuleName())) {
            taskParam.setModuleName(DownloadInitHelper.getInstance().getDefaultModuleName());
        }
    }

    public boolean pause(int generateId) throws RemoteException {
        return mManager.pause(generateId);
    }

    public boolean resume(int generateId) throws RemoteException {
        return mManager.resume(generateId);
    }

    public boolean restart(int generateId) throws RemoteException {
        return mManager.restart(generateId);
    }

    public boolean deleteTaskAndAllFile(int generateId) throws RemoteException {
        return mManager.deleteTaskAndAllFile(generateId);
    }

    public boolean deleteTasksAndAllFile(int[] generateIds) throws RemoteException {
        return mManager.deleteTasksAndAllFile(generateIds);
    }

    public boolean deleteTaskWithoutFile(int generateId) throws RemoteException {
        return mManager.deleteTaskWithoutFile(generateId);
    }

    public boolean deleteTasksWithoutFile(int[] generateIds) throws RemoteException {
        return mManager.deleteTasksWithoutFile(generateIds);
    }

    public boolean deleteTask(int generateId) throws RemoteException {
        return mManager.deleteTask(generateId);
    }

    public boolean deleteTasks(int[] generateIds) throws RemoteException {
        return mManager.deleteTasks(generateIds);
    }

    public boolean setNetworkTypes(int networkTypes, int id) throws RemoteException {
        return mManager.setNetworkTypes(networkTypes, id);
    }

    public void networkChanged() throws RemoteException {
        mManager.networkChanged();
    }

    @Override
    public void onDownloadManagerRunTask(DownloadInnerTask task) {
        removeStopServiceMessage();
        LogUtil.d(" remove stop service task, time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
    }

    @Override
    public void onDownloadManagerIdle() {
        stopServiceDelayedIfIdle();
    }

    public void shutdown() {
        if (mManager != null) {
            mManager.pauseAll();
            return;
        }
        sendStopServiceMessage();
    }

    public void stopServiceDelayedIfIdle() {
        if (mManager.isDownloadManagerIdle()) {
            sendStopServiceMessageDelay();
        }
    }

    private void removeStopServiceMessage() {
        if(mHandler != null) {
            mHandler.removeMessages(MSG_STOP_SERVICES);
        }
    }

    private void sendStopServiceMessageDelay() {
        LogUtil.i(" send stop services message delay, time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        int delayTime = mDownloadInitConfig.getGlobalConfig().getStopServiceDelayTimeIfIdle();
        if(mHandler != null) {
            mHandler.sendEmptyMessageDelayed(MSG_STOP_SERVICES, delayTime);
        }
    }

    private void sendStopServiceMessage() {
        LogUtil.i(" send stop services message, time: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        if(mHandler != null) {
            mHandler.sendEmptyMessage(MSG_STOP_SERVICES);
        }
    }

    private void stopService(){
        LogUtil.i("prepare stopService ");
        stopSelf();
    }

    private void processAction(Intent intent) {
        int action = intent.getIntExtra(IDownloadServiceStub.KEY_ACTION, -1);
        LogUtil.i(" service stub process action :" + action);
        TaskParam param = null;
        int id = -1;
        int[] ids;
        try {
            switch (action) {
                case IDownloadServiceStub.ACTION_START_TASK:
                    param = intent.getParcelableExtra(IDownloadServiceStub.KEY_TASK);
                    if (param == null) {
                        LogUtil.w(" start task param is null! ");
                        return;
                    }
                    start(param);
                    break;
                case IDownloadServiceStub.ACTION_RESTART_TASK:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" restart task[-1] failed! ");
                        return;
                    }
                    restart(id);
                    break;
                case IDownloadServiceStub.ACTION_PAUSE_TASK:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" pause task[-1] failed! ");
                        return;
                    }
                    pause(id);
                    break;
/*                case IDownloadServiceStub.ACTION_PAUSE_MOBILE_TASK:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if(id == -1){
                        LogUtil.w(" pause[mobile] task[-1] failed! ");
                        return;
                    }
                    pauseTaskForConnectedToMobile(id);
                    break;*/
                case IDownloadServiceStub.ACTION_RESUME_TASK:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" resume task[-1] failed! ");
                        return;
                    }
                    resume(id);
                    break;
                case IDownloadServiceStub.ACTION_DELETE_TASK:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" delete task[-1] failed! ");
                        return;
                    }
                    deleteTask(id);
                    break;
                case IDownloadServiceStub.ACTION_DELETE_TASK_ALL_FILE:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" delete task[-1] and all file failed ! ");
                        return;
                    }
                    deleteTaskAndAllFile(id);
                    break;
                case IDownloadServiceStub.ACTION_DELETE_TASK_WITHOUT_FILE:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" delete task[-1] without file failed! ");
                        return;
                    }
                    deleteTaskWithoutFile(id);
                    break;
                case IDownloadServiceStub.ACTION_DELETE_TASKS:
                    ids = intent.getIntArrayExtra(IDownloadServiceStub.KEY_TASK_IDS);
                    if (ids == null || ids.length < 1) {
                        LogUtil.w(" delete tasks, but ids is empty! ");
                        return;
                    }
                    deleteTasks(ids);
                    break;
                case IDownloadServiceStub.ACTION_DELETE_TASKS_ALL_FILE:
                    ids = intent.getIntArrayExtra(IDownloadServiceStub.KEY_TASK_IDS);
                    if (ids == null || ids.length < 1) {
                        LogUtil.w(" delete tasks, but ids is empty! ");
                        return;
                    }
                    deleteTasksAndAllFile(ids);
                    break;
                case IDownloadServiceStub.ACTION_DELETE_TASKS_WITHOUT_FILE:
                    ids = intent.getIntArrayExtra(IDownloadServiceStub.KEY_TASK_IDS);
                    if (ids == null || ids.length < 1) {
                        LogUtil.w(" delete tasks, but ids is empty! ");
                        return;
                    }
                    deleteTasksWithoutFile(ids);
                    break;
                case IDownloadServiceStub.ACTION_SET_NETWORK:
                    id = intent.getIntExtra(IDownloadServiceStub.KEY_TASK_ID, -1);
                    if (id == -1) {
                        LogUtil.w(" set task network type = -1 failed! ");
                        return;
                    }
                    int networkTypes = intent.getIntExtra(IDownloadServiceStub.KEY_ARGS, -1);
                    if (networkTypes == -1) {
                        LogUtil.w(" set task network type = -1 failed! ");
                        return;
                    }
                    setNetworkTypes(networkTypes, id);
                    break;
                default:
                    break;
            }
        } catch (RemoteException ex) {
            LogUtil.e(ex, " process action error ");
        }
    }
}
