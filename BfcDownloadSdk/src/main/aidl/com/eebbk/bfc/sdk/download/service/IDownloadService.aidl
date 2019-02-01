// IDownloadServiceAidlInterface.aidl
package com.eebbk.bfc.sdk.download.service;

import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.service.IDownloadCallback;

interface IDownloadService {

    oneway void registerCallback(in IDownloadCallback callback);

    oneway void unregisterCallback(in IDownloadCallback callback);

    boolean start(in TaskParam taskParam);

    boolean pause(int generateId);

    boolean resume(int generateId);

    boolean restart(int generateId);

    boolean deleteTaskAndAllFile(int generateId);

    boolean deleteTasksAndAllFile(inout int[] generateIds);

    boolean deleteTaskWithoutFile(int generateId);

    boolean deleteTasksWithoutFile(inout int[] generateIds);

    boolean deleteTask(int generateId);

    boolean deleteTasks(inout int[] generateIds);

    boolean setNetworkTypes(int networkTypes, int id);

    oneway void networkChanged();

}
