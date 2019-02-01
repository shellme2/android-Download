package com.eebbk.bfc.sdk.download.services;

import android.os.RemoteException;

import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.service.IDownloadCallback;
import com.eebbk.bfc.sdk.download.service.IDownloadService;

/**
 * Desc: 下载服务接口实现类
 * Author: llp
 * Create Time: 2016-10-18 22:12
 * Email: jacklulu29@gmail.com
 */

public class DownloadServiceStub extends IDownloadService.Stub {

    private DownloadService mService;

    DownloadServiceStub(DownloadService service) {
        this.mService = service;
    }

    @Override
    public void registerCallback(IDownloadCallback callback) throws RemoteException {
        // service不支持跨进程，所有这里为节约开销，直接注册消息接收器，不使用IDownloadCallback
        // 跨进程时才注册IDownloadCallback，通过aidl通信
        mService.registerCallback(callback);
    }

    @Override
    public void unregisterCallback(IDownloadCallback callback) throws RemoteException {
        mService.unregisterCallback(callback);
    }

    @Override
    public boolean start(TaskParam taskParam) throws RemoteException {
        return mService.start(taskParam);
    }


    @Override
    public boolean pause(int generateId) throws RemoteException {
        return mService.pause(generateId);
    }

    @Override
    public boolean resume(int generateId) throws RemoteException {
        return mService.resume(generateId);
    }

    @Override
    public boolean restart(int generateId) throws RemoteException {
        return mService.restart(generateId);
    }


    @Override
    public boolean deleteTaskAndAllFile(int generateId) throws RemoteException {
        return mService.deleteTaskAndAllFile(generateId);
    }

    @Override
    public boolean deleteTasksAndAllFile(int[] generateIds) throws RemoteException {
        return mService.deleteTasksAndAllFile(generateIds);
    }

    @Override
    public boolean deleteTaskWithoutFile(int generateId) throws RemoteException {
        return mService.deleteTaskWithoutFile(generateId);
    }

    @Override
    public boolean deleteTasksWithoutFile(int[] generateIds) throws RemoteException {
        return mService.deleteTasksWithoutFile(generateIds);
    }

    @Override
    public boolean deleteTask(int generateId) throws RemoteException {
        return mService.deleteTask(generateId);
    }

    @Override
    public boolean deleteTasks(int[] generateIds) throws RemoteException {
        return mService.deleteTasks(generateIds);
    }

    @Override
    public boolean setNetworkTypes(int networkTypes, int id) throws RemoteException {
        return mService.setNetworkTypes(networkTypes, id);
    }

    @Override
    public void networkChanged() throws RemoteException {
        mService.networkChanged();
    }
}
