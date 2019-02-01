package com.eebbk.bfc.sdk.download;

import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;

import com.eebbk.bfc.sdk.download.db.DatabaseHelper;
import com.eebbk.bfc.sdk.download.db.DownloadTaskColumns;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadConnectListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;
import com.eebbk.bfc.sdk.download.message.ConfigChangedMsg;
import com.eebbk.bfc.sdk.download.message.DownloadBaseMessage;
import com.eebbk.bfc.sdk.download.message.IMessageReceiver;
import com.eebbk.bfc.sdk.download.message.MessageCallback;
import com.eebbk.bfc.sdk.download.message.OperationMsg;
import com.eebbk.bfc.sdk.download.message.TaskCacheQueue;
import com.eebbk.bfc.sdk.download.service.IDownloadService;
import com.eebbk.bfc.sdk.download.services.DownloadService;
import com.eebbk.bfc.sdk.download.services.IDownloadServiceStub;
import com.eebbk.bfc.sdk.download.util.CloseableUtil;
import com.eebbk.bfc.sdk.download.util.ExtrasConverter;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.SyHandler;
import com.eebbk.bfc.sdk.downloadmanager.ITask;
import com.eebbk.bfc.sdk.downloadmanager.Query;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * Desc: 下载服务
 * Author: llp
 * Create Time: 2016-10-07 20:26
 * Email: jacklulu29@gmail.com
 */

public class BfcDownload implements IMessageReceiver {

    private final static Class<?> SERVICE_CLASS = DownloadService.class;

    private ArrayList<OnDownloadConnectListener> mConnectionListener = new ArrayList<>();
    private SyHandler mUIHandler;
    private MessageCallback mMessageCallback;
    private IDownloadService mService;

    private final Object mNetworkChangedTaskLock = new Object();
    private NetworkChangeTask mNetworkChangedTask;
    private IDownloadCallback mCallback;

    DownloadInitHelper mDownloadInitConfig;

    private final static class HolderClass {
        private static final BfcDownload INSTANCE = new BfcDownload();
    }

    private BfcDownload (){
        mMessageCallback = new MessageCallback();
        mUIHandler = new SyHandler(Looper.getMainLooper(), mMessageCallback);
        mCallback = new IDownloadCallback();

        mDownloadInitConfig = DownloadInitHelper.getInstance();
    }

    public static BfcDownload getImpl() {
        return HolderClass.INSTANCE;
    }

