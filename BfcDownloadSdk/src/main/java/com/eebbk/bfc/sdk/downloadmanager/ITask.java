package com.eebbk.bfc.sdk.downloadmanager;

import android.support.annotation.NonNull;

import com.eebbk.bfc.sdk.download.IDownloadTaskExtra;
import com.eebbk.bfc.sdk.download.IDownloadTaskState;
import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.TaskState;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.sdk.download.net.NetworkType;

import java.util.HashMap;

/**
 * Desc: 提供给外部使用的下载任务接口，用于查询相关信息,无修改权限
 * Author: llp
 * Create Time: 2016-09-28 9:31
 * Email: jacklulu29@gmail.com
 */

public interface ITask extends IDownloadTaskExtra, IDownloadTaskState {

    /**
     * 无效任务ID
     */
    int INVALID_GENERATE_ID = -1;

    /**
     * 获取下载任务ID，通过url和保存路径生成
     *
     * @return 任务ID
     */
    int getId();

    /**
     * 获取模块名称
     *
     * @return 模块名
     */
    String getModuleName();

    /**
     * 获取下载地址
     *
     * @return 下载地址
     */
    String getUrl();

    /**
     * 获取经过decodeUTF8转换过的url
     *
     * @return 下载地址（转换过）
     */
    String getRealUrl();

    /**
     * 获取下载文件名称
     *
     * @return 文件名称
     */
    String getFileName();

    /**
     * 获取下载文件后缀名
     *
     * @return 文件后缀名
     */
    String getFileExtension();

    /**
     * 获取下载文件保存路径
     *
     * @return 文件保存路径
     */
    String getSavePath();

    /**
     * 获取预设的下载文件大小
     *
     * @return 预设文件大小
     */
    long getPresetFileSize();

    /**
     * 获取真实的下载文件大小
     *
     * @return 真实文件大小
     */
    long getFileSize();

    /**
     * 是否自动检测文件大小
     *
     * @return true自动检测，false不检测
     */
    boolean isAutoCheckSize();

    /**
     * 获取下载任务优先级
     *
     * @return 任务优先级
     */
    int getPriority();

    /**
     * 文件校验类型，详细可查看{@link CheckType}, 也可以自定义
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
     * 获取文件校验码
     *
     * @return 校验码
     * @deprecated 建议使用{@link #getCheckCode()}
     */
    @Deprecated
    String getMD5();

    /**
     * 文件校验开关是否打开
     *
     * @return true打开，false关闭
     */
    boolean isCheckEnable();

    /**
     * 是否需要校验文件，将根据配置的参数以及校验开关决定
     *
     * @return true会校验，false不会校验
     */
    boolean needCheckFile();

    /**
     * 获取网络类型
     *
     * @return 网络类型
     */
    int getNetworkTypes();

    /**
     * 是否需要排队
     *
     * @return true排队，false不排队（超过任务上传同样需要等待）
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
     * @deprecated 建议使用　{@link #getStringExtra}、{@link #getIntExtra}等
     */
    @Deprecated
    HashMap<String, String> getExtras();

    /**
     * 获取异常信息
     *
     * @return 异常信息
     */
    Throwable getException();

    /**
     * 是否允许使用移动网络
     *
     * @return true允许，false不允许
     */
    boolean isAllowMobileNet();

    /**
     * 是否允许使用Wifi网络
     *
     * @return true允许，false不允许
     */
    boolean isAllowWifiNet();

    /**
     * 是否允许使用蓝牙网络
     *
     * @return true允许，false不允许
     */
    boolean isAllowBluetoothNet();

    /**
     * 获取转换前状态
     *
     * @return 错误码
     * @deprecated 建议使用{@link #getErrorCode()}
     */
    @Deprecated
    int getOriginState();

    /**
     * 是否允许修改保存路径
     *
     * @return true允许，false不允许
     */
    boolean hasAllowAdjustSavePath();

    /**
     * 获取通知显示方式
     *
     * @return 通知方式
     */
    int getNotificationVisibility();

    /**
     * 获取解压文件保存路径
     *
     * @return 解压文件保存路径
     */
    String getUnpackPath();

