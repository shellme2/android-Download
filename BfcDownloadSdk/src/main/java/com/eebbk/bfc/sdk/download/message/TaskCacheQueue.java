package com.eebbk.bfc.sdk.download.message;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.SparseArray;

import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.downloadmanager.DownloadTask;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Desc: 监听缓存队列
 * Author: llp
 * Create Time: 2016-10-20 15:22
 * Email: jacklulu29@gmail.com
 */

public class TaskCacheQueue {

    // 单任务监听队列
    private SparseArray<TaskCallback> mTaskCallbacks = new SparseArray<>();
    // 单任务监听队列读写锁
    private ReadWriteLock mTaskCallbackLock = new ReentrantReadWriteLock();

    // tag任务队列
    private final HashMap<String, ArrayList<ITask>> mTagTasks = new HashMap<>();

    // 全局监听读写锁
    private ReadWriteLock mListenersLock = new ReentrantReadWriteLock();
    // 全局监听队列
    private final HashMap<String, ArrayList<IDownloadListener>> mListeners = new HashMap<>();

    // 全局监听读写锁
    private ReadWriteLock mOperationListenersLock = new ReentrantReadWriteLock();
    // 下载操作监听
    private final HashMap<String, ArrayList<OnDownloadOperationListener>> mOperationListeners = new HashMap<>();

    public TaskCacheQueue(){

    }

    public int size(){
        return mTaskCallbacks.size();
    }

    public void addTask(final ITask task) {
        mTaskCallbackLock.writeLock().lock();
        try {
            TaskCallback taskCallback = mTaskCallbacks.get(task.getId());
            if(taskCallback == null){
                taskCallback = new TaskCallback(task.getId());
            }
            ArrayList<ITask> tasks = taskCallback.mListener;
            if (!tasks.contains(task)){
                tasks.add(task);
                mTaskCallbacks.append(task.getId(), taskCallback);
                LogUtil.i(" register task["+task.getId()+"] listener success");
            } else {
                if(LogUtil.isDebug()){
                    LogUtil.i(" register task["+task.getId()+"] listener, but already has! ");
                }
            }
        } finally {
            mTaskCallbackLock.writeLock().unlock();
        }
    }

    public void removeTask(final ITask task){
        mTaskCallbackLock.writeLock().lock();
        try {
            TaskCallback taskCallback = mTaskCallbacks.get(task.getId());
            if(taskCallback == null){
                LogUtil.i(" no found task callback");
                return;
            }
            ArrayList<ITask> list = taskCallback.mListener;
            if(list.contains(task)){
                list.remove(task);
                LogUtil.i(" unregister task["+task.getId()+"] listener success");
            } else {
                LogUtil.i(" unregister task["+task.getId()+"] listener, but no found ");
            }
            // 如果对此任务已不存在任何监听,移除数据
            if(list.isEmpty()){
                ArrayList<IDownloadListener> listeners = mListeners.get(task.getModuleName());
                if(listeners == null || listeners.isEmpty()){
                    mTaskCallbacks.remove(task.getId());
                }
            }
        } finally {
            mTaskCallbackLock.writeLock().unlock();
        }
    }

    public @NonNull TaskCallback getTaskCallback(int id){
        mTaskCallbackLock.readLock().lock();
        TaskCallback taskCallback = null;
        try {
            taskCallback = mTaskCallbacks.get(id);

            if(taskCallback == null){
                mTaskCallbackLock.readLock().unlock();
                mTaskCallbackLock.writeLock().lock();
                try {
                    taskCallback = mTaskCallbacks.get(id);
                    if(taskCallback == null){
                        taskCallback = new TaskCallback(id);
                        mTaskCallbacks.put(id, taskCallback);
                    }
                } finally {
                    mTaskCallbackLock.writeLock().unlock();
                }
                mTaskCallbackLock.readLock().lock();
            }
        } finally {
            mTaskCallbackLock.readLock().unlock();
        }

        return taskCallback;
    }

