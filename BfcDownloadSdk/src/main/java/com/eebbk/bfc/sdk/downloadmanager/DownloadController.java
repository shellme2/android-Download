package com.eebbk.bfc.sdk.downloadmanager;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.eebbk.bfc.flowmonitor.FlowMonitor;
import com.eebbk.bfc.sdk.behavior.utils.ListUtils;
import com.eebbk.bfc.sdk.download.BfcDownload;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.GlobalConfig;
import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.exception.DownloadNoInitException;
import com.eebbk.bfc.sdk.download.listener.IDownloadListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadConnectListener;
import com.eebbk.bfc.sdk.download.listener.OnDownloadOperationListener;
import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 下载控制器
 */
public class DownloadController implements IController {

    private static final Object INIT_LOCK = new Object();

    private static final class DownloadControllerHolder {
        private static final DownloadController INSTANCE = new DownloadController();
    }

    public static IController getInstance() {
        return DownloadControllerHolder.INSTANCE;
    }

    /**
     * 初始化下载器，调用默认配置和构造器
     *
     * @param appContext The application context.
     */
    public static void init(@NonNull final Context appContext) {
        init(appContext, null);
    }

    /**
     * 初始化下载器
     *
     * @param context The application context.
     * @param config  参数配置
     */
    public static void init(@NonNull final Context context, @Nullable GlobalConfig config) {
        synchronized (INIT_LOCK) {
            if (context == null) {
                throw new IllegalArgumentException(" context must not null! ");
            }

            if (config == null) {
                LogUtil.w(" config is null, use default config! ");
                config = new GlobalConfig.Builder().build();
            }

            // init log
            final String packageName = context.getPackageName();
            final int index = packageName.lastIndexOf(".");
            final String tagSuffix = index == -1 ? packageName : packageName.substring(index);
            LogUtil.setLog(LogUtil.buildLog(config.isDebug(), config.getSaveLogPath(), tagSuffix), config.isDebug());

            DownloadInitHelper.getInstance().init(context, config);

            LogUtil.i("init " + SDKVersion.getLibraryName() + ", version: " + SDKVersion.getVersionName() + ",  code: " + SDKVersion.getSDKInt() + ", build: " + SDKVersion.getBuildName());

            //初始化星域sdk
            CDNManager.initXYVodSDK(config.isDebug());

            //请求服务器CDN type
            CDNManager.requestForCDNType(context);

            FlowMonitor.init();
        }
    }

    @Override
    public void registerConnectionListener(OnDownloadConnectListener listener) {
        BfcDownload.getImpl().registerConnectionListener(listener);
    }

    @Override
    public void unregisterConnectionListener(OnDownloadConnectListener listener) {
        BfcDownload.getImpl().unregisterConnectionListener(listener);
    }

    //    @Override
    private synchronized void startService() {
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            throw new DownloadNoInitException();
        }
        if (BfcDownload.getImpl().isConnected()) {
            LogUtil.d(" service already started ");
            return;
        }
        BfcDownload.getImpl().startService(DownloadInitHelper.getInstance().getAppContext());
    }

//    @Override
    /*private synchronized void stopService() {
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            LogUtil.w(" stop service, but context is null ");
            return;
        }
        BfcDownload.getImpl().stopService(DownloadInitHelper.getInstance().getAppContext());
    }

    @Override
    public void stopServiceIfIdle() {
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            LogUtil.w(" stop service if idle, but context is null ");
            return;
        }
        BfcDownload.getImpl().stopServiceIfIdle(DownloadInitHelper.getInstance().getAppContext());
    }*/