    /**
     * 是否显示实时状态（下载速度、剩余时间）（默认false），受{@link ITask.Builder#setMinProgressTime(int)}方法影响，
     * 设置setMinProgressTime >= 0 时此值有效，实时状态返回将跟随进度同时返回
     *
     * @return true显示，false则否
     */
    boolean isShowRealTimeInfo();

    /**
     * <pre>下载进度回调间隔时间，单位毫秒，(默认值{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MIN_PROGRESS_TIME})。
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     * 如果要设置请调用{@link ITask.Builder#setMinProgressTime(int)}方法
     * 如果设置<0,将不会回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，只会回调进度的第一次和最后一次进度消息，没有下载速度、剩余时间等
     * 设置=0，将会实时回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，，即时回调进度消息
     * 设置>0,将会根据设置的间隔时间进行回调
     * </pre>
     *
     * @return 间隔时间
     */
    int getMinProgressTime();

    /**
     * 是否自动解压（默认否）
     *
     * @return true解压，false则否
     */
    boolean isAutoUnpack();

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
     * 获取下载任务开启多线程数量
     *
     * @return 下载任务开启多线程数量
     */
    int getDownloadThreads();

    /**
     * 下载任务是否已结束，标识任务将不会在运行，除非让任务重现开始
     *
     * @return true任务已结束，false未结束
     */
    boolean isFinished();

    /**
     * 将当前任务clone出一个新的任务，将不会保留当前任务的监听，深度clone数据
     *
     * @return 新的任务，不包含监听
     */
    ITask cloneTask();

    /**
     * 复制指定任务的数据到当前任务中，将会保留当前任务的监听<br/>
     *
     * @param task 指定任务，被复制数据的任务
     * @return 当前任务，复制了指定任务的数据
     */
    ITask updateData(ITask task);

    /**
     * 获取文件下载监听
     *
     * @return 下载监听
     */
    OnDownloadListener getOnDownloadListener();

    /**
     * 获取文件校验监听
     *
     * @return 校验监听
     */
    OnCheckListener getOnCheckListener();

    /**
     * 获取文件解压监听
     *
     * @return 解压监听
     */
    OnUnpackListener getOnUnpackListener();

    /**
     * 设置下载监听<br/>
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param downloadListener 下载监听
     * @return 下载任务
     */
    ITask setOnDownloadListener(OnDownloadListener downloadListener);

    /**
     * 设置校验监听<br/>
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param checkListener 校验监听
     * @return 任务
     */
    ITask setOnCheckListener(OnCheckListener checkListener);

    /**
     * 设置解压监听<br/>
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param unpackListener 解压监听
     * @return 下载任务
     */
    ITask setOnUnpackListener(OnUnpackListener unpackListener);

    TaskParam getTaskParam();

    ITask setTaskParam(TaskParam param);

    TaskState getTaskState();

    ITask setTaskState(TaskState taskState);

    ITask setTaskState(TaskStateInfo stateInfo);

    /**
     * 回收资源，注销监听,任务不使用时必须尽早调用此方法，避免监听没有注销导致内存泄露
     */
    void recycle();

    interface CheckType {
        /**
         * 不校验
         */
        String NON = "NON";
        /**
         * MD5校验类型(小于300K文件会校验)
         */
        String MD5 = "MD5";
        /**
         * MD5校验类型（任意文件都会进行校验）
         */
        String MD5_EX = "MD5_EX";
        /**
         * HASH校验类型
         */
        String HASH = "HASH";
    }

    interface Notification {
        /**
         * This download is visible but only shows in the notifications
         * while it's in progress.
         */
        int VISIBILITY_VISIBLE = 0;

        /**
         * This download is visible and shows in the notifications while
         * in progress and after completion.
         */
        int VISIBILITY_VISIBLE_NOTIFY_COMPLETED = 1;

        /**
         * This download doesn't show in the UI or in the notifications.
         */
        int VISIBILITY_HIDDEN = 2;

        /**
         * This download shows in the notifications after completion ONLY.
         * It is usuable only with
         * DownloadManager.addCompletedDownload(String, String,
         * boolean, String, String, long, boolean).
         */
        int VISIBILITY_VISIBLE_NOTIFY_ONLY_COMPLETION = 3;
    }

