package com.eebbk.bfc.sdk.download;

import android.support.annotation.Nullable;

import com.eebbk.bfc.sdk.download.check.BaseValidator;
import com.eebbk.bfc.sdk.download.check.MD5ExValidator;
import com.eebbk.bfc.sdk.download.check.MD5Validator;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.unpack.DownloadBaseUnpacker;
import com.eebbk.bfc.sdk.download.unpack.DownloadUnpacker;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc: 下载全局设置
 * Author: llp
 * Create Time: 2016-09-26 18:04
 * Email: jacklulu29@gmail.com
 */

public class GlobalConfig {

    /**
     * 是否启用调试模式
     */
    private boolean mDebug;

    /**
     * 日志保存路径，开启debug模式有效
     */
    private String mSaveLogPath;

    /*******************        下载部分        ******************/
    /**
     * 默认网络类型
     */
    private int mNetWorkType;

    /**
     * 下载保存路径，此路径不可用时将会启用默认保存路径
     */
    private String mSavePath;
    /**
     * 同时下载任务数
     */
    private int mDownloadTaskCount;
    /**
     * 最大不排队任务数量，超过将不能添加
     */
    private int mMaxNoNeedQueueTasks;

    /**
     * <pre>下载进度回调间隔时间，单位毫秒，默认值{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MIN_PROGRESS_TIME}
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     * 如果设置<0,将不会回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即没有进度消息
     * 设置=0，将会实时回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即时回调进度消息
     * 设置>0,将会根据设置的间隔时间进行回调
     * </pre>
     */
    private int mMinProgressTime;
    /**
     * 是否实时显示下载速度、剩余时间等
     */
    private boolean mShowDownloadRealTime;
    /**
     * 断点下载模式下，多线程下载数量
     */
    private int mDownloadThreads;
    /**
     * 应用退出后是否启用后台服务下载（后期考虑，默认否）
     */
    private boolean mRunInBackgroundAfterAppExit;
    /**
     * 删除未下载完成的任务时是否关联删除下载文件
     */
    private boolean mDeleteFileDownloadNoEnd;
    /**
     * 删除已下载完成的任务时是否要关联删除下载文件
     */
    private boolean mDeleteFileDownloadEnd;

    /**
     * 最大下载失败重试次数
     */
    private int mMaxRetries;

    /**
     * 最大重定向次数
     */
    private int mMaxRedirects;

    /**
     * The maximum number of rows in the database (FIFO)
     */
    private int mMaxDownloadsInDb;

    /*******************        校验部分        ******************/
    /**
     * 是否实时显示校验速度、剩余时间等
     */
    private boolean mShowVerifyRealTime;

    /*******************        解压部分        ******************/
    /**
     * 是否自动解压
     */
    private boolean mAutoUnpack;
    /**
     * 解压保存路径
     */
    private String mUnpackPath;
    /**
     * 是否实时显示解压速度、剩余时间等
     */
    private boolean mShowUnpackRealTime;
    /**
     * 下载模式
     */
    private int mDownloadMode;
    /**
     * 文件是否支持seek，能设置文件总大小
     */
    private boolean mFilePreAllocation;

    /**
     * 下载文件是否自动校验文件大小，预设文件大小必须 > 0 才会生效
     */
    private boolean mAutoCheckSize;
    /**
     * 是否进行校验
     */
    private boolean mCheckEnable;
    /**
     * 是否进行排队，false时任务将不会等待，直接进行下载，不影响下载任务数量， 有最大限制，默认为{@link C.DownLoadConfig#DEFAULT_MAX_NO_NEED_QUEUE_TASKS}
     */
    private boolean mNeedQueue;
    /**
     * 通知显示方式
     */
    private int mNotificationVisibility;
    /**
     * 是否允许修改保存路径
     */
    private boolean mAllowAdjustSavePath;
    /**
     * 解压完成后是否删除源文件
     */
    private boolean mDeleteSourceAfterUnpack;

    /**
     * 任务空闲时，停止下载服务延迟时间
     */
    private int mStopServiceDelayTimeIfIdle;

    /**
     * 是否允许使用中国电信2g网络，默认false，
     * 允许使用电信2g网络，可能会造成手机无法打电话
     */
    private boolean mIsAllowMobileNet2g;

    private boolean mIsNeedScanMedia;

    /**
     * 校验器列表
     */
    private List<BaseValidator.Creator> mValidators;

