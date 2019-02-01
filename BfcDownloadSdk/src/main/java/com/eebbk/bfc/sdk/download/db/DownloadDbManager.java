package com.eebbk.bfc.sdk.download.db;

import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.DownloadManager;
import com.eebbk.bfc.sdk.download.db.asyn.DbOperation;
import com.eebbk.bfc.sdk.download.db.asyn.DownloadAsynDbTask;
import com.eebbk.bfc.sdk.download.db.asyn.UpdateProgressOperation;
import com.eebbk.bfc.sdk.download.db.data.TaskParamInfo;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.List;

/**
 * Desc: 数据库操作管理器
 * Author: llp
 * Create Time: 2017-02-06 10:19
 * Email: jacklulu29@gmail.com
 */

public class DownloadDbManager {

    private IDatabaseMode mDatabaseMode;
    private DownloadAsynDbTask mAsynDbTask;

    public DownloadDbManager(IDatabaseMode databaseMode){
        this.mDatabaseMode = databaseMode;
        this.mAsynDbTask = new DownloadAsynDbTask();
    }

    /**
     * 根据generateId查找任务
     *
     * @param manager  下载管理器
     * @param generateId 任务id
     * @return 任务
     */
    public DownloadInnerTask find(final DownloadManager manager, final int generateId){
        if(mDatabaseMode != null){
            return mDatabaseMode.find(manager, generateId);
        }
        return null;
    }

    /**
     * 根据generateId批量查找任务
     *
     * @param manager  下载管理器
     * @param generateIds 任务id数组
     * @return 任务
     */
    public List<DownloadInnerTask> find(final DownloadManager manager, final int[] generateIds) {
        if (mDatabaseMode != null) {
            return mDatabaseMode.find(manager, generateIds);
        }
        return null;
    }

    /**
     * 插入新任务
     *
     * @param taskParamInfo  任务配置信息
     * @param taskStateInfo 任务临时状态信息
     * @return 任务ID
     */
    public int insert(final TaskParamInfo taskParamInfo, final TaskStateInfo taskStateInfo){
        if(mDatabaseMode != null){
            return mDatabaseMode.insert(taskParamInfo, taskStateInfo);
        }
        return -1;

    }

    /**
     * 根据ID更新网络类型值
     * @param taskParamInfo 任务配置信息
     */
    public void updateDownloadNetworkTypes(TaskParamInfo taskParamInfo){
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadNetworkTypes(taskParamInfo.generateId, taskParamInfo.paramsId, taskParamInfo.networkTypes);
        }
    }

    /**
     * 根据ID只更新状态
     * @param taskStateInfo 任务状态信息
     */
    public void updateDownloadStatus(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadStatus(taskStateInfo);
        }
    }

    /**
     * 根据ID更新部分配置和状态
     * @param taskParamInfo 任务配置信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateDownloadParamAndStatus(TaskParamInfo taskParamInfo, TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadParamAndStatus(
                    taskParamInfo.generateId, taskParamInfo.paramsId, taskParamInfo.fileName,
                    taskParamInfo.fileExtension, taskParamInfo.savePath, taskParamInfo.networkTypes, taskStateInfo);
        }
    }

    /**
     * 根据ID更新进度
     * @param taskStateInfo 任务状态信息
     */
    public void updateDownloadProgress(TaskStateInfo taskStateInfo, boolean asyn){
        if(mAsynDbTask != null && mDatabaseMode != null){
            if(asyn){
                mAsynDbTask.add(new UpdateProgressOperation(mDatabaseMode, taskStateInfo));
            } else {
                UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
                forceUpdateDownloadProgress(operation);
                operation.operation();
            }
        }
    }

    private void forceUpdateDownloadProgress(UpdateProgressOperation operation){
        if(mDatabaseMode != null && mAsynDbTask != null){
            DbOperation dbOperation = mAsynDbTask.getOperationInList(operation);
            if(dbOperation != null){
                if(LogUtil.isDebug()){
                    LogUtil.d(" force update download progress:" + dbOperation.getData());
                }
                dbOperation.operation();
            }
        }
    }

    /**
     * 根据ID更新下载重试相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateDownloadRetry(TaskStateInfo taskStateInfo){
        if(taskStateInfo == null){
            LogUtil.e("任务信息为null,下载重试失败...");
            return ;
        }
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadRetry(taskStateInfo);
        }
    }

    /**
     * 根据ID更新下载失败相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateDownloadFailure(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadFailure(taskStateInfo);
        }
    }

    /**
     * 根据ID更新下载成功相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateDownloadSuccess(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadSuccess(taskStateInfo);
        }
    }

    /**
     * 根据ID更新校验失败相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateCheckFailure(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateCheckFailure(taskStateInfo);
        }
    }

    /**
     * 根据ID更新校验成功相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateCheckSuccess(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateCheckSuccess(taskStateInfo);
        }
    }

    /**
     * 根据ID更新部分配置和状态
     * @param taskParamInfo 任务配置信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateUnpackParamAndStatus(TaskParamInfo taskParamInfo, TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateUnpackParamAndStatus(taskParamInfo.generateId, taskParamInfo.paramsId, taskParamInfo.unpackPath, taskStateInfo);
        }
    }

    /**
     * 根据ID更新解压失败相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateUnpackFailure(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateUnpackFailure(taskStateInfo);
        }
    }

    /**
     * 根据ID更新解压成功相关信息
     * @param taskStateInfo 任务状态信息
     */
    public void updateUnpackSuccess(TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateUnpackSuccess(taskStateInfo);
        }
    }

    /**
     * 更新任务状态值、errorCode、异常，保存所有值
     * @param taskStateInfo 任务临时状态信息，包含数据库任务ID、任务状态值、errorCode、异常
     */
    public void updateDownloadState(final TaskStateInfo taskStateInfo){
        UpdateProgressOperation operation = new UpdateProgressOperation(mDatabaseMode, taskStateInfo);
        forceUpdateDownloadProgress(operation);
        if(mDatabaseMode != null){
            mDatabaseMode.updateDownloadState(taskStateInfo);
        }
    }

    /**
     * 删除任务记录
     *
     * @param taskParamInfo 配置信息
     * @param taskStateInfo 状态信息
     */
    public void delete(final TaskParamInfo taskParamInfo, final TaskStateInfo taskStateInfo){
        if(mDatabaseMode != null && mAsynDbTask != null){
            mDatabaseMode.delete(taskParamInfo.generateId, taskParamInfo.paramsId, taskStateInfo);
        }
    }

    public void delete(List<DownloadInnerTask> downloadInnerTaskList) {
        if(mDatabaseMode != null && mAsynDbTask != null){
            mDatabaseMode.delete(downloadInnerTaskList);
        }
    }

    public void destroy(){
        if(mAsynDbTask != null){
            mAsynDbTask.destroy();
            mAsynDbTask = null;
        }
        mDatabaseMode = null;
    }

}