    class Builder {

        private DownloadTask mTask;

        public Builder( @NonNull String url) {
            mTask = new DownloadTask( url);
        }

        public Builder(@NonNull String url, String fileName, String savePath) {
            mTask = new DownloadTask( url, fileName, savePath);
        }

        public Builder(@NonNull String url, String fileName, long fileSize, String fileExtension, String md5Code) {
            mTask = new DownloadTask( url, fileName, fileSize, fileExtension, md5Code);
        }

        public Builder(TaskParam param) {
            mTask = new DownloadTask(param);
        }

        // -------------------------- param ------------------------------

        /**
         * 设置当前任务所属模块，不设置即使用默认模块名称<br/>
         * 注意：如果任务设置了模块名称，查询时必须使用带模块名的方法才能查询到，<br/>
         * 否则查询到的任务都是默认模块的
         *
         * @param moduleName 模块名
         * @return 任务构造器
         */
        public Builder setModuleName(String moduleName) {
            mTask.getTaskParam().setModuleName(moduleName);
            return this;
        }

        /**
         * 设置下载地址
         *
         * @param url 下载地址
         * @return 任务构造器
         */
        public Builder setUrl(String url) {
            mTask.getTaskParam().setUrl(url);
            return this;
        }

        /**
         * 设置下载文件名称
         *
         * @param fileName 文件名称
         * @return 任务构造器
         */
        public Builder setFileName(String fileName) {
            mTask.getTaskParam().setFileName(fileName);
            return this;
        }

        /**
         * <pre>设置下载文件后缀名
         * 如果设置为null或者不设置，将会自动从{@link #setFileName(String)}中或者请求头中获取
         * 如果设置为""或者其他都将视为后缀名使用
         * 如果文件名和后缀名同时设置了，最终会使用设置的后缀名，不会使用文件名中的后缀
         * </pre>
         *
         * @param fileExtension 文件扩展名称
         * @return 任务构造器
         */
        public Builder setFileExtension(String fileExtension) {
            mTask.getTaskParam().setFileExtension(fileExtension);
            return this;
        }

        /**
         * 设置下载文件保存路径
         *
         * @param savePath 保存路径
         * @return 任务构造器
         */
        public Builder setSavePath(String savePath) {
            mTask.getTaskParam().setSavePath(savePath);
            return this;
        }

        /**
         * 设置预设下载文件大小
         *
         * @param fileSize 文件大小
         * @return 任务构造器
         */
        public Builder setPresetFileSize(long fileSize) {
            mTask.getTaskParam().setPresetFileSize(fileSize);
            return this;
        }

        /**
         * 设置自动检测文件大小是否相符，当设置的fileSize > 0时
         *
         * @param autoCheckSize true自动检测，false不检测
         * @return 任务构造器
         */
        public Builder setAutoCheckSize(boolean autoCheckSize) {
            mTask.getTaskParam().setAutoCheckSize(autoCheckSize);
            return this;
        }

        /**
         * 设置下载优先级， >= 0, 小于等于{@link Integer#MAX_VALUE}，值越大优先级越高
         *
         * @param priority 任务优先级
         * @return 任务构造器
         */
        public Builder setPriority(int priority) {
            mTask.getTaskParam().setPriority(priority);
            return this;
        }

        /**
         * 设置文件校验类型，类型可查看：{@link CheckType}，也可以自定义
         *
         * @param checkType 校验类型
         * @return 任务构造器
         */
        public Builder setCheckType(String checkType) {
            mTask.getTaskParam().setCheckType(checkType);
            return this;
        }

        /**
         * 设置文件校验码
         *
         * @param checkCode 校验码
         * @return 任务构造器
         */
        public Builder setCheckCode(String checkCode) {
            mTask.getTaskParam().setCheckCode(checkCode);
            return this;
        }

        /**
         * 设置md5校验码，校验类型将会修改为{@link CheckType#MD5}
         *
         * @param md5 MD5类型的校验码
         * @return 任务构造器
         */
        @Deprecated
        public Builder setMD5(String md5) {
            mTask.getTaskParam().setCheckType(CheckType.MD5);
            mTask.getTaskParam().setCheckCode(md5);
            return this;
        }