    /**
     * 解压器
     */
    private DownloadBaseUnpacker.Creator mUnpackCreator;

    private GlobalConfig(){
        mDebug = false;

        mNetWorkType = NetworkType.DEFAULT_NETWORK;
        mSavePath = FileUtil.getDefaultSaveRootPath();
        mDownloadTaskCount = C.DownLoadConfig.DEFAULT_DOWNLOAD_TASK_COUNT;
        mDownloadThreads = C.DownLoadConfig.DEFAULT_DOWNLOAD_THREAD_COUNT;
        mMaxNoNeedQueueTasks = C.DownLoadConfig.DEFAULT_MAX_NO_NEED_QUEUE_TASKS;
        mRunInBackgroundAfterAppExit = false;
        mDeleteFileDownloadNoEnd = true;
        mDeleteFileDownloadEnd = false;
        mMaxRetries = C.DownLoadConfig.DEFAULT_MAX_RETRIES;
        mMaxRedirects = C.DownLoadConfig.DEFAULT_MAX_REDIRECTS;
        mMaxDownloadsInDb = C.DownLoadConfig.DEFAULT_MAX_DOWNLOADS;
        mMinProgressTime = C.DownLoadConfig.DEFAULT_MIN_PROGRESS_TIME;
        mShowDownloadRealTime = false;

        mShowVerifyRealTime = false;

        mAutoUnpack = false;
        mShowUnpackRealTime = false;

        mDownloadMode = C.DownloadMode.BREAK_POINT;

        mFilePreAllocation = false;

        mAutoCheckSize = true;
        mCheckEnable = true;
        mNeedQueue = true;
        mNotificationVisibility = ITask.Notification.VISIBILITY_HIDDEN;
        mAllowAdjustSavePath = false;
        mDeleteSourceAfterUnpack = true;

        mStopServiceDelayTimeIfIdle = C.DownLoadConfig.STOP_SERVICE_DELAY_TIME_IF_IDLE;

        mIsAllowMobileNet2g = true;

        mIsNeedScanMedia = true;

        // 初始化校验器
        mValidators = new ArrayList<>();
        mValidators.add(new MD5Validator.MD5ValidatorCreator());
        mValidators.add(new MD5ExValidator.MD5ExValidatorCreator());

        // 初始化解压器
        mUnpackCreator = new DownloadUnpacker.Creator();
    }

    /**
     * 下载配置构造器，方便用户进行配置
     */
    public static class Builder {

        private GlobalConfig config;

        public Builder(){
            config = new GlobalConfig();
        }

        /**
         * 设置是否启用调试模式，调试模式将会打印详细日志、
         * 默认false
         *
         * @param debugModel 调试模式
         * @return 全局配置构造器
         */
        public Builder setDebug(boolean debugModel){
            config.mDebug = debugModel;
            return this;
        }

        /**
         * 设置日志保存地址<br/>
         * 默认不保存<br/>
         * 注意：setDebug(true)才生效,需要声明存储卡读写权限
         * @param saveLogPath 日志保存地址
         * @return 全局配置构造器
         */
        public Builder setSaveLogPath(String saveLogPath){
            config.mSaveLogPath = saveLogPath;
            return this;
        }

        /**
         * 设置全局网络类型，将会作为下载任务的默认网络类型，默认为{@link NetworkType#DEFAULT_NETWORK}
         * @param networkType 网络类型
         * @return 全局配置构造器
         */
        public Builder setNetworkType(int networkType){
            config.mNetWorkType = networkType;
            return this;
        }

        /**
         * 设置默认保存路径
         * @param savePath 保存路径
         * @return 全局配置构造器
         */
        public Builder setSavePath(String savePath){
            config.mSavePath = savePath;
            return this;
        }

        /**
         * 设置可以同时下载的任务数量，默认为{@link C.DownLoadConfig#DEFAULT_DOWNLOAD_TASK_COUNT}<br/>
         * 注意：暂不开放此接口
         * @param downloadTaskCount 同时下载任务数量
         * @return 全局配置构造器
         */
        private Builder setDownloadTaskCount(int downloadTaskCount){
            config.mDownloadTaskCount = downloadTaskCount;
            return this;
        }

        /**
         *  设置最大不排队任务数量，超过将不能添加(默认5个)<br/>
         *  注意：暂不开放此接口
         * @param count 最大不排队任务数量
         * @return 全局配置构造器
         */
        public Builder setMaxNoNeedQueueTasks(int count){
            if(count > C.DownLoadConfig.DEFAULT_MAX_NO_NEED_QUEUE_TASKS){
                count = C.DownLoadConfig.DEFAULT_MAX_NO_NEED_QUEUE_TASKS;
            }
            config.mMaxNoNeedQueueTasks = count;
            return this;
        }


