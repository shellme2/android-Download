package com.eebbk.bfc.download.demo.basic.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;
import com.eebbk.bfc.download.demo.baseui.DownloadConfigUIHelper;
import com.eebbk.bfc.download.demo.baseui.DownloadHandlerPanelUIHelper;
import com.eebbk.bfc.download.demo.baseui.DownloadStatusUIHelper;
import com.eebbk.bfc.download.demo.baseui.IDownloadHandler;
import com.eebbk.bfc.download.demo.baseui.ShowTaskInfoDialog;
import com.eebbk.bfc.download.demo.basic.presenter.SingleTaskPresenter;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

public class SingleTaskActivity extends BaseActivity implements ISingleTaskView, IDownloadHandler, View.OnClickListener {

    private SingleTaskPresenter mPresenter;
    private DownloadConfigUIHelper mDownloadConfigPanel;
    private DownloadStatusUIHelper mDownloadStatusPanel;
    private DownloadHandlerPanelUIHelper mDownloadHandlerPanel;

    private Button mShowConfigBtn;
    private ScrollView mConfigPanelSv;
    private Button mShowHandlerBtn;
    private ScrollView mHandlerPanelSv;
    private ScrollView mStatusPanelSv;

    private ProgressBar mProgressBar;
    private TextView mNumberProgressTv;

