package com.eebbk.bfc.sdk.downloadmanager;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.TaskState;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;

import java.util.HashMap;

/**
 * Desc: 下载任务配置和状态信息
 * Author: llp
 * Create Time: 2016-09-27 16:39
 * Email: jacklulu29@gmail.com
 */

public class DownloadTask implements ITask {

    private TaskParam mParam = new TaskParam();
    private TaskState mTaskState = new TaskState();
    private String mTag;

    private OnDownloadListener mDownloadListener;
    private OnCheckListener mCheckListener;
    private OnUnpackListener mUnpackListener;


    /**
     * 创建下载任务,建议使用
     *
     * @param url	下载地址
     */
    protected DownloadTask(@NonNull String url){
        mParam = mParam.setUrl(url);
    }

    /**
     * 创建下载任务,建议使用
     *
     * @param url	下载地址
     * @param fileName 文件名 注意：带后缀
     * @param savePath 保存路径 注意：不带文件名
     */
    protected DownloadTask( @NonNull String url , String fileName , String savePath){
        mParam.setUrl(url)
                .setFileName(fileName)
                .setSavePath(savePath);
    }

    /**
     * 创建下载任务,建议使用
     *
     * @param url 下载地址
     * @param fileName 文件名 注意：带后缀
     * @param fileSize 预设文件大小
     * @param fileExtension 文件后缀名
     * @param md5Code 文件校验MD5值
     */
    protected DownloadTask( @NonNull String url, String fileName, long fileSize, String fileExtension, String md5Code) {
        mParam.setUrl(url)
                .setFileName(fileName)
                .setPresetFileSize(fileSize)
                .setFileExtension(fileExtension)
                .setCheckCode(md5Code);
    }

    protected DownloadTask(TaskParam param){
        if(param == null){
            throw new IllegalArgumentException(" task param must not null! ");
        }
        mParam = param;
    }


    @Override
    public HashMap<String, String> getExtras() {
        return mParam.getExtrasMap();
    }

    @Override
    public TaskParam getTaskParam(){
        return this.mParam;
    }

    @Override
    public DownloadTask setTaskParam(TaskParam param) {
        this.mParam = param;
        return this;
    }

    @Override
    public TaskState getTaskState(){
        return mTaskState;
    }

    @Override
    public DownloadTask setTaskState(TaskState taskState){
        mTaskState = taskState;
        return this;
    }

    public DownloadTask setTaskState(TaskStateInfo stateInfo){
        if(stateInfo == null){
            return this;
        }
        mTaskState.setState(stateInfo.state)
                .setSpeed(stateInfo.speed)
                .setLastTime(stateInfo.lastTime)
                .setFileSize(stateInfo.totalSize)
                .setFinishSize(stateInfo.finishSize)
                .setErrorCode(stateInfo.errorCode)
                .setException(stateInfo.exception);
        return this;
    }

    //*******************************  对外公开的查询方法  ***********************************/

    @Override
    public String getUrl() {
        //返回转换过的原始地址
        String url = mParam.getUrl();
        if(!TextUtils.isEmpty(url) && CDNManager.isXYVodUrl(url)){
            return CDNManager.transformSourceUrl(url);
        }
        return url;
    }

    @Override
    public String getRealUrl() {
        // 返回真正的下载地址
        return mParam.getUrl();
    }

    @Override
    public String getFileName() {
        return mParam.getFileName();
    }

    @Override
    public String getFileExtension() {
        return mParam.getFileExtension();
    }

    @Override
    public String getSavePath() {
        return mParam.getSavePath();
    }

    @Override
    public long getPresetFileSize() {
        return mParam.getPresetFileSize();
    }

    @Override
    public boolean isAutoCheckSize() {
        return mParam.isAutoCheckSize();
    }

    @Override
    public int getPriority() {
        return mParam.getPriority();
    }

    @Override
    public String getCheckType() {
        return mParam.getCheckType();
    }

    @Override
    public String getCheckCode() {
        return mParam.getCheckCode();
    }

    @Override
    public boolean isCheckEnable() {
        return mParam.isCheckEnable();
    }

    @Override
    public boolean needCheckFile() {
        return mParam.needCheckFile();
    }

    @Override
    public int getNetworkTypes() {
        return mParam.getNetworkTypes();
    }

    @Override
    public boolean isAllowMobileNet() {
        return NetworkParseUtil.containsMobile(mParam.getNetworkTypes());
    }

    @Override
    public boolean isAllowWifiNet() {
        return NetworkParseUtil.containsWifi(mParam.getNetworkTypes());
    }

    @Override
    public boolean isAllowBluetoothNet() {
        return NetworkParseUtil.containsBluetooth(mParam.getNetworkTypes());
    }

    @Override
    public boolean isNeedQueue() {
        return mParam.isNeedQueue();
    }

    @Override
    public String getReserver() {
        return mParam.getReserver();
    }

    @Override
    public int getId() {
        return mParam.getGenerateId();
    }

    @Override
    public String getModuleName() {
        if(mParam == null){
            return null;
        }
        return mParam.getModuleName();
    }

    @Override
    public int getState() {
        return mTaskState.getState();
    }

    @Override
    public String getSpeed() {
        return DownloadUtils.getSpeedString(mTaskState.getSpeed(), "", 2);
    }

    @Override
    public long getSpeedNumber(){
        return mTaskState.getSpeed();
    }

    @Override
    public String getLastTime() {
        return DownloadUtils.formatLastTime(mTaskState.getLastTime(), "");
    }

    @Override
    public long getLastTimeSeconds() {
        return mTaskState.getLastTime();
    }