        /**
         * 是否实时显示下载速度、剩余时间等;默认false
         * 启用将可能对下载效率有微小的影响
         * @param show true显示下载速度、剩余时间
         * @return 全局配置构造器
         * @deprecated 已过时，请调用{@link #setMinProgressTime(int)}替换
         */
        public Builder setShowDownloadRealTime(boolean show){
            config.mShowDownloadRealTime = show;
            return this;
        }

        /**
         * <pre>设置进度更新最小间隔时间，单位毫秒;默认值{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MIN_PROGRESS_TIME}
         * 同时会影响到下载速度、剩余时间的统计以及回调显示
         * 如果设置<0,将不会回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即没有进度消息
         * 设置=0，将会实时回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即时回调进度消息
         * 设置>0,将会根据设置的间隔时间进行回调
         * </pre>
         * @param mMinProgressTime 进度更新最小间隔时间 毫秒
         * @return  全局配置构造器
         */
        public Builder setMinProgressTime(int mMinProgressTime) {
            config.mMinProgressTime = mMinProgressTime;
            return this;
        }

        /**
         * 设置多线程下载数量，默认为{@link C.DownLoadConfig#DEFAULT_DOWNLOAD_THREAD_COUNT}
         * 启动断点下载才有效，且下载服务器必须支持
         *
         * @param downloadThreadCount 多线程下载数量
         * @return 全局配置构造器
         */
        public Builder setDownloadThreads(int downloadThreadCount){
            config.mDownloadThreads = downloadThreadCount;
            return this;
        }

        /**
         * 应用退出后是否启用后台服务下载（后期考虑，默认false）
         *
         * @param exitContinue 应用退出后是否继续
         * @return 全局配置构造器
         */
        private Builder setRunInBackgroundAfterAppExit(boolean exitContinue){
            config.mRunInBackgroundAfterAppExit = exitContinue;
            return this;
        }

        /**
         * 删除未下载完成的任务时是否关联删除下载文件;
         * 默认true
         *
         * @param deleteFile true删除
         * @return 全局配置构造器
         */
        public Builder setDeleteFileDownloadNoEnd(boolean deleteFile){
            config.mDeleteFileDownloadNoEnd = deleteFile;
            return this;
        }

        /**
         * 删除已下载完成的任务时是否要关联删除下载文件;
         * 默认false
         *
         * @param deleteFile true删除
         * @return 全局配置构造器
         */
        public Builder setDeleteFileDownloadEnd(boolean deleteFile){
            config.mDeleteFileDownloadNoEnd = deleteFile;
            return this;
        }

        /**
         * 是否实时显示校验速度、剩余时间等
         * 默认false
         * 开启将可能会对校验有微小的影响
         * @param showVerifyRealTime true显示
         * @return 全局配置构造器
         */
        public Builder setShowVerifyRealTime(boolean showVerifyRealTime){
            config.mShowVerifyRealTime = showVerifyRealTime;
            return this;
        }

        /**
         * 是否自动解压
         * 默认true
         * @param autoUnpack true自动解压
         * @return 全局配置构造器
         */
        public Builder setAutoUnpack(boolean autoUnpack){
            config.mAutoUnpack = autoUnpack;
            return this;
        }

        /**
         * 设置解压文件保存路径
         * @param unpackPath 解压路径
         * @return 全局配置构造器
         */
        public Builder setUnpackPath(String unpackPath){
            config.mUnpackPath = unpackPath;
            return this;
        }

        /**
         * 是否实时显示解压速度、剩余时间等
         * 默认false
         * @param showUnpackRealTime true显示
         * @return 全局配置构造器
         */
        public Builder setShowUnpackRealTime(boolean showUnpackRealTime){
            config.mShowUnpackRealTime = showUnpackRealTime;
            return this;
        }

        /**
         * 设置下载失败最大重试次数;默认{@link C.DownLoadConfig#DEFAULT_MAX_RETRIES}
         *
         * @param maxRetries 最大重试次数
         * @return 全局配置构造器
         */
        public Builder setMaxRetries(int maxRetries) {
            config.mMaxRetries = maxRetries;
            return this;
        }

