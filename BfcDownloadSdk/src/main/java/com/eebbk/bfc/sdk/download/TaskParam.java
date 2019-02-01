package com.eebbk.bfc.sdk.download;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eebbk.bfc.common.app.SharedPreferenceUtils;
import com.eebbk.bfc.sdk.download.exception.DownloadNoInitException;
import com.eebbk.bfc.sdk.download.net.CdnFlagBean;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.ExtrasConverter;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.CDNManager;
import com.eebbk.bfc.sdk.downloadmanager.ITask;
import com.eebbk.bfc.sequence.SequenceTools;

import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.HashMap;

/**
 * Desc: 下载任务参数信息,创建此实例对象必须先讲下载器初始化
 * Author: llp
 * Create Time: 2016-09-28 21:35
 * Email: jacklulu29@gmail.com
 */
public class TaskParam implements Parcelable {


    private int mGenerateId = ITask.INVALID_GENERATE_ID;
    /**
     * 下载地址
     */
    private String mUrl;

    /**
     * 下载文件名称
     */
    private String mFileName;
    /**
     * 下载文件扩展名
     */
    private String mFileExtension;
    /**
     * 下载文件保存路径
     */
    private String mSavePath;
    /**
     * 预设下载文件大小
     */
    private long mFileSize = -1;
    /**
     * 下载文件是否自动校验文件大小，必须{@link #mFileSize} > 0
     */
    private boolean mAutoCheckSize;
    /**
     * 任务优先级
     */
    private int mPriority = 0;
    /**
     * 文件校验类型，比如{@link ITask.CheckType#MD5}, {@link ITask.CheckType#HASH}
     */
    private String mCheckType = ITask.CheckType.MD5;
    /**
     * 文件校验码
     */
    private String mCheckCode;
    /**
     * 是否进行校验
     */
    private boolean mCheckEnable;
    /**
     * 下载可用的网络类型：{@link NetworkType#NETWORK_WIFI}, {@link NetworkType#NETWORK_MOBILE}, {@link NetworkType#NETWORK_BLUETOOTH},
     */
    private int mNetworkTypes;
    /**
     * 是否进行排队，false时任务将不会等待，直接进行下载，不影响下载任务数量， 有最大限制，默认为{@link C.DownLoadConfig#DEFAULT_MAX_NO_NEED_QUEUE_TASKS}
     */
    private boolean mNeedQueue;
    /**
     * 保留字段
     */
    private String mReserver;
    /**
     * 扩展字段的数据
     */
    private HashMap<String, String> mExtrasMap = new HashMap<>();
    /**
     * 通知显示方式
     */
    private int mNotificationVisibility;
    /**
     * 是否允许修改保存路径
     */
    private boolean mAllowAdjustSavePath;

    /**
     * 下载进度回调间隔时间，单位毫秒，(默认值{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MIN_PROGRESS_TIME})。
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     */
    private int mMinProgressTime;

    /**
     * 是否显示实时状态（下载速度、剩余时间）（默认是）
     */
    private boolean mShowRealTimeInfo;

    /**
     * 是否自动解压（默认否）
     */
    private boolean mAutoUnpack;
    /**
     * 解压保存路径
     */
    private String mUnpackPath;
    /**
     * 解压完成后是否删除源文件
     */
    private boolean mDeleteSourceAfterUnpack;
    /**
     * 删除未下载完成任务时同时删除缓存文件（默认是）
     */
    private boolean mDeleteNoEndTaskAndCache;
    /**
     * 删除已下载完成任务时同时删除文件（默认否）
     */
    private boolean mDeleteEndTaskAndCache;
    /**
     * 下载任务开启多线程数量
     */
    private int mDownloadThreads;

    /**
     * 模块名称
     */
    private String mModuleName;


    public TaskParam() {
        initFromGlobalConfig();
    }

