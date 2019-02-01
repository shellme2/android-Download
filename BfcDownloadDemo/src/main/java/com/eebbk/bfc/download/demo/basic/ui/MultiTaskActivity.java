package com.eebbk.bfc.download.demo.basic.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;
import com.eebbk.bfc.download.demo.baseui.DownloadConfigUIHelper;
import com.eebbk.bfc.download.demo.baseui.DownloadSearchUIHelper;
import com.eebbk.bfc.download.demo.basic.presenter.MultiTaskPresenter;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.List;

public class MultiTaskActivity extends BaseActivity implements IMultiTaskView, View.OnClickListener {

    private DownloadConfigUIHelper mDownloadConfigPanel;
    private DownloadSearchUIHelper mDownloadSearchPanel;

    private TextView mTaskTitleTv;

    private Button mAddTaskBtn;
    private Button mSearchBtn;
    private Button mPauseAllTaskBtn;
    private Button mResumeAllTaskBtn;
    private Button mDeleteAllTaskBtn;
    private Button mRestartAllTaskBtn;
    private Button mRegisterOperationBtn;
    private Button mUnregisterOperationBtn;
    private Button mShowDownloadInfoBtn;
    private Button mClearDownloadInfoBtn;

    private LinearLayout mAddTaskPanel;
    private Button mAddSingleTaskBtn;
    private EditText mNeedQueueModuleNameEtv;
    private Button mAddTasksNoQueueBtn;
    private EditText mNoQueueModuleNameEtv;
    private Button mCancelAddPanelBtn;

    private ListView mListView;
    private MultiTaskAdapter mAdapter;

    private Button mCancelAddTaskBtn;
    private Button mOKAddTaskBtn;

