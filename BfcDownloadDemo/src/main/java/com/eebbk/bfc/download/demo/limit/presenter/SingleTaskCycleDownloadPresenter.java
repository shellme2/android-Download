package com.eebbk.bfc.download.demo.limit.presenter;

import android.text.Html;

import com.eebbk.bfc.download.demo.baseui.IDownloadTaskConfig;
import com.eebbk.bfc.download.demo.limit.model.SingleTaskCycleDownloadModel;
import com.eebbk.bfc.download.demo.limit.ui.SingleTaskCycleDownloadView;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-21 17:08
 * Email: jacklulu29@gmail.com
 */

public class SingleTaskCycleDownloadPresenter {

    private SingleTaskCycleDownloadView mView;
    private IDownloadTaskConfig mConfigView;
    private SingleTaskCycleDownloadModel mModel;

    private OnDownloadListener mDownloadListener;
    private OnCheckListener mCheckListener;
    private OnUnpackListener mUnpackListener;

    public SingleTaskCycleDownloadPresenter(){
        mDownloadListener = createOnDownloadListener();
        mCheckListener = createOnCheckListener();
        mUnpackListener = createOnUnpackListener();
    }

    public void bindView(SingleTaskCycleDownloadView view){
        this.mView = view;
        mModel = new SingleTaskCycleDownloadModel(this.mView.getContext());
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
        ITask task = DownloadController.getInstance().getTaskById(taskId);
        if(task == null){
            // 任务已被删除，清除缓存记录
            mModel.saveTaskId(ITask.INVALID_GENERATE_ID);
            mView.showDataChangedByInit(null);
            mView.showToast(" 任务ID: " + taskId + "已被删除 ");
        } else {
            // 给新发现的任务注册监听
            task.setOnDownloadListener(mDownloadListener)
                    .setOnCheckListener(mCheckListener)
                    .setOnUnpackListener(mUnpackListener);
            registerListener(task);
            mView.showDataChangedByInit(task);
            mView.showToast(" 发现任务,ID: " + task.getId());
            boolean isStop = mModel.getIsStop();
            if(isStop){
                mView.onTestStop();
            } else {
                mView.onTestStart();
            }
        }
    }

    public ITask createTask(){
        // 创建任务并配置参数
        ITask.Builder builder = DownloadController.buildTask(mConfigView.getUrl())
                .setFileName(mConfigView.getFileName()) // 设置文件名，可包含文件后缀
                //.setFileExtension(mConfigView.getFileExtension()) // 设置文件后缀,暂不支持
                .setSavePath(mConfigView.getSavePath()) //设置下载文件保存路径
                .setPresetFileSize(mConfigView.getPresetFileSize()) // 设置文件预设大小，用来比较跟真实文件大小是否一致
                .setAutoCheckSize(mConfigView.isAutoCheckSize()) //设置是否自动比较文件大小（设置了文件预设大小才有效）
                .setPriority(mConfigView.getPriority()) //设置优先级
                .setCheckType(mConfigView.getCheckType()) //设置校验类型
                .setCheckCode(mConfigView.getCheckCode()) //设置校验码
                .setCheckEnable(mConfigView.isCheckEnable()) //设置校验开关，为true才会进行校验
                .setNetworkTypes(mConfigView.getNetworkTypes()) //设置下载可以使用的网络类型
                .setReserver(mConfigView.getReserver()) // 设置自定义字段
                .setShowRealTimeInfo(mConfigView.isShowRealTimeInfo()) // 设置显示实时速度
                .setMinProgressTime(mConfigView.getMinProgressTime()) // 设置进度回调时间，<0不回调(只会回调进度的第一次和最后一次)，=0实时回调，>0按设置的间隔时间回调
                .setAutoUnpack(mConfigView.isAutoUnpack()) // 设置自动解压
                .setUnpackPath(mConfigView.getUnpackPath()) // 设置解压文件保存路径，如果不设置，则会选择提取路径（下载路径+文件名称）保存
                .setDeleteNoEndTaskAndCache(mConfigView.isDeleteNoEndTaskAndCache()) // 设置默认删除任务时是否删除缓存文件（未下完的文件），默认删除
                .setDeleteEndTaskAndCache(mConfigView.isDeleteEndTaskAndCache()) // 设置默认删除任务时是否删除已下载的文件,默认不删除
                .setModuleName(mConfigView.getModuleName())

                //.setNotificationVisibility(mConfigView.getNotificationVisibility()) // 暂不支持
                //.setAllowAdjustSavePath(mConfigView.hasAllowAdjustSavePath()) // 暂不支持
                //.setDownloadThreads(mConfigView.getDownloadThreads()) // 暂不支持

                .setOnDownloadListener(mDownloadListener) // 增加下载监听
                .setOnCheckListener(mCheckListener) // 增加文件校验监听
                .setOnUnpackListener(mUnpackListener); // 增加解压监听
        return builder.setAutoUnpack(true).build();
    }

