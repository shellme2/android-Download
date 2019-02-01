package com.eebbk.bfc.download.demo.baseui;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.HashMap;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-23 16:07
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadTaskConfig {

    int getId();
    String getUrl();
    String getFileName();
    String getFileExtension();
    String getSavePath();
    long getPresetFileSize();
    boolean isAutoCheckSize();
    /**
     * 获取下载任务优先级
     *
     * @return 任务优先级,>0,值越大优先级越高
     */
    int getPriority();

    /**
     * 文件校验类型，可以查看{@link ITask.CheckType}，也可以自定义
     *
     * @return 校验类型
     */
    String getCheckType();

    /**
     * 获取文件校验码
     *
     * @return 校验码
     */
    String getCheckCode();

    /**
     * 是否进行文件校验
     *
     * @return true为进行校验，false不校验
     */
    boolean isCheckEnable();

    /**
     * 获取网络类型
     *
     * @return 网络类型，可能包含多种网络
     */
    int getNetworkTypes();

    /**
     * 是否需要排队
     *
     * @return true需要排队，false不需要排队（超过任务上限同样需要等待）
     */
    boolean isNeedQueue();

    /**
     * 获取保留字段
     *
     * @return 保留字段
     */
    String getReserver();

    /**
     * 获取扩展字段的数据
     *
     * @return 扩展字段
     */
    HashMap<String, String> getExtrasMap();

    /**
     * 设置通知方式
     *
     * @return 通知方式
     */
    int getNotificationVisibility();

    /**
     * 是否允许修改保存路径
     *
     * @return true为允许修改，false不允许
     */
    boolean hasAllowAdjustSavePath();

    /**
     * 是否显示实时状态（下载速度、剩余时间）（默认是）
     *
     * @return true显示，false则否
     */
    boolean isShowRealTimeInfo();

    /**
     * 获取进度回调时间
     * @return
     */
    int getMinProgressTime();

    /**
     * 是否自动解压（默认否）
     *
     * @return true解压，false则否
     */
    boolean isAutoUnpack();
    /**
     * 获取解压文件保存路径
     *
     * @return 解压文件保存路径
     */
    String getUnpackPath();

    /**
     * 解压后是否自动删除源文件
     *
     * @return true删除，false不删除
     */
    boolean isDeleteSourceAfterUnpack();

    /**
     * 删除未下载完成任务时同时删除缓存文件（默认是）
     *
     * @return true则删除，false则否
     */
    boolean isDeleteNoEndTaskAndCache();

    /**
     * 删除已下载完成任务时同时删除文件（默认否）
     *
     * @return true则删除，false则否
     */
    boolean isDeleteEndTaskAndCache();

    /**
     * 获取多线程下载一个任务开启的线程数量
     *
     * @return 线程数量
     */
    int getDownloadThreads();

    /**
     * 获取模块名
     * @return 模块名
     */
    String getModuleName();

    void showPanel();

    void hidePanel();

    boolean isShow();

}
