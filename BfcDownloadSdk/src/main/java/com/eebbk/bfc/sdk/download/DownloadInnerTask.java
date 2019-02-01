package com.eebbk.bfc.sdk.download;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.eebbk.bfc.sdk.download.db.data.TaskParamInfo;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.download.util.LogUtil;

/**
 * Desc: 下载内部任务，保存任务信息和状态,负责转换内外部task对象
 * Author: llp
 * Create Time: 2016-10-08 10:57
 * Email: jacklulu29@gmail.com
 */

public class DownloadInnerTask extends DownloadQueue.DownloadTask implements Comparable<DownloadInnerTask> {

    /**
     * 任务阶段：下载
     */
    public static final int TASK_PHASE_DOWNLOAD = 1;
    /**
     * 任务阶段：校验
     */
    public static final int TASK_PHASE_CHECK = 2;
    /**
     * 任务阶段：解压
     */
    public static final int TASK_PHASE_UNPACK = 3;

    /**
     * 任务配置信息
     */
    private TaskParamInfo mTaskParamInfo;
    private TaskStateInfo mTaskStateInfo;

    private long mProgressStatisticsLatestTime = 0;
    private long mSpeedStatisticsLatestTime = 0;
    private long mSpeedStatisticsLatestSize = 0;

    private static int sCount = 0;
    private static final Object sBindLock = new Object();
    /**
     * 关联运行时线程id
     */
    private String mTaskRunnableId;

    /**
     * 任务已被删除标记
     */
    private boolean mTaskIsDeleted = false;

    public DownloadInnerTask(TaskParam taskParam){
        // 转换为内部task
        mTaskParamInfo = new TaskParamInfo(taskParam);
        mTaskStateInfo = new TaskStateInfo();
    }

    public TaskParam createTaskParam(){
        // 转换为外部task
        TaskParam taskParam = new TaskParam();
        mTaskParamInfo.copyDataToTaskParam(taskParam);
        return taskParam;
    }

    public DownloadInnerTask(Cursor cursor){
        mTaskParamInfo = new TaskParamInfo();
        mTaskParamInfo.bindFromDownloadTask(cursor);
        mTaskStateInfo = new TaskStateInfo();
        mTaskStateInfo.bindFromDownloadTask(cursor);
    }

    public void bindRunnable(@NonNull String runnableId){
        if(isBindRunnable()){
            if(isBindRunnable(runnableId)){
                LogUtil.d(" task already bind runnable: " + runnableId);
                return;
            } else {
                LogUtil.i(" task already bind old runnableId: " + mTaskRunnableId + " replace new runnableId: " + runnableId);
            }
        }
        synchronized (sBindLock){
            mTaskRunnableId = runnableId;
        }
    }

    public void unbindRunnable(){
        if(isBindRunnable()){
            synchronized (sBindLock){
                mTaskRunnableId = null;
            }
        }
    }

    public boolean isBindRunnable(){
        synchronized (sBindLock){
            return mTaskRunnableId != null;
        }
    }

    public boolean isBindRunnable(@NonNull String runnableId){
        synchronized (sBindLock){
            return runnableId.equals(mTaskRunnableId);
        }
    }

    public String getRunnableId(){
        synchronized (sBindLock){
            return mTaskRunnableId;
        }
    }

    public synchronized int getGenerateIdCount(){
        return sCount >= Integer.MAX_VALUE - 1 ? sCount=0 : ++sCount;
    }

    public String generateRunnableId(){
        return mTaskParamInfo.generateId + "|" + getGenerateIdCount();
    }

    @Override
    public int getGenerateId(){
        return mTaskParamInfo.generateId;
    }

    public String getModuleName(){
        return mTaskParamInfo.moduleName;
    }

    @Override
    public boolean isNeedQueue() {
        return mTaskParamInfo == null || mTaskParamInfo.needQueue;
    }

    public synchronized void setTaskIsDeleted() {
        this.mTaskIsDeleted = true;
    }

    public synchronized boolean isTaskIsDeleted() {
        return mTaskIsDeleted;
    }

    public ContentValues toParamsContentValues(){
        return mTaskParamInfo.toContentValues();
    }

    public ContentValues toTaskStateValues(){
        return mTaskStateInfo.toContentValues();
    }

    public TaskParamInfo getTaskParamInfo() {
        return mTaskParamInfo;
    }

    public DownloadInnerTask setTaskParamInfo(TaskParamInfo taskParamInfo) {
        this.mTaskParamInfo = taskParamInfo;
        return this;
    }

    public TaskStateInfo getTaskStateInfo() {
        return mTaskStateInfo;
    }

    public DownloadInnerTask setTaskStateInfo(TaskStateInfo taskStateInfo) {
        this.mTaskStateInfo = taskStateInfo;
        return this;
    }

    public void setTaskParamId(long paramId){
        this.mTaskParamInfo.paramsId = String.valueOf(paramId);
    }

    public synchronized int getState(){
        return mTaskStateInfo.state;
    }

