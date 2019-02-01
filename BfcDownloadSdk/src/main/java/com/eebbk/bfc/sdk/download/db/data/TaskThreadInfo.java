package com.eebbk.bfc.sdk.download.db.data;

import android.content.ContentValues;
import android.database.Cursor;

import com.eebbk.bfc.sdk.download.db.DatabaseHelper.ThreadsColumns;

/**
 * Desc: 下载任务线程实体类
 * Author: llp
 * Create Time: 2016-10-06 16:11
 * Email: jacklulu29@gmail.com
 */

public class TaskThreadInfo {

    private String mThreadId;
    private String mTaskId;
    private int mStartPosition;
    private int mEndPosition;
    private int mFinishSize;
    private int mRetryTime;
    private String mErrorCode;
    private String mException;
    private boolean mIsFinish = false;

    public TaskThreadInfo(){
        this(null, 0, 0, 0);
    }

    public TaskThreadInfo(String taskId, int startPosition, int endPosition){
        this(taskId, startPosition, endPosition, 0);
    }

    public TaskThreadInfo(String taskId, int startPosition, int endPosition, int finishSize){
        mThreadId = null;
        mTaskId  = taskId;
        mStartPosition = startPosition;
        mEndPosition = endPosition;
        mFinishSize = finishSize;
        mRetryTime = 0;
        mErrorCode = null;
        mException = null;
        mIsFinish = false;
    }

    public void bind(Cursor cursor){
        mThreadId = String.valueOf(cursor.getLong(cursor.getColumnIndex(ThreadsColumns._ID)));
        mTaskId  = String.valueOf(cursor.getLong(cursor.getColumnIndex(ThreadsColumns.TASK_ID)));
        mStartPosition = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.START_POSITION));
        mEndPosition = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.END_POSITION));
        mFinishSize = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.FINISH_SIZE));
        mRetryTime = cursor.getInt(cursor.getColumnIndex(ThreadsColumns.RETRY_TIME));
        mErrorCode = cursor.getString(cursor.getColumnIndex(ThreadsColumns.ERROR_CODE));
        mException = cursor.getString(cursor.getColumnIndex(ThreadsColumns.EXCEPTION));
    }

    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();
        values.put(ThreadsColumns.TASK_ID, mTaskId);
        values.put(ThreadsColumns.START_POSITION, mStartPosition);
        values.put(ThreadsColumns.END_POSITION, mEndPosition);
        values.put(ThreadsColumns.FINISH_SIZE, mFinishSize);
        values.put(ThreadsColumns.RETRY_TIME, mRetryTime);
        values.put(ThreadsColumns.ERROR_CODE, mErrorCode);
        values.put(ThreadsColumns.EXCEPTION, mException);
        return values;
    }

    public String getThreadId() {
        return mThreadId;
    }

    public TaskThreadInfo setThreadId(String threadId) {
        this.mThreadId = threadId;
        return this;
    }

    public String getTaskId() {
        return mTaskId;
    }

    public TaskThreadInfo setTaskId(String taskId) {
        this.mTaskId = taskId;
        return this;
    }

    public int getStartPosition() {
        return mStartPosition;
    }

    public TaskThreadInfo setStartPosition(int startPosition) {
        this.mStartPosition = startPosition;
        return this;
    }

    public int getEndPosition() {
        return mEndPosition;
    }

    public TaskThreadInfo setEndPosition(int endPosition) {
        this.mEndPosition = endPosition;
        return this;
    }

    public int getFinishSize() {
        return mFinishSize;
    }

    public TaskThreadInfo setFinishSize(int finishSize) {
        this.mFinishSize = finishSize;
        return this;
    }

    public int getRetryTime() {
        return mRetryTime;
    }

    public TaskThreadInfo setRetryTime(int retryTime) {
        this.mRetryTime = retryTime;
        return this;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public TaskThreadInfo setErrorCode(String errorCode) {
        this.mErrorCode = errorCode;
        return this;
    }

    public String getException() {
        return mException;
    }

    public TaskThreadInfo setException(String exception) {
        this.mException = exception;
        return this;
    }

    public boolean isFinish(){
        return mIsFinish;
    }

    public boolean isComplete(){
        return mStartPosition + mFinishSize == mEndPosition;
    }

}
