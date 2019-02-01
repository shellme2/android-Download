package com.eebbk.bfc.sdk.download.message;

import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.eebbk.bfc.sdk.download.DownloadManager;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.da.DownloadDACollect;
import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;

/**
 * Desc: 消息回调处理，到这里已经回调到UI线程
 * Author: llp
 * Create Time: 2016-10-20 15:16
 * Email: jacklulu29@gmail.com
 */

public class MessageCallback implements Handler.Callback {

    private TaskCacheQueue mTaskCacheQueue = new TaskCacheQueue();


    public boolean hasListener(final int id, final String moduleName) {
        TaskCacheQueue.TaskCallback callback = mTaskCacheQueue.getTaskCallback(id);
        ArrayList<IDownloadListener> arrayList = mTaskCacheQueue.getListeners(moduleName);
        boolean has = !callback.mListener.isEmpty() || (arrayList != null && !arrayList.isEmpty());
        LogUtil.d(" task[" + id + "] has listener: " + has);
        return has;
    }

    public boolean registerListener(@NonNull String tag, @NonNull final ITask task) {
        getTaskQueue().addTask(tag, task);
        return true;
    }

    public boolean registerListener(@NonNull final ITask task) {
        if (getTaskQueue() != null) {
            getTaskQueue().addTask(task);
            return true;
        }
        LogUtil.w(" register task[" + task.getId() + "] to cache queue, but cache queue is null! ");
        return false;
    }

    public boolean unregisterListener(@NonNull final ITask task) {
        if (getTaskQueue() != null) {
            getTaskQueue().removeTask(task);
            return true;
        }
        return false;
    }

    public boolean unregisterListener(@NonNull String tag) {
        if (getTaskQueue() != null) {
            getTaskQueue().removeTask(tag);
            return true;
        }
        return false;
    }

    public void unregisterTaskAll(final int id) {
        if (getTaskQueue() != null) {
            getTaskQueue().removeTaskAll(id);
        }
    }

    public boolean registerListener(@NonNull final IDownloadListener listener, @NonNull String moduleName) {
        if (getTaskQueue() != null) {
            getTaskQueue().addListener(listener, moduleName);
            return true;
        }
        LogUtil.w(" register listener to cache queue, but cache queue is null! ");
        return false;
    }

    public boolean unregisterListener(@NonNull final IDownloadListener listener, @NonNull String moduleName) {
        if (getTaskQueue() != null) {
            getTaskQueue().removeListener(listener, moduleName);
            return true;
        }
        return false;
    }

    public boolean registerOperationListener(@NonNull final OnDownloadOperationListener listener, @NonNull String moduleName) {
        if (getTaskQueue() != null) {
            getTaskQueue().addListener(listener, moduleName);
            return true;
        }
        LogUtil.w(" register listener to cache queue, but cache queue is null! ");
        return false;
    }

    public boolean unregisterOperationListener(@NonNull final OnDownloadOperationListener listener, @NonNull String moduleName) {
        if (getTaskQueue() != null) {
            getTaskQueue().removeListener(listener, moduleName);
            return true;
        }
        return false;
    }

    public boolean hasOperationListener(final String moduleName) {
        if (getTaskQueue() != null) {
            ArrayList<OnDownloadOperationListener> arrayList = mTaskCacheQueue.getOperationListeners(moduleName);
            boolean has = arrayList != null && !arrayList.isEmpty();
            if (LogUtil.isDebug()) {
                LogUtil.d(" module[" + moduleName + "] has operation listener: " + has);
            }
            return has;
        }
        LogUtil.w(" module[" + moduleName + "] has no operation listener in cache queue! ");
        return false;
    }

