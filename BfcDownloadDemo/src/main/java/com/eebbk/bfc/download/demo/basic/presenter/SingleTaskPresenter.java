package com.eebbk.bfc.download.demo.basic.presenter;

import android.os.Environment;
import android.text.TextUtils;

import com.eebbk.bfc.download.demo.baseui.IDownloadTaskConfig;
import com.eebbk.bfc.download.demo.basic.model.SingleTaskModel;
import com.eebbk.bfc.download.demo.basic.ui.ISingleTaskView;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-21 17:08
 * Email: jacklulu29@gmail.com
 */

public class SingleTaskPresenter {

    private ISingleTaskView mView;
    private IDownloadTaskConfig mConfigView;
    private SingleTaskModel mModel;

    public SingleTaskPresenter(){

    }

    public void bindView(ISingleTaskView view){
        this.mView = view;
        mModel = new SingleTaskModel(this.mView.getContext());
    }

    public void bindDownloadConfigView(IDownloadTaskConfig view){
        this.mConfigView = view;
    }

    /**
     * 加载历史任务
     */
    public void loadHistoryTask(){
        int taskId = mModel.getTaskId();
        if(taskId == ITask.INVALID_GENERATE_ID){
            return;
        }
        // 查找任务
        ITask task = DownloadController.getInstance().getTaskById(taskId);
        if(task == null){
            // 任务已被删除，清除缓存记录
            mModel.saveTaskId(ITask.INVALID_GENERATE_ID);
            mView.showDataChangedByInit(null);
            mView.showToast(" 任务ID: " + taskId + "已被删除 ");
        } else {
            // 创建监听
            task.setOnDownloadListener(createOnDownloadListener())
                    .setOnCheckListener(createOnCheckListener())
                    .setOnUnpackListener(createOnUnpackListener());
            // 给任务注册监听
            registerListener(task);
            mView.showDataChangedByInit(task);
            mView.showToast(" 发现任务,ID: " + task.getId());
        }
    }

    public ITask createTask(){
        String path = Environment.getExternalStorageDirectory().getPath() + "/downloadTest/";
        // 创建任务并配置参数
        ITask.Builder builder = DownloadController.buildTask(mConfigView.getUrl())
            .setFileName(mConfigView.getFileName()) // 设置文件名，可包含文件后缀
            .setFileExtension(mConfigView.getFileExtension()) // 设置文件后缀,如果文件名和后缀名同时设置了，最终会使用设置的后缀名，不会使用文件名中的后缀
            .setSavePath(path/*mConfigView.getSavePath()*/) //设置下载文件保存路径
//            .setPresetFileSize(mConfigView.getPresetFileSize()) // 设置文件预设大小，用来比较跟真实文件大小是否一致
//            .setAutoCheckSize(mConfigView.isAutoCheckSize()) //设置是否自动比较文件大小（设置了文件预设大小才有效）

//            .setPriority(mConfigView.getPriority()) //设置优先级
            .setCheckType(ITask.CheckType.MD5_EX) //设置校验类型
            .setCheckCode("545111BE3D8E98EE061D83DCE1CD1C4F") //设置校验码
            .setCheckEnable(true) //设置校验开关，为true才会进行校验
            .setNetworkTypes(mConfigView.getNetworkTypes()) //设置下载可以使用的网络类型
//            .setReserver(mConfigView.getReserver()) // 设置自定义字段
//            .setExtras(mConfigView.getExtrasMap()) // 设置扩展字段
            .setShowRealTimeInfo(mConfigView.isShowRealTimeInfo()) // 设置显示实时速度
            .setMinProgressTime(mConfigView.getMinProgressTime()) // 设置进度回调时间，<0不回调(只会回调进度的第一次和最后一次)，=0实时回调，>0按设置的间隔时间回调
//            .setAutoUnpack(mConfigView.isAutoUnpack()) // 设置自动解压
//            .setUnpackPath(mConfigView.getUnpackPath()) // 设置解压文件保存路径，如果不设置，则会选择提取路径（下载路径+文件名称）保存
//            .setDeleteNoEndTaskAndCache(mConfigView.isDeleteNoEndTaskAndCache()) // 设置默认删除任务时是否删除缓存文件（未下完的文件），默认删除
//            .setDeleteEndTaskAndCache(mConfigView.isDeleteEndTaskAndCache()) // 设置默认删除任务时是否删除已下载的文件,默认不删除
            .setModuleName(mConfigView.getModuleName()) // 设置任务所属模块

            .setNotificationVisibility(mConfigView.getNotificationVisibility()) // 暂不支持
            //.setAllowAdjustSavePath(mConfigView.hasAllowAdjustSavePath()) // 暂不支持
            //.setDownloadThreads(mConfigView.getDownloadThreads()) // 暂不支持

            .setOnDownloadListener(createOnDownloadListener()) // 增加下载监听
            .setOnCheckListener(createOnCheckListener()) // 增加文件校验监听
            .setOnUnpackListener(createOnUnpackListener()); // 增加解压监听
        return builder.build();
    }

