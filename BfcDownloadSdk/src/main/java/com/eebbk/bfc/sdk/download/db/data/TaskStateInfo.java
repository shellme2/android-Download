package com.eebbk.bfc.sdk.download.db.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.db.DatabaseHelper.TasksColumns;
import com.eebbk.bfc.sdk.download.db.DownloadTaskColumns;
import com.eebbk.bfc.sdk.download.exception.DownloadBaseException;

/**
 * Desc: 下载任务实体类
 * Author: llp
 * Create Time: 2016-10-06 11:38
 * Email: jacklulu29@gmail.com
 */

public class TaskStateInfo {

    /**
     * 任务
     */
    public String id;
    public String paramId;
    /**
     * 状态
     */
    public  int state;
    /**
     * 任务阶段
     */
    public int taskPhase;
    /**
     * 速度
     */
    public long speed;
    /**
     * 剩余时间
     */
    public long lastTime;
    /**
     * 下载文件真实总大小
     */
    public long totalSize;
    /**
     * 已完成大小
     */
    public long finishSize;
    /**
     * 重试次数
     */
    public int retryTime;
    /**
     * 任务创建时间戳
     */
    public long buildTime;
    /**
     * 请求头缓存标记
     */
    public String eTag;
    /**
     * 下载完成时间戳
     */
    public long downloadFinishTime;
    /**
     * 文件校验完成时间戳
     */
    public long checkFinishTime;
    /**
     * 文件解压完成时间戳
     */
    public long unpackFinishTime;
    /**
     * 错误码
     */
    public String errorCode;
    /**
     * 异常
     */
    public Throwable exception;

    public TaskStateInfo(){
        id = null;
        state = Status.DOWNLOAD_WAITING;
        taskPhase = DownloadInnerTask.TASK_PHASE_DOWNLOAD;
        speed = -1;
        lastTime = -1;
        totalSize = 0;
        finishSize = 0;
        retryTime = 0;
        buildTime = System.currentTimeMillis();
        downloadFinishTime = -1;
        checkFinishTime = -1;
        unpackFinishTime = -1;
        errorCode = null;
        exception = null;
    }

    public TaskStateInfo(TaskStateInfo other){
        id = other.id;
        paramId = other.paramId;
        state = other.state;
        taskPhase = other.taskPhase;
        speed = other.speed;
        lastTime = other.lastTime;
        totalSize = other.totalSize;
        finishSize = other.finishSize;
        retryTime = other.retryTime;
        buildTime = other.buildTime;
        eTag = other.eTag;
        downloadFinishTime = other.downloadFinishTime;
        checkFinishTime = other.checkFinishTime;
        unpackFinishTime = other.unpackFinishTime;
        errorCode = other.errorCode;
        exception = other.exception;
    }

    public void bindFromDownloadTask(Cursor cursor){
        id = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns._ID));
        paramId = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.PARAM_ID));
        state = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.STATE));
        taskPhase = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.TASK_PHASE));
        speed = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.SPEED));
        lastTime = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.LAST_TIME));
        totalSize = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.TOTAL_SIZE));
        finishSize = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.FINISH_SIZE));
        retryTime = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.RETRY_TIME));
        buildTime = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.BUILD_TIME));
        eTag = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.ETAG));
        downloadFinishTime = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.DOWNLOAD_FINISH_TIME));
        checkFinishTime = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.CHECK_FINISH_TIME));
        unpackFinishTime = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.UNPACK_FINISH_TIME));
        errorCode = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.ERROR_CODE));
        String exceptionStr = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.EXCEPTION));
        if(!TextUtils.isEmpty(exceptionStr)){
            exception = new DownloadBaseException(errorCode, exceptionStr);
        }
    }

    public ContentValues toContentValues(){
        final ContentValues values = new ContentValues();
        values.put(TasksColumns.PARAM_ID, paramId);
        values.put(TasksColumns.STATE, state);
        values.put(TasksColumns.TASK_PHASE, taskPhase);
        values.put(TasksColumns.TOTAL_SIZE, totalSize);
        values.put(TasksColumns.FINISH_SIZE, finishSize);
        values.put(TasksColumns.SPEED, speed);
        values.put(TasksColumns.LAST_TIME, lastTime);
        values.put(TasksColumns.RETRY_TIME, retryTime);
        values.put(TasksColumns.BUILD_TIME, buildTime);
        values.put(TasksColumns.ETAG, eTag);
        values.put(TasksColumns.DOWNLOAD_FINISH_TIME, downloadFinishTime);
        values.put(TasksColumns.CHECK_FINISH_TIME, checkFinishTime);
        values.put(TasksColumns.UNPACK_FINISH_TIME, unpackFinishTime);
        values.put(TasksColumns.ERROR_CODE, errorCode);
        values.put(TasksColumns.EXCEPTION, exception ==null?"": exception.toString());
        return values;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" taskStateInfo[ ");
        sb.append(" \r\n id: " + id);
        sb.append(", paramId: " + paramId);
        sb.append(", state: " + state);
        sb.append(", taskPhase: " + taskPhase);
        sb.append(" \r\n speed: " + speed);
        sb.append(", lastTime: " + lastTime);
        sb.append(", totalSize: " + totalSize);
        sb.append(", finishSize: " + finishSize);
        sb.append(" \r\n eTag: " + eTag);
        sb.append(", retryTime: " + retryTime);
        sb.append(" \r\n buildTime: " + buildTime);
        sb.append(", downloadFinishTime: " + downloadFinishTime);
        sb.append(", checkFinishTime: " + checkFinishTime);
        sb.append(" \r\n errorCode: " + errorCode);
        sb.append(" \r\n exception: " + (exception ==null?"": exception.toString()));
        sb.append("\r\n ]");
        return sb.toString();
    }

}