    public void addTask(String tag, ITask task){
        synchronized (mTagTasks){
            ArrayList<ITask> tasks = mTagTasks.get(tag);
            if(tasks == null){
                tasks = new ArrayList<>();
            }
            // 已在tag队列中
            if(tasks.contains(task)){
                LogUtil.i(" already in the tag queue! ");
                return;
            }
            // 从旧tag队列中移除
            if(task instanceof DownloadTask){
                String tempTag = ((DownloadTask) task).getTag();
                ArrayList<ITask> oldList = mTagTasks.get(tempTag);
                if(oldList != null && !oldList.isEmpty() && oldList.contains(task)){
                    oldList.remove(task);
                    if(LogUtil.isDebug()){
                        LogUtil.d(" remove from old tag["+tempTag+"] queue! ");
                    }
                }
            } else {
                // 不支持设置tag类型
                LogUtil.e(" task is no support to add tag!  ");
                return;
            }
            // 添加到新的队列中
            ((DownloadTask) task).setTag(tag);
            tasks.add(task);
            mTagTasks.put(tag, tasks);
            LogUtil.i(" add tag["+tag+"] to the queue success! ");

            // 添加到id对应的缓存监听中
            addTask(task);
        }
    }

    public void removeTask(String tag){
        synchronized (mTagTasks){
            ArrayList<ITask> tasks = mTagTasks.get(tag);
            if(tasks == null || tasks.isEmpty()){
                LogUtil.i(" has no this tag: " + tag);
                return;
            }
            // 移除对应的缓存监听
            for(ITask task : tasks){
                if(task != null){
                    removeTask(task);
                }
            }
            // 移除tag
            mTagTasks.remove(tag);
            LogUtil.i(" remove tag["+tag+"] from the queue success! ");
        }
    }

    public void removeTaskAll(final int id){
        mTaskCallbackLock.writeLock().lock();
        try {
            if(mTaskCallbacks.get(id) == null){
                LogUtil.i(" unregister task["+id+"] all listener, but no found ");
            } else {
                mTaskCallbacks.remove(id);
                LogUtil.i(" unregister task["+id+"] all listener success");
            }
        } finally {
            mTaskCallbackLock.writeLock().unlock();
        }
    }

    public @Nullable ArrayList<ITask> getTasks(@Nullable TaskCallback taskCallback){
        mTaskCallbackLock.readLock().lock();
        try {
            if (mTaskCallbacks.size() > 0) {
                if (taskCallback == null || taskCallback.mListener.isEmpty()) {
                    return null;
                }
                ArrayList<ITask> list = taskCallback.mListener;
                ArrayList<ITask> tempList = new ArrayList<>();
                for (ITask iTask : list) {
                    if (iTask == null) {
                        LogUtil.d(" no found task in queue ");
                        continue;
                    }
                    tempList.add(iTask);
                }
                return tempList;
            }
            return null;
        } finally {
            mTaskCallbackLock.readLock().unlock();
        }
    }

    public void addListener(@NonNull IDownloadListener listener, @NonNull String moduleName){
        mListenersLock.writeLock().lock();
        try {
            ArrayList<IDownloadListener> listeners = mListeners.get(moduleName);
            if(listeners == null){
                listeners = new ArrayList<>();
            }
            if(listeners.contains(listener)){
                LogUtil.i(" register multitask listener, but already has ");
                return;
            }
            listeners.add(listener);
            mListeners.put(moduleName, listeners);
            LogUtil.i(" register multitask listener success ");
        } finally {
            mListenersLock.writeLock().unlock();
        }
    }