        /**
         * 设置在数据库中下载任务最大保存条数;默认{@link C.DownLoadConfig#DEFAULT_MAX_DOWNLOADS}
         *
         * @param mMaxDownloadsInDb 最大保存条数
         * @return 全局配置构造器
         */
        public Builder setMaxDownloadsInDb(int mMaxDownloadsInDb) {
            config.mMaxDownloadsInDb = mMaxDownloadsInDb;
            return this;
        }

        /**
         * 设置是否自动校验下载文件大小，预设文件大小必须 > 0 才会生效
         * 设置为true将会自动比较预设文件大小与真实大小是否相等;默认true
         *
         * @param autoCheckSize 自动比对文件大小
         * @return 全局配置构造器
         */
        public Builder setAutoCheckSize(boolean autoCheckSize) {
            config.mAutoCheckSize = autoCheckSize;
            return this;
        }

        /**
         * 设置是否进行文件完整性校验， true进行校验，false不校验;默认true
         *
         * @param checkEnable true校验，false不校验
         * @return 全局配置构造器
         */
        public Builder setCheckEnable(boolean checkEnable) {
            config.mCheckEnable = checkEnable;
            return this;
        }

        /*
         * 设置通知显示方式，后期开放
        public Builder setNotificationVisibility(int notificationVisibility) {
            config.notificationVisibility = notificationVisibility;
            return this;
        }
        */

        /**
         * 设置是否允许修改保存路径，true允许，false不允许;默认false
         *
         * @param allowAdjustSavePath true允许，false不允许
         * @return 全局配置构造器
         */
        public Builder setAllowAdjustSavePath(boolean allowAdjustSavePath) {
            config.mAllowAdjustSavePath = allowAdjustSavePath;
            return this;
        }

        /**
         * 设置解压完成后是否删除源文件，true删除，false不删除;默认true
         *
         * @param deleteSourceAfterUnpack true删除，false不删除
         * @return 全局配置构造器
         */
        public Builder setDeleteSourceAfterUnpack(boolean deleteSourceAfterUnpack) {
            config.mDeleteSourceAfterUnpack = deleteSourceAfterUnpack;
            return this;
        }

        /**
         * 设置下载兼容模式，设置的越低兼容的版本越多。
         *
         * @param downloadMode 下载模式
         * @return 全局配置构造器
         */
        public Builder setDownloadMode(int downloadMode) {
            config.mDownloadMode = downloadMode;
            return this;
        }

        /**
         * 设置是否允许使用2g网络，默认true，
         * 允许使用电信2g网络，如果当前手机是电信2g的，可能会造成手机无法打电话
         *
         * @param allowMobileNet2g true为允许
         * @return 全局配置构造器
         */
        public Builder setAllowMobileNet2g(boolean allowMobileNet2g) {
            config.mIsAllowMobileNet2g = allowMobileNet2g;
            return this;
        }

        /**
         * 设置是否需要刷新媒体库，默认true，
         *
         * @param needScanMedia true为需要
         * @return 全局配置构造器
         */
        public Builder setNeedScanMedia(boolean needScanMedia) {
            config.mIsNeedScanMedia = needScanMedia;
            return this;
        }

        /**
         * 设置解压构造器
         * @param creator 解压构造器
         * @return 全局配置构造器
         */
        public Builder setUnpacker(@Nullable DownloadBaseUnpacker.Creator creator){
            config.mUnpackCreator = creator;
            return this;
        }

        /**
         * 添加校验器
         * @param creator 校验构造器
         * @return 全局配置构造器
         */
        public Builder addValidator(@Nullable BaseValidator.Creator creator){
            if(creator == null){
                return this;
            }
            if(config.mValidators == null){
                config.mValidators = new ArrayList<>();
            }
            config.mValidators.add(creator);
            return this;
        }

        /**
         * 创建全局配置
         * @return 全局配置
         */
        public GlobalConfig build(){
            return config;
        }

    }

    public boolean isDebug() {
        return mDebug;
    }

    public String getSaveLogPath() {
        return mSaveLogPath;
    }

    public int getNetWorkType() {
        return mNetWorkType;
    }

    public boolean isAllowMobile2g(){
        return mIsAllowMobileNet2g;
    }

    public boolean isNeedScanMedia(){
        return mIsNeedScanMedia;
    }

    public String getSavePath() {
        return mSavePath;
    }

    public int getDownloadTaskCount() {
        return mDownloadTaskCount;
    }