        /**
         * 是否打开校验开关，设置true打开，false关闭
         *
         * @param enable true打开，false关闭
         * @return 任务构造器
         */
        public Builder setCheckEnable(boolean enable) {
            mTask.getTaskParam().setCheckEnable(enable);
            return this;
        }

        /**
         * <pre>下载可用的网络类型：{@link NetworkType#NETWORK_WIFI}, {@link NetworkType#NETWORK_MOBILE}, {@link NetworkType#NETWORK_BLUETOOTH},
         * 默认已有wifi和蓝牙网络
         * 如需使用多种类型的网络请用“|”连接，如 int networkTypes = {@link NetworkType#NETWORK_WIFI} | {@link NetworkType#NETWORK_BLUETOOTH};
         * 注意：配置时生效，任务运行后在调用此方法设置无效
         *       运行时如果需要修改网络类型，请调用{@link DownloadController#setNetworkTypes(int, ITask...)}}
         * </pre>
         *
         * @param networkType 网络类型
         * @return 任务构造器
         */
        public Builder setNetworkTypes(int networkType) {
            mTask.getTaskParam().setNetworkTypes(networkType);
            return this;
        }

        /**
         * <pre>增加移动网络权限，最终都是调用{@link #setNetworkTypes(int)}设置网络类型
         * 注意：配置时生效，任务运行后在调用此方法设置无效
         *       运行时如果需要修改网络类型，请调用{@link DownloadController#setNetworkTypes(int, ITask...)}}
         * </pre>
         *
         * @return 任务构造器
         */
        public Builder addMobileNet() {
            return setNetworkTypes(mTask.getTaskParam().getNetworkTypes() | NetworkType.NETWORK_MOBILE);
        }

        /**
         * 是否进行排队，false时任务将不会等待，直接进行下载，不影响下载任务数量， 有最大限制，
         * 默认为{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MAX_NO_NEED_QUEUE_TASKS}
         *
         * @param needQueue true需要排队，false不需要
         * @return 任务构造器
         */
        public Builder setNeedQueue(boolean needQueue) {
            mTask.getTaskParam().setNeedQueue(needQueue);
            return this;
        }

        /**
         * 保留字段
         *
         * @param reserver 保留字段
         * @return 任务构造器
         */
        public Builder setReserver(String reserver) {
            mTask.getTaskParam().setReserver(reserver);
            return this;
        }

        /**
         * 设置扩展字段的数据
         *
         * @param extrasMap 扩展字段
         * @return 任务构造器
         */
        public Builder setExtras(HashMap<String, String> extrasMap) {
            mTask.getTaskParam().setExtrasMap(extrasMap);
            return this;
        }

        /**
         * 添加扩展字段值
         *
         * @param key   扩展字段key
         * @param value 扩展字段值
         * @return 任务构造器
         * @deprecated 建议使用设置使用{@link #putExtra}，获取使用{@link #getStringExtra}等
         */
        @Deprecated
        public Builder addExtras(String key, String value) {
            mTask.getTaskParam().putExtra(key, value);
            return this;
        }

        /**
         * 设置是否允许修改保存路径
         *
         * @param allow ture允许，false不允许
         * @return 任务构造器
         */
        public Builder setAllowAdjustSavePath(boolean allow) {
            mTask.getTaskParam().setAllowAdjustSavePath(allow);
            return this;
        }

        /**
         * 设置通知显示方式
         *
         * @param pNotificationVisibility 同时方式
         * @return 任务构造器
         */
        public Builder setNotificationVisibility(int pNotificationVisibility) {
            mTask.getTaskParam().setNotificationVisibility(pNotificationVisibility);
            return this;
        }

        /**
         * 是否显示实时状态（下载速度、剩余时间）（默认false），受{@link #setMinProgressTime(int)}方法影响，
         * 设置setMinProgressTime >= 0 时此值有效，实时状态返回将跟随进度同时返回
         *
         * @param show true显示，false则否
         * @return 任务构造器
         */
        public Builder setShowRealTimeInfo(boolean show) {
            mTask.getTaskParam().setShowRealTimeInfo(show);
            return this;
        }