    private ITask mTask;
    private boolean mIsInit = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task);
        setTitle("单任务下载测试");

        mIsInit = false;
        mDownloadConfigPanel = new DownloadConfigUIHelper();
        mDownloadStatusPanel = new DownloadStatusUIHelper();
        mDownloadHandlerPanel = new DownloadHandlerPanelUIHelper(this);
        mPresenter = new SingleTaskPresenter();
        mPresenter.bindView(this);
        mPresenter.bindDownloadConfigView(mDownloadConfigPanel);
        initView();
        showInit();

        // 加载历史任务
        mPresenter.loadHistoryTask();
    }

    private void initView(){
        mShowConfigBtn = findView(R.id.show_config_panel_btn);
        mShowConfigBtn.setOnClickListener(this);
        mConfigPanelSv = findView(R.id.task_config_panel_sv);
        mDownloadConfigPanel.bindView(mConfigPanelSv);
        mDownloadConfigPanel.initSingleTaskPanel();

        mShowHandlerBtn = findView(R.id.show_handler_panel_btn);
        mShowHandlerBtn.setOnClickListener(this);
        mHandlerPanelSv = findView(R.id.handler_panel_sv);
        mDownloadHandlerPanel.bindView(mHandlerPanelSv, this);

        mStatusPanelSv = findView(R.id.status_panel_sv);
        mDownloadStatusPanel.bindView(mStatusPanelSv);
        initProgressPanel();
    }

    private void initProgressPanel(){
        mProgressBar = findView(R.id.task_progress_bar);
        mNumberProgressTv = findView(R.id.number_progress_tv);
    }

    public void showInit(){
        if(!mIsInit){
            mDownloadConfigPanel.loadViewByDefaultTask();
            mIsInit = true;
        }
        mDownloadConfigPanel.setConfigViewEnable(true);
        ITask task = mPresenter.createTask();
        mDownloadStatusPanel.updateView(task);
        updateProgressView(task);
    }

    public void showDataChangedByInit(ITask task){
        showDataChanged(task, true);
    }

    public void showDataChanged(ITask task){
        showDataChanged(task, false);
    }

    /**
     * 数据有变化
     * @param task 任务
     * @param isInit 是否初始化
     */
    private void showDataChanged(ITask task, boolean isInit){
        mTask = task;
        // 置空任务
        if(mTask == null){
            // 初始化配置任务界面
            showInit();
            // 更新操作界面
            mDownloadHandlerPanel.onDataChanged(null);
            mDownloadConfigPanel.setConfigViewEnable(true);
        } else {
            updateView(task);
            if(isInit){
                // 更新配置界面
                mDownloadConfigPanel.updateView(task);
            }
            mDownloadHandlerPanel.onDataChanged(task);
            mDownloadConfigPanel.setConfigViewEnable(false);
        }
    }

    @Override
    public Context getContext() {
        return this;
    }

    @Override
    public void showToast(String msg) {
        ToastUtil.showToast(this, msg);
    }

    @Override
    public boolean isFinishing() {
        return super.isFinishing();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 界面销毁，注销单任务监听，并及时回收资源
        if(mTask != null){
            mPresenter.unregisterListener(mTask);
            mTask.recycle();
        }
        mTask = null;
        mPresenter = null;
        L.e(" onDestroy ");
    }

    @Override
    public void onClick(View v) {
        if(mPresenter == null){
            return;
        }
        switch (v.getId()){
            case R.id.show_config_panel_btn:
                if(mConfigPanelSv.getVisibility() == View.VISIBLE){
                    mConfigPanelSv.setVisibility(View.GONE);
                    mShowConfigBtn.setText("展开");
                } else {
                    mConfigPanelSv.setVisibility(View.VISIBLE);
                    mShowConfigBtn.setText("收起");
                    mHandlerPanelSv.setVisibility(View.GONE);
                    mShowHandlerBtn.setText("展开");
                }
                break;
            case R.id.show_handler_panel_btn:
                if(mHandlerPanelSv.getVisibility() == View.VISIBLE){
                    mHandlerPanelSv.setVisibility(View.GONE);
                    mShowHandlerBtn.setText("操作");
                } else {
                    mHandlerPanelSv.setVisibility(View.VISIBLE);
                    mShowHandlerBtn.setText("收起");
                    mConfigPanelSv.setVisibility(View.GONE);
                    mShowConfigBtn.setText("展开");
                }
                break;
            default:
                break;
        }
    }

    private boolean checkTask(ITask task){
        if(task == null){
            ToastUtil.showToast(this, " 没有可处理的任务，请先点击“开始”或者“查看”！ ");
            return true;
        }
        if(task.getId() == ITask.INVALID_GENERATE_ID){
            ToastUtil.showToast(this, " 任务ID无效，请检测配置是否正确 ");
            return true;
        }
        return false;
    }

    private void updateView(ITask task){
        mDownloadStatusPanel.updateView(task);
        updateProgressView(task);
    }

    private void updateProgressView(ITask task){
        showProgressView(task.getState(), task.getFileSize(), task.getFinishSize());
    }

    private void showProgressView(int state, long total, long finished){
        int progress = 0;
        if(total > 0 && finished >= 0){
            progress = (int)(finished * 100 / total);
            if(progress > 100){
                progress = 100;
            }
            mNumberProgressTv.setText(DownloadUtils.formatProgress(total, finished, 100.00, 2) + "%");
            mNumberProgressTv.setVisibility(View.VISIBLE);
        } else {
            mNumberProgressTv.setVisibility(View.INVISIBLE);
        }
        mProgressBar.setProgress(progress);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onStartBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(mTask == null){
            mTask = mPresenter.createTask();
            if(checkTask(mTask)){
                return;
            }
        }
        mPresenter.startTaskAndRegisterListener(mTask);
    }

    @Override
    public void onPauseBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.pauseTask(mTask);
    }

    @Override
    public void onResumeBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.resumeTask(mTask);
    }

    @Override
    public void onRestartBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.restartTask(mTask);
    }

    @Override
    public void onLookInfoBtnClick() {
        if(mTask == null || mTask.getId() == ITask.INVALID_GENERATE_ID){
            ToastUtil.showToast(this, " 没有可处理的任务！");
            return;
        }
        if(!mPresenter.refreshTask(mTask)){
            showToast("刷新数据失败，任务可能已被删除");
        }
        ShowTaskInfoDialog.show(this, DemoUtil.getTaskInfo(mTask));
    }

    @Override
    public void onNetworkChanged(int networkTypes) {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.setNetworkTypes(networkTypes, mTask);
    }

    @Override
    public void onRegisterListenerBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        // 创建监听
        if(mTask.getOnDownloadListener() == null){
            mTask.setOnDownloadListener(mPresenter.createOnDownloadListener());
        }
        if(mTask.getOnCheckListener() == null){
            mTask.setOnCheckListener(mPresenter.createOnCheckListener());
        }
        if(mTask.getOnUnpackListener() == null){
            mTask.setOnUnpackListener(mPresenter.createOnUnpackListener());
        }
        // 注册监听
        boolean result = mPresenter.registerListener(mTask);
        if(result){
            ToastUtil.showToast(this, " 注册监听成功，可以实时查看进度了！ ");
        } else {
            ToastUtil.showToast(this, " 注册监听失败，详细请查看日志！ ");
        }
    }

    @Override
    public void onUnregisterListenerBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        boolean result = mPresenter.unregisterListener(mTask);
        if(result){
            ToastUtil.showToast(this, " 注销监听成功！ ");
        } else {
            ToastUtil.showToast(this, " 注销监听失败，详细请查看日志！ ");
        }
    }

    @Override
    public void onRegisterListenerByTagBtnClick(String tag) {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        // 创建监听
        if(mTask.getOnDownloadListener() == null){
            mTask.setOnDownloadListener(mPresenter.createOnDownloadListener());
        }
        if(mTask.getOnCheckListener() == null){
            mTask.setOnCheckListener(mPresenter.createOnCheckListener());
        }
        if(mTask.getOnUnpackListener() == null){
            mTask.setOnUnpackListener(mPresenter.createOnUnpackListener());
        }
        // 注册监听
        boolean result = mPresenter.registerListener(tag, mTask);
        if(result){
            showToast(" 按tag: "+tag+" 注册监听成功，可以实时查看进度了！ ");
        } else {
            showToast( " 按tag: "+tag+" 监听失败，详细请查看日志！ ");
        }
    }

    @Override
    public void onUnregisterListenerByTagBtnClick(String tag) {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        boolean result = mPresenter.unregisterListener(tag);
        if(result){
            ToastUtil.showToast(this, " 按tag: "+tag+" 注销监听成功！ ");
        } else {
            ToastUtil.showToast(this, " 按tag: "+tag+" 注销监听失败，详细请查看日志！ ");
        }
    }

    @Override
    public void onDeleteByDefault() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.deleteTask(mTask);
    }

    @Override
    public void onDeleteAllFile() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.deleteTaskAndAllFile(mTask);
    }

    @Override
    public void onDeleteWithoutFile() {
        if(mPresenter == null){
            return;
        }
        if(checkTask(mTask)){
            return;
        }
        mPresenter.deleteTaskWithoutFile(mTask);
    }

}