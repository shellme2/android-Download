package com.eebbk.bfc.download.demo.limit.ui;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;
import com.eebbk.bfc.download.demo.baseui.DownloadConfigUIHelper;
import com.eebbk.bfc.download.demo.baseui.DownloadStatusUIHelper;
import com.eebbk.bfc.download.demo.baseui.ShowTaskInfoDialog;
import com.eebbk.bfc.download.demo.limit.presenter.SingleTaskCycleDownloadPresenter;
import com.eebbk.bfc.download.demo.util.PowerWakeLock;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

public class SingleTaskCycleDownloadTestActivity extends BaseActivity implements SingleTaskCycleDownloadView, View.OnClickListener {

    private SingleTaskCycleDownloadPresenter mPresenter;
    private DownloadConfigUIHelper mDownloadConfigPanel;
    private DownloadStatusUIHelper mDownloadStatusPanel;

    private Button mShowConfigBtn;
    private ScrollView mConfigPanelSv;
    private ScrollView mStatusPanelSv;

    private ProgressBar mProgressBar;
    private TextView mNumberProgressTv;

    private Button mStartBtn;
    private Button mDeleteBtn;
    private Button mLookInfoBtn;

    private ITask mTask;
    private boolean mIsInit = false;
    private boolean mIsStop = true;
    private PowerWakeLock mWakeLock;

    private Handler mHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task_cycle_download);
        setTitle("单任务循环下载测试");

        mIsInit = false;
        mDownloadConfigPanel = new DownloadConfigUIHelper();
        mDownloadStatusPanel = new DownloadStatusUIHelper();
        mPresenter = new SingleTaskCycleDownloadPresenter();
        mPresenter.bindView(this);
        mPresenter.bindDownloadConfigView(mDownloadConfigPanel);
        initView();
        showInit();

        // 加载历史任务
        mPresenter.loadHistoryTask();

        mWakeLock = new PowerWakeLock();
        mWakeLock.acquireWakeLock(this.getApplicationContext());
    }

    private void initView(){
        mShowConfigBtn = findView(R.id.show_config_panel_btn);
        mShowConfigBtn.setOnClickListener(this);
        mConfigPanelSv = findView(R.id.task_config_panel_sv);
        mDownloadConfigPanel.bindView(mConfigPanelSv);
        mDownloadConfigPanel.initSingleTaskPanel();

        mStatusPanelSv = findView(R.id.status_panel_sv);
        mDownloadStatusPanel.bindView(mStatusPanelSv);
        initProgressPanel();

        mStartBtn = findView(R.id.start_btn);
        mStartBtn.setOnClickListener(this);
        mDeleteBtn = findView(R.id.delete_btn);
        mDeleteBtn.setOnClickListener(this);
        mLookInfoBtn = findView(R.id.look_info_btn);
        mLookInfoBtn.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.startTest(mTask);
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
     */
    private void showDataChanged(ITask task, boolean isInit){
        if(mTask != null && task != null){
            // 只更新数据，如果写成mTask = task，mTask中原有的监听将会丢失
            mTask.updateData(task);
        } else {
            mTask = task;
        }
        // 置空任务
        if(task == null){
            // 初始化配置任务界面
            showInit();
        } else {
            updateView(task);
            if(isInit){
                // 更新配置界面
                mDownloadConfigPanel.updateView(task);
            }
            mDownloadConfigPanel.setConfigViewEnable(false);

            if(task.isFinished()){
                /*mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(mPresenter != null){
                            mPresenter.restartTask(mTask);
                        }
                    }
                }, 50);*/
                mPresenter.restartTask(mTask);
            }
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
        if(mWakeLock != null){
            mWakeLock.releaseWakeLock();
            mWakeLock = null;
        }
        if(mTask != null && mPresenter != null){
            mPresenter.unregisterListener(mTask);
        }
        mTask = null;
        mPresenter = null;
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
                }
                break;
            case R.id.start_btn:
                mConfigPanelSv.setVisibility(View.GONE);
                mShowConfigBtn.setText("展开");
                onStartBtnClick();
                break;
            case R.id.delete_btn:
                if(!checkTask(mTask)){
                    return;
                }
                mPresenter.deleteTask(mTask);
                break;
            case R.id.look_info_btn:
                mPresenter.showTestInfo();
                break;
            default:
                break;
        }
    }

    private boolean checkTask(ITask task){
        if(task == null){
            ToastUtil.showToast(this, " 没有可处理的任务 ");
            return false;
        }
        if(task.getId() == ITask.INVALID_GENERATE_ID){
            ToastUtil.showToast(this, " 任务ID无效，请检测配置是否正确 ");
            return false;
        }
        return true;
    }

    private void updateView(ITask task){
        mTask = task;
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

    public void onStartBtnClick() {
        if(mPresenter == null){
            return;
        }
        if(mIsStop){
            if(mTask == null){
                mTask = mPresenter.createTask();
                if(!checkTask(mTask)){
                    return;
                }
            }
            mPresenter.startTest(mTask);
        } else {
            if(mTask == null){
                return;
            }
            mPresenter.stopTest(mTask);
        }
    }

    @Override
    public void onTestStart(){
        mIsStop = false;
        // 更新操作界面
        mStartBtn.setText("停止");
    }

    @Override
    public void onTestStop() {
        mIsStop = true;
        // 更新操作界面
        mStartBtn.setText("开始");
    }

    @Override
    public void showTestInfo(CharSequence charSequence) {
        ShowTaskInfoDialog.show(this, charSequence);
    }
}