    public int getTaskPhase(){
        return mTaskStateInfo.taskPhase;
    }

    public DownloadInnerTask setTaskPhase(int taskPhase){
        mTaskStateInfo.taskPhase = taskPhase;
        return this;
    }

    public synchronized DownloadInnerTask setState(int state){
        mTaskStateInfo.state = state;
        return this;
    }

    public boolean isDownloadComplete(){
        return getState() > Status.DOWNLOAD_FAILURE;
    }

    public boolean isTaskFinished(){
        return DownloadUtils.isTaskFinished(getState(), isCheckEnable(),
                getCheckType(), getCheckCode(), isAutoUnpack());
    }

    public boolean needCheckFile(){
        return DownloadUtils.needCheckFile(isCheckEnable(), getCheckType(), getCheckCode());
    }

    public boolean isWait(){
        return getState() == Status.DOWNLOAD_WAITING;
    }

    public boolean isPause(){
        return getState() == Status.DOWNLOAD_PAUSE;
    }

    @Override
    public int compareTo(@NonNull DownloadInnerTask another) {
        // 如果不需要排队，任务优先级最高
        if(!mTaskParamInfo.needQueue){
            return 1;
        } else if(!another.mTaskParamInfo.needQueue){
            return 1;
        } else {
            // 优先级越高，值越大，越排在前面
            // 同等优先级的情况，按先进先出的规则排序
            return this.getPriority() == another.getPriority() ?
                    this.getSequence() - another.getSequence() :
                    this.getPriority() - another.getPriority();
        }
    }

    /**
     * 删除所有临时文件
     */
    public void deleteAllFiles(){
        deleteTempFile();
        deleteTargetFile();
    }

    /**
     * 根据配置决定是否删除临时文件
     */
    public void deleteFiles(){
        if(mTaskParamInfo.deleteNoEndTaskAndCache){
            deleteTempFile();
        }
        if(mTaskParamInfo.deleteEndTaskAndCache){
            deleteTargetFile();
        }
    }

    private void deleteTempFile(){
        final String tempFilePath = getTempFilePath();
        boolean result = FileUtil.deleteFile(tempFilePath);
        LogUtil.i(DownloadUtils.formatString(" runnable id[%s] delete temp file[%s], result: %s", getGenerateId(), tempFilePath, result));
    }

    private void deleteTargetFile(){
        final String targetFilePath = getTargetFilePath();
        boolean result = FileUtil.deleteFile(targetFilePath);
        LogUtil.i(DownloadUtils.formatString(" runnable id[%s] delete target file[%s], result: %s", getGenerateId(), targetFilePath, result));
    }

    /**
     * 设置下载地址（重定向时用到）
     * @param url 下载url
     */
    public DownloadInnerTask setUrl(String url){
        mTaskParamInfo.url = url;
        return this;
    }

    /**
     * 获取已完成大小
     * @return 已完成大小
     */
    public synchronized long getFinishSize(){
        return mTaskStateInfo.finishSize;
    }

    public synchronized DownloadInnerTask setFinishSize(long finishSize){
        mTaskStateInfo.finishSize = finishSize;
        return this;
    }

    /**
     * 设置文件总大小
     * @param totalSize 总大小
     */
    public synchronized DownloadInnerTask setTotalSize(long totalSize){
        mTaskStateInfo.totalSize = totalSize;
        return this;
    }

    /**
     * 获取文件总大小
     * @return 文件总大小
     */
    public synchronized long getTotalSize(){
        return mTaskStateInfo.totalSize;
    }

    public String getFileName() {
        return mTaskParamInfo.fileName;
    }

    public DownloadInnerTask setFileName(String fileName) {
        mTaskParamInfo.fileName = fileName;
        return this;
    }

    public String getFileExtension() {
        return mTaskParamInfo.fileExtension;
    }

    public DownloadInnerTask setFileExtension(String extension) {
        mTaskParamInfo.fileExtension = extension;
        return this;
    }

    /**
     * 修改任务网络类型
     * @param networkTypes 网络类型
     */
    public synchronized DownloadInnerTask editNetworkTypes(final int networkTypes){
        mTaskParamInfo.networkTypes = networkTypes;
        return this;
    }

    public synchronized DownloadInnerTask setProgressStatisticsLatestTime(long progressStatisticsLatestTime) {
        this.mProgressStatisticsLatestTime = progressStatisticsLatestTime;
        return this;
    }

    public synchronized long getProgressStatisticsLatestTime() {
        return mProgressStatisticsLatestTime;
    }

    public synchronized DownloadInnerTask setSpeedStatisticsLatestTime(long speedStatisticsLatestTime) {
        this.mSpeedStatisticsLatestTime = speedStatisticsLatestTime;
        return this;
    }

    /**
     * 获取最近的速度统计时间
     * @return 最近统计时间
     */
    public synchronized long getSpeedStatisticsLatestTime() {
        return mSpeedStatisticsLatestTime;
    }

    public long getSpeedStatisticsLatestSize() {
        return mSpeedStatisticsLatestSize;
    }

