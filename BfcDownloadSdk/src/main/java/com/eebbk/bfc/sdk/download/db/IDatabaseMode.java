package com.eebbk.bfc.sdk.download.db;

import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.DownloadManager;
import com.eebbk.bfc.sdk.download.db.data.TaskParamInfo;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;

import java.util.List;

/**
 * Desc: 下载数据库业务接口类
 * Author: llp
 * Create Time: 2016-10-07 16:54
 * Email: jacklulu29@gmail.com
 */

public interface IDatabaseMode {

    /**
     * 根据generateId查找任务
     *
     * @param manager  下载管理器
     * @param generateId 任务id
     * @return 任务
     */
    DownloadInnerTask find(final DownloadManager manager, final int generateId);

    List<DownloadInnerTask> find(final DownloadManager manager, final int[] generateIds);

    /**
     * 插入新任务
     *
     * @param taskParamInfo  任务配置信息
     * @param taskStateInfo 任务临时状态信息
     * @return 任务ID
     */
    int insert(final TaskParamInfo taskParamInfo, final TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新网络类型值
     * @param generateId   任务唯一id
     * @param paramsId 任务配置信息唯一id
     * @param networkTypes 网络类型
     * @return 影响行数
     */
    int updateDownloadNetworkTypes(int generateId, String paramsId, int networkTypes);

    /**
     * 根据ID只更新状态
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateDownloadStatus(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新部分配置和状态
     * @param generateId   任务唯一id
     * @param paramsId 任务配置信息唯一id
     * @param fileName 下载文件名称
     * @param fileExtension 下载文件扩展名
     * @param savePath 保存路径
     * @param networkTypes 网络类型
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateDownloadParamAndStatus(int generateId, String paramsId, String fileName,
                                     String fileExtension, String savePath, int networkTypes,
                                     TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新进度
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateDownloadProgress(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新下载重试相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateDownloadRetry(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新下载失败相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateDownloadFailure(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新下载成功相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateDownloadSuccess(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新校验失败相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateCheckFailure(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新校验成功相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateCheckSuccess(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新部分配置和状态
     * @param generateId   任务唯一id
     * @param paramsId 任务配置信息唯一id
     * @param unpackPath 解压保存路径
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateUnpackParamAndStatus(int generateId, String paramsId, String unpackPath, TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新解压失败相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateUnpackFailure(TaskStateInfo taskStateInfo);

    /**
     * 根据ID更新解压成功相关信息
     * @param taskStateInfo 任务状态信息
     * @return 影响行数
     */
    int updateUnpackSuccess(TaskStateInfo taskStateInfo);

    /**
     * 更新任务状态值、errorCode、异常，保存所有值
     * @param taskStateInfo 任务临时状态信息，包含数据库任务ID、任务状态值、errorCode、异常
     * @return 影响行数
     */
    int updateDownloadState(final TaskStateInfo taskStateInfo);

    /**
     * 删除任务记录
     *
     * @param generateId   任务唯一id
     * @param paramsId 任务配置信息唯一id
     * @param taskStateInfo 状态信息
     * @return true成功，false失败
     */
    boolean delete(int generateId, String paramsId, final TaskStateInfo taskStateInfo);

    /**
     * 删除任务记录
     *
     * @param generateId   任务唯一id
     * @param paramsId 任务配置信息唯一id
     * @param taskStateInfo 状态信息
     * @return true成功，false失败
     */
    boolean delete(List<DownloadInnerTask> downloadInnerTaskList);
}