    @Override
    public long getFileSize() {
        return mTaskState.getFileSize();
    }

    @Override
    public long getFinishSize() {
        return mTaskState.getFinishSize();
    }

    @Override
    public String getReasonCode() {
        return mTaskState.getErrorCode();
    }

    @Override
    public String getErrorCode() {
        return mTaskState.getErrorCode();
    }

    @Override
    public Throwable getException() {
        return mTaskState.getException();
    }

    @Override
    public int getOriginState() {
        return mTaskState.getState();
    }

    @Override
    public String getMD5() {
        return mParam.getCheckCode();
    }

    @Override
    public String getNeedtime() {
        return getLastTime();
    }

    @Override
    public long getLoadedSize() {
        return getFinishSize();
    }

    @Override
    public boolean hasAllowAdjustSavePath() {
        return mParam.hasAllowAdjustSavePath();
    }

    @Override
    public int getNotificationVisibility() {
        return mParam.getNotificationVisibility();
    }

    @Override
    public boolean isShowRealTimeInfo() {
        return mParam.isShowRealTimeInfo();
    }

    @Override
    public int getMinProgressTime() {
        return mParam.getMinProgressTime();
    }

    @Override
    public boolean isAutoUnpack() {
        return mParam.isAutoUnpack();
    }

    @Override
    public String getUnpackPath() {
        return mParam.getUnpackPath();
    }

    @Override
    public boolean isDeleteNoEndTaskAndCache() {
        return mParam.isDeleteNoEndTaskAndCache();
    }

    @Override
    public boolean isDeleteEndTaskAndCache() {
        return mParam.isDeleteEndTaskAndCache();
    }

    @Override
    public int getDownloadThreads() {
        return mParam.getDownloadThreads();
    }

    @Override
    public boolean isPauseByUser(){
        return DownloadUtils.isPauseByUser(getState(), getErrorCode());
    }

    @Override
    public boolean isPauseByNetwork() {
        return DownloadUtils.isPauseByNetwork(getState(), getErrorCode());
    }

    @Override
    public boolean isPauseByOutOfSpace() {
        return DownloadUtils.isPauseByOutOfSpace(getState(), getErrorCode());
    }

    public void setTag(String tag){
        mTag = tag;
    }

    public String getTag(){
        return mTag;
    }

    //*************************************** extras ************************************/


    @Override
    public String getStringExtra(String name) {
        return mParam.getStringExtra(name);
    }

    @Override
    public int getIntExtra(String name, int defaultValue) {
        return mParam.getIntExtra(name, defaultValue);
    }

    @Override
    public boolean getBooleanExtra(String name, boolean defaultValue) {
        return mParam.getBooleanExtra(name, defaultValue);
    }

    @Override
    public float getFloatExtra(String name, float defaultValue) {
        return mParam.getFloatExtra(name, defaultValue);
    }

    @Override
    public double getDoubleExtra(String name, double defaultValue) {
        return mParam.getDoubleExtra(name, defaultValue);
    }

    @Override
    public char getCharExtra(String name, char defaultValue) {
        return mParam.getCharExtra(name, defaultValue);
    }

    @Override
    public byte getByteExtra(String name, byte defaultValue) {
        return mParam.getByteExtra(name, defaultValue);
    }

    @Override
    public byte[] getByteArrayExtra(String name) {
        return mParam.getByteArrayExtra(name);
    }

    @Override
    public short getShortExtra(String name, short defaultValue) {
        return mParam.getShortExtra(name, defaultValue);
    }


    //*******************************  listener  ***********************************/


    public DownloadTask setOnDownloadListener(OnDownloadListener downloadListener) {
        if(this.mDownloadListener != downloadListener){
            this.mDownloadListener = downloadListener;
        }
        return this;
    }

    public DownloadTask setOnCheckListener(OnCheckListener checkListener) {
        if(this.mCheckListener != checkListener){
            this.mCheckListener = checkListener;
        }
        return this;
    }

    public DownloadTask setOnUnpackListener(OnUnpackListener unpackListener) {
        if(this.mUnpackListener != unpackListener){
            this.mUnpackListener = unpackListener;
        }
        return this;
    }

    @Override
    public OnDownloadListener getOnDownloadListener() {
        return mDownloadListener;
    }

    @Override
    public OnCheckListener getOnCheckListener() {
        return mCheckListener;
    }

    @Override
    public OnUnpackListener getOnUnpackListener() {
        return mUnpackListener;
    }


    @Override
    public boolean isFinished() {
        return DownloadUtils.isTaskFinished(getState(), isCheckEnable(),
                getCheckType(), getCheckCode(), isAutoUnpack());
    }

    @Override
    public DownloadTask cloneTask() {
        DownloadTask task = (DownloadTask)new Builder(mParam).build();
        task.mTaskState = new TaskState(mTaskState);
        return task;
    }

    @Override
    public ITask updateData(ITask task) {
        mParam.copyData(task.getTaskParam());
        mTaskState.copyData(task.getTaskState());
        return this;
    }

    @Override
    public void recycle() {
        LogUtil.i(" recycle task: " + getId());
        if(getId() != ITask.INVALID_GENERATE_ID){
            DownloadController.getInstance().unregisterTaskListener(this);
        }
        mDownloadListener = null;
        mCheckListener = null;
        mUnpackListener = null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("DownloadTask [");
        sb.append("\r\n mParam: " + mParam);
        sb.append("\r\n mTaskState: " + mTaskState);
        sb.append("\r\n ]");
        return sb.toString();
    }

}
