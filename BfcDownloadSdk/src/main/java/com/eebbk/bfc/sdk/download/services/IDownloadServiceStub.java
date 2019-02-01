package com.eebbk.bfc.sdk.download.services;

/**
 * Desc: 下载服务连接接口
 * Author: llp
 * Create Time: 2016-10-18 21:04
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadServiceStub {

    String KEY_ACTION = "action";
    String KEY_TASK = "task";
    String KEY_TASK_ID = "task_id";
    String KEY_TASK_IDS = "task_ids";
    String KEY_ARGS = "args";

    /**
     * 执行开始任务动作
     */
    int ACTION_START_TASK = 0;
    /**
     * 执行重试任务动作
     */
    int ACTION_RESTART_TASK = 1;
    /**
     * 执行暂停任务动作
     */
    int ACTION_PAUSE_TASK = 2;
    /**
     * 执行暂停移动数据任务动作
     */
    int ACTION_PAUSE_MOBILE_TASK = 3;
    /**
     * 执行恢复任务动作
     */
    int ACTION_RESUME_TASK = 4;
    /**
     * 执行删除任务动作
     */
    int ACTION_DELETE_TASK = 5;
    /**
     * 执行删除任务动作
     */
    int ACTION_DELETE_TASK_ALL_FILE = 6;
    /**
     * 执行删除任务动作
     */
    int ACTION_DELETE_TASK_WITHOUT_FILE = 7;
    /**
     * 执行设置任务网络动作
     */
    int ACTION_SET_NETWORK = 8;
    /**
     * 执行批量删除任务动作
     */
    int ACTION_DELETE_TASKS = 9;
    /**
     * 执行批量删除任务动作
     */
    int ACTION_DELETE_TASKS_ALL_FILE = 10;
    /**
     * 执行批量删除任务动作
     */
    int ACTION_DELETE_TASKS_WITHOUT_FILE = 11;
}