    public void clearTasks() {
        if (getTaskQueue() != null) {
            getTaskQueue().clear();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        DownloadBaseMessage message = (DownloadBaseMessage) msg.obj;
        if (message == null) {
            LogUtil.w(" handle message, but message is null! ");
            return false;
        }
        processMessage(message);
        return true;
    }

    public TaskCacheQueue getTaskQueue() {
        return mTaskCacheQueue;
    }

    public void processMessage(@NonNull final DownloadBaseMessage message) {
        if (message instanceof OperationMsg) {
            // 处理操作回调
            processOperationMsg((OperationMsg) message);
        } else {
            // 处理任务状态回调
            processTaskCallback(message);
            /*// 任务已经完成，移除监听
            if(message.isIsTaskFinished()){
                if(getTaskQueue() != null){
                    getTaskQueue().removeTaskAll(message.getId());
                }
            }*/
        }
    }

    private void processOperationMsg(@NonNull final OperationMsg message) {
        if (getTaskQueue() == null) {
            return;
        }
        ArrayList<OnDownloadOperationListener> listeners = getTaskQueue().getOperationListeners(message.getModuleName());
        if (listeners == null || listeners.isEmpty()) {
            return;
        }
        for (OnDownloadOperationListener listener : listeners) {
            if (listener == null) {
                continue;
            }
            if (message.getOperation() == DownloadManager.OPERATION_ADD) {
                listener.onTaskAdd(message.getId());
            } else if (message.getOperation() == DownloadManager.OPERATION_DELETE) {
                listener.onTaskDelete(message.getId());
            }
        }
        LogUtil.d(" process operation msg, id[" + message.getId() + "] operation[" + message.getOperation() + "]");
    }

    private void processTaskCallback(@NonNull final DownloadBaseMessage message) {
        if (getTaskQueue() == null) {
            return;
        }
        TaskCacheQueue.TaskCallback callback = getTaskQueue().getTaskCallback(message.getId());
        ArrayList<ITask> tasks = getTaskQueue().getTasks(callback);
        ArrayList<IDownloadListener> listeners = mTaskCacheQueue.getListeners(message.getModuleName());
        if ((tasks == null || tasks.isEmpty()) && (listeners == null || listeners.isEmpty())) {
            LogUtil.d(" process callback, but listener =null and message = " + message);
            return;
        }
        if (callback.taskData == null) {
            // 会有性能影响？
            callback.taskData = DownloadController.getInstance().getTaskById(callback.id);
        }
        if (callback.taskData == null) {
            LogUtil.i(" no found task in db, is run cache task? now remove all listener! ");
            getTaskQueue().removeTaskAll(callback.id);
            return;
        }

        // 更新任务数据
        updateTask(callback.taskData, message);
        // 处理单任务监听
        if (tasks != null && !tasks.isEmpty()) {
            for (ITask task : tasks) {
                if (task == null) {
                    continue;
                }
                LogUtil.d(" process single task callback, message[" + message + "] task[" + task.getId() + "]");
                if (message.getState() <= Status.DOWNLOAD_SUCCESS) {
                    if (task.getOnDownloadListener() != null) {
                        processDownloadMsg(callback.taskData, task.getOnDownloadListener(), message, task);
                    }
                } else if (message.getState() <= Status.CHECK_SUCCESS) {
                    if (task.getOnCheckListener() != null) {
                        processCheckMsg(callback.taskData, task.getOnCheckListener(), message, task);
                    }
                } else if (message.getState() <= Status.UNPACK_SUCCESS) {
                    if (task.getOnUnpackListener() != null) {
                        processUnpackMsg(callback.taskData, task.getOnUnpackListener(), message, task);
                    }
                }
            }
        }
        // 处理全局监听
        if (listeners != null && !listeners.isEmpty()) {
            for (IDownloadListener listener : listeners) {
                if (listener == null) {
                    continue;
                }
                LogUtil.d(" process multitask callback, message[" + message + "] task[" + callback.id + "]" + " listener[" + listener + "]");
                if (message.getState() <= Status.DOWNLOAD_SUCCESS) {
                    processDownloadMsg(callback.taskData, listener.getOnDownloadListener(), message, null);
                } else if (message.getState() <= Status.CHECK_SUCCESS) {
                    processCheckMsg(callback.taskData, listener.getOnCheckListener(), message, null);
                } else if (message.getState() <= Status.UNPACK_SUCCESS) {
                    processUnpackMsg(callback.taskData, listener.getOnUnpackListener(), message, null);
                }
            }
        }

        // 任务完成或者用户手动暂停，移除当前任务所有监听，避免长时间占用内存，继续任务时再注册监听
        // 注意：0.9.5版本，去掉自动注销，改由用户手动注销，避免重下任务时无法监听
            /*if (callback.taskData.isFinished() || callback.taskData.isPauseByUser()) {
                LogUtil.i(" process callback, task[" + callback.id + "] is finished unregister all listener");
                BfcDownload.getImpl().unregisterTaskAllListener(callback.id);
            }*/
    }

    private void updateTask(@NonNull ITask task, @NonNull final DownloadBaseMessage message) {
        switch (message.getState()) {
            case Status.DOWNLOAD_WAITING:
                if (message instanceof SizeMsg) {
                    SizeMsg sizeMsg = (SizeMsg) message;
                    task.getTaskState().setFileSize(sizeMsg.getTotalSize()).setFinishSize(sizeMsg.getFinishedSize());
                }
                task.getTaskState().onDownloadWaiting();
                break;
            case Status.DOWNLOAD_STARTED:
                LogUtil.i("CDN-->下载开始-->DOWNLOAD_STARTED");
                task.getTaskState().onDownloadStarted();
                break;
            case Status.DOWNLOAD_CONNECTED:
                if (message instanceof ConnectedMessage) {
                    ConnectedMessage connectedMessage = (ConnectedMessage) message;
                    task.getTaskState().onDownloadConnected(connectedMessage.getTotalSize(), connectedMessage.getFinishedSize());
                    task.getTaskParam().setFileName(connectedMessage.getFileName()).setFileExtension(connectedMessage.getFileExtension());
                }
                break;
            case Status.DOWNLOAD_PROGRESS:
                if (message instanceof ProgressMsg) {
                    ProgressMsg progressMessage = (ProgressMsg) message;
                    task.getTaskState().onDownloading(task.isShowRealTimeInfo(), progressMessage.getTotalSize(), progressMessage.getFinishedSize(), progressMessage.getSpeed());
                }
                break;
            case Status.DOWNLOAD_PAUSE:
                LogUtil.i("CDN-->下载暂停-->DOWNLOAD_PAUSE");
                if (message instanceof ErrorMsg) {
                    ErrorMsg pauseMessage = (ErrorMsg) message;
                    task.getTaskState().onDownloadPause(pauseMessage.getTotalSize(), pauseMessage.getFinishedSize(), pauseMessage.getErrorCode(), pauseMessage.getThrowable());
                }
                break;
            case Status.DOWNLOAD_RETRY:
                ErrorMsg retryMessage = (ErrorMsg) message;
                task.getTaskState().onDownloadRetry(retryMessage.getTotalSize(), retryMessage.getFinishedSize(), retryMessage.getErrorCode(), retryMessage.getThrowable());
                break;
            case Status.DOWNLOAD_FAILURE:
                LogUtil.i("CDN-->下载失败-->DOWNLOAD_FAILURE");
                ErrorMsg failureMessage = (ErrorMsg) message;
                // 刷新数据
                task.getTaskState().onDownloadFailure(failureMessage.getTotalSize(), failureMessage.getFinishedSize(), failureMessage.getErrorCode(), failureMessage.getThrowable());
                //埋点统计
                DownloadDACollect.downloadError(task, failureMessage);
                break;
            case Status.DOWNLOAD_SUCCESS:
                LogUtil.i("CDN-->下载成功-->DOWNLOAD_SUCCESS");

                SizeMsg successMessage = (SizeMsg) message;
                task.getTaskState().onDownloadSuccess(successMessage.getTotalSize(), successMessage.getFinishedSize());

                //由于迅雷那边5s刷新一次,这里做个延时埋点的方法
                Handler handler = new Handler();
                final ITask tempTask = task;
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        DownloadDACollect.downloadSuccess(tempTask);
                    }
                }, 5000);
                break;
            case Status.CHECK_STARTED:
                SizeMsg checkStartedMessage = (SizeMsg) message;
                task.getTaskState().onCheckStarted(checkStartedMessage.getTotalSize(), checkStartedMessage.getFinishedSize());
                break;
            case Status.CHECK_PROGRESS:
                ProgressMsg checkProcessMessage = (ProgressMsg) message;
                task.getTaskState().onChecking(checkProcessMessage.getTotalSize(), checkProcessMessage.getFinishedSize());
                break;
            case Status.CHECK_FAILURE:
                ErrorMsg checkFailureMessage = (ErrorMsg) message;
                task.getTaskState().onCheckFailure(checkFailureMessage.getTotalSize(), checkFailureMessage.getFinishedSize(), checkFailureMessage.getErrorCode(), checkFailureMessage.getThrowable());
                DownloadDACollect.downloadError(task, checkFailureMessage);
                break;
            case Status.CHECK_SUCCESS:
                SizeMsg checkSuccessMessage = (SizeMsg) message;
                task.getTaskState().onCheckSuccess(checkSuccessMessage.getTotalSize(), checkSuccessMessage.getFinishedSize());
                break;
            case Status.UNPACK_STARTED:
                SizeMsg unpackStartedMessage = (SizeMsg) message;
                task.getTaskState().onUnpackStarted(unpackStartedMessage.getTotalSize(), unpackStartedMessage.getFinishedSize());
                break;
            case Status.UNPACK_PROGRESS:
                ProgressMsg unpackProgressMessage = (ProgressMsg) message;
                task.getTaskState().onUnpacking(unpackProgressMessage.getTotalSize(), unpackProgressMessage.getFinishedSize());
                break;
            case Status.UNPACK_FAILURE:
                ErrorMsg unpackFailureMessage = (ErrorMsg) message;
                task.getTaskState().onUnpackFailure(unpackFailureMessage.getTotalSize(), unpackFailureMessage.getFinishedSize(), unpackFailureMessage.getErrorCode(), unpackFailureMessage.getThrowable());
                DownloadDACollect.downloadError(task, unpackFailureMessage);
                break;
            case Status.UNPACK_SUCCESS:
                SizeMsg unpackSuccessMessage = (SizeMsg) message;
                task.getTaskState().onUnpackSuccess(unpackSuccessMessage.getTotalSize(), unpackSuccessMessage.getFinishedSize());
                break;
            default:
                break;
        }
    }

    private void processDownloadMsg(@NonNull ITask task, @Nullable OnDownloadListener listener, @NonNull DownloadBaseMessage message, @Nullable ITask sourceTask) {
        if (listener == null) {
            return;
        }
        ITask temp = task;
        switch (message.getState()) {
            case Status.DOWNLOAD_WAITING:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onDownloadWaiting(temp);
                break;
            case Status.DOWNLOAD_STARTED:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onDownloadStarted(temp);
                break;
            case Status.DOWNLOAD_CONNECTED:
                if (message instanceof ConnectedMessage) {
                    ConnectedMessage connectedMessage = (ConnectedMessage) message;
                    if (sourceTask != null) {
                        sourceTask.updateData(task);
                        temp = sourceTask;
                    }
                    listener.onDownloadConnected(temp, connectedMessage.isResuming(), temp.getFinishSize(), temp.getFileSize());
                }
                break;
            case Status.DOWNLOAD_PROGRESS:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onDownloading(temp, temp.getFinishSize(), temp.getFileSize());
                break;
            case Status.DOWNLOAD_PAUSE:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onDownloadPause(temp, temp.getErrorCode());
                break;
            case Status.DOWNLOAD_RETRY:
                ErrorMsg retryMessage = (ErrorMsg) message;
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onDownloadRetry(temp, retryMessage.getRetries(), temp.getErrorCode(), temp.getException());
                break;
            case Status.DOWNLOAD_FAILURE:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                // 回调监听
                listener.onDownloadFailure(temp, temp.getErrorCode(), temp.getException());
                break;
            case Status.DOWNLOAD_SUCCESS:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onDownloadSuccess(temp);
                break;
            default:
                break;

        }
    }

    private void processCheckMsg(@NonNull ITask task, @Nullable OnCheckListener listener, @NonNull DownloadBaseMessage message, @Nullable ITask sourceTask) {
        if (listener == null) {
            return;
        }
        ITask temp = task;
        switch (message.getState()) {
            case Status.CHECK_STARTED:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onCheckStarted(temp, temp.getFileSize());
                break;
            case Status.CHECK_PROGRESS:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onChecking(temp, temp.getFinishSize(), temp.getFileSize());
                break;
            case Status.CHECK_FAILURE:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onCheckFailure(temp, temp.getErrorCode(), temp.getException());
                break;
            case Status.CHECK_SUCCESS:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onCheckSuccess(temp);
                break;
            default:
                break;
        }
    }

    private void processUnpackMsg(@NonNull ITask task, @Nullable OnUnpackListener listener, @NonNull DownloadBaseMessage message, @Nullable ITask sourceTask) {
        if (listener == null) {
            return;
        }
        ITask temp = task;
        switch (message.getState()) {
            case Status.UNPACK_STARTED:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onUnpackStarted(temp, temp.getFileSize());
                break;
            case Status.UNPACK_PROGRESS:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onUnpacking(temp, temp.getFinishSize(), temp.getFileSize());
                break;
            case Status.UNPACK_FAILURE:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onUnpackFailure(temp, temp.getErrorCode(), temp.getException());
                break;
            case Status.UNPACK_SUCCESS:
                if (sourceTask != null) {
                    sourceTask.updateData(task);
                    temp = sourceTask;
                }
                listener.onUnpackSuccess(temp);
                break;
            default:
                break;
        }
    }
}