    public void removeListener(@NonNull IDownloadListener listener, @NonNull String moduleName){
        mListenersLock.writeLock().lock();
        try {
            ArrayList<IDownloadListener> listeners = mListeners.get(moduleName);
            if(listeners == null || listeners.size() < 1){
                LogUtil.i(" unregister multitask listener, but is null ");
                return ;
            }
            if(listeners.contains(listener)){
                listeners.remove(listener);
                LogUtil.i(" unregister multitask listener success ");
            } else {
                LogUtil.i(" unregister multitask listener, but no found ");
            }
        } finally {
            mListenersLock.writeLock().unlock();
        }
    }

    public ArrayList<IDownloadListener> getListeners(@NonNull String moduleName){
        mListenersLock.readLock().lock();
        try {
            ArrayList<IDownloadListener> listeners = mListeners.get(moduleName);
            ArrayList<IDownloadListener> tempList = new ArrayList<>();
            if(listeners != null && !listeners.isEmpty()){
                for(IDownloadListener iDownloadListener : listeners){
                    if(iDownloadListener != null){
                        tempList.add(iDownloadListener);
                    }
                }
            }

            return tempList;
        } finally {
            mListenersLock.readLock().unlock();
        }
    }

    public void addListener(@NonNull OnDownloadOperationListener listener, @NonNull String moduleName){
        mOperationListenersLock.writeLock().lock();
        try {
            ArrayList<OnDownloadOperationListener> listeners = mOperationListeners.get(moduleName);
            if(listeners == null){
                listeners = new ArrayList<>();
            }
            if(listeners.contains(listener)){
                LogUtil.i(" register operation listener, but already has ");
                return;
            }
            listeners.add(listener);
            mOperationListeners.put(moduleName, listeners);
            LogUtil.i(" register operation listener success ");
        } finally {
            mOperationListenersLock.writeLock().unlock();
        }
    }

    public void removeListener(@NonNull OnDownloadOperationListener listener, @NonNull String moduleName){
        mOperationListenersLock.writeLock().lock();
        try {
            ArrayList<OnDownloadOperationListener> listeners = mOperationListeners.get(moduleName);
            if(listeners == null || listeners.size() < 1){
                LogUtil.i(" unregister operation listener, but is null ");
                return ;
            }
            if(listeners.contains(listener)){
                listeners.remove(listener);
                LogUtil.i(" unregister operation listener success ");
            } else {
                LogUtil.i(" unregister operation listener, but no found ");
            }
        } finally {
            mOperationListenersLock.writeLock().unlock();
        }
    }

    public ArrayList<OnDownloadOperationListener> getOperationListeners(@NonNull String moduleName){
        mOperationListenersLock.readLock().lock();
        try {
            ArrayList<OnDownloadOperationListener> listeners = mOperationListeners.get(moduleName);
            ArrayList<OnDownloadOperationListener> tempList = new ArrayList<>();
            if(listeners != null && !listeners.isEmpty()){
                for(OnDownloadOperationListener iDownloadListener : listeners){
                    if(iDownloadListener != null){
                        tempList.add(iDownloadListener);
                    }
                }
            }

            return tempList;
        } finally {
            mOperationListenersLock.readLock().unlock();
        }
    }

    public void clear(){
        mTaskCallbackLock.writeLock().lock();
        try {
            mTaskCallbacks.clear();
            LogUtil.i(" clear all single task listeners ");
        } finally {
            mTaskCallbackLock.writeLock().unlock();
        }

        mListenersLock.writeLock().lock();
        try {
            mListeners.clear();
            LogUtil.i(" clear all multitask listeners ");
        } finally {
            mListenersLock.writeLock().unlock();
        }

        mOperationListenersLock.writeLock().lock();
        try {
            mOperationListeners.clear();
            LogUtil.i(" clear all operation listeners ");
        } finally {
            mOperationListenersLock.writeLock().unlock();
        }
    }

    public static class TaskCallback {

        public int id;
        public ITask taskData;
        public final ArrayList<ITask> mListener;

        public TaskCallback(int id){
            this.id = id;
            this.mListener = new ArrayList<>();
        }

    }

}