    private ServiceConnection mDownloadServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            IDownloadService downloadService = IDownloadService.Stub.asInterface(service);
            BfcDownload.onServiceConnected(downloadService);
            LogUtil.i("--- onServiceConnected");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            BfcDownload.onServiceDisConnected();
        }
    };

    public synchronized static void onServiceConnected(IDownloadService service){
        LogUtil.i("download client - onServiceConnected");
        if(getImpl().mService == service){
            return;
        }
        getImpl().mService = service;
        try {
            // 注册监听
            getImpl().mService.registerCallback(getImpl().mCallback);
        } catch (RemoteException e) {
            LogUtil.e(e, " register callback error ");
        }

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        LogUtil.i(" start service connected time:" + format.format(new Date()) + "");

        if(!getImpl().mConnectionListener.isEmpty()){
            for(OnDownloadConnectListener listener : getImpl().mConnectionListener){
                if(listener != null){
                    listener.onConnected();
                }
            }
        }
    }

    public synchronized static void onServiceDisConnected(){
        if(getImpl().mService == null){
            return;
        }
        try {
            getImpl().mService.unregisterCallback(getImpl().mCallback);
        } catch (RemoteException e) {
            LogUtil.e(e, " unregister callback error ");
        }
        getImpl().mService = null;

        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
        LogUtil.i(" stop service disconnected time:" + format.format(new Date()) + "");

        if(!getImpl().mConnectionListener.isEmpty()){
            for(OnDownloadConnectListener listener : getImpl().mConnectionListener){
                if(listener != null){
                    listener.onDisconnected();
                }
            }
        }
    }

    public synchronized boolean isConnected(){
        return getImpl().mService != null;
    }

    public void registerConnectionListener(OnDownloadConnectListener listener){
        if(listener == null){
            LogUtil.w(" connection listener is null! ");
            return;
        }
        mConnectionListener.add(listener);
    }

    public void unregisterConnectionListener(OnDownloadConnectListener listener){
        if(listener == null){
            LogUtil.w(" connection listener is null! ");
            return;
        }
        mConnectionListener.remove(listener);
    }

    synchronized private boolean checkAndConnectService(Context context){
       if (isConnected()){
           return true;
       }

       Intent intent = new Intent(context, DownloadService.class);
       return context.bindService(intent, mDownloadServiceConnection, Context.BIND_AUTO_CREATE );
    }

    public void startService(@NonNull Context context){
        Intent i = new Intent(context, SERVICE_CLASS);
        context.startService(i);
    }

    public void startService(@NonNull Context context, int action, TaskParam taskParam){
        Intent i = new Intent(context, SERVICE_CLASS);
        i.putExtra(IDownloadServiceStub.KEY_ACTION, action);
        i.putExtra(IDownloadServiceStub.KEY_TASK, taskParam);
        context.startService(i);
    }

    public void startService(@NonNull Context context, int action, int id){
        Intent i = new Intent(context, SERVICE_CLASS);
        i.putExtra(IDownloadServiceStub.KEY_ACTION, action);
        i.putExtra(IDownloadServiceStub.KEY_TASK_ID, id);
        context.startService(i);
    }

    public void startService(@NonNull Context context, int action, int[] ids){
        // intent 参数大小大于512K会导致程序闪退，理论上可以接受1W+级别的任务ID传输，导致闪退的情况应该很难出现
        Intent i = new Intent(context, SERVICE_CLASS);
        i.putExtra(IDownloadServiceStub.KEY_ACTION, action);
        i.putExtra(IDownloadServiceStub.KEY_TASK_IDS, ids);
        context.startService(i);
    }

    public void startService(@NonNull Context context, int action, int id, int args){
        Intent i = new Intent(context, SERVICE_CLASS);
        i.putExtra(IDownloadServiceStub.KEY_ACTION, action);
        i.putExtra(IDownloadServiceStub.KEY_TASK_ID, id);
        i.putExtra(IDownloadServiceStub.KEY_ARGS, args);
        context.startService(i);
    }

    /*public void stopService(@NonNull Context context){
        // 注销服务连接监听
        if(!mConnectionListener.isEmpty()){
            mConnectionListener.clear();
        }
        // 注销所有消息监听
        if(mMessageCallback != null){
            mMessageCallback.clearTasks();
        }
        if(isConnected()){
            LogUtil.w(" service is connected, shutdown! ");
            mService.shutdown();
        }
        *//*Intent i = new Intent(context, SERVICE_CLASS);
        context.stopService(i);*//*
        mService = null;

    }*/

    /*public void stopServiceIfIdle(@NonNull Context context){
        LogUtil.i(" stop service if idle... ");
        // 注销服务连接监听
        if(!mConnectionListener.isEmpty()){
            mConnectionListener.clear();
            LogUtil.i(" clear all connection listener ");
        }
        // 注销所有消息监听
        if(mMessageCallback != null){
            mMessageCallback.clearTasks();
        }
        if(isConnected()){
            LogUtil.i(" service is connected, stop service if idle ");
            mService.stopServiceDelayedIfIdle();
        }
    }*/

    public void startTask(@NonNull final ITask task, @NonNull Context context){
        if(!isConnected()){
            if(LogUtil.isDebug()){
                LogUtil.w(" start task[" + task.getTaskParam() + "], but the download service no connected. ");
            } else {
                LogUtil.w(" start task[" + task.getId() + "], but the download service no connected. ");
            }

            startService(context, IDownloadServiceStub.ACTION_START_TASK, task.getTaskParam());
            return ;
        }
        try {
            mService.start(task.getTaskParam());
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An remote exception occurred while trying to start "));
        }
    }

    public void restartTask(@NonNull final ITask task, @NonNull Context context){
        task.getTaskState().setState(Status.DOWNLOAD_INVALID)
                .setErrorCode(null)
                .setException(null)
                .setLastTime(-1)
                .setSpeed(0)
                .setFileSize(0)
                .setFinishSize(0);
        if(!isConnected()){
            if(LogUtil.isDebug()){
                LogUtil.w(" restart task[" + task.getTaskParam() + "], but the download service no connected. ");
            } else {
                LogUtil.w(" start task[" + task.getId() + "], but the download service no connected. ");
            }
            startService(context, IDownloadServiceStub.ACTION_RESTART_TASK, task.getId());
            return ;
        }
        try {
            mService.restart(task.getId());
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An remote exception occurred while trying to restart[" + task.getId() + "]"));
        }
    }

    public void pauseTask(@NonNull final ITask task, @NonNull Context context){
        if(!isConnected()){
            if(LogUtil.isDebug()){
                LogUtil.w(" pause task[" + task.getTaskParam() + "], but the download service no connected. ");
            } else {
                LogUtil.w(" start task[" + task.getId() + "], but the download service no connected. ");
            }
            startService(context, IDownloadServiceStub.ACTION_PAUSE_TASK, task.getId());
            return ;
        }
        try {
            mService.pause(task.getId());
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An remote exception occurred while trying to pause[" + task.getId() + "]"));
        }
    }