    public void refreshTaskData(ITask task){
        if(task == null){
            return;
        }
        if(mModel.getIsStop()){
            return;
        }
        ITask task1 = DownloadController.getInstance().getTaskById(task.getId());
        if(task1 == null){
            return;
        }
        if(mView != null && !mView.isFinishing()){
            mView.showDataChanged(task1);
        }
    }

    public void startTest(ITask task){
        if(task == null){
            return;
        }
        ITask task1 = DownloadController.getInstance().getTaskById(task.getId());
        task.setOnDownloadListener(mDownloadListener) // 增加下载监听
                .setOnCheckListener(mCheckListener) // 增加文件校验监听
                .setOnUnpackListener(mUnpackListener); // 增加解压监听
        DownloadController.getInstance().registerTaskListener(task);
        if(task1 != null){
            DownloadController.getInstance().reloadTask(task);
        } else {
            DownloadController.getInstance().addTask(task);
        }
        if(mModel.getTaskId() != task.getId()){
            mModel.saveTaskId(task.getId());
            mModel.saveStartTime(System.currentTimeMillis());
            mModel.savePauseTimes(0);
            mModel.saveRetryTimes(0);
            mModel.saveSuccessTimes(0);
            mModel.saveFailureTimes(0);
            mModel.saveEndTime(0);
        }
        mModel.saveIsStop(false);
        if(mView != null && !mView.isFinishing()){
            mView.showDataChanged(task);
            mView.onTestStart();
        }
    }

    public void stopTest(ITask task){
        DownloadController.getInstance().pauseTask(task);
        mModel.saveIsStop(true);
        mModel.saveEndTime(System.currentTimeMillis());
        if(mView != null && !mView.isFinishing()){
            mView.showDataChanged(task);
            mView.onTestStop();
        }
    }

    public void deleteTask(ITask task){
        DownloadController.getInstance().deleteTask(task);
        mModel.saveTaskId(ITask.INVALID_GENERATE_ID);
        mModel.saveIsStop(true);
        mView.showDataChanged(null);
        mView.onTestStop();
        mView.showToast(" 删除任务成功 ");
    }

    public void restartTask(ITask task){
        if(mModel.getIsStop()){
            L.w(" already stopped!!! ");
            return;
        }
        DownloadController.getInstance().reloadTask(task);
        mView.showDataChanged(task);
    }

    public void showTestInfo(){
        if(mView == null || mView.isFinishing()){
            return;
        }
        int id = mModel.getTaskId();
        if(id == ITask.INVALID_GENERATE_ID){
            mView.showToast("没有要查看的任务");
            return;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<div>");
        sb.append("任务ID：" + mModel.getTaskId());
        sb.append("</div>");
        sb.append("<div>");
        sb.append("任务开始时间：" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(mModel.getStartTime())));
        sb.append("</div>");
        sb.append("<div>");
        sb.append("暂停次数：" + mModel.getPauseTimes());
        sb.append("</div>");
        sb.append("<div>");
        sb.append("重试次数：" + mModel.getRetryTimes());
        sb.append("</div>");
        sb.append("<div>");
        sb.append("失败次数：" + mModel.getFailureTimes());
        sb.append("</div>");
        sb.append("<div>");
        sb.append("成功次数：" + mModel.getSuccessTimes());
        sb.append("</div>");
        sb.append("<div>");
        long endTime = mModel.getEndTime();
        String endTimeStr = endTime == 0 ? " - - " :
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(endTime));
        sb.append("结束时间：" + endTimeStr);
        sb.append("</div>");
        mView.showTestInfo(Html.fromHtml(sb.toString()));
    }

    public boolean registerListener(ITask task){
        return DownloadController.getInstance().registerTaskListener(task) > 0;
    }

    public void unregisterListener(ITask task){
        DownloadController.getInstance().unregisterTaskListener(task);
    }

    public void addDownloadListener(ITask task){
        task.setOnDownloadListener(mDownloadListener);
    }

    public void addCheckListener(ITask task){
        task.setOnCheckListener(mCheckListener);
    }

    public void addUnpackListener(ITask task){
        task.setOnUnpackListener(mUnpackListener);
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
                mModel.savePauseTimes(mModel.getPauseTimes()+1);
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable throwable) {
                L.i("onDownloadRetry");
                mModel.saveRetryTimes(mModel.getRetryTimes()+1);
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onDownloadFailure");
                mModel.saveFailureTimes(mModel.getFailureTimes()+1);
                if (mView != null && !mView.isFinishing()) {
                    mView.showDataChanged(task);
                }
            }

            @Override
            public void onDownloadSuccess(ITask task) {
                L.i("onDownloadSuccess");
                mModel.saveSuccessTimes(mModel.getSuccessTimes()+1);
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