    public boolean isShowDownloadRealTime() {
        return mShowDownloadRealTime;
    }

    public int getMinProgressTime(){
        return mMinProgressTime;
    }

    public int getDownloadThreads() {
        return mDownloadThreads;
    }

    private boolean isRunInBackgroundAfterAppExit() {
        return mRunInBackgroundAfterAppExit;
    }

    public boolean isDeleteFileDownloadNoEnd() {
        return mDeleteFileDownloadNoEnd;
    }

    public boolean isDeleteFileDownloadEnd() {
        return mDeleteFileDownloadEnd;
    }

    public boolean isShowVerifyRealTime() {
        return mShowVerifyRealTime;
    }

    public boolean isAutoUnpack() {
        return mAutoUnpack;
    }

    public String getUnpackPath() {
        return mUnpackPath;
    }

    public boolean isShowUnpackRealTime() {
        return mShowUnpackRealTime;
    }

    public int getMaxRetries() {
        return mMaxRetries;
    }

    public int getMaxRedirects() {
        return mMaxRedirects;
    }

    public int getMaxDownloadsInDb() {
        return mMaxDownloadsInDb;
    }

    public int getMaxNoNeedQueueTasks() {
        return mMaxNoNeedQueueTasks;
    }

    public int getDownloadMode() {
        return mDownloadMode;
    }

    public boolean isFilePreAllocation() {
        return mFilePreAllocation;
    }

    public boolean isAutoCheckSize() {
        return mAutoCheckSize;
    }

    public boolean isCheckEnable() {
        return mCheckEnable;
    }

    public boolean isNeedQueue() {
        return mNeedQueue;
    }

    public int getNotificationVisibility() {
        return mNotificationVisibility;
    }

    public boolean isAllowAdjustSavePath() {
        return mAllowAdjustSavePath;
    }

    public boolean isDeleteSourceAfterUnpack() {
        return mDeleteSourceAfterUnpack;
    }

    public int getStopServiceDelayTimeIfIdle() {
        return mStopServiceDelayTimeIfIdle;
    }

    public List<BaseValidator.Creator> getValidators() {
        return mValidators;
    }

    public DownloadBaseUnpacker.Creator getUnpackCreator() {
        return mUnpackCreator;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(" GlobalConfig [");
        sb.append("\r\n mDebug:" + mDebug);
        sb.append("\r\n mNetWorkType:" + mNetWorkType);
        sb.append("\r\n savePath:" + mSavePath);
        sb.append("\r\n mDownloadTaskCount:" + mDownloadTaskCount);
        sb.append("\r\n mMaxNoNeedQueueTasks:" + mMaxNoNeedQueueTasks);
        sb.append("\r\n downloadThreads:" + mDownloadThreads);
        sb.append("\r\n mRunInBackgroundAfterAppExit:" + mRunInBackgroundAfterAppExit);
        sb.append("\r\n mDeleteFileDownloadNoEnd:" + mDeleteFileDownloadNoEnd);
        sb.append("\r\n mDeleteFileDownloadEnd:" + mDeleteFileDownloadEnd);
        sb.append("\r\n mMaxRetries:" + mMaxRetries);
        sb.append("\r\n mMaxRedirects:" + mMaxRedirects);
        sb.append("\r\n mMaxDownloadsInDb:" + mMaxDownloadsInDb);
        sb.append("\r\n minProgressTime:" + mMinProgressTime);
        sb.append("\r\n init mShowDownloadRealTime:" + mShowDownloadRealTime);
        sb.append("\r\n mShowVerifyRealTime:" + mShowVerifyRealTime);
        sb.append("\r\n autoUnpack:" + mAutoUnpack);
        sb.append("\r\n mShowUnpackRealTime:" + mShowUnpackRealTime);
        sb.append("\r\n mDownloadMode:" + mDownloadMode);
        sb.append("\r\n mFilePreAllocation:" + mFilePreAllocation);

        sb.append("\r\n autoCheckSize:" + mAutoCheckSize);
        sb.append("\r\n checkEnable:" + mCheckEnable);
        sb.append("\r\n needQueue:" + mNeedQueue);
        sb.append("\r\n notificationVisibility:" + mNotificationVisibility);
        sb.append("\r\n allowAdjustSavePath:" + mAllowAdjustSavePath);
        sb.append("\r\n deleteSourceAfterUnpack:" + mDeleteSourceAfterUnpack);
        sb.append("\r\n ]");
        return sb.toString();
    }
}