    private void initFromGlobalConfig(){

        GlobalConfig config = DownloadInitHelper.getInstance().getGlobalConfig();
        if (config == null){
            return;
        }
        mSavePath = config.getSavePath();
        mAutoCheckSize = config.isAutoCheckSize();
        mCheckEnable = config.isCheckEnable();
        mNetworkTypes = config.getNetWorkType();
        mNeedQueue = config.isNeedQueue();
        mNotificationVisibility = config.getNotificationVisibility();
        mAllowAdjustSavePath = config.isAllowAdjustSavePath();
        mMinProgressTime = config.getMinProgressTime();
        mShowRealTimeInfo = config.isShowDownloadRealTime();
        mAutoUnpack = config.isAutoUnpack();
        mUnpackPath = config.getUnpackPath();
        mDeleteSourceAfterUnpack = config.isDeleteSourceAfterUnpack();
        mDeleteNoEndTaskAndCache = config.isDeleteFileDownloadNoEnd();
        mDeleteEndTaskAndCache = config.isDeleteFileDownloadEnd();
        mDownloadThreads = config.getDownloadThreads();

        mModuleName = DownloadInitHelper.getInstance().getDefaultModuleName();
    }

    public TaskParam(@NonNull TaskParam otherParam) {
        this();
        copyData(otherParam);
    }

    public void copyData(@Nullable TaskParam otherParam) {
        if (otherParam == null) {
            return;
        }
        mGenerateId = otherParam.mGenerateId;
        mUrl = otherParam.mUrl;
        mFileName = otherParam.mFileName;
        mFileExtension = otherParam.mFileExtension;
        mSavePath = otherParam.mSavePath;
        mFileSize = otherParam.mFileSize;
        mAutoCheckSize = otherParam.mAutoCheckSize;
        mPriority = otherParam.mPriority;
        mCheckType = otherParam.mCheckType;
        mCheckCode = otherParam.mCheckCode;
        mCheckEnable = otherParam.mCheckEnable;
        mNetworkTypes = otherParam.mNetworkTypes;
        mNeedQueue = otherParam.mNeedQueue;
        /** 保留字段*/
        mReserver = otherParam.mReserver;
        /** 扩展字段的数据 */
        mExtrasMap = otherParam.mExtrasMap;

        mDownloadThreads = otherParam.mDownloadThreads;

        mNotificationVisibility = otherParam.mNotificationVisibility;
        mAllowAdjustSavePath = otherParam.mAllowAdjustSavePath;

        mAutoUnpack = otherParam.mAutoUnpack;
        mUnpackPath = otherParam.mUnpackPath;
        mDeleteSourceAfterUnpack = otherParam.mDeleteSourceAfterUnpack;
        mDeleteNoEndTaskAndCache = otherParam.mDeleteNoEndTaskAndCache;
        mDeleteEndTaskAndCache = otherParam.mDeleteEndTaskAndCache;

        mModuleName = otherParam.mModuleName;

        mMinProgressTime = otherParam.mMinProgressTime;
        mShowRealTimeInfo = otherParam.mShowRealTimeInfo;
    }

    public void build() {
        generateId();
    }

    private boolean isvalid(){
        //存储时间
        long startTime = SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("BFC_DOWNLOAD_CDN_Start_Cache_TIME", 0L);
        //当前时间
        long currentTime = System.currentTimeMillis();

        if(0L != startTime){
            //缓存是否大于6小时
            if((currentTime - startTime) / (1000 * 60 * 60) >= 6){
                return false;
            }
        }

        return true;
    }

    public TaskParam setInnerCopyUrl(String url){
        mUrl = url;

        return this;
    }

