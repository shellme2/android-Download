package com.eebbk.bfc.sdk.download.db;

import android.provider.BaseColumns;
import com.eebbk.bfc.sdk.download.db.DatabaseHelper.ParamsColumns;
import com.eebbk.bfc.sdk.download.db.DatabaseHelper.TasksColumns;

/**
 * Desc: 下载任务字段集合
 * Author: llp
 * Create Time: 2016-10-23 22:09
 * Email: jacklulu29@gmail.com
 */

public class DownloadTaskColumns implements BaseColumns {

    /**
     * 根据url和保存地址生成的唯一id
     */
    public static final String GENERATE_ID = ParamsColumns.GENERATE_ID;
    /**
     * 下载地址
     */
    public static final String URL = ParamsColumns.URL;
    /**
     * 文件名
     */
    public static final String FILE_NAME = ParamsColumns.FILE_NAME;
    /**
     * 文件后缀名
     */
    public static final String FILE_EXTENSION = ParamsColumns.FILE_EXTENSION;
    /**
     * 文件保存路径
     */
    public static final String SAVE_PATH = ParamsColumns.SAVE_PATH;
    /**
     * 预设文件大小
     */
    public static final String PRESET_FILE_SIZE = ParamsColumns.PRESET_FILE_SIZE;
    /**
     * 自动检测文件大小是否符合预设文件大小
     */
    public static final String AUTO_CHECK_SIZE = ParamsColumns.AUTO_CHECK_SIZE;
    /**
     * 优先级
     */
    public static final String PRIORITY = ParamsColumns.PRIORITY;
    /**
     * 文件校验类型
     */
    public static final String CHECK_TYPE = ParamsColumns.CHECK_TYPE;
    /**
     * 文件校验码
     */
    public static final String CHECK_CODE = ParamsColumns.CHECK_CODE;
    /**
     * 是否进行文件校验
     */
    public static final String CHECK_ENABLE = ParamsColumns.CHECK_ENABLE;
    /**
     * 可用网络类型
     */
    public static final String NETWORK_TYPES = ParamsColumns.NETWORK_TYPES;
    /**
     * 是否需求排队
     */
    public static final String NEED_QUEUE = ParamsColumns.NEED_QUEUE;
    /**
     * 预留字段
     */
    public static final String RESERVER = ParamsColumns.RESERVER;
    /**
     * 扩展字段
     */
    public static final String EXTRAS_MAP = ParamsColumns.EXTRAS_MAP;
    /**
     * 是否显示通知
     */
    public static final String NOTIFICATION_VISIBILITY = ParamsColumns.NOTIFICATION_VISIBILITY;
    /**
     * 是否允许修改保存路径
     */
    public static final String ALLOW_ADJUST_SAVE_PATH = ParamsColumns.ALLOW_ADJUST_SAVE_PATH;
    /**
     * 是否显示实时状态（下载速度、剩余时间）
     */
    public static final String SHOW_REAL_TIME_INFO = ParamsColumns.SHOW_REAL_TIME_INFO;
    /**
     * 下载进度回调间隔时间，单位毫秒，(默认1000ms)。
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     */
    public static final String MIN_PROGRESS_TIME = ParamsColumns.MIN_PROGRESS_TIME;
    /**
     * 是否自动解压
     */
    public static final String AUTO_UNPACK = ParamsColumns.AUTO_UNPACK;
    /**
     * 解压文件保存路径
     */
    public static final String UNPACK_PATH = ParamsColumns.UNPACK_PATH;
    /**
     * 解压后是否自动删除源文件
     */
    public static final String DELETE_SOURCE_AFTER_UNPACK = ParamsColumns.DELETE_SOURCE_AFTER_UNPACK;
    /**
     * 删除未下载完成任务时自动删除缓存文件
     */
    public static final String DELETE_NO_END_TASK_AND_CACHE = ParamsColumns.DELETE_NO_END_TASK_AND_CACHE;
    /**
     * 删除已下载完成任务时自动删除文件
     */
    public static final String DELETE_END_TASK_AND_CACHE = ParamsColumns.DELETE_END_TASK_AND_CACHE;
    /**
     * 下载任务开启多线程数量
     */
    public static final String DOWNLOAD_THREADS = ParamsColumns.DOWNLOAD_THREADS;
    /**
     * 是否允许漫游
     */
    public static final String DOWNLOAD_ALLOW_ROAMING = ParamsColumns.DOWNLOAD_ALLOW_ROAMING;
    /**
     * 模块名称
     */
    public static final String MODULE_NAME = ParamsColumns.MODULE_NAME;



    /**
     * 任务配置ID，对应数据库ID
     */
    public static final String PARAM_ID = TasksColumns.PARAM_ID;
    /**
     * 状态（下载、校验、解压）
     */
    public static final String STATE = TasksColumns.STATE;
    /**
     * 任务阶段：下载、校验、解压
     * add by llp , db version 3
     */
    public static final String TASK_PHASE = TasksColumns.TASK_PHASE;
    /**
     * 文件总大小
     */
    public static final String TOTAL_SIZE = TasksColumns.TOTAL_SIZE;
    /**
     * 已完成大小（下载、校验、解压）
     */
    public static final String FINISH_SIZE = TasksColumns.FINISH_SIZE;
    /**
     * 速度（下载、校验、解压）
     */
    public static final String SPEED = TasksColumns.SPEED;
    /**
     * 剩余时间（下载、校验、解压）
     */
    public static final String LAST_TIME = TasksColumns.LAST_TIME;
    /**
     * 下载重试次数
     */
    public static final String RETRY_TIME = TasksColumns.RETRY_TIME;
    /**
     * 任务创建时间
     */
    public static final String BUILD_TIME = TasksColumns.BUILD_TIME;
    /**
     * 请求头缓存标记
     */
    public static final String ETAG = TasksColumns.ETAG;
    /**
     * 下载完成时间
     */
    public static final String DOWNLOAD_FINISH_TIME = TasksColumns.DOWNLOAD_FINISH_TIME;
    /**
     * 校验完成时间
     */
    public static final String CHECK_FINISH_TIME = TasksColumns.CHECK_FINISH_TIME;
    /**
     * 解压完成时间
     */
    public static final String UNPACK_FINISH_TIME = TasksColumns.UNPACK_FINISH_TIME;
    /**
     * 错误码
     */
    public static final String ERROR_CODE = TasksColumns.ERROR_CODE;
    /**
     * 异常
     */
    public static final String EXCEPTION = TasksColumns.EXCEPTION;

}
