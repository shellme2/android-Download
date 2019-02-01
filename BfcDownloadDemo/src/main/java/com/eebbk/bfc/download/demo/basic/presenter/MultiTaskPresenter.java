package com.eebbk.bfc.download.demo.basic.presenter;

import android.os.Environment;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.eebbk.bfc.download.demo.baseui.DownloadSearchUIHelper;
import com.eebbk.bfc.download.demo.baseui.IDownloadTaskConfig;
import com.eebbk.bfc.download.demo.basic.monitor.DownloadInfoMonitor;
import com.eebbk.bfc.download.demo.basic.ui.IMultiTaskView;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.listener.OnCheckListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;
import com.eebbk.bfc.sdk.download.listener.OnUnpackListener;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.eebbk.bfc.sdk.downloadmanager.DownloadListener;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.eebbk.bfc.download.demo.basic.presenter.MultiTaskPresenter.TaskType.HTTP_SINGLE_BIG;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-25 10:45
 * Email: jacklulu29@gmail.com
 */

public class MultiTaskPresenter implements DownloadSearchUIHelper.ISearchHandler {

    private IMultiTaskView mView;
    private IDownloadTaskConfig mConfigView;
    private DownloadListener mListener;
    private OnDownloadOperationListener mOperationListener;

    private Handler mHandler;

    private String mOldModuleName = null;
    private boolean mFirstRegister = true;

    public enum TaskType {
        CHINESE_MULTI,
        ENGLISH_MULTI,
        MATH_MULTI,
        HTTPS_MULTI,
        HTTPS_SINGLE_BIG,
        HTTPS_SINGLE_MIDDLE,
        HTTPS_SINGLE_SMALL,
        HTTP_SINGLE_BIG,
        HTTP_SINGLE_MIDDLE,
        HTTP_SINGLE_SMALL
    }

    public MultiTaskPresenter() {
        mHandler = new Handler();
        mListener = new DownloadListener();
        addDownloadListener();
        addCheckListener();
        addUnpackListener();
    }

    public void bindView(IMultiTaskView view) {
        this.mView = view;
    }

    public void bindDownloadConfigView(IDownloadTaskConfig view) {
        this.mConfigView = view;
    }

    public void loadHistoryTasks() {
        searchTasksOrAll();
    }

    public void startTask() {
        // 直接创建任务
        ITask task = createTaskByConfigPanel();
        if (task.getId() == ITask.INVALID_GENERATE_ID) {
            mView.showToast(" 无效的任务， id = -1 ");
            return;
        }
        // 开始任务
        addTask(task);
    }

    public void addTask(ITask task) {
        DownloadController.getInstance().addTask(task);
        searchTasksOrAll();
    }

    public void pauseTask(ITask task) {
        DownloadController.getInstance().pauseTask(task);
    }

    public void resumeTask(ITask task) {
        DownloadController.getInstance().resumeTask(task);
    }

    public void restartTask(ITask task) {
        DownloadController.getInstance().reloadTask(task);
    }

    public void deleteTask(ITask task) {
        DownloadController.getInstance().deleteTask(task);
        // 重新从数据库加载任务数据
        searchTasksOrAll();
    }

    public void deleteTaskWithoutFile(ITask task) {
        DownloadController.getInstance().deleteTaskWithoutFile(task);
        // 重新从数据库加载任务数据
        searchTasksOrAll();
    }

    public void deleteTaskAndAllFile(ITask task) {
        DownloadController.getInstance().deleteTaskAndAllFile(task);
        // 重新从数据库加载任务数据
        searchTasksOrAll();
    }

    public void refreshTaskAndNotify(ITask task) {
        if (task == null) {
            return;
        }
        if (DownloadController.getInstance().refreshData(task) > 0) {
            if (mView != null) {
                L.i("刷新任务[id=" + task.getId() + "]成功， networkTypes: " + task.getNetworkTypes());
                mView.showTaskChanged(task);
            }
        } else {
            if (mView != null) {
                mView.showToast("刷新任务[id=" + task.getId() + "]失败，任务可能已被删除");
            }
        }
    }

    public boolean refreshTask(ITask task) {
        return DownloadController.getInstance().refreshData(task) > 0;
    }