    private MultiTaskPresenter mPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_task);
        setTitle("多任务下载测试");

        mDownloadConfigPanel = new DownloadConfigUIHelper();
        mDownloadConfigPanel.bindView(findView(R.id.add_config_panel_ly));
        mPresenter = new MultiTaskPresenter();
        mPresenter.bindView(this);
        mPresenter.bindDownloadConfigView(mDownloadConfigPanel);
        initView();
        initBtnListener();
        mDownloadConfigPanel.loadViewByDefaultTask();

        mDownloadSearchPanel = new DownloadSearchUIHelper(mPresenter);
        mDownloadSearchPanel.initView(this, findView(R.id.search_panel_layout));

        // 注册监听
        //mPresenter.registerListener();
        // 加载历史任务
        mPresenter.loadHistoryTasks();
    }

    private void initView() {
        mTaskTitleTv = findView(R.id.task_title_tv);
        mAddTaskBtn = findView(R.id.add_task_btn);
        mPauseAllTaskBtn = findView(R.id.pause_all_task_btn);
        mResumeAllTaskBtn = findView(R.id.resume_all_btn);
        mDeleteAllTaskBtn = findView(R.id.delete_all_task_btn);
        mRestartAllTaskBtn = findView(R.id.restart_all_btn);
        mRegisterOperationBtn = findView(R.id.register_operation_btn);
        mUnregisterOperationBtn = findView(R.id.unregister_operation_btn);
        mShowDownloadInfoBtn = findView(R.id.show_download_info);
        mClearDownloadInfoBtn = findView(R.id.clear_download_info);

        mAddTaskPanel = findView(R.id.add_task_panel);
        mSearchBtn = findView(R.id.seach_btn);
        mAddSingleTaskBtn = findView(R.id.add_single_task_btn);
        mNeedQueueModuleNameEtv = findView(R.id.need_queue_module_name_etv);
        mAddTasksNoQueueBtn = findView(R.id.add_tasks_no_queue_btn);
        mNoQueueModuleNameEtv = findView(R.id.no_queue_module_name_etv);
        mCancelAddPanelBtn = findView(R.id.cancel_add_panel_btn);

        mListView = findView(R.id.tasks_list_lv);
        mAdapter = new MultiTaskAdapter(this, mPresenter);
        mListView.setAdapter(mAdapter);
        mListView.setEmptyView(findView(R.id.empty_view));

        mCancelAddTaskBtn = findView(R.id.cancel_add_btn);
        mOKAddTaskBtn = findView(R.id.ok_add_btn);
    }

    private void initBtnListener() {
        mAddTaskBtn.setOnClickListener(this);
        mSearchBtn.setOnClickListener(this);
        mPauseAllTaskBtn.setOnClickListener(this);
        mResumeAllTaskBtn.setOnClickListener(this);
        mDeleteAllTaskBtn.setOnClickListener(this);
        mRestartAllTaskBtn.setOnClickListener(this);
        mRegisterOperationBtn.setOnClickListener(this);
        mUnregisterOperationBtn.setOnClickListener(this);
        mShowDownloadInfoBtn.setOnClickListener(this);
        mClearDownloadInfoBtn.setOnClickListener(this);
        findView(R.id.add_ch_tasks_need_queue_btn).setOnClickListener(this);
        findView(R.id.add_math_tasks_need_queue_btn).setOnClickListener(this);
        findView(R.id.add_en_tasks_need_queue_btn).setOnClickListener(this);
        findView(R.id.add_https_tasks_need_queue_btn).setOnClickListener(this);
        findView(R.id.add_http_single_task_big).setOnClickListener(this);
        findView(R.id.add_http_single_task_middle).setOnClickListener(this);
        findView(R.id.add_http_single_task_small).setOnClickListener(this);
        findView(R.id.add_https_single_task_big).setOnClickListener(this);
        findView(R.id.add_https_single_task_middle).setOnClickListener(this);
        findView(R.id.add_https_single_task_small).setOnClickListener(this);
        findView(R.id.add_http_single_task_big).setOnClickListener(this);
        findView(R.id.add_http_single_task_middle).setOnClickListener(this);
        findView(R.id.add_http_single_task_small).setOnClickListener(this);


        mAddSingleTaskBtn.setOnClickListener(this);
        mAddTasksNoQueueBtn.setOnClickListener(this);
        mCancelAddPanelBtn.setOnClickListener(this);

        mCancelAddTaskBtn.setOnClickListener(this);
        mOKAddTaskBtn.setOnClickListener(this);
    }

    @Override
    public boolean isFinishing() {
        return super.isFinishing();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAdapter != null) {
            mAdapter.clear();
        }
        if (mPresenter != null) {
            mPresenter.unregisterListener();
            mPresenter.unregisterOperationListener();
            mPresenter = null;
        }
    }

    @Override
    public List<ITask> getData() {
        if (mAdapter == null || mPresenter == null) {
            return null;
        }
        return mAdapter.getItems();
    }

    @Override
    public void setListData(final String moduleName, final List<ITask> data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mAdapter == null || mPresenter == null) {
                    return;
                }
                if (TextUtils.isEmpty(moduleName)) {
                    mTaskTitleTv.setText("默认模块 任务列表");
                } else {
                    mTaskTitleTv.setText("模块:[" + moduleName + "] 任务列表");
                }
                mAdapter.setItems(data);
                mAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (mDownloadConfigPanel.isShow()) {
            mDownloadConfigPanel.hidePanel();
            return;
        }
        if (mAddTaskPanel.getVisibility() == View.VISIBLE) {
            mAddTaskPanel.setVisibility(View.GONE);
            final InputMethodManager inputMethodManager = (InputMethodManager) this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(mAddTaskPanel.getWindowToken(), 0);
            return;
        }
        if (mDownloadSearchPanel.isShow()) {
            mDownloadSearchPanel.hidePanel();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onClick(View v) {
        if (mPresenter == null || mAdapter == null) {
            return;
        }
        String moduleName = null;
        switch (v.getId()) {
            case R.id.add_task_btn:
                mAddTaskPanel.setVisibility(View.VISIBLE);
                mDownloadConfigPanel.hidePanel();
                break;
            case R.id.seach_btn:
                mDownloadSearchPanel.showPanel();
                break;
            case R.id.pause_all_task_btn:
                mPresenter.pauseAllTask();
                break;
            case R.id.resume_all_btn:
                mPresenter.resumeAllTask();
                break;
            case R.id.delete_all_task_btn:
                mPresenter.deleteAllTask();
                break;
            case R.id.restart_all_btn:
                mPresenter.restartAllTask();
                break;
            case R.id.add_single_task_btn:
                mAddTaskPanel.setVisibility(View.GONE);
                mDownloadConfigPanel.showPanel();
                break;
            case R.id.add_ch_tasks_need_queue_btn:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.CHINESE_MULTI);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_math_tasks_need_queue_btn:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.MATH_MULTI);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_en_tasks_need_queue_btn:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.ENGLISH_MULTI);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_https_tasks_need_queue_btn:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTPS_MULTI);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_http_single_task_big:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTP_SINGLE_BIG);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_http_single_task_middle:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTP_SINGLE_MIDDLE);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_http_single_task_small:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTP_SINGLE_SMALL);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_https_single_task_big:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTPS_SINGLE_BIG);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_https_single_task_middle:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTPS_SINGLE_MIDDLE);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_https_single_task_small:
                moduleName = mNeedQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNeedQueue(moduleName, MultiTaskPresenter.TaskType.HTTPS_SINGLE_SMALL);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.add_tasks_no_queue_btn:
                moduleName = mNoQueueModuleNameEtv.getEditableText().toString();
                mPresenter.addTasksNoQueue(moduleName);
                mAddTaskPanel.setVisibility(View.GONE);
                break;
            case R.id.cancel_add_panel_btn:
                mAddTaskPanel.setVisibility(View.GONE);
                final InputMethodManager inputMethodManager = (InputMethodManager) this.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(mAddTaskPanel.getWindowToken(), 0);
                break;
            case R.id.cancel_add_btn:
                mDownloadConfigPanel.hidePanel();
                break;
            case R.id.ok_add_btn:
                mDownloadConfigPanel.hidePanel();
                mPresenter.startTask();
                break;
            case R.id.register_operation_btn:
                mPresenter.registerOperationListener();
                break;
            case R.id.unregister_operation_btn:
                mPresenter.unregisterOperationListener();
                break;
            case R.id.show_download_info:
                String downloadInfo = mPresenter.showDownloadInfo();
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(downloadInfo).setCancelable(true).show();
                break;
            case R.id.clear_download_info:
                mPresenter.clearDownloadInfo();
                break;
            default:
                break;
        }
    }

    @Override
    public void closeSearchPanel() {
        mDownloadSearchPanel.hidePanel();
    }

    @Override
    public String getSearchModuleName() {
        return mDownloadSearchPanel.getModuleName();
    }

    @Override
    public int getSearchType() {
        return mDownloadSearchPanel.getSearchType();
    }

    @Override
    public int getSearchId() {
        return mDownloadSearchPanel.getSearchId();
    }

    @Override
    public int getSearchStatus() {
        return mDownloadSearchPanel.getSearchStatus();
    }

    @Override
    public String[] getSearchExtraKeys() {
        return mDownloadSearchPanel.getSearchExtraKeys();
    }

    @Override
    public String[] getSearchExtraValues() {
        return mDownloadSearchPanel.getSearchExtraValues();
    }

    @Override
    public void showTaskChanged(ITask task) {
        List<ITask> tasks = mAdapter.getItems();
        int index = -1;
        if (tasks != null && !tasks.isEmpty()) {
            for (ITask tempTask : tasks) {
                if (tempTask.getId() == task.getId()) {
                    // 刷新数据
                    tempTask.updateData(task);
                    index = tasks.indexOf(tempTask);
                    break;
                }
            }
            if (index >= 0) {
                updateView(index);
            }
        }
    }

    private void updateView(int itemIndex) {
        //得到第一个可显示控件的位置，
        int visiblePosition = mListView.getFirstVisiblePosition();
        //只有当要更新的view在可见的位置时才更新，不可见时，跳过不更新
        if (itemIndex - visiblePosition >= 0) {
            //得到要更新的item的vie
            View view = mListView.getChildAt(itemIndex - visiblePosition);
            //调用adapter更新界面
            mAdapter.updateView(view, itemIndex);
        }
    }

    @Override
    public void showToast(String msg) {
        ToastUtil.showToast(this, msg);
    }

}