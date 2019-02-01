package com.eebbk.bfc.sdk.download;

import android.support.annotation.Nullable;

/**
 * Desc: 下载任务临时状态信息
 * Author: llp
 * Create Time: 2016-11-01 10:35
 * Email: jacklulu29@gmail.com
 */

public class TaskState {

    /**
     * 状态
     */
    private int mState;
    /**
     * 速度 （单位 byte/s）
     */
    private long mSpeed;
    /**
     * 剩余时间 (单位秒 s)
     */
    private long mLastTime;
    /**
     * 下载文件真实大小
     */
    private long mFileSize;
    /**
     * 已完成大小
     */
    private long mFinishSize;
    /**
     * 错误码
     */
    private String mErrorCode;
    /**
     * 异常
     */
    private Throwable mException;

    public TaskState(){
        initData(Status.DOWNLOAD_INVALID, 0, -1, 0, 0, null, null);
    }

    public TaskState(TaskState other){
        if(other != null){
            copyData(other);
        } else {
            initData(Status.DOWNLOAD_INVALID, 0, -1, 0, 0, null, null);
        }
    }

    public void copyData(@Nullable TaskState other){
        if(other == null){
            return;
        }
        mState = other.mState;
        mSpeed = other.mSpeed;
        mLastTime = other.mLastTime;
        mFileSize = other.mFileSize;
        mFinishSize = other.mFinishSize;
        mErrorCode = other.mErrorCode;
        mException = other.mException;
    }

    public int getState() {
        return mState;
    }

    public TaskState setState(int mState) {
        this.mState = mState;
        return this;
    }

    public long getSpeed() {
        return mSpeed;
    }

    public TaskState setSpeed(long mSpeed) {
        this.mSpeed = mSpeed;
        return this;
    }

    public long getLastTime() {
        return mLastTime;
    }

    public TaskState setLastTime(long mLastTime) {
        this.mLastTime = mLastTime;
        return this;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public TaskState setFileSize(long mFileSize) {
        this.mFileSize = mFileSize;
        return this;
    }

    public long getFinishSize() {
        return mFinishSize;
    }

    public TaskState setFinishSize(long mFinishSize) {
        this.mFinishSize = mFinishSize;
        return this;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public TaskState setErrorCode(String errorCode) {
        this.mErrorCode = errorCode;
        return this;
    }

    public Throwable getException() {
        return mException;
    }

    public TaskState setException(Throwable mException) {
        this.mException = mException;
        return this;
    }

    public void onDownloadWaiting(){
        initData(Status.DOWNLOAD_WAITING, 0, -1, mFileSize, mFinishSize, null, null);
    }

    public void onDownloadStarted(){
        initData(Status.DOWNLOAD_STARTED, 0, -1, mFileSize, mFinishSize, null, null);
    }

    public void onDownloadConnected(long total, long finished){
        initData(Status.DOWNLOAD_CONNECTED, 0, -1, total, finished, null, null);
    }

    public void onDownloading(boolean showRealTimeInfo, long total, long finished, long speed){
        long lastTime = -1;
        if(showRealTimeInfo && total > 0 && speed > 0 && (total - finished) >= 0){
            lastTime = (long)Math.ceil((double)(total - finished) / (double)speed);
        }
        initData(Status.DOWNLOAD_PROGRESS, speed, lastTime, total, finished, null, null);
    }

    public void onDownloadPause(long total, long finished, String errorCode, Throwable throwable){
        initData(Status.DOWNLOAD_PAUSE, 0, -1, total, finished, errorCode, throwable);
    }

    public void onDownloadRetry(long total, long finished, String errorCode, Throwable throwable){
        initData(Status.DOWNLOAD_RETRY, 0, -1, total, finished, errorCode, throwable);
    }

    public void onDownloadFailure(long total, long finished, String errorCode, Throwable throwable){
        initData(Status.DOWNLOAD_FAILURE, 0, -1, total, finished, errorCode, throwable);
    }

    public void onDownloadSuccess(long total, long finished){
        initData(Status.DOWNLOAD_SUCCESS, 0, -1, total, finished, null, null);
    }

    public void onCheckStarted(long total, long finished) {
        initData(Status.CHECK_STARTED, 0, -1, total, finished, null, null);
    }

    public void onChecking(long total, long finished) {
        initData(Status.CHECK_PROGRESS, 0, -1, total, finished, null, null);
    }

    public void onCheckFailure(long total, long finished, String errorCode, Throwable throwable) {
        initData(Status.CHECK_FAILURE, 0, -1, total, finished, errorCode, throwable);
    }

    public void onCheckSuccess(long total, long finished) {
        initData(Status.CHECK_SUCCESS, 0, -1, total, finished, null, null);
    }

    public void onUnpackStarted(long total, long finished) {
        initData(Status.UNPACK_STARTED, 0, -1, total, finished, null, null);
    }

    public void onUnpacking(long total, long finished) {
        initData(Status.UNPACK_PROGRESS, 0, -1, total, finished, null, null);
    }

    public void onUnpackFailure(long total, long finished, String errorCode, Throwable throwable) {
        initData(Status.UNPACK_FAILURE, 0, -1, total, finished, errorCode, throwable);
    }

    public void onUnpackSuccess(long total, long finished) {
        initData(Status.UNPACK_SUCCESS, 0, -1, total, finished, null, null);
    }

    public void initData(int state, long speed, long lastTime, long total, long finished,
                          String errorCode, Throwable throwable){
        mState = state;
        mSpeed = speed;
        mLastTime = lastTime;
        mFileSize = total;
        mFinishSize = finished;
        mErrorCode = errorCode;
        mException = throwable;
    }
}