    public boolean removeMobileNet(ITask task) {
        return setNetworkTypes(NetworkParseUtil.removeNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE), task);
    }

    public boolean addMobileNet(ITask task) {
        return setNetworkTypes(NetworkParseUtil.addNetworkType(task.getNetworkTypes(), NetworkType.NETWORK_MOBILE), task);
    }

    private boolean setNetworkTypes(int networkTypes, ITask task) {
        L.d(" set network types " + networkTypes);
        DownloadController.getInstance().setNetworkTypes(networkTypes, task);
        mView.showTaskChanged(task);
        return false;
    }

    public void pauseAllTask() {
        ITask[] tasks = getData();
        if (tasks != null) {
            DownloadController.getInstance().pauseTask(tasks);
        }
    }

    public void resumeAllTask() {
        ITask[] tasks = getData();
        if (tasks != null) {
            DownloadController.getInstance().resumeTask(tasks);
        }
    }

    public void deleteAllTask() {
        // 删除列表任务
        ITask[] tasks = getData();
        if (tasks != null) {
            DownloadController.getInstance().deleteTaskAndAllFile(tasks);
        }
        // 重新从数据库加载任务数据
        searchTasksOrAll();
    }

    public void restartAllTask() {
        ITask[] tasks = getData();
        if (tasks != null) {
            DownloadController.getInstance().reloadTask(tasks);
        }
        // 重新从数据库加载任务数据
        searchTasksOrAll();
    }

    public void addTasksNeedQueue(String moduleName, TaskType taskType) {
        List<String> urlsList = new ArrayList<>();
        switch (taskType) {
            case CHINESE_MULTI:
                urlsList = readUrlFromFile("download_ch.txt");
                break;
            case MATH_MULTI:
                urlsList = readUrlFromFile("download_math.txt");
                break;
            case ENGLISH_MULTI:
                urlsList = readUrlFromFile("download_en.txt");
                break;
            case HTTPS_MULTI:
                urlsList = readUrlFromFile("download_https.txt");
                break;
            case HTTP_SINGLE_BIG:
               // urlsList = readUrlFromFile("download_http_single_big.txt");
                urlsList = readUrlFromFile("download_http_md5.txt");
                break;
            case HTTP_SINGLE_MIDDLE:
                urlsList = readUrlFromFile("download_http_single_middle.txt");
                break;
            case HTTP_SINGLE_SMALL:
                urlsList = readUrlFromFile("download_http_single_small.txt");
                break;
            case HTTPS_SINGLE_BIG:
                urlsList = readUrlFromFile("download_https_single_big.txt");
                break;
            case HTTPS_SINGLE_MIDDLE:
                urlsList = readUrlFromFile("download_https_single_middle.txt");
                break;
            case HTTPS_SINGLE_SMALL:
                urlsList = readUrlFromFile("download_https_single_small.txt");
                break;
            default:
                urlsList = readUrlFromFile("download_ch.txt");
                break;
        }

        if (!urlsList.isEmpty()) {
            if ( taskType == HTTP_SINGLE_BIG ) {
                LogUtil.e("---->走校验MD5通道");
                // 读取第一行不知道为什么字符出问题，跳过第一行
                for (int j = 1; j < urlsList.size(); j++) {
                    String[]item_info = urlsList.get(j).split("--->");
                    if (item_info.length <= 1) {
                        addTask(createTask(item_info[0], "1C879F8AD0D58093E714E5D8C1E7435C",moduleName));
                    } else {
                        addTask(createTask(item_info[0], item_info[1],moduleName));
                    }
//                addTask(createTaskOnlyUrl(urlsList.get(j)).setNeedQueue(true).setModuleName(moduleName).build());
                }

            } else {
                // 读取第一行不知道为什么字符出问题，跳过第一行
                for (int j = 1; j < urlsList.size(); j++) {
//                    addTask(createTask(urlsList.get(j),moduleName));
                    addTask(createTaskOnlyUrl(urlsList.get(j)).setNeedQueue(true).setModuleName(moduleName).build());
                }
            }
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadHistoryTasks();
            }
        }, 500);
        DownloadInfoMonitor.setDownloadStartTimeFlag();
    }

    private List<String> readUrlFromFile(String fileName) {
        List<String> urlsList = new ArrayList<>();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(DownloadInitHelper.getInstance().getAppContext().getAssets().open(fileName)));
            String line;
            while ((line = reader.readLine()) != null) {
                urlsList.add(line);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            ToastUtil.showLongToast(DownloadInitHelper.getInstance().getAppContext(), "未发现下载任务文件：" + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return urlsList;
    }

    public void addTasksNoQueue(String moduleName) {
        String[] urls = {
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2015/09/01/004702061_719be2a0-44dd-4aa8-a6ab-e1e7656da182.apk",
            "http://mfile.eebbk.net/h600s/market/H10/other/VideoPlayer1.2/VideoPlayer.apk",
            "http://mfile.eebbk.net/h600s/market/H10/primary/ComoEnglish1.1/ComoEnglish.apk",
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2014/11/24/141900469_happywords.apk",
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2014/12/02/084602364_readingcard.apk",
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2015/09/01/051401572_c25169b9-c0c1-40c6-ad6b-837b5e9daf04.apk",
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2015/11/30/180633027_7aab4008bda39dc3.apk",
            "http://mfile.eebbk.net/h600s/market/H10/primary/InterestingApplications_Junior1.0/InterestingApplications_Junior.apk",
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2015/02/05/105043301_learnspell.apk",
            "http://7oxjx1.com2.z0.glb.clouddn.com/marketDeveloper/2015/11/13/081439562_c9b7c458ede99ffd.apk"
        };

        for (String url : urls) {
            addTask(createTaskOnlyUrl(url).setNeedQueue(false).setModuleName(moduleName).build());
        }

        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadHistoryTasks();
            }
        }, 500);
    }

    @Override
    public void searchTasks(boolean ifErrorThenAll) {
        // 通知界面数据改变，需要重新加载
        if (mView != null && !mView.isFinishing()) {
            int searchType = mView.getSearchType();
            String moduleName = mView.getSearchModuleName();

            if (mFirstRegister) {
                mFirstRegister = false;
                // 注册新模块监听
                registerListener(moduleName);
                registerOperationListener(moduleName);
            } else if ((!TextUtils.isEmpty(mOldModuleName) && !mOldModuleName.equals(moduleName)) || (!TextUtils.isEmpty(moduleName) && !moduleName.equals(mOldModuleName))) {
                // 注销旧模块监听
                unregisterListener(mOldModuleName);
                unregisterOperationListener(mOldModuleName);
                // 注册新模块监听
                registerListener(moduleName);
                registerOperationListener(moduleName);
            }
            mOldModuleName = moduleName;

            switch (searchType) {
                case IMultiTaskView.SEARCH_ALL:
                    // 查找所有任务
                    searchAll(moduleName);
                    break;
                case IMultiTaskView.SEARCH_BY_ID:
                    // 按id查找任务
                    int id = mView.getSearchId();
                    if (id == ITask.INVALID_GENERATE_ID) {
                        if (ifErrorThenAll) {
                            searchAll(moduleName);
                        } else {
                            mView.showToast("无效的任务id，请在“查找任务”界面重新输入");
                        }
                        return;
                    }
                    searchById(id);
                    break;
                case IMultiTaskView.SEARCH_BY_STATUS:
                    // 按状态查找任务
                    int state = mView.getSearchStatus();
                    searchByStatus(moduleName, state);
                    break;
                case IMultiTaskView.SEARCH_BY_EXTRAS:
                    // 按扩展字段查找任务
                    String[] extraKeys = mView.getSearchExtraKeys();
                    String[] extraValues = mView.getSearchExtraValues();
                    searchByExtras(moduleName, extraKeys, extraValues);
                    break;
                default:
                    break;
            }
            mView.closeSearchPanel();
        }
    }

    /**
     * 查找任务
     */
    private void searchTasksOrAll() {
        searchTasks(true);
    }

    private ITask[] getData() {
        if (mView == null) {
            return null;
        }
        List<ITask> taskList = mView.getData();
        if (taskList == null || taskList.isEmpty()) {
            return null;
        }
        ITask[] tasks = taskList.toArray(new ITask[taskList.size()]);
        return tasks;
    }

    private boolean registerListener(String moduleName) {
        if (TextUtils.isEmpty(moduleName)) {
            return DownloadController.getInstance().registerTaskListener(mListener);
        } else {
            return DownloadController.getInstance().registerTaskListener(mListener, moduleName);
        }
    }

    private boolean unregisterListener(String moduleName) {
        if (TextUtils.isEmpty(moduleName)) {
            return DownloadController.getInstance().unregisterTaskListener(mListener);
        } else {
            return DownloadController.getInstance().unregisterTaskListener(mListener, moduleName);
        }
    }

    public boolean unregisterListener() {
        return unregisterListener(mOldModuleName);
    }

    private void searchById(int id) {
        String moduleName = null;
        // 按id查找任务
        ITask task = DownloadController.getInstance().getTaskById(id);
        if (mView != null && !mView.isFinishing()) {
            ArrayList<ITask> tasks = new ArrayList<>();
            if (task != null) {
                tasks.add(task);
                moduleName = task.getModuleName();
            }
            mView.setListData(moduleName, tasks);
        }
    }

    private void searchByStatus(String moduleName, int state) {
        // 按状态查找任务
        ArrayList<ITask> tasks = null;
        if (TextUtils.isEmpty(moduleName)) {
            tasks = DownloadController.getInstance().getTaskByStatus(state);
        } else {
            tasks = DownloadController.getInstance().getTaskByStatus(state, moduleName);
        }

        if (mView != null && !mView.isFinishing()) {
            mView.setListData(moduleName, tasks);
        }
    }

    private void searchByExtras(String moduleName, String[] keys, String[] values) {
        // 按扩展字段查找任务
        // 按状态查找任务
        ArrayList<ITask> tasks = null;
        if (TextUtils.isEmpty(moduleName)) {
            tasks = DownloadController.getInstance().getTaskByExtras(keys, values);
        } else {
            tasks = DownloadController.getInstance().getTaskByExtras(keys, values, moduleName);
        }
        if (mView != null && !mView.isFinishing()) {
            mView.setListData(moduleName, tasks);
        }
    }

    private void searchAll(String moduleName) {
        //获取所有任务
        ArrayList<ITask> tasks = null;
        if (TextUtils.isEmpty(moduleName)) {
            tasks = DownloadController.getInstance().getTask();
        } else {
            tasks = DownloadController.getInstance().getTask(moduleName);
        }
        if (mView != null && !mView.isFinishing()) {
            mView.setListData(moduleName, tasks);
        }
    }

    @Override
    public void closeSearchPanel() {
        mView.closeSearchPanel();
    }

    private ITask.Builder createTaskOnlyUrl(String url) {
        String path = Environment.getExternalStorageDirectory().getPath() + "/downloadTest/";
        String[] tempList1 = url.split("/");
        String fileName = tempList1[tempList1.length - 1];
        String fileEx = fileName.substring(fileName.length() - 4);
        return DownloadController.buildTask(url)
            .setSavePath( /*((Context) mView).getCacheDir().getAbsolutePath()*/path)
            .setFileName(fileName)
            .setFileExtension(fileEx)
            .setShowRealTimeInfo(true);
    }

    private ITask.Builder createTaskCheckFile(String url, String checkType, String checkCode) {
        return DownloadController.buildTask(url)
            .setCheckEnable(true)
            .setCheckType(checkType)
            .setCheckCode(checkCode);
    }

    public ITask.Builder createTaskUnpack(String url) {
        return DownloadController.buildTask(url)
            .setAutoUnpack(true);
    }

    public ITask.Builder createTaskCheckFileAndUnpack(String url, String checkType, String checkCode) {
        return createTaskCheckFile(url, checkType, checkCode).setAutoUnpack(true);
    }

    public ITask.Builder createTaskChangeName(String url, String fileName) {
        String savePath = Environment.getExternalStorageDirectory() + File.separator + "BfcDownload" + File.separator;
        return DownloadController.buildTask(url, fileName, savePath);
    }

    public ITask.Builder createTaskChangeNameAndExtension(String url, String fileName, String extension) {
        //String savePath = Environment.getExternalStorageDirectory() + File.separator + "BfcDownload" + File.separator;
        return DownloadController.buildTask(url)
            .setFileName(fileName)
            .setFileExtension(extension);
    }

    private ITask createTaskByConfigPanel() {
        // 创建任务并配置参数
        ITask task = DownloadController.buildTask(mConfigView.getUrl())
            .setFileName(mConfigView.getFileName())
            .setFileExtension(mConfigView.getFileExtension())
            .setSavePath(mConfigView.getSavePath())
            .setPresetFileSize(mConfigView.getPresetFileSize())
            .setAutoCheckSize(mConfigView.isAutoCheckSize())
            .setPriority(mConfigView.getPriority())
            .setCheckType(mConfigView.getCheckType())
            .setCheckCode(mConfigView.getCheckCode())
            .setCheckEnable(mConfigView.isCheckEnable())
            .setNetworkTypes(mConfigView.getNetworkTypes())
            .setNeedQueue(mConfigView.isNeedQueue())
            .setReserver(mConfigView.getReserver())
            .setShowRealTimeInfo(mConfigView.isShowRealTimeInfo())
            .setMinProgressTime(mConfigView.getMinProgressTime())
            .setAutoUnpack(mConfigView.isAutoUnpack())
            .setUnpackPath(mConfigView.getUnpackPath())
            .setDeleteNoEndTaskAndCache(mConfigView.isDeleteNoEndTaskAndCache())
            .setDeleteEndTaskAndCache(mConfigView.isDeleteEndTaskAndCache())
            .setExtras(mConfigView.getExtrasMap())
            .setModuleName(mConfigView.getModuleName())
            .build();
        return task;
    }

    private void addDownloadListener() {
        mListener.setOnDownloadListener(new OnDownloadListener() {
            @Override
            public void onDownloadWaiting(ITask task) {
                L.i("onDownloadWaiting: " + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onDownloadStarted(ITask task) {
                L.i("onDownloadStarted: " + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
                DownloadInfoMonitor.insertToAllTask(task);
            }

            @Override
            public void onDownloadConnected(ITask task, boolean resuming, long finishedSize, long totalSize) {
                L.i("onDownloadConnected: " + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onDownloading(ITask task, long finishedSize, long totalSize) {
                L.i("onDownloading: " + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onDownloadPause(ITask task, String errorCode) {
                L.i("onDownloadPause: " + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable throwable) {
                L.i("onDownloadRetry" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onDownloadFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onDownloadFailure" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
                DownloadInfoMonitor.insertToFailTask(task);
            }

            @Override
            public void onDownloadSuccess(ITask task) {
                L.i("onDownloadSuccess" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
                DownloadInfoMonitor.insertToSuccessTask(task);
            }
        });
    }

    private void addCheckListener() {
        mListener.setOnCheckListener(new OnCheckListener() {
            @Override
            public void onCheckStarted(ITask task, long totalSize) {
                L.i("onCheckStarted" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onChecking(ITask task, long finishedSize, long totalSize) {
                L.i("onChecking" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onCheckFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onCheckFailure" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onCheckSuccess(ITask task) {
                L.i("onCheckSuccess" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }
        });
    }

    private void addUnpackListener() {
        mListener.setOnUnpackListener(new OnUnpackListener() {
            @Override
            public void onUnpackStarted(ITask task, long totalSize) {
                L.i("onUnpackStarted" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onUnpacking(ITask task, long finishedSize, long totalSize) {
                L.i("onUnpacking" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onUnpackFailure(ITask task, String errorCode, Throwable throwable) {
                L.i("onUnpackFailure" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }

            @Override
            public void onUnpackSuccess(ITask task) {
                L.i("onUnpackSuccess" + task.getId() + " | " + task.getRealUrl());
                if (mView != null && !mView.isFinishing()) {
                    mView.showTaskChanged(task);
                }
            }
        });
    }

    public void registerOperationListener() {
        if (mOperationListener != null) {
            mView.showToast(" 已经注册过了 ");
            return;
        }
        mOperationListener = new OnDownloadOperationListener() {
            @Override
            public void onTaskAdd(int taskId) {
                if (mView != null && !mView.isFinishing()) {
                    mView.showToast(" 监听到操作：添加任务，任务ID： " + taskId);
                }
            }

            @Override
            public void onTaskDelete(int taskId) {
                if (mView != null && !mView.isFinishing()) {
                    mView.showToast(" 监听到操作：删除任务，任务ID： " + taskId);
                }
            }
        };
        registerOperationListener(mOldModuleName);
    }

    private void registerOperationListener(String moduleName) {
        if (mOperationListener == null) {
            return;
        }
        boolean result = DownloadController.getInstance().registerOperationListener(mOperationListener, moduleName);
        mView.showToast(" 为模块：" + (TextUtils.isEmpty(moduleName) ? "默认" : moduleName) + " 注册操作监听成功： " + DemoUtil.getBooleanStr(result));
    }

    public void unregisterOperationListener() {
        unregisterOperationListener(mOldModuleName);
        mOperationListener = null;
    }

    private void unregisterOperationListener(String moduleName) {
        if (mOperationListener == null) {
            return;
        }
        boolean result = DownloadController.getInstance().unregisterOperationListener(mOperationListener, moduleName);
        mView.showToast(" 为模块：" + (TextUtils.isEmpty(moduleName) ? "默认" : moduleName) + " 注销操作监听成功： " + DemoUtil.getBooleanStr(result));
    }

    public String showDownloadInfo() {
        return DownloadInfoMonitor.getDownloadInfo();
    }

    public void clearDownloadInfo() {
        DownloadInfoMonitor.clearDownloadInfo();
    }

    public ITask createTask(String url, String md5, String moduleName){
        String path = Environment.getExternalStorageDirectory().getPath() + "/downloadTest/";
        // 创建任务并配置参数
        ITask.Builder builder = DownloadController.buildTask(url)
                .setModuleName(moduleName)
                .setFileName(mConfigView.getFileName()) // 设置文件名，可包含文件后缀
                .setFileExtension(mConfigView.getFileExtension()) // 设置文件后缀,如果文件名和后缀名同时设置了，最终会使用设置的后缀名，不会使用文件名中的后缀
                .setSavePath(path/*mConfigView.getSavePath()*/) //设置下载文件保存路径
//            .setPresetFileSize(mConfigView.getPresetFileSize()) // 设置文件预设大小，用来比较跟真实文件大小是否一致
//            .setAutoCheckSize(mConfigView.isAutoCheckSize()) //设置是否自动比较文件大小（设置了文件预设大小才有效）

//            .setPriority(mConfigView.getPriority()) //设置优先级
                .setCheckType(ITask.CheckType.MD5_EX) //设置校验类型
                .setCheckCode(md5) //设置校验码
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
                .setOnDownloadListener(mDownloadListener) // 增加下载监听
                .setOnCheckListener(mOnCheckLister) // 增加文件校验监听
                .setOnUnpackListener(mUnPackListener); // 增加解压监听
        ITask newTask =  builder.build();
        DownloadController.getInstance().registerTaskListener(newTask);
        return builder.build();
    }

    private static OnDownloadListener mDownloadListener = new OnDownloadListener() {
        @Override
        public void onDownloadWaiting(ITask task) {

            LogUtil.e("--------------> onDownloadWaiting");
//            downloadEventCallback(1, 0,null);
        }

        @Override
        public void onDownloadStarted(ITask task) {
            LogUtil.e("--------------> onDownloadStarted");
//            downloadEventCallback(2, 0,null);
        }

        @Override
        public void onDownloadConnected(ITask task, boolean resuming, long finishedSize, long totalSize) {
            LogUtil.e("--------------> onDownloadConnected finishedSize :" + finishedSize);
//            downloadEventCallback(3, 0,null);
        }

        @Override
        public void onDownloading(ITask task, long finishedSize, long totalSize) {
//            LogUtil.e("--------------> onDownloading finishedSize:"+finishedSize +", totalSize:" + totalSize +", fileName:" + task.getFileName() );
        }

        @Override
        public void onDownloadPause(ITask task, String errorCode) {
            LogUtil.e("--------------> onDownloadPause");
        }

        @Override
        public void onDownloadRetry(ITask task, int retries, String errorCode, Throwable throwable) {
//            downloadEventCallback(6, 0,errorCode);
        }

        @Override
        public void onDownloadFailure(ITask task, String errorCode, Throwable throwable) {
            LogUtil.e("--------------> onDownloadFailure error:" + errorCode);
        }

        @Override
        public void onDownloadSuccess(ITask task) {
            LogUtil.e("--------------> onDownloadSuccess");
        }
    };

    private static OnUnpackListener mUnPackListener = new OnUnpackListener() {
        @Override
        public void onUnpackStarted(ITask task, long totalSize) {
            LogUtil.e("--------------> onUnpackStarted" + task.getFileName());

        }

        @Override
        public void onUnpacking(ITask task, long finishedSize, long totalSize) {
            LogUtil.e("--------------> onUnpacking" + task.getFileName());

        }

        @Override
        public void onUnpackFailure(ITask task, String errorCode, Throwable throwable) {
            LogUtil.e("--------------> onUnpackFailure ");
        }

        @Override
        public void onUnpackSuccess(ITask task) {
            LogUtil.e("--------------> onUnpackSuccess" + task.getFileName());
        }
    };

    private static OnCheckListener mOnCheckLister = new OnCheckListener() {
        @Override
        public void onCheckStarted(ITask task, long totalSize) {
            LogUtil.e("--------------> onCheckStarted");
        }

        @Override
        public void onChecking(ITask task, long finishedSize, long totalSize) {

        }

        @Override
        public void onCheckFailure(ITask task, String errorCode, Throwable throwable) {
            LogUtil.e("--------------> onCheckFailure, errorCode:" + errorCode);
        }

        @Override
        public void onCheckSuccess(ITask task) {
            LogUtil.e("--------------> onCheckSuccess: taskName ->" + task.getFileName());
        }
    };
}