    public DownloadInnerTask setSpeedStatisticsLatestSize(long speedStatisticsLatestSize) {
        this.mSpeedStatisticsLatestSize = speedStatisticsLatestSize;
        return this;
    }

    public String getETag(){
        return mTaskStateInfo.eTag;
    }

    public DownloadInnerTask setETag(String etag){
        mTaskStateInfo.eTag = etag;
        return this;
    }

    public String getTargetFilePath() {
        return DownloadUtils.getTargetFilePath(mTaskParamInfo.savePath, true, mTaskParamInfo.fileName, mTaskParamInfo.fileExtension);
    }

    public String getTempFilePath() {
        if (getTargetFilePath() == null) {
            return null;
        }

        return DownloadUtils.getTempPath(getTargetFilePath(), DownloadInitHelper.getInstance().getDefaultModuleName());
    }

    public String getUrl() {
        return mTaskParamInfo.url;
    }

    public String getSavePath() {
        return mTaskParamInfo.savePath;
    }

    public long getPresetFileSize() {
        return mTaskParamInfo.presetFileSize;
    }

    public boolean isAutoCheckSize() {
        return mTaskParamInfo.autoCheckSize;
    }

    public int getPriority() {
        return mTaskParamInfo.priority;
    }

    public String getCheckType() {
        return mTaskParamInfo.checkType;
    }

    public String getCheckCode() {
        return mTaskParamInfo.checkCode;
    }

    public boolean isCheckEnable() {
        return mTaskParamInfo.checkEnable;
    }

    public int getNetworkTypes() {
        return mTaskParamInfo.networkTypes;
    }

    public String getReserver() {
        return mTaskParamInfo.reserver;
    }

    public int getNotificationVisibility() {
        return mTaskParamInfo.notificationVisibility;
    }

    public boolean isAllowAdjustSavePath() {
        return mTaskParamInfo.allowAdjustSavePath;
    }

    /**
     * 是否需要实时计算当前的下载速度和下载所需时间
     * @return
     */
    public boolean isShowRealTimeInfo(){
        return mTaskParamInfo.showRealTimeInfo;
    }

    public int getScheduleIntervalTimeByMs(){
        return mTaskParamInfo.minProgressTime;
    }

    public boolean isAutoUnpack() {
        return mTaskParamInfo.autoUnpack;
    }

    public String getUnpackPath() {
        return mTaskParamInfo.unpackPath;
    }

    public DownloadInnerTask setUnpackPath(String unpackPath) {
        mTaskParamInfo.unpackPath = unpackPath;
        return this;
    }

    public boolean isDeleteSourceAfterUnpack() {
        return mTaskParamInfo.deleteSourceAfterUnpack;
    }

    public boolean isDeleteNoEndTaskAndCache() {
        return mTaskParamInfo.deleteNoEndTaskAndCache;
    }

    public boolean isDeleteEndTaskAndCache() {
        return mTaskParamInfo.deleteEndTaskAndCache;
    }

    public int getDownloadThreads() {
        return mTaskParamInfo.downloadThreads;
    }

    public boolean isAllowRoaming() {
        return mTaskParamInfo.allowRoaming;
    }



    public synchronized long getSpeed() {
        return mTaskStateInfo.speed;
    }

    public synchronized DownloadInnerTask setSpeed(long speed){
        mTaskStateInfo.speed = speed;
        return this;
    }

    public synchronized long getLastTime() {
        return mTaskStateInfo.lastTime;
    }

    public synchronized DownloadInnerTask setLastTime(long lastTime){
        mTaskStateInfo.lastTime = lastTime;
        return this;
    }

    public synchronized int getRetryTime() {
        return mTaskStateInfo.retryTime;
    }

    public synchronized DownloadInnerTask setRetryTime(int retryTime){
        mTaskStateInfo.retryTime = retryTime;
        return this;
    }

    public String getErrorCode() {
        return mTaskStateInfo.errorCode;
    }

    public DownloadInnerTask setErrorCode(String errorCode){
        mTaskStateInfo.errorCode = errorCode;
        return this;
    }

    public Throwable getException() {
        return mTaskStateInfo.exception;
    }

    public DownloadInnerTask setException(Throwable throwable){
        mTaskStateInfo.exception = throwable;
        return this;
    }

    public DownloadInnerTask setDownloadFinishTime(long downloadFinishTime) {
        mTaskStateInfo.downloadFinishTime = downloadFinishTime;
        return this;
    }

    public DownloadInnerTask setCheckFinishTime(long checkFinishTime) {
        mTaskStateInfo.checkFinishTime = checkFinishTime;
        return this;
    }

    public DownloadInnerTask setUnpackFinishTime(long unpackFinishTime) {
        mTaskStateInfo.unpackFinishTime = unpackFinishTime;
        return this;
    }

    public DownloadInnerTask setBuildTime(long buildTime) {
        mTaskStateInfo.buildTime = buildTime;
        return this;
    }

}