        /**
         * <pre>下载进度回调间隔时间，单位毫秒，(默认值{@link com.eebbk.bfc.sdk.download.C.DownLoadConfig#DEFAULT_MIN_PROGRESS_TIME})。
         * 同时会影响到下载速度、剩余时间的统计以及回调时机
         * 如果设置<0,将不会回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，只会回调进度的第一次和最后一次进度消息，没有下载速度、剩余时间等
         * 设置=0，将会实时回调下载监听中的{@link com.eebbk.bfc.sdk.download.listener.OnDownloadListener#onDownloading(ITask, long, long)}方法，即时回调进度消息等
         * 设置>0,将会根据设置的间隔时间回调进度消息等
         * </pre>
         *
         * @param minProgressTime 间隔时间 毫秒
         * @return 任务构造器
         */
        public Builder setMinProgressTime(int minProgressTime) {
            mTask.getTaskParam().setMinProgressTime(minProgressTime);
            return this;
        }

        /**
         * 设置是否自动解压（默认否）
         *
         * @param unpack true解压， false不解压
         * @return 任务构造器
         */
        public Builder setAutoUnpack(boolean unpack) {
            mTask.getTaskParam().setAutoUnpack(unpack);
            return this;
        }

        /**
         * 获取解压文件保存路径
         *
         * @param path 保存路径
         * @return 任务构造器
         */
        public Builder setUnpackPath(String path) {
            mTask.getTaskParam().setUnpackPath(path);
            return this;
        }

        /**
         * 设置解压后是否自动删除源文件,默认true
         *
         * @return true自动删除，false不删除
         */
        public Builder setDeleteSourceAfterUnpack(boolean delete) {
            mTask.getTaskParam().setDeleteSourceAfterUnpack(delete);
            return this;
        }

        /**
         * 设置删除未下载完成任务时同时删除缓存文件（默认是）
         *
         * @param delete true删除，false则否
         * @return 任务构造器
         */
        public Builder setDeleteNoEndTaskAndCache(boolean delete) {
            mTask.getTaskParam().setDeleteNoEndTaskAndCache(delete);
            return this;
        }

        /**
         * 设置删除已下载完成任务时同时删除文件（默认否）
         *
         * @param delete true删除，false则否
         * @return 任务构造器
         */
        public Builder setDeleteEndTaskAndCache(boolean delete) {
            mTask.getTaskParam().setDeleteEndTaskAndCache(delete);
            return this;
        }

        /**
         * 设置下载任务开启多线程数量
         *
         * @param downloadThreads 线程数量
         * @return 任务构造器
         */
        public Builder setDownloadThreads(int downloadThreads) {
            mTask.getTaskParam().setDownloadThreads(downloadThreads);
            return this;
        }

        // -------------------------- Extras ------------------------------

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, String value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, long value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, int value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, boolean value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, float value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, double value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, char value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, byte value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, byte[] value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        /**
         * 设置扩展信息
         *
         * @param name  key
         * @param value 值
         * @return 任务构造器
         */
        public Builder putExtra(String name, short value) {
            mTask.getTaskParam().putExtra(name, value);
            return this;
        }

        // -------------------------- listener ------------------------------

        /**
         * 设置文件下载监听，可监听下载状态变化、下载进度等
         *
         * @param listener 下载监听
         * @return 任务构造器
         */
        public Builder setOnDownloadListener(OnDownloadListener listener) {
            mTask.setOnDownloadListener(listener);
            return this;
        }

        /**
         * 设置文件校验监听，可监听校验文件状态变化、进度等
         *
         * @param listener 校验监听
         * @return 任务构造器
         */
        public Builder setOnCheckListener(OnCheckListener listener) {
            mTask.setOnCheckListener(listener);
            return this;
        }

        /**
         * 设置文件解压监听，可监听解压文件状态变化、进度等
         *
         * @param listener 解压监听
         * @return 任务构造器
         */
        public Builder setOnUnpackListener(OnUnpackListener listener) {
            mTask.setOnUnpackListener(listener);
            return this;
        }

        /**
         * 构建任务
         *
         * @return 任务
         */
        public ITask build() {
            mTask.getTaskParam().build();
            return mTask;
        }
    }

}