/*    public void pauseTaskForConnectedToMobile(@NonNull final ITask task, @NonNull Context context){
        if(!isConnected()){
            if(LogUtil.isDebug()){
                LogUtil.w(" pause by mobile, task[" + task.getTaskParam() + "], but the download service no connected. ");
            } else {
                LogUtil.w(" pause by mobile, task[" + task.getId() + "], but the download service no connected. ");
            }
            startService(context, IDownloadServiceStub.ACTION_PAUSE_MOBILE_TASK, task.getId());
            return ;
        }
        try {
            mService.pauseTaskForConnectedToMobile(task.getId());
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An remote exception occurred while trying to restart[" + task.getId() + "]"));
        }
    }*/

    public void resumeTask(@NonNull final ITask task, @NonNull Context context){
        if(!isConnected()){
            if(LogUtil.isDebug()){
                LogUtil.w(" resume, task[" + task.getTaskParam() + "], but the download service no connected. ");
            } else {
                LogUtil.w(" resume task[" + task.getId() + "], but the download service no connected. ");
            }
            startService(context, IDownloadServiceStub.ACTION_RESUME_TASK, task.getId());
            return ;
        }
        try {
            mService.resume(task.getId());
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An remote exception occurred while trying to resume[" + task.getId() + "]"));
        }
    }

    public void deleteTask(final int id, @NonNull Context context){
        unregisterTaskAllListener(id);
        if (!isConnected()) {
            LogUtil.w(" delete task[" + id + "], but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_DELETE_TASK, id);
            return;
        }
        try {
            mService.deleteTask(id);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION, " An remote exception occurred while trying to delete[" + id + "]"));
        }
    }

    public void deleteTasks(final int[] ids, @NonNull Context context) {
        for (int id : ids) {
            unregisterTaskAllListener(id);
        }

        if (!isConnected()) {
            LogUtil.w(" delete tasks, but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_DELETE_TASKS, ids);
            return;
        }

        try {
            mService.deleteTasks(ids);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION, " An remote exception occurred while trying to delete ids"));
        }
    }

    public void deleteTaskAndAllFile(final int id, @NonNull Context context) {
        unregisterTaskAllListener(id);
        if (!isConnected()) {
            LogUtil.w(" delete task[" + id + "] and all file, but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_DELETE_TASK_ALL_FILE, id);
            return;
        }
        try {
            mService.deleteTaskAndAllFile(id);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION, " An remote exception occurred while trying to delete[" + id + "]"));
        }
    }

    public void deleteTasksAndAllFile(final int[] ids, @NonNull Context context) {
        for (int id : ids) {
            unregisterTaskAllListener(id);
        }

        if (!isConnected()) {
            LogUtil.w(" delete tasks and all file, but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_DELETE_TASKS_ALL_FILE, ids);
            return;
        }

        try {
            mService.deleteTasksAndAllFile(ids);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION, " An remote exception occurred while trying to delete ids"));
        }
    }

    public void deleteTaskWithoutFile(final int id, @NonNull Context context) {
        unregisterTaskAllListener(id);
        if (!isConnected()) {
            LogUtil.w(" delete task[" + id + "] and without file, but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_DELETE_TASK_WITHOUT_FILE, id);
            return;
        }
        try {
            mService.deleteTaskWithoutFile(id);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION, " An remote exception occurred while trying to delete[" + id + "]"));
        }
    }

    public void deleteTasksWithoutFile(final int[] ids, @NonNull Context context) {
        for (int id : ids) {
            unregisterTaskAllListener(id);
        }

        if (!isConnected()) {
            LogUtil.w(" delete tasks without file, but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_DELETE_TASKS_WITHOUT_FILE, ids);
            return;
        }

        try {
            mService.deleteTasksWithoutFile(ids);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION, " An remote exception occurred while trying to delete ids"));
        }
    }

    @WorkerThread
    public @Nullable ArrayList<DownloadInnerTask> getTask(@NonNull final Context appContext, @NonNull final Query query){
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = query.runQuery(resolver,
                    DatabaseHelper.getTasksColumnsProjection(),
                    mDownloadInitConfig.getProviderConfig().taskUri);
            if(cursor == null){
                LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_GET_TASK_CURSOR_IS_NULL,
                        " An exception occurred while trying to get task["+query+"], reason: cursor = null"));
                return null;
            }
            ArrayList<DownloadInnerTask> tasks = new ArrayList<>();
            DownloadInnerTask requestTask;
            if(cursor.getCount() > 0 && cursor.moveToFirst()){
                do {
                    requestTask = new DownloadInnerTask(cursor);
                    tasks.add(requestTask);
                } while (cursor.moveToNext());
            }
            return tasks;
        }catch (Throwable throwable) {
            LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An exception occurred while trying to get task: " + query));
        } finally {
            CloseableUtil.close(cursor);
        }
        return null;
    }


    public @Nullable
    DownloadInnerTask getTaskById(@NonNull final Context appContext, final int id){
        ContentResolver resolver = appContext.getContentResolver();
        DownloadInnerTask requestTask = null;
        Cursor cursor = null;
        try {
            String selection = DownloadTaskColumns.GENERATE_ID + "=? ";
            String[] selectionArgs = { String.valueOf(id) };
            cursor = resolver.query(mDownloadInitConfig.getProviderConfig().taskUri,
                    DatabaseHelper.getTasksColumnsProjection(), selection, selectionArgs, null);
            if(cursor == null){
                LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_GET_TASK_CURSOR_IS_NULL,
                        " An exception occurred while trying to get task[" + id + "] , reason: cursor = null"));
                return null;
            }
            if(cursor.getCount() > 0 && cursor.moveToFirst()){
                requestTask = new DownloadInnerTask(cursor);
            }
        }catch (Throwable throwable) {
            LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An exception occurred while trying to get task[" + id + "]"));
        } finally {
            CloseableUtil.close(cursor);
        }
        return requestTask;
    }

    public @Nullable ArrayList<DownloadInnerTask> getTaskByStatus(
            @NonNull final Context appContext,
            final int status, @NonNull final String moduleName){
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = null;
        try {
            String selection = DownloadTaskColumns.STATE + "=? AND " + DownloadTaskColumns.MODULE_NAME + "=?";
            String[] selectionArgs = { String.valueOf(status), moduleName };
            cursor = resolver.query(mDownloadInitConfig.getProviderConfig().taskUri, DatabaseHelper.getTasksColumnsProjection(), selection, selectionArgs, null);
            if(cursor == null){
                LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_GET_TASK_CURSOR_IS_NULL,
                        " An exception occurred while trying to get task by status[" + status + "] , reason: cursor = null"));
                return null;
            }
            ArrayList<DownloadInnerTask> tasks = new ArrayList<>();
            DownloadInnerTask requestTask;
            if(cursor.getCount() > 0 && cursor.moveToFirst()){
                do {
                    requestTask = new DownloadInnerTask(cursor);
                    tasks.add(requestTask);
                } while (cursor.moveToNext());
            }
            return tasks;
        }catch (Throwable throwable) {
            LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An exception occurred while trying to get task by status[" + status + "]"));
        } finally {
            CloseableUtil.close(cursor);
        }
        return null;
    }

    public @Nullable ArrayList<DownloadInnerTask> getTask(@NonNull final Context appContext,
                                                          @NonNull final String moduleName){
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = null;
        try {
            String selection = DownloadTaskColumns.MODULE_NAME + "=?";
            String[] args = { moduleName };
            cursor = resolver.query(mDownloadInitConfig.getProviderConfig().taskUri,
                    DatabaseHelper.getTasksColumnsProjection(), selection, args, null);
            if(cursor == null){
                LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_GET_TASK_CURSOR_IS_NULL,
                        " An exception occurred while trying to get all tasks, reason: cursor = null"));
                return null;
            }
            ArrayList<DownloadInnerTask> tasks = new ArrayList<>();
            DownloadInnerTask requestTask;
            if(cursor.getCount() > 0 && cursor.moveToFirst()){
                do {
                    requestTask = new DownloadInnerTask(cursor);
                    tasks.add(requestTask);
                } while (cursor.moveToNext());
            }
            return tasks;
        }catch (Throwable throwable) {
            LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An exception occurred while trying to get task by all tasks"));
        } finally {
            CloseableUtil.close(cursor);
        }
        return null;
    }

    public @Nullable ArrayList<DownloadInnerTask> getTaskByExtras(
            @NonNull final Context appContext,
            @NonNull final HashMap<String, String> extras,
            @NonNull final String moduleName){
        ContentResolver resolver = appContext.getContentResolver();
        Cursor cursor = null;
        try {
            String selection = getWhereClauseForExtras(extras);
            // 加上模块名搜索
            if(!extras.isEmpty()){
                selection += " AND ";
            }
            selection += DownloadTaskColumns.MODULE_NAME + "=?";
            String[] selectionArgs = getWhereArgsForExtras(extras, moduleName);
            cursor = resolver.query(mDownloadInitConfig.getProviderConfig().taskUri, DatabaseHelper.getTasksColumnsProjection(), selection, selectionArgs, null);
            if(cursor == null){
                LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_GET_TASK_CURSOR_IS_NULL,
                        " An exception occurred while trying to get task by extras, reason: cursor = null"));
                return null;
            }
            ArrayList<DownloadInnerTask> tasks = new ArrayList<>();
            DownloadInnerTask requestTask;
            if(cursor.getCount() > 0 && cursor.moveToFirst()){
                do {
                    requestTask = new DownloadInnerTask(cursor);
                    tasks.add(requestTask);
                } while (cursor.moveToNext());
            }
            return tasks;
        }catch (Throwable throwable) {
            LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An exception occurred while trying to get task by extras"));
        } finally {
            CloseableUtil.close(cursor);
        }
        return null;
    }

    private String getWhereClauseForExtras(@NonNull HashMap<String, String> map)  {
        if(map.isEmpty()){
            return "";
        }
        StringBuilder whereClause = new StringBuilder();

        Iterator<?> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next();
            whereClause.append(DownloadTaskColumns.EXTRAS_MAP);
            whereClause.append(" LIKE ? ");

            if (iter.hasNext()) {
                whereClause.append(" AND ");
            }
        }

        return whereClause.toString();
    }

    /**
     * Get the selection args for a clause
     * @throws UnsupportedEncodingException
     */
    private String[] getWhereArgsForExtras(@NonNull HashMap<String, String> map,
                                           @NonNull String moduleName) throws UnsupportedEncodingException {
        if(map.isEmpty()){
            return new String[]{ moduleName };
        }
        String[] whereArgs = new String[map.size() + 1];
        Iterator<?> iter = map.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            whereArgs[i] = "%" + ExtrasConverter.encodeFormat((String) entry.getKey(), (String) entry.getValue()) + "%";
            i++;
        }
        // 最后加上模块名
        whereArgs[i] = moduleName;

        return whereArgs;
    }

    public void setNetworkTypes(final int networkTypes, final int id, @NonNull Context context){
        if(!isConnected()){
            LogUtil.w(" set task[" + id + "] network types, but the download service no connected. ");
            startService(context, IDownloadServiceStub.ACTION_SET_NETWORK, id, networkTypes);
            return ;
        }
        try {
            mService.setNetworkTypes(networkTypes, id);
        } catch (RemoteException e) {
            LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                    " An remote exception occurred while trying to set task[" + id + "] network types"));
        }
    }

    public boolean registerTaskListener(@NonNull ITask task){
        if(mMessageCallback == null){
            LogUtil.w(" register task listener failed, callback is null! ");
            return false;
        }
        if(task.getOnDownloadListener() == null
                && task.getOnCheckListener() == null
                && task.getOnUnpackListener() == null){
            if(LogUtil.isDebug()){
                LogUtil.i(" no register task listeners, because listeners is null! ");
            }
            return false;
        }
        return mMessageCallback.registerListener(task);
    }

    public boolean registerTaskListener(@NonNull String tag, @NonNull ITask task){
        if(mMessageCallback == null){
            LogUtil.w(" register task listener[tag] failed, callback is null! ");
            return false;
        }
        if(task.getOnDownloadListener() == null
                && task.getOnCheckListener() == null
                && task.getOnUnpackListener() == null){
            if(LogUtil.isDebug()){
                LogUtil.i(" no register task listeners[tag], because listeners is null! ");
            }
            return false;
        }
        return mMessageCallback.registerListener(tag, task);
    }

    public boolean unregisterTaskListener(@NonNull ITask task){
        if(mMessageCallback == null){
            LogUtil.w(" unregister task listener failed, callback is null! ");
            return false;
        }
        return mMessageCallback.unregisterListener(task);
    }

    public boolean unregisterTaskListener(@NonNull String tag){
        if(mMessageCallback == null){
            LogUtil.w(" unregister task listener[tag] failed, callback is null! ");
            return false;
        }
        return mMessageCallback.unregisterListener(tag);
    }

    public void unregisterTaskAllListener(final int id) {
        if(mMessageCallback == null){
            LogUtil.w(" unregister task listener failed, callback is null! ");
            return ;
        }
        mMessageCallback.unregisterTaskAll(id);
    }

    public boolean registerTaskListener(@NonNull IDownloadListener listener, @NonNull String moduleName){
        if(mMessageCallback == null){
            LogUtil.w(" register task listener failed, callback is null! ");
            return false;
        }
        return mMessageCallback.registerListener(listener, moduleName);
    }

    public boolean unregisterTaskListener(@NonNull IDownloadListener listener, @NonNull String moduleName){
        if(mMessageCallback == null){
            LogUtil.w(" unregister task listener failed, callback is null! ");
            return false;
        }
        return mMessageCallback.unregisterListener(listener, moduleName);
    }

    public boolean registerOperationListener(@NonNull OnDownloadOperationListener listener, @NonNull String moduleName){
        if(mMessageCallback == null){
            LogUtil.w(" unregister operation listener failed, callback is null! ");
            return false;
        }
        return mMessageCallback.registerOperationListener(listener, moduleName);
    }

    public boolean unregisterOperationListener(@NonNull OnDownloadOperationListener listener, @NonNull String moduleName){
        if(mMessageCallback == null){
            LogUtil.w(" unregister operation listener failed, callback is null! ");
            return false;
        }
        return mMessageCallback.unregisterOperationListener(listener, moduleName);
    }

    private boolean hasListener(final int id, final String moduleName){
        if(mMessageCallback == null){
            LogUtil.w(" message callback is null! ");
            return false;
        }
        return mMessageCallback.hasListener(id, moduleName);
    }

    /**
     * 获取消息接收器
     * @return 消息接收器
     */
    public IMessageReceiver getMessageReceiver(){
        return this;
    }

    @Override
    public void onMessageReceive(DownloadBaseMessage message) {
        if(!isConnected()){
            return;
        }
        if(null == message || message.getId() == ITask.INVALID_GENERATE_ID){
            LogUtil.i(" message is null or id is invalid! ");
            return;
        }
        if(message instanceof ConfigChangedMsg){
            if(!hasListener(message.getId(), message.getModuleName())){
                LogUtil.d(" update task["+message.getId()+"] networkTypes,but no cache tasks! ");
                return;
            }
            ConfigChangedMsg msg = (ConfigChangedMsg) message;
            LogUtil.i(" update cache task networkTypes[" + msg.getNetworkTypes() + "]");
            TaskCacheQueue.TaskCallback callback = mMessageCallback.getTaskQueue().getTaskCallback(message.getId());
            if(callback.taskData != null){
                callback.taskData.getTaskParam().setNetworkTypes(msg.getNetworkTypes());
            }
            return;
        }
        else if(message instanceof OperationMsg){
            if(mMessageCallback == null || !mMessageCallback.hasOperationListener(message.getModuleName())){
                if(LogUtil.isDebug()){
                    LogUtil.d(" task["+message.getId()+"] operation["+((OperationMsg) message).getOperation()+"],but no listener! ");
                }
                return;
            }
        }
        else {
            if(!hasListener(message.getId(), message.getModuleName())){
                if(LogUtil.isDebug()){
                    LogUtil.d(" update task["+message.getId()+"] ,but no cache tasks! ");
                }
                return;
            }
        }

        if(mUIHandler != null){
            mUIHandler.sendMessage(mUIHandler.obtainMessage(0, message));
        }
    }

    /**
     * 网络发生了变化
     */
    public void networkChanged(){
        synchronized (mNetworkChangedTaskLock){
            if(mNetworkChangedTask == null){
                mNetworkChangedTask = new NetworkChangeTask();
                new Thread(mNetworkChangedTask).start();
            } else {
                LogUtil.d(" network changed, but already has task running! ");
            }
        }
    }

    private class NetworkChangeTask implements Runnable {
        @Override
        public void run() {
            if(!isConnected()){
                LogUtil.d(" network changed but service no connected ");

                if(mDownloadInitConfig.getAppContext() != null){
                    ContentResolver resolver = mDownloadInitConfig.getAppContext().getContentResolver();
                    Cursor cursor = null;
                    try {
                        // 查询未完成的任务，未完成状态判断查考DownloadTask.isFinished()方法
                        String state  =DownloadTaskColumns.STATE;
                        String selectionStr = "NOT (" +
                                " (" + state + "==? or " + state + "==? or " + state + "==? or " + state + "==?) " +
                                " or (" + state + "==? and (" + DownloadTaskColumns.ERROR_CODE + "==? or "
                                + DownloadTaskColumns.ERROR_CODE + "==?))" +
                                " or (" + state + "==? and (" + DownloadTaskColumns.CHECK_ENABLE + "==0 or "
                                + DownloadTaskColumns.CHECK_TYPE + " is null or "
                                + DownloadTaskColumns.CHECK_CODE + " is null or "
                                + DownloadTaskColumns.CHECK_CODE + "==?) and "
                                + DownloadTaskColumns.AUTO_UNPACK + "==0) " +
                                " or (" + state + "==? and " + DownloadTaskColumns.AUTO_UNPACK + " == 0)" +
                                ")";
                        String[] args = {
                                ""+Status.UNPACK_SUCCESS,
                                ""+Status.DOWNLOAD_FAILURE,
                                ""+Status.CHECK_FAILURE,
                                ""+Status.UNPACK_FAILURE,

                                ""+Status.DOWNLOAD_PAUSE,
                                ErrorCode.Values.DOWNLOAD_PAUSE_BY_USER,
                                ErrorCode.Values.DOWNLOAD_OUT_OF_SPACE,

                                ""+Status.DOWNLOAD_SUCCESS,
                                ITask.CheckType.NON,

                                ""+Status.CHECK_SUCCESS
                        };
                        LogUtil.d("network Connected; try to resume download;  selectionStr:" + selectionStr);
                        cursor = resolver.query(mDownloadInitConfig.getProviderConfig().taskUri, DatabaseHelper.getTasksColumnsProjection(), selectionStr, args, null);
                        if(cursor == null){
                            LogUtil.e(ErrorCode.format(ErrorCode.Values.DOWNLOAD_RELOAD_ALL_TASKS_CURSOR_IS_NULL,
                                    " reload all tasks but cursor = null"));
                            return ;
                        }
                        int count = cursor.getCount();
                        if(count > 0){
                            LogUtil.i(" network changed, has ["+count+"] unfinished tasks, to restart download service");
                            if(null != mDownloadInitConfig.getAppContext()){
                                startService(mDownloadInitConfig.getAppContext());
                            }
                        }
                    }catch (Throwable throwable) {
                        LogUtil.e(throwable, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                                " An exception occurred while trying to get task by all tasks"));
                    } finally {
                        CloseableUtil.close(cursor);
                    }
                }
            } else {
                try {
                    if(null != mService){
                        mService.networkChanged();
                    }
                } catch (RemoteException e) {
                    LogUtil.e(e, ErrorCode.format(ErrorCode.Values.DOWNLOAD_REMOTE_EXCEPTION,
                            " An remote exception occurred while network changed"));
                }
            }

            synchronized (mNetworkChangedTaskLock){
                mNetworkChangedTask = null;
            }
        }
    }

    public static class IDownloadCallback extends com.eebbk.bfc.sdk.download.service.IDownloadCallback.Stub {
        @Override
        public void callback(DownloadBaseMessage msg) throws RemoteException {
            IMessageReceiver receiver = BfcDownload.getImpl().getMessageReceiver();
            if(receiver != null){
                receiver.onMessageReceive(msg);
            }
        }
    }

}
