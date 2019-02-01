package com.eebbk.bfc.sdk.downloadmanager;

import com.eebbk.bfc.sdk.download.GlobalConfig;
import com.eebbk.bfc.sdk.download.IModuleController;
import com.eebbk.bfc.sdk.download.IMyController;
import com.eebbk.bfc.sdk.download.INetworkAccessController;
import com.eebbk.bfc.sdk.download.exception.DownloadNoInitException;
import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载基础接口
 */
public interface IController extends INetworkAccessController, IMyController, IModuleController {

    /**
     * 获取全局配置
     *
     * @return 全局配置
     * @throws DownloadNoInitException
     */
    GlobalConfig getGlobalConfig() throws DownloadNoInitException;

    /**
     * 添加任务
     *
     * @param pTasks 单任务或任务数组皆可
     */
    void addTask(ITask... pTasks);

    /**
     * 删除任务，按配置删除文件
     *
     * @param ids 通过 downloadID删除任务，单任务或任务数组皆可
     */
    void deleteTask(int... ids);

    /**
     * 删除任务，按配置删除文件
     *
     * @param pTasks 单任务或任务数组皆可
     */
    void deleteTask(ITask... pTasks);

    /**
     * 删除任务，按配置删除文件
     *
     * @param tasks 任务List
     */
    void deleteTask(List<ITask> tasks);

    /**
     * 删除任务和所有文件
     *
     * @param ids 通过 downloadID删除任务，单任务或任务数组皆可
     */
    void deleteTaskAndAllFile(int... ids);

    /**
     * 删除任务和所有文件
     *
     * @param pTasks 单任务或任务数组皆可
     */
    void deleteTaskAndAllFile(ITask... pTasks);

    /**
     * 删除任务和所有文件
     *
     * @param tasks 任务List
     */
    void deleteTaskAndAllFile(List<ITask> tasks);

    /**
     * 删除任务，不删除文件
     *
     * @param ids 通过 downloadID删除任务，单任务或任务数组皆可
     */
    void deleteTaskWithoutFile(int... ids);

    /**
     * 删除任务，不删除文件
     *
     * @param pTasks 单任务或任务数组皆可
     */
    void deleteTaskWithoutFile(ITask... pTasks);

    /**
     * 删除任务，不删除文件
     *
     * @param tasks 任务List
     */
    void deleteTaskWithoutFile(List<ITask> tasks);

    /**
     * 刷新任务状态
     *
     * @param pTasks 单任务或任务数组皆可
     * @return 成功刷新的数量
     */
    int refreshData(ITask... pTasks);

    /**
     * 查询任务
     *
     * @param id 根据downloadID查询
     * @return 下载任务
     */
    ITask getTaskById(int id);

    /**
     * 查询任务
     *
     * @param url      下载地址
     * @param savePath 保存路径
     * @return 下载任务
     */
    ITask getTask(String url, String savePath);

    /**
     * 克隆任务，监听事件不会克隆，其他参数属性全部一致<br/>
     * 注意：监听事件不会从旧任务中继承
     *
     * @param task    目标任务
     * @param srcTask 被clone的任务
     * @return true成功，false失败
     */
    boolean cloneData(ITask task, ITask srcTask);

    /**
     * 查询任务
     *
     * @param status 根据下载状态查询
     * @return 相应状态的下载列表
     */
    ArrayList<ITask> getTaskByStatus(int status);

    /**
     * 获取所有的下载任务
     *
     * @return 所有任务列表
     */
    ArrayList<ITask> getTask();

    /**
     * 查询任务
     *
     * @param query 根据自定义query查询符合条件的下载任务
     * @return 符合query的任务列表
     */
    ArrayList<ITask> getTask(Query query);

    /**
     * 查询任务 根据扩展字段
     *
     * @param keys   键数组
     * @param values 值数组
     * @return 相应的任务列表
     */
    ArrayList<ITask> getTaskByExtras(String[] keys, String[] values);

    /**
     * 查询任务 根据扩展字段与query
     *
     * @param query  查询对象
     * @param keys   键数组
     * @param values 值数组
     * @return 相应的任务列表
     */
    ArrayList<ITask> getTaskByExtras(Query query, String[] keys, String[] values);

    /**
     * 重新下载 根据下载任务
     *
     * @param pTasks 重新下载的任务数组
     */
    void reloadTask(ITask... pTasks);

    /**
     * 暂停下载 根据下载任务
     *
     * @param pTasks 暂停的任务数组
     */
    void pauseTask(ITask... pTasks);

    /**
     * 恢复下载 根据下载任务
     */
    void resumeTask(ITask... pTasks);

    /**
     * 为指定的任务注册一个监听，一个任务可以注册多个监听
     *
     * @param tasks 任务
     * @return 注册成功的数量
     */
    int registerTaskListener(ITask... tasks);

    /**
     * 为指定的任务注册一个监听，并与一个标记相关联,一个标记可以与多个任务关联。<br/>
     * 注销时，通过{@link #unregisterTaskListener(String)}可以把标记关联的所有任务监听都同时注销掉<br/>
     * 标记为空时将会调用默认注册方法{@link #registerTaskListener(ITask...)}注册监听<br/>
     * 多次调用此方法为一个任务关联不同标记时，最后一个标记生效
     *
     * @param tag   标记
     * @param tasks 任务
     * @return 注册成功的数量
     */
    int registerTaskListener(String tag, ITask... tasks);

    /**
     * 为指定的任务注销监听
     *
     * @param tasks 任务
     * @return 注销成功的数量
     */
    int unregisterTaskListener(ITask... tasks);

    /**
     * 通过指定的标记注销所有标记关联的任务监听<br/>
     * 可以通过{@link #registerTaskListener(String, ITask...)}方式注册任务监听，并与标记关联
     *
     * @param tag 标记
     * @return 注销成功
     */
    boolean unregisterTaskListener(String tag);

    /**
     * 注销指定任务的所有监听
     *
     * @param tasks 任务
     */
    void unregisterTaskAllListener(ITask... tasks);

    /**
     * 注销指定任务的所有监听
     *
     * @param ids 任务id
     */
    void unregisterTaskAllListener(int... ids);

    /**
     * 注册全局监听，可以监听所有下载任务
     *
     * @param listener 下载监听
     * @return 注册成功的数量
     */
    boolean registerTaskListener(IDownloadListener listener);

    /**
     * 注销全局监听
     *
     * @param listener 下载监听
     * @return 注销成功的数量
     */
    boolean unregisterTaskListener(IDownloadListener listener);

    /**
     * 注册下载操作监听，可以监听任务被添加、删除等操作，给默认模块注册
     *
     * @param listener 操作监听
     * @return true为成功，false为失败
     */
    boolean registerOperationListener(OnDownloadOperationListener listener);

    /**
     * 注销下载操作监听，不使用时请及时注销，不注销监听将可能导致内存泄露，给默认模块注销
     *
     * @param listener 操作监听
     * @return true为成功，false为失败
     */
    boolean unregisterOperationListener(OnDownloadOperationListener listener);
}