    /**
     * 设置下载地址
     * @param url 下载url
     * @return 下载任务配置
     */
    public TaskParam setUrl(String url) {
        if (TextUtils.isEmpty(url)) {
            mUrl = url;
            return this;
        }

        //获取缓存值
        CdnFlagBean.DataBean dataBean = null;
        String cacheStr;

        cacheStr = SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("CDN_TYPE_INFO", "");

        if(!isvalid()){//过期,判断失败连续失败次数是否大于5
            int failedNum = SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("CDN_REQUEST_FAILED_NUM", 0);
            if(failedNum >= 5){
                if(!TextUtils.isEmpty(cacheStr)){
                    try {
                        dataBean = SequenceTools.deserialize(cacheStr, CdnFlagBean.DataBean.class);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }else {//请求服务器CDN type
                    CDNManager.requestForCDNType(DownloadInitHelper.getInstance().getAppContext());
                }
            }
        }else{
            if(!TextUtils.isEmpty(cacheStr)){
                try {
                    dataBean = SequenceTools.deserialize(cacheStr, CdnFlagBean.DataBean.class);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }else{//请求服务器CDN type
                CDNManager.requestForCDNType(DownloadInitHelper.getInstance().getAppContext());
            }
        }

        if(null != dataBean){
            String flag = dataBean.getFlag();
            String whiteFlag = dataBean.getWhiteFlag();

            if(!flag.equals(whiteFlag)){//标识和白名单标识不一致
                String packageName = DownloadInitHelper.getInstance().getAppContext().getPackageName();
                String domain = DownloadUtils.getDomain(url);

                if(dataBean.isInApkList(packageName) || dataBean.isInDomainList(domain) || dataBean.isInurlList(url)){//确定是否在白名单中
                    if("2".equals(whiteFlag)){
                        if(!CDNManager.isXYVodUrl(url)){
                            mUrl = CDNManager.url_REWRITE(url);
                        }else{
                            mUrl = url;
                        }
                    }else{
                        mUrl = url;
                    }

                    return this;
                }
            }

            LogUtil.i("CDN-->下载转换前:" + url);
            if("2".equals(flag)){
                if(!CDNManager.isXYVodUrl(url)){
                    mUrl = CDNManager.url_REWRITE(url);
                }else{
                    mUrl = url;
                }
                LogUtil.i("CDN-->下载转换后:" + mUrl);
            }else{
                mUrl = url;
            }
        }else {
            mUrl = url;
        }

        return this;
    }

    /**
     * 设置下载文件名称
     *
     * @param fileName 文件名
     * @return 下载任务配置
     */
    public TaskParam setFileName(String fileName) {
        mFileName = fileName;
        return this;
    }

    /**
     * 设置下载文件后缀名
     *
     * @param fileExtension 后缀名
     * @return 下载任务配置
     */
    public TaskParam setFileExtension(String fileExtension) {
        if (!TextUtils.isEmpty(fileExtension) && fileExtension.startsWith(".")) {
            fileExtension = fileExtension.substring(1);
        }
        mFileExtension = fileExtension;
        return this;
    }

    /**
     * 设置下载文件保存路径
     *
     * @param savePath 保存路径，为null时将使用默认保存路径
     * @return 下载任务配置
     */
    public TaskParam setSavePath(String savePath) {
        if (TextUtils.isEmpty(savePath)) {
            mSavePath = DownloadInitHelper.getInstance().getGlobalConfig().getSavePath();
            LogUtil.i(" set path empty, use default[" + mSavePath + "]");
        } else {
            mSavePath = savePath;
        }
        return this;
    }

    /**
     * 生成任务id，通过url和保存路径生成
     */
    private void generateId() {
        if (TextUtils.isEmpty(mUrl) || TextUtils.isEmpty(mSavePath)) {
            mGenerateId = ITask.INVALID_GENERATE_ID;
            return;
        }

        if(!CDNManager.isXYVodUrl(mUrl)){
            mGenerateId = DownloadUtils.generateId(mUrl, mSavePath);
        }else{
            mGenerateId = DownloadUtils.generateId(CDNManager.transformSourceUrl(mUrl), mSavePath);
        }
    }

    /**
     * 获取任务id，通过url和保存路径生成
     *
     * @return 任务id
     */
    public int getGenerateId() {
        return mGenerateId;
    }

    public TaskParam setGenerateId(int generateId) {
        mGenerateId = generateId;

        return this;
    }

    /**
     * 获取模块名
     *
     * @return 模块名
     */
    public String getModuleName() {
        return mModuleName;
    }

    /**
     * 设置模块名
     *
     * @param moduleName 模块名
     * @return 下载任务配置
     */
    public TaskParam setModuleName(String moduleName) throws DownloadNoInitException {
        if (TextUtils.isEmpty(moduleName)) {
            LogUtil.w(" set module name is null, use default module name! ");
            if (DownloadInitHelper.getInstance().getAppContext() == null) {
                throw new DownloadNoInitException();
            }
            this.mModuleName = DownloadInitHelper.getInstance().getDefaultModuleName();
            return this;
        }
        this.mModuleName = moduleName;
        return this;
    }

    /**
     * 设置预设下载文件大小
     *
     * @param fileSize 文件大小
     * @return 下载任务配置
     */
    public TaskParam setPresetFileSize(long fileSize) {
        if (fileSize <= 0) {
            fileSize = -1;
        }
        mFileSize = fileSize;
        return this;
    }

    /**
     * 设置自动检测文件大小是否相符，当设置的fileSize > 0时
     *
     * @param autoCheckSize true为自动检测，false则不自动检测
     * @return 下载任务配置
     */
    public TaskParam setAutoCheckSize(boolean autoCheckSize) {
        mAutoCheckSize = autoCheckSize;
        return this;
    }

    /**
     * 设置下载优先级， >= 0, 小于等于整形的最大值，值越大优先级越高
     *
     * @param priority 优先级
     * @return 下载任务配置
     */
    public TaskParam setPriority(int priority) {
        mPriority = priority;
        return this;
    }

    /**
     * 文件校验类型，比如{@link com.eebbk.bfc.sdk.downloadmanager.ITask.CheckType#MD5},
     * {@link com.eebbk.bfc.sdk.downloadmanager.ITask.CheckType#HASH}
     *
     * @param checkType 校验类型
     * @return 下载任务配置
     */
    public TaskParam setCheckType(String checkType) {
        mCheckType = checkType;
        return this;
    }

    /**
     * 文件校验码
     *
     * @param checkCode 校验码
     * @return 下载任务配置
     */
    public TaskParam setCheckCode(String checkCode) {
        mCheckCode = checkCode;
        return this;
    }

    /**
     * 是否进行校验
     *
     * @param enable true为进行检验，false则否
     * @return 下载任务配置
     */
    public TaskParam setCheckEnable(boolean enable) {
        mCheckEnable = enable;
        return this;
    }

    /**
     * 下载可用的网络类型：{@link NetworkType#NETWORK_WIFI}, {@link NetworkType#NETWORK_MOBILE}, {@link NetworkType#NETWORK_BLUETOOTH},
     *
     * @param networkType 网络类型
     * @return 下载任务配置
     */
    public TaskParam setNetworkTypes(int networkType) {
        mNetworkTypes = networkType;
        return this;
    }

    /**
     * 是否进行排队，false时任务将不会等待，直接进行下载，不影响下载任务数量， 有最大限制，默认为{@link C.DownLoadConfig#DEFAULT_MAX_NO_NEED_QUEUE_TASKS}
     *
     * @param needQueue true为需要排队，false则否
     * @return 下载任务配置
     */
    public TaskParam setNeedQueue(boolean needQueue) {
        mNeedQueue = needQueue;
        return this;
    }

    /**
     * 保留字段
     *
     * @param reserver 保留字段
     * @return 下载任务配置
     */
    public TaskParam setReserver(String reserver) {
        mReserver = reserver;
        return this;
    }

    /**
     * 设置扩展字段的数据
     *
     * @param extrasMap 扩展字段
     * @return 下载任务配置
     */
    public TaskParam setExtrasMap(HashMap<String, String> extrasMap) {
        mExtrasMap = extrasMap;
        return this;
    }

    /**
     * 获取下载地址
     *
     * @return 下载地址
     */

    public String getUrl() {
        return mUrl;
    }

    /**
     * 获取下载文件名称
     *
     * @return 文件名称
     */
    public String getFileName() {
        return mFileName;
    }

    /**
     * 获取下载文件后缀名
     *
     * @return 文件后缀名
     */
    public String getFileExtension() {
        return mFileExtension;
    }

    /**
     * 获取下载文件保存路径
     *
     * @return 文件保存地址
     */
    public String getSavePath() {
        return mSavePath;
    }

    /**
     * 获取预设的下载文件大小
     *
     * @return 预设文件大小
     */
    public long getPresetFileSize() {
        return mFileSize;
    }

    /**
     * 是否自动检测文件大小
     *
     * @return true自动检测，false不检测
     */
    public boolean isAutoCheckSize() {
        return mAutoCheckSize;
    }

    /**
     * 获取下载任务优先级
     *
     * @return 任务优先级
     */
    public int getPriority() {
        return mPriority;
    }

    /**
     * 文件校验类型，比如{@link com.eebbk.bfc.sdk.downloadmanager.ITask.CheckType#MD5},
     * {@link com.eebbk.bfc.sdk.downloadmanager.ITask.CheckType#HASH}
     *
     * @return 校验类型
     */
    public String getCheckType() {
        return mCheckType;
    }

    /**
     * 获取文件校验码
     *
     * @return 校验码
     */
    public String getCheckCode() {
        return mCheckCode;
    }

    /**
     * 是否进行文件校验
     *
     * @return true进行校验，false不校验
     */
    public boolean isCheckEnable() {
        return mCheckEnable;
    }

    /**
     * 是否需要校验文件，将根据配置的参数以及校验开关决定
     *
     * @return true回校验，false不校验
     */
    public boolean needCheckFile() {
        return DownloadUtils.needCheckFile(isCheckEnable(), getCheckType(), getCheckCode());
    }

    /**
     * 获取网络类型
     *
     * @return 网络类型
     */
    public int getNetworkTypes() {
        return mNetworkTypes;
    }

    /**
     * 是否需要排队
     *
     * @return true需求排队，false不需要
     */
    public boolean isNeedQueue() {
        return mNeedQueue;
    }

    /**
     * 获取保留字段
     *
     * @return 保留字段
     */
    public String getReserver() {
        return mReserver;
    }

    /**
     * 获取扩展字段的数据
     *
     * @return 扩展字段
     */
    public HashMap<String, String> getExtrasMap() {
        return mExtrasMap;
    }

    /**
     * 是否允许使用Wifi网络
     *
     * @return true允许，false不允许
     */
    public boolean isAllowWifiNet() {
        return NetworkParseUtil.containsWifi(mNetworkTypes);
    }

    /**
     * 是否允许使用移动网络
     *
     * @return true允许，false不允许
     */
    public boolean isAllowMobileNet() {
        return NetworkParseUtil.containsMobile(mNetworkTypes);
    }

    /**
     * 是否允许使用蓝牙网络
     *
     * @return true允许，false不允许
     */
    public boolean isAllowBluetoothNet() {
        return NetworkParseUtil.containsBluetooth(mNetworkTypes);
    }

    /**
     * 设置通知方式
     *
     * @param pNotificationVisibility 通知方式， 详见{@link com.eebbk.bfc.sdk.downloadmanager.ITask.Notification}
     * @return 下载任务配置
     */
    public TaskParam setNotificationVisibility(int pNotificationVisibility) {
        mNotificationVisibility = pNotificationVisibility;
        return this;
    }

    /**
     * 获取通知方式
     *
     * @return 通知方式
     */
    public int getNotificationVisibility() {
        return mNotificationVisibility;
    }

    /**
     * 是否允许修改保存路径
     *
     * @return true允许，false不允许
     */
    public boolean hasAllowAdjustSavePath() {
        return mAllowAdjustSavePath;
    }

    /**
     * 设置是否允许修改保存路径
     *
     * @param allow true允许，false
     * @return 下载任务配置
     */
    public TaskParam setAllowAdjustSavePath(boolean allow) {
        mAllowAdjustSavePath = allow;
        return this;
    }

    /**
     * <pre>
     * 是否显示实时状态（下载速度、剩余时间）（默认false），受{@link #setMinProgressTime(int)}方法影响，
     * 设置setMinProgressTime >= 0 时此值有效，实时状态返回将跟随进度同时返回
     * </pre>
     *
     * @param show true显示，false则否
     * @return 下载任务配置
     */
    public TaskParam setShowRealTimeInfo(boolean show) {
        mShowRealTimeInfo = show;
        return this;
    }

    /**
     * <pre>下载进度回调间隔时间，单位毫秒，(默认1000ms)。
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     * 如果设置<0,将不会回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，只会回调进度的第一次和最后一次进度消息，没有下载速度、剩余时间等
     * 设置=0，将会实时回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即时回调进度消息
     * 设置>0,将会根据设置的间隔时间进行回调
     * </pre>
     *
     * @param minProgressTime 间隔时间 毫秒
     * @return 下载任务配置
     */
    public TaskParam setMinProgressTime(int minProgressTime) {
        this.mMinProgressTime = minProgressTime;
        return this;
    }

    /**
     * 设置是否自动解压（默认否）
     *
     * @param unpack true解压， false不解压
     * @return 下载任务配置
     */
    public TaskParam setAutoUnpack(boolean unpack) {
        mAutoUnpack = unpack;
        return this;
    }

    /**
     * 设置删除未下载完成任务时同时删除缓存文件（默认是）
     *
     * @param delete true删除，false则否
     * @return 下载任务配置
     */
    public TaskParam setDeleteNoEndTaskAndCache(boolean delete) {
        mDeleteNoEndTaskAndCache = delete;
        return this;
    }

    /**
     * 设置删除已下载完成任务时同时删除文件（默认否）
     *
     * @param delete true删除，false则否
     * @return 下载任务配置
     */
    public TaskParam setDeleteEndTaskAndCache(boolean delete) {
        mDeleteEndTaskAndCache = delete;
        return this;
    }

    /**
     * 是否显示实时状态（下载速度、剩余时间）（默认false），受{@link #setMinProgressTime(int)}方法影响，
     * 设置setMinProgressTime >= 0 时此值有效，实时状态返回将跟随进度同时返回
     *
     * @return true显示，false则否
     */
    public boolean isShowRealTimeInfo() {
        return mShowRealTimeInfo && mMinProgressTime >= 0;
    }

    /**
     * <pre>下载进度回调间隔时间，单位毫秒，(默认值{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MIN_PROGRESS_TIME})。
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     * 如果设置<0,将不会回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，只会回调进度的第一次和最后一次进度消息，没有下载速度、剩余时间等
     * 设置=0，将会实时回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即时回调进度消息
     * 设置>0,将会根据设置的间隔时间进行回调
     * </pre>
     *
     * @return 间隔时间
     */
    public int getMinProgressTime() {
        return mMinProgressTime;
    }

    /**
     * 是否自动解压（默认否）
     *
     * @return true解压，false则否
     */
    public boolean isAutoUnpack() {
        return mAutoUnpack;
    }

    /**
     * 获取解压文件保存路径
     *
     * @param path 保存路径
     * @return 下载任务配置
     */
    public TaskParam setUnpackPath(String path) {
        mUnpackPath = path;
        return this;
    }

    /**
     * 获取解压文件保存路径
     *
     * @return 解压文件保存路径
     */
    public String getUnpackPath() {
        return mUnpackPath;
    }

    /**
     * 设置解压后是否自动删除源文件,默认true
     *
     * @return true自动删除，false不删除
     */
    public TaskParam setDeleteSourceAfterUnpack(boolean delete) {
        mDeleteSourceAfterUnpack = delete;
        return this;
    }

    /**
     * 解压后是否自动删除源文件
     *
     * @return true自动删除，false不删除
     */
    public boolean isDeleteSourceAfterUnpack() {
        return mDeleteSourceAfterUnpack;
    }

    /**
     * 删除未下载完成任务时同时删除缓存文件（默认是）
     *
     * @return true则删除，false则否
     */
    public boolean isDeleteNoEndTaskAndCache() {
        return mDeleteNoEndTaskAndCache;
    }

    /**
     * 删除已下载完成任务时同时删除文件（默认否）
     *
     * @return true则删除，false则否
     */
    public boolean isDeleteEndTaskAndCache() {
        return mDeleteEndTaskAndCache;
    }

    /**
     * 获取下载任务开启多线程数量
     *
     * @return 下载任务开启多线程数量
     */
    public int getDownloadThreads() {
        return mDownloadThreads;
    }

    /**
     * 设置下载任务开启多线程数量
     *
     * @param downloadThreads 线程数量
     * @return 下载任务配置
     */
    public TaskParam setDownloadThreads(int downloadThreads) {
        mDownloadThreads = downloadThreads;
        return this;
    }

    protected String extrasToString() {
        return ExtrasConverter.extrasToString(mExtrasMap);
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, String value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, value);
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, int value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, boolean value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, float value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, long value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, double value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, char value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, byte value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, byte[] value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 添加下载任务扩展字段，用于记录此任务的一些特殊信息
     *
     * @param name  扩展字段key
     * @param value 扩展字段value
     * @return 下载任务配置
     */
    public TaskParam putExtra(@NonNull String name, short value) {
        if (!checkExtraKey(name)) {
            return this;
        }
        mExtrasMap.put(name, String.valueOf(value));
        return this;
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name 扩展字段key
     * @return 值
     */
    public String getStringExtra(String name) {
        if (!checkExtraKey(name)) {
            return null;
        }
        return mExtrasMap.get(name);
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public int getIntExtra(String name, int defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            try {
                return Integer.parseInt(mExtrasMap.get(name));
            } catch (Exception e) {
                LogUtil.e(" parse int error: " + e);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public boolean getBooleanExtra(String name, boolean defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            return Boolean.parseBoolean(mExtrasMap.get(name));
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public float getFloatExtra(String name, float defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            try {
                return Float.parseFloat(mExtrasMap.get(name));
            } catch (Exception e) {
                LogUtil.e(" parse float error: " + e);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public double getDoubleExtra(String name, double defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            try {
                return Double.parseDouble(mExtrasMap.get(name));
            } catch (Exception e) {
                LogUtil.e(" parse float error: " + e);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public char getCharExtra(String name, char defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            try {
                return (char) Integer.parseInt(mExtrasMap.get(name));
            } catch (Exception e) {
                LogUtil.e(" parse float error: " + e);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public byte getByteExtra(String name, byte defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            try {
                return Byte.parseByte(mExtrasMap.get(name));
            } catch (Exception e) {
                LogUtil.e(" parse float error: " + e);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name 扩展字段key
     * @return 值
     */
    public byte[] getByteArrayExtra(String name) {
        if (!checkExtraKey(name)) {
            return null;
        }
        if (mExtrasMap.get(name) == null) {
            return null;
        }
        return mExtrasMap.get(name).getBytes(Charset.defaultCharset());
    }

    /**
     * 获取下载任务扩展字段内容
     *
     * @param name         扩展字段key
     * @param defaultValue 默认值
     * @return 值
     */
    public short getShortExtra(String name, short defaultValue) {
        if (!checkExtraKey(name)) {
            return defaultValue;
        }
        if (mExtrasMap.containsKey(name)) {
            try {
                return Short.parseShort(mExtrasMap.get(name));
            } catch (Exception e) {
                LogUtil.e(" parse float error: " + e);
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    private static boolean checkExtraKey(String key) {
        if (TextUtils.isEmpty(key)) {
            LogUtil.w(" extra key must not null! ");
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("TaskParam[");
        sb.append("\r\n generateId: " + mGenerateId);
        try {
            sb.append("\r\n url(decode): " + URLDecoder.decode(mUrl, "UTF-8"));
        } catch (Exception e) {
            sb.append("\r\n url(decode): decode error!");
        }

        sb.append("\r\n module: " + mModuleName);
        sb.append("\r\n urlExtend: " + mUrl);
        sb.append("\r\n fileName: " + mFileName);
        sb.append("\r\n fileExtension: " + mFileExtension);
        sb.append("\r\n savePath: " + mSavePath);
        sb.append("\r\n mFileSize: " + mFileSize);
        sb.append("\r\n autoCheckSize: " + mAutoCheckSize);
        sb.append("\r\n priority: " + mPriority);
        sb.append("\r\n checkType: " + mCheckType);
        sb.append("\r\n checkCode: " + mCheckCode);
        sb.append("\r\n checkEnable: " + mCheckEnable);
        sb.append("\r\n networkTypes: " + mNetworkTypes);
        sb.append("\r\n needQueue: " + mNeedQueue);
        sb.append("\r\n reserver: " + mReserver);
        sb.append("\r\n extrasMap: " + extrasToString());
        sb.append("\r\n notificationVisibility: " + mNotificationVisibility);
        sb.append("\r\n allowAdjustSavePath: " + mAllowAdjustSavePath);
        sb.append("\r\n minProgressTime: " + mMinProgressTime);
        sb.append("\r\n init showRealTimeInfo: " + mShowRealTimeInfo + ", final value:" + isShowRealTimeInfo());
        sb.append("\r\n autoUnpack: " + mAutoUnpack);
        sb.append("\r\n unpackPath: " + mUnpackPath);
        sb.append("\r\n deleteSourceAfterUnpack: " + mDeleteSourceAfterUnpack);
        sb.append("\r\n deleteNoEndTaskAndCache: " + mDeleteNoEndTaskAndCache);
        sb.append("\r\n deleteEndTaskAndCache: " + mDeleteEndTaskAndCache);
        sb.append("\r\n downloadThreads: " + mDownloadThreads);
        sb.append("\r\n ]");
        return sb.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mGenerateId);
        dest.writeString(this.mUrl);
        dest.writeString(this.mFileName);
        dest.writeString(this.mFileExtension);
        dest.writeString(this.mSavePath);
        dest.writeLong(this.mFileSize);
        dest.writeByte(this.mAutoCheckSize ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mPriority);
        dest.writeString(this.mCheckType);
        dest.writeString(this.mCheckCode);
        dest.writeByte(this.mCheckEnable ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mNetworkTypes);
        dest.writeByte(this.mNeedQueue ? (byte) 1 : (byte) 0);
        dest.writeString(this.mReserver);
        dest.writeSerializable(this.mExtrasMap);
        dest.writeInt(this.mNotificationVisibility);
        dest.writeByte(this.mAllowAdjustSavePath ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mMinProgressTime);
        dest.writeByte(this.mShowRealTimeInfo ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mAutoUnpack ? (byte) 1 : (byte) 0);
        dest.writeString(this.mUnpackPath);
        dest.writeByte(this.mDeleteSourceAfterUnpack ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mDeleteNoEndTaskAndCache ? (byte) 1 : (byte) 0);
        dest.writeByte(this.mDeleteEndTaskAndCache ? (byte) 1 : (byte) 0);
        dest.writeInt(this.mDownloadThreads);
        dest.writeString(this.mModuleName);
    }

    protected TaskParam(Parcel in) {
        this.mGenerateId = in.readInt();
        this.mUrl = in.readString();
        this.mFileName = in.readString();
        this.mFileExtension = in.readString();
        this.mSavePath = in.readString();
        this.mFileSize = in.readLong();
        this.mAutoCheckSize = in.readByte() != 0;
        this.mPriority = in.readInt();
        this.mCheckType = in.readString();
        this.mCheckCode = in.readString();
        this.mCheckEnable = in.readByte() != 0;
        this.mNetworkTypes = in.readInt();
        this.mNeedQueue = in.readByte() != 0;
        this.mReserver = in.readString();
        this.mExtrasMap = (HashMap<String, String>) in.readSerializable();
        this.mNotificationVisibility = in.readInt();
        this.mAllowAdjustSavePath = in.readByte() != 0;
        this.mMinProgressTime = in.readInt();
        this.mShowRealTimeInfo = in.readByte() != 0;
        this.mAutoUnpack = in.readByte() != 0;
        this.mUnpackPath = in.readString();
        this.mDeleteSourceAfterUnpack = in.readByte() != 0;
        this.mDeleteNoEndTaskAndCache = in.readByte() != 0;
        this.mDeleteEndTaskAndCache = in.readByte() != 0;
        this.mDownloadThreads = in.readInt();
        this.mModuleName = in.readString();
    }

    public static final Creator<TaskParam> CREATOR = new Creator<TaskParam>() {
        @Override
        public TaskParam createFromParcel(Parcel source) {
            return new TaskParam(source);
        }

        @Override
        public TaskParam[] newArray(int size) {
            return new TaskParam[size];
        }
    };
}