    public void startTaskAndRegisterListener(ITask task){
        if(task == null || task.getId() == ITask.INVALID_GENERATE_ID){
            mView.showToast(" 无效的任务，任务为空或者id=-1 ");
            return;
        }
        DownloadController.getInstance().registerTaskListener(task);
        DownloadController.getInstance().addTask(task);
        mModel.saveTaskId(task.getId());
        mView.showDataChanged(task);
    }

    public boolean refreshTask(ITask task){
        return DownloadController.getInstance().refreshData(task) > 0;
    }

    public void pauseTask(ITask task){
        DownloadController.getInstance().pauseTask(task);
    }

    public void resumeTask(ITask task){
        DownloadController.getInstance().resumeTask(task);
    }

    public void restartTask(ITask task){
        DownloadController.getInstance().reloadTask(task);
    }

    public void deleteTask(ITask task){
        // 删除任务会自动注销任务监听
        DownloadController.getInstance().deleteTask(task);
        // 销毁任务
        task.recycle();
        task = null;
        mModel.saveTaskId(ITask.INVALID_GENERATE_ID);
        mView.showDataChanged(null);
        mView.showToast(" 删除任务成功 ");
    }

    public void deleteTaskWithoutFile(ITask task){
        // 删除任务会自动注销任务监听
        DownloadController.getInstance().deleteTaskWithoutFile(task);
        // 销毁任务
        task.recycle();
        task = null;
        mModel.saveTaskId(ITask.INVALID_GENERATE_ID);
        mView.showDataChanged(null);
        mView.showToast(" 删除任务成功 ");
    }

    public void deleteTaskAndAllFile(ITask task){
        // 删除任务会自动注销任务监听
        DownloadController.getInstance().deleteTaskAndAllFile(task);
        // 销毁任务
        task.recycle();
        task = null;
        mModel.saveTaskId(ITask.INVALID_GENERATE_ID);
        mView.showDataChanged(null);
        mView.showToast(" 删除任务成功 ");
    }

    public void removeMobileNet(ITask task){
        setNetworkTypes(NetworkParseUtil.removeNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE), task);
        mView.showDataChanged(task);
    }

    public void addMobileNet(ITask task){
        setNetworkTypes(NetworkParseUtil.addNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE), task);
        mView.showDataChanged(task);
    }

    public void setNetworkTypes(int networkTypes, ITask task){
        L.d(" set network types " + networkTypes);
        DownloadController.getInstance().setNetworkTypes(networkTypes, task);
        mView.showDataChanged(task);
    }

    public boolean registerListener(ITask task){
        return DownloadController.getInstance().registerTaskListener(task) > 0;
    }

    public boolean unregisterListener(ITask task){
        return DownloadController.getInstance().unregisterTaskListener(task) > 0;
    }

    public boolean registerListener(String tag, ITask task){
        if(TextUtils.isEmpty(tag)){
            mView.showToast("tag是空的，将按默认方式注册监听");
        }
        return DownloadController.getInstance().registerTaskListener(tag, task) > 0;
    }

    public boolean unregisterListener(String tag){
        if(TextUtils.isEmpty(tag)){
            mView.showToast("tag不能为空！");
            return false;
        }
        return DownloadController.getInstance().unregisterTaskListener(tag);
    }

    public OnDownloadListener createOnDownloadListener(){
        return new OnDownloadListener() {
            @Override
            public void onDownloadWaiting(ITask task) {
                L.i("onDownloadWaiting");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadStarted(ITask task) {
                L.i("onDownloadStarted");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadConnected(ITask task, boolean resuming, long finishedSize, long totalSize) {
                L.i("onDownloadConnected");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloading(ITask task, long finishedSize, long totalSize) {
                L.i("onDownloading");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadPause(ITask task, String errorCode) {
                L.i("onDownloadPause");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable throwable) {
                L.i("onDownloadRetry");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onDownloadFailure");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadSuccess(ITask task) {
                L.i("onDownloadSuccess");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }
        };
    }

    public OnCheckListener createOnCheckListener(){
        return new OnCheckListener() {
            @Override
            public void onCheckStarted(ITask task, long totalSize) {
                L.i("onCheckStarted");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onChecking(ITask task, long finishedSize, long totalSize) {
                L.i("onChecking");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onCheckFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onCheckFailure");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onCheckSuccess(ITask task) {
                L.i("onCheckSuccess");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }
        };
    }

    public OnUnpackListener createOnUnpackListener(){
        return new OnUnpackListener() {
            @Override
            public void onUnpackStarted(ITask task, long totalSize) {
                L.i("onUnpackStarted");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onUnpacking(ITask task, long finishedSize, long totalSize) {
                L.i("onUnpacking");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onUnpackFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onUnpackFailure");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onUnpackSuccess(ITask task) {
                L.i("onUnpackSuccess");
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }
        };
    }

}