//    @Override
    /*private void onDestroy() {
        stopService();
        DownloadInitHelper.getInstance().onDestroy();
    }*/

    /**
     * 根据url和默认配置创建任务, 如果url为空将会抛出IllegalArgumentException异常
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param url 下载地址
     * @return 任务构造器
     * @throws IllegalArgumentException
     */
    public static
    @NonNull
    ITask.Builder buildTask(@NonNull String url) {
        return new ITask.Builder(url);
    }

    /**
     * 根据url、文件名、保存路径和默认配置创建任务, 如果url为空将会抛出IllegalArgumentException异常
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param url      下载地址
     * @param fileName 保存文件名称
     * @param savePath 保存路径
     * @return 任务构造器
     */
    public static
    @NonNull
    ITask.Builder buildTask(@NonNull String url, @Nullable String fileName, @Nullable String savePath) {
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            throw new DownloadNoInitException();
        }
        return new ITask.Builder(url, fileName, savePath);
    }

    /**
     * <pre>创建下载任务, 如果url为空将会抛出IllegalArgumentException异常
     * fileSize > 0将会进行文件大小检测
     * md5校验码不为空，将会进行md5校验
     * </pre>
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param url           下载地址
     * @param fileName      保存文件名称
     * @param fileSize      预设文件大小
     * @param fileExtension 文件扩展名
     * @param md5Code       md5校验码
     * @return 任务构造器
     */
    private static
    @NonNull
    ITask.Builder buildTask(@NonNull String url, @Nullable String fileName, long fileSize, @Nullable String fileExtension, @Nullable String md5Code) {
        return new ITask.Builder(url, fileName, fileSize, fileExtension, md5Code);
    }

    /**
     * 根据任务配置来创建任务
     * 注意：任务不使用时必须尽早调用{@link ITask#recycle()}进行回收任务，避免监听造成内存泄露
     *
     * @param param 任务配置
     * @return 任务构造器
     */
    public static
    @NonNull
    ITask.Builder buildTask(@NonNull TaskParam param) {
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            throw new DownloadNoInitException();
        }
        String moduleName = param.getModuleName();
        if (TextUtils.isEmpty(moduleName)) {
            LogUtil.w(" module name is null, use default! ");
            moduleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        return new ITask.Builder(param).setModuleName(moduleName);
    }

    @Override
    public GlobalConfig getGlobalConfig() {
        synchronized (INIT_LOCK) {
            if (DownloadInitHelper.getInstance().getAppContext() == null) {
                throw new DownloadNoInitException();
            }
            return DownloadInitHelper.getInstance().getGlobalConfig();
        }
    }

    @Override
    public void addTask(ITask... pTasks) {
        if (pTasks == null || pTasks.length < 1) {
            LogUtil.w(" add task, but tasks is empty! ");
            return;
        }
        for (ITask task : pTasks) {
            if (!checkTask(task)) {
                continue;
            }
            BfcDownload.getImpl().startTask(task, DownloadInitHelper.getInstance().getAppContext());
        }
    }

    @Override
    public void deleteTask(int... ids) {
        if (ids == null || ids.length < 1) {
            LogUtil.w(" delete task, but ids is empty! ");
            return;
        }
        int[] filterIds = filterIds(ids);

        if (filterIds.length == 1) {
            LogUtil.i(" delete tasks, single task, use single delete function! ");
            BfcDownload.getImpl().deleteTask(filterIds[0], DownloadInitHelper.getInstance().getAppContext());
            return;
        }
        LogUtil.i(" delete tasks, multi task, use multi delete function! ");
        BfcDownload.getImpl().deleteTasks(filterIds, DownloadInitHelper.getInstance().getAppContext());
    }

    @Override
    public void deleteTask(ITask... pTasks) {
        deleteTask(getIds(pTasks));
    }

    @Override
    public void deleteTask(List<ITask> tasks) {
        deleteTask(getIds(tasks));
    }

    @Override
    public void deleteTaskAndAllFile(int... ids) {
        if (ids == null || ids.length < 1) {
            LogUtil.w(" delete task, but ids is empty! ");
            return;
        }

        int[] filterIds = filterIds(ids);

        if (filterIds.length == 1) {
            LogUtil.i(" delete tasks, deleteTaskAndAllFile, single task, use single delete function! ");
            BfcDownload.getImpl().deleteTaskAndAllFile(filterIds[0], DownloadInitHelper.getInstance().getAppContext());
            return;
        }
        LogUtil.i(" delete tasks, deleteTaskAndAllFile, multi task, use multi delete function! ");
        BfcDownload.getImpl().deleteTasksAndAllFile(filterIds, DownloadInitHelper.getInstance().getAppContext());
    }

    @Override
    public void deleteTaskAndAllFile(ITask... pTasks) {
        deleteTaskAndAllFile(getIds(pTasks));
    }

    @Override
    public void deleteTaskAndAllFile(List<ITask> tasks) {
        deleteTaskAndAllFile(getIds(tasks));
    }

    @Override
    public void deleteTaskWithoutFile(int... ids) {
        if (ids == null || ids.length < 1) {
            LogUtil.w(" delete task, but ids is empty! ");
            return;
        }

        int[] filterIds = filterIds(ids);

        if (filterIds.length == 1) {
            LogUtil.i(" delete tasks, deleteTaskWithoutFile, single task, use single delete function! ");
            BfcDownload.getImpl().deleteTaskWithoutFile(filterIds[0], DownloadInitHelper.getInstance().getAppContext());
            return;
        }
        LogUtil.i(" delete tasks, deleteTaskWithoutFile, multi task, use multi delete function! ");
        BfcDownload.getImpl().deleteTasksWithoutFile(filterIds, DownloadInitHelper.getInstance().getAppContext());
    }

    @Override
    public void deleteTaskWithoutFile(ITask... pTasks) {
        deleteTaskWithoutFile(getIds(pTasks));
    }

    @Override
    public void deleteTaskWithoutFile(List<ITask> tasks) {
        deleteTaskWithoutFile(getIds(tasks));
    }

    @Override
    public void reloadTask(ITask... pTasks) {
        if (pTasks == null || pTasks.length < 1) {
            LogUtil.w(" reload task, but ids is empty! ");
            return;
        }
        for (ITask task : pTasks) {
            if (task == null || task.getId() == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            BfcDownload.getImpl().restartTask(task, DownloadInitHelper.getInstance().getAppContext());
        }
    }

    @Override
    public void pauseTask(ITask... pTasks) {
        if (pTasks == null || pTasks.length < 1) {
            LogUtil.w(" pause task, but ids is empty! ");
            return;
        }
        for (ITask task : pTasks) {
            if (task == null || task.getId() == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            BfcDownload.getImpl().pauseTask(task, DownloadInitHelper.getInstance().getAppContext());
        }
    }


    @Override
    public void resumeTask(ITask... pTasks) {
        if (pTasks == null || pTasks.length < 1) {
            LogUtil.w(" pause task for connected to mobile, but ids is empty! ");
            return;
        }
        for (ITask task : pTasks) {
            if (task == null || task.getId() == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            BfcDownload.getImpl().resumeTask(task, DownloadInitHelper.getInstance().getAppContext());
        }
    }

    @Override
    public int refreshData(ITask... pTasks) {
        int result = 0;
        // 通过ID查询,还可以通过状态查询
        if (null == pTasks || 0 == pTasks.length) {
            return result;
        }
        for (ITask task : pTasks) {
            ITask srcTask = this.getTaskById(task.getId());
            if (null == srcTask) {
                continue;
            }
            if (cloneData(task, srcTask)) {
                result++;
            }
        }

        return result;
    }

    @Override
    public boolean cloneData(@NonNull final ITask targetTask, @NonNull final ITask srcTask) {
        if (null == srcTask || targetTask == null) {
            return false;
        }
        targetTask.setTaskParam(srcTask.getTaskParam()).setTaskState(srcTask.getTaskState());
        return true;
    }

    @Override
    public
    @Nullable
    ITask getTaskById(int id) {
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            throw new DownloadNoInitException();
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }

        DownloadInnerTask requestTask = BfcDownload.getImpl().getTaskById(DownloadInitHelper.getInstance().getAppContext(), id);
        if (requestTask == null) {
            return null;
        }
        TaskStateInfo stateInfo = requestTask.getTaskStateInfo();
        ITask task = buildTask(requestTask.createTaskParam()).build();
        task.setTaskState(stateInfo);
        return task;
    }

    @Override
    public
    @Nullable
    ITask getTask(@NonNull String url, @Nullable String savePath) {
        ITask task = buildTask(url, null, savePath).build();
        return getTaskById(task.getId());
    }

    @Override
    public
    @Nullable
    ArrayList<ITask> getTaskByStatus(int status) {
        return getTaskByStatus(status, null);
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTaskByStatus(int status, @Nullable String moduleName) {
        Query query = new Query();
        query.setFilterByStatus(status);
        query.setModuleName(moduleName);
        return getTask(query);
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTask() {
        Query query = new Query();
        return getTask(query);
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTask(@Nullable String moduleName) {
        if (TextUtils.isEmpty(moduleName)) {
            moduleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        Query query = new Query();
        query.setModuleName(moduleName);
        return getTask(query);
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTask(@NonNull Query query) {
        if (query == null) {
            throw new IllegalArgumentException(" Query must not null! ");
        }
        if (DownloadInitHelper.getInstance().getAppContext() == null) {
            throw new DownloadNoInitException();
        }

        ArrayList<DownloadInnerTask> requestTaskList = BfcDownload.getImpl()
            .getTask(DownloadInitHelper.getInstance().getAppContext(), query);
        return convertTask(requestTaskList);
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTaskByExtras(@NonNull Query query, @NonNull String[] keys, @NonNull String[] values) {
        if (query == null) {
            throw new IllegalArgumentException(" Query must not null! ");
        }
        query.addFilterByExtras(keys, values);
        return getTask(query);
    }

    private
    @NonNull
    ArrayList<ITask> convertTask(ArrayList<DownloadInnerTask> requestTaskList) {
        ArrayList<ITask> iTasks = new ArrayList<>();
        if (null == requestTaskList || requestTaskList.isEmpty()) {
            return iTasks;
        }
        ITask task;
        TaskStateInfo stateInfo;
        for (DownloadInnerTask requestTask : requestTaskList) {
            stateInfo = requestTask.getTaskStateInfo();
            task = buildTask(requestTask.createTaskParam()).build();
            ((DownloadTask) task).setTaskState(stateInfo);
            iTasks.add(task);
        }

        return iTasks;
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTaskByExtras(@Nullable String[] keys, @Nullable String[] values) {
        return getTaskByExtras(keys, values, null);
    }

    @Override
    public
    @NonNull
    ArrayList<ITask> getTaskByExtras(@Nullable String[] keys, @Nullable String[] values, @Nullable String moduleName) {
        Query query = new Query();
        query.setModuleName(moduleName);
        query.addFilterByExtras(keys, values);
        return getTask(query);
    }

    @Override
    public void setNetworkTypes(int networkTypes, ITask... tasks) {
        setNetworkTypes(networkTypes, getIds(tasks));
    }

    @Override
    public void setNetworkTypes(int networkTypes, int... ids) {
        if (ids == null || ids.length < 1) {
            LogUtil.w(" set network types, but ids is empty! ");
            return;
        }
        for (int id : ids) {
            if (id == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            BfcDownload.getImpl().setNetworkTypes(networkTypes, id, DownloadInitHelper.getInstance().getAppContext());
        }
    }

    @Override
    public int registerTaskListener(ITask... tasks) {
        int result = 0;
        if (tasks == null || tasks.length < 1) {
            LogUtil.w(" register task listener, but tasks is empty! ");
            return result;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        for (ITask task : tasks) {
            if (!checkTask(task)) {
                continue;
            }
            if (BfcDownload.getImpl().registerTaskListener(task)) {
                result++;
            }
        }
        return result;
    }

    @Override
    public int registerTaskListener(String tag, ITask... tasks) {
        if (!TextUtils.isEmpty(tag)) {
            int result = 0;
            if (tasks == null || tasks.length < 1) {
                LogUtil.w(" register task listener has tag, but tasks is empty! ");
                return result;
            }
            if (!BfcDownload.getImpl().isConnected()) {
                startService();
            }
            for (ITask task : tasks) {
                if (!checkTask(task)) {
                    continue;
                }
                if (BfcDownload.getImpl().registerTaskListener(tag, task)) {
                    result++;
                }
            }
            return result;
        } else {
            LogUtil.w(" tag is null, use default method to register task listener ");
            return registerTaskListener(tasks);
        }
    }

    @Override
    public int unregisterTaskListener(ITask... tasks) {
        int result = 0;
        if (tasks == null || tasks.length < 1) {
            LogUtil.w(" unregister task listener, but tasks is empty! ");
            return result;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        for (ITask task : tasks) {
            if (!checkTask(task)) {
                continue;
            }
            if (BfcDownload.getImpl().unregisterTaskListener(task)) {
                result++;
            }
        }
        return result;
    }

    @Override
    public boolean unregisterTaskListener(@NonNull String tag) {
        if (TextUtils.isEmpty(tag)) {
            throw new IllegalArgumentException("unregister task listener, but tag is empty! ");
        }
        return BfcDownload.getImpl().unregisterTaskListener(tag);
    }

    @Override
    public void unregisterTaskAllListener(ITask... tasks) {
        unregisterTaskAllListener(getIds(tasks));
    }

    @Override
    public void unregisterTaskAllListener(int... ids) {
        if (ids == null || ids.length < 1) {
            LogUtil.w(" unregister listener, but tasks is empty! ");
            return;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        for (int id : ids) {
            if (id == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            BfcDownload.getImpl().unregisterTaskAllListener(id);
        }
    }

    @Override
    public boolean registerTaskListener(IDownloadListener listener) {
        return registerTaskListener(listener, null);
    }

    @Override
    public boolean registerTaskListener(IDownloadListener listener, String moduleName) {
        if (listener == null) {
            return false;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        if (TextUtils.isEmpty(moduleName)) {
            moduleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        return BfcDownload.getImpl().registerTaskListener(listener, moduleName);
    }

    @Override
    public boolean unregisterTaskListener(IDownloadListener listener) {
        return unregisterTaskListener(listener, null);
    }

    @Override
    public boolean unregisterTaskListener(IDownloadListener listener, String moduleName) {
        if (listener == null) {
            return false;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        if (TextUtils.isEmpty(moduleName)) {
            moduleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        return BfcDownload.getImpl().unregisterTaskListener(listener, moduleName);
    }

    @Override
    public boolean registerOperationListener(OnDownloadOperationListener listener) {
        return registerOperationListener(listener, null);
    }

    @Override
    public boolean unregisterOperationListener(OnDownloadOperationListener listener) {
        return unregisterOperationListener(listener, null);
    }

    @Override
    public boolean registerOperationListener(OnDownloadOperationListener listener, String moduleName) {
        if (listener == null) {
            return false;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        if (TextUtils.isEmpty(moduleName)) {
            moduleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        return BfcDownload.getImpl().registerOperationListener(listener, moduleName);
    }

    @Override
    public boolean unregisterOperationListener(OnDownloadOperationListener listener, String moduleName) {
        if (listener == null) {
            return false;
        }
        if (!BfcDownload.getImpl().isConnected()) {
            startService();
        }
        if (TextUtils.isEmpty(moduleName)) {
            moduleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        return BfcDownload.getImpl().unregisterOperationListener(listener, moduleName);
    }

    private boolean checkTask(ITask task) {
        if (task == null) {
            LogUtil.w(" task is null! ");
            return false;
        }
        // 根据url和保存地址生成唯一的id
        int generateId = task.getId();
        if (generateId == ITask.INVALID_GENERATE_ID) {
            LogUtil.e("task: url[" + task.getUrl() + "] savePath" + task.getSavePath() + " has invalid id(-1)! ");
            return false;
        }
        return true;
    }

    private int[] getIds(ITask... tasks) {
        if (tasks != null && tasks.length > 0) {
            int[] ids = new int[tasks.length];
            int i = 0;
            for (ITask task : tasks) {
                if (!checkTask(task)) {
                    ids[i++] = ITask.INVALID_GENERATE_ID;
                } else {
                    ids[i++] = task.getId();
                }
            }
            return ids;
        }
        return null;
    }

    private int[] getIds(List<ITask> tasks) {
        if (!ListUtils.isEmpty(tasks)) {
            int[] ids = new int[tasks.size()];
            int i = 0;
            for (ITask task : tasks) {
                if (!checkTask(task)) {
                    ids[i++] = ITask.INVALID_GENERATE_ID;
                } else {
                    ids[i++] = task.getId();
                }
            }
            return ids;
        }
        return null;
    }

    private int[] filterIds(int[] srcIds) {
        int usefulIdCnt = 0;
        for (int id : srcIds) {
            if (id == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            usefulIdCnt++;
        }

        int[] filterIds = new int[usefulIdCnt];

        int index = 0;
        for (int id : srcIds) {
            if (id == ITask.INVALID_GENERATE_ID) {
                continue;
            }
            filterIds[index] = id;
            index++;
        }

        return filterIds;
    }
}
