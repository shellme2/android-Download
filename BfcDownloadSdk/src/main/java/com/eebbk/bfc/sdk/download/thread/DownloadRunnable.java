package com.eebbk.bfc.sdk.download.thread;

import android.Manifest;
import android.text.TextUtils;

import com.eebbk.bfc.flowmonitor.FlowMonitor;
import com.eebbk.bfc.flowmonitor.IFlowBean;
import com.eebbk.bfc.sdk.download.C;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.DownloadManager;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.check.ICheckCallback;
import com.eebbk.bfc.sdk.download.check.IValidator;
import com.eebbk.bfc.sdk.download.da.DownloadDACollect;
import com.eebbk.bfc.sdk.download.db.DownloadDbManager;
import com.eebbk.bfc.sdk.download.db.data.TaskParamInfo;
import com.eebbk.bfc.sdk.download.exception.DownloadAuthenticationFileException;
import com.eebbk.bfc.sdk.download.exception.DownloadBaseException;
import com.eebbk.bfc.sdk.download.exception.DownloadCheckException;
import com.eebbk.bfc.sdk.download.exception.DownloadNetworkException;
import com.eebbk.bfc.sdk.download.exception.DownloadOutOfSpaceException;
import com.eebbk.bfc.sdk.download.exception.DownloadRedirectException;
import com.eebbk.bfc.sdk.download.exception.DownloadRetryException;
import com.eebbk.bfc.sdk.download.exception.DownloadStopException;
import com.eebbk.bfc.sdk.download.exception.DownloadUnpackException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.io.DownloadRandomAccess;
import com.eebbk.bfc.sdk.download.io.IDownloadOutputStream;
import com.eebbk.bfc.sdk.download.message.MessageFactory;
import com.eebbk.bfc.sdk.download.unpack.IDownloadUnpackCallback;
import com.eebbk.bfc.sdk.download.unpack.IDownloadUnpacker;
import com.eebbk.bfc.sdk.download.util.CloseableUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkUtil;
import com.squareup.okhttp.CacheControl;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.NoRouteToHostException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

/**
 * Desc: 下载任务执行
 * Author: llp
 * Create Time: 2016-10-08 19:09
 * Email: jacklulu29@gmail.com
 */

public class DownloadRunnable extends DownloadBaseRunnable {

    private static final int TOTAL_VALUE_IN_CHUNKED_RESOURCE = -1;
    /**
     * None of the ranges in the request's Range header field overlap the current extent of the
     * selected resource or that the set of ranges requested has been rejected due to invalid
     * ranges or an excessive request of small or overlapping ranges.
     */
    private static final int HTTP_REQUESTED_RANGE_NOT_SATISFIABLE = 416;

    private static final int HTTP_TEMP_REDIRECT = 307;
    private static final int HTTP_PERM_REDIRECT = 308;
    private static final int HTTP_MULT_CHOICE = 300;
    private static final int HTTP_MOVED_PERM = 301;
    private static final int HTTP_MOVED_TEMP = 302;
    private static final int HTTP_SEE_OTHER = 303;

    private int mRetryTimes = 0;
    private int mSpecialRetries = 0;

    private DownloadInnerTask mTask;
    private boolean mHasError = false;
    private boolean mRevisedInterval = false;
    private boolean isResumeDownloadAvailable;

    private DownloadManager mManager;
    private OkHttpClient mOkHttpClient;
    private DownloadDbManager mDbManager;
    private IDownloadUnpacker mUnpacker;
    private IValidator mValidator;
    private InputStream mInputStream = null;
    private IDownloadOutputStream mOutputStream = null;

    public DownloadRunnable(DownloadManager manager, DownloadInnerTask task, OkHttpClient okHttpClient, DownloadDbManager dbManager, IDownloadUnpacker unpacker, IValidator validator) {
        super(manager, task.getRunnableId(), task.getGenerateId());
        mManager = manager;
        mTask = task;
        mOkHttpClient = okHttpClient;
        mDbManager = dbManager;
        mUnpacker = unpacker;
        mValidator = validator;
        mHasError = false;
        isResumeDownloadAvailable = false;
    }

    @Override
    public void onDownload() {
        if (mTask != null && mTask.getTaskParamInfo() != null) {
            FlowMonitor.getOperate().addInfo(new IFlowBean.Builder().setInfo(mTask.getTaskParamInfo().toString()).setUniqueTag(getRunnableId()).build());
        }

        DownloadDACollect.downloadStart(mTask);
        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始任务", "准备下载").setUniqueTag(getRunnableId()).setExtras(mTask).build());
        onStarted();

        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("准备流程结束", "开始下载").setUniqueTag(getRunnableId()).setExtras(mTask).build());
        download();

        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("下载流程结束", "校验文件").setUniqueTag(getRunnableId()).setExtras(mTask).build());
        checkFile();

        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验流程结束", "解压文件").setUniqueTag(getRunnableId()).setExtras(mTask).build());
        unpackFile();

    }

    private void download() {
        try {
            tryDownload();
        } catch (DownloadRetryException e) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "下载异常", "尝试重试", "进入重试流程").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            if (processRetryException(e)) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("进入重试流程", "开始下载").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                download();
            } else {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("进入重试流程", "重试结束", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            }
        } catch (DownloadStopException e) {
            if (e instanceof DownloadNetworkException) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "下载异常", "网络问题-暂停任务", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                onNetworkError(e.getErrorCode(), e);
            } else if (e instanceof DownloadOutOfSpaceException) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "下载异常", "空间不足-暂停任务", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                onOutOfSpaceError(e.getErrorCode(), e);
            } else {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "下载异常", "结束任务", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                onDownloadError(e);
            }
        } catch (ConnectException e) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "下载异常", "连接异常", "进入重试流程").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            String errorMsg = DownloadUtils.formatString(" runnable[%s] download connect exception, retries[%s]", getRunnableId(), mRetryTimes);
            DownloadBaseException exception = new DownloadBaseException(ErrorCode.Values.DOWNLOAD_CONNECT_EXCEPTION, errorMsg, e);
            if (processRetryException(exception)) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("进入重试流程", "开始下载").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                download();
            } else {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("进入重试流程", "重试结束", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            }
        } catch (Throwable throwable) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "下载异常", "未知异常", "进入重试流程").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            LogUtil.e(throwable, " unknown error! ");
            DownloadBaseException exception = new DownloadStopException(ErrorCode.Values.DOWNLOAD_UNKNOWN, " download runnable[" + getRunnableId() + "] unknown exception ", throwable);
            if (processRetryException(exception)) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("进入重试流程", "开始下载").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                download();
            } else {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("进入重试流程", "重试结束", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            }
        }
    }


    private void tryDownload() throws DownloadRetryException, DownloadStopException, DownloadRedirectException, IOException {
        if (checkCancel()) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "任务已取消", "下载流程正常退出", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }

        // 已经是校验或者解压阶段，直接重新走校验、解压流程
        if (mTask.getTaskPhase() == DownloadInnerTask.TASK_PHASE_CHECK || mTask.getTaskPhase() == DownloadInnerTask.TASK_PHASE_UNPACK) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "任务已结束，校验解压中", "下载流程正常退出", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }

        // 检查网络权限清单
        if (!checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
            throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_NO_NETWORK_PERMISSION,
                DownloadUtils.formatString("can't start the download runnable %s, because this task require network, but user application " +
                    "nor current process has %s, so we can't check whether " + "the network type connection.", getRunnableId(), Manifest.permission.ACCESS_NETWORK_STATE));
        }

        // 检查网络
        int flag = NetworkUtil.checkNetwork(mTask.getTotalSize(), mTask.getNetworkTypes(), isRoamingAllowed(), DownloadInitHelper.getInstance().getGlobalConfig().isAllowMobile2g());
        if (flag != NetworkUtil.NETWORK_OK) {
            throwNetworkException(flag);
        }

        // 下载
        downloadFile();
    }

    private void downloadFile() throws DownloadStopException, IOException, DownloadRedirectException, DownloadRetryException {
        if (checkCancel()) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "任务已取消", "下载流程正常退出", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }
        DownloadInnerTask download = mTask;

        Response response = null;
        long soFar = 0;
        final String id = getRunnableId();
        try {
            // Step 1, check state
            if (!checkState()) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "任务状态检查失败", "下载流程正常退出", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                return;
            }

            LogUtil.d(DownloadUtils.formatString("start download %s %s", id, download.getUrl()));

            // Step 2, handle resume from breakpoint
            checkIsResumeAvailable();
            try {
                response = connectDownloadUrl();
            } catch (SocketTimeoutException e) {
                LogUtil.w(e, "socket time out when connect download url");
                throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_CONNECT_SOCKET_TIME_OUT, DownloadUtils.formatString("socket time out when connect download url " + e.getMessage()), e);
            }

            if (checkCancel()) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("连接下载地址", "任务已取消", "下载流程正常退出", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                return;
            }

            final boolean isSucceedStart = response.code() == HttpURLConnection.HTTP_OK;
            final boolean isSucceedResume = response.code() == HttpURLConnection.HTTP_PARTIAL && isResumeDownloadAvailable;

            if (isResumeDownloadAvailable && !isSucceedResume) {
                String log = DownloadUtils.formatString("tried to resume from the break point[%d], but the " + "response code is %d, not 206(PARTIAL).", download.getFinishSize(), response.code());
                LogUtil.w(log);
            }

            if (isSucceedStart || isSucceedResume) {
                long total = download.getTotalSize();
                final String transferEncoding = response.header("Transfer-Encoding");
                // Step 5, check response's header
                if (isSucceedStart || total <= 0) {
                    if (transferEncoding == null) {
                        total = response.body().contentLength();
                    } else {
                        // if transfer not nil, ignore content-length
                        total = TOTAL_VALUE_IN_CHUNKED_RESOURCE;
                    }
                }

                // TODO consider if not is chunked & http 1.0/(>=http1.1 & connect not be keep live) may not give content-length
                if (total < 0) {
                    // invalid total length
                    final boolean isEncodingChunked = transferEncoding != null && transferEncoding.equals("chunked");
                    if (!isEncodingChunked) {
                        // not chunked transfer encoding data
                        if (DownloadInitHelper.getInstance().getGlobalConfig().getDownloadMode() == C.DownloadMode.LOW) {
                            // do not response content-length either not chunk transfer encoding,
                            // but HTTP lenient is true, so handle as the case of transfer encoding chunk
                            total = TOTAL_VALUE_IN_CHUNKED_RESOURCE;
                            LogUtil.d(DownloadUtils.formatString("%d response header is not legal but " + "HTTP lenient is true, so handle as the case of " + "transfer encoding chunk", id));
                        } else {
                            throw new DownloadStopException("can't know the size of the " + "download file, and its Transfer-Encoding is not Chunked " + "either.\nyou can ignore such exception by call GlobalConfig to " + "setDownloadMode C.DownloadMode.LOW");
                        }
                    }
                }

                if (isSucceedResume) {
                    soFar = download.getFinishSize();
                }

                onConnected(isSucceedResume, total, findEtag(response), findFilename(response));

                // Step 7, start fetch datum from input stream & write to file
                fetch(response, isSucceedResume, soFar, total);
            } else {
                processAbnormalStatus(download, response);
            }
        } finally {
            final Response closeResponse = response;
            if ((closeResponse != null && closeResponse.body() != null) || mInputStream != null || mOutputStream != null) {
                // 关闭流交给异步处理(关闭流会消耗一定时间大概1~300ms)，实现快速响应取消或者停止操作
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        CloseableUtil.close(mInputStream);

                        try {
                            if (mOutputStream != null) {
                                //noinspection ThrowFromFinallyBlock
                                mOutputStream.sync();
                            }
                        } catch (IOException e) {
                            LogUtil.w(e, " sync output stream error ");
                        } finally {
                            CloseableUtil.close(mOutputStream);
                        }

                        if (closeResponse != null && closeResponse.body() != null) {
                            try {
                                closeResponse.body().close();
                            } catch (IOException e) {
                                LogUtil.w("close response error ");
                            }
                        }
                    }
                }).start();
            }
        }

    }

    private boolean processRetryException(DownloadBaseException e) {
        mRetryTimes++;
        // 如果发生的是读写数据异常，而且已完成大小>0,即正常下载了部分文件，只是由于网络不稳定导致
        // 重试并减去这次重试次数，避免文件下载失败
        if (mTask.getFinishSize() > 0 && mTask.getTotalSize() != -1 && e != null) {
            if (ErrorCode.Values.DOWNLOAD_SOCKET_TIME_OUT.equals(e.getErrorCode()) || (ErrorCode.Values.DOWNLOAD_READ_OR_WRITE_IO_EXCEPTION.equals(e.getErrorCode()))) {
                mSpecialRetries++;
                LogUtil.w("socket timeout or io exception but finish size > 0, retry download[" + mRetryTimes + "] special retries[" + mSpecialRetries + "]! ");
            }
        }
        if (mSpecialRetries < C.DownLoadConfig.DEFAULT_MAX_SPECIAL_RETRIES && mRetryTimes <= C.DownLoadConfig.DEFAULT_MAX_RETRIES) {
//        if (mRetryTimes <= C.DownLoadConfig.DEFAULT_MAX_RETRIES) {
            // retry
            try {
                onRetry(e, mRetryTimes);
            } catch (Throwable throwable) {
                LogUtil.e(throwable, "onRetry error! ");
                DownloadBaseException exception = new DownloadStopException(ErrorCode.Values.DOWNLOAD_UNKNOWN, " download runnable[" + getRunnableId() + "] onRetry exception ", throwable);
                processRetryException(exception);
                return false;
            }
            return true;
        }

        // 超过重试次数，下载失败
        LogUtil.w(DownloadUtils.formatString(" runnable[%s] download (normal retries)[%s] MAX[%s] - (special retries)[%s] MAX_SPECIAL[%s], stop download ", getRunnableId(), mRetryTimes,
            C.DownLoadConfig.DEFAULT_MAX_RETRIES, mSpecialRetries,  C.DownLoadConfig.DEFAULT_MAX_SPECIAL_RETRIES ));
//        LogUtil.w(DownloadUtils.formatString(" runnable[%s] download (normal retries)[%s] MAX[%s], stop download ", getRunnableId(), mRetryTimes,
//            C.DownLoadConfig.DEFAULT_MAX_RETRIES));
        // error
        onDownloadError(e);
        return false;
    }

    private boolean checkState() throws DownloadNetworkException {
        if (checkCancel()) {
            return false;
        }
        // 检测网络是否允许
        int flag = NetworkUtil.checkNetwork(mTask.getTotalSize(), mTask.getNetworkTypes(), isRoamingAllowed(), DownloadInitHelper.getInstance().getGlobalConfig().isAllowMobile2g());
        if (flag != NetworkUtil.NETWORK_OK) {
            throwNetworkException(flag);
            return false;
        }
        return true;
    }

    private Response connectDownloadUrl() throws IOException, DownloadNetworkException {
        Request.Builder requestBuilder = new Request.Builder()
            .url(mTask.getUrl())
            .tag(mTask.getGenerateId())
            .cacheControl(CacheControl.FORCE_NETWORK); // 目前没有指定cache，下载任务非普通REST请求，用户已经有了存储的地方

        addHeader(requestBuilder);

        // start download----------------
        // Step 3, init request
        final Request request = requestBuilder.get().build();
        LogUtil.d(DownloadUtils.formatString("%s request header %s", mTask.getGenerateId(), request.headers()));

        Response response;
        try {
            Call call = mOkHttpClient.newCall(request);
            // Step 4, build connect
            response = call.execute();
        } catch (UnknownHostException e) {  // 此异常本不应该捕获的, 公司网络由于这个异常, 大量失败, 加入重试
            LogUtil.w(e, "don't find address; retry");
            throw new DownloadNetworkException(ErrorCode.Values.DOWNLOAD_NETWORK_REQUEST_NO_ADDRESS, e);
        } catch (NoRouteToHostException e) {     // 此异常本不应该捕获的, 公司网络由于这个异常, 大量失败, 加入重试
            LogUtil.w(e, "No route to host; retry");
            throw new DownloadNetworkException(ErrorCode.Values.DOWNLOAD_NETWORK_REQUEST_NO_ROUTE_TO_HOST, e);
        }

        return response;
    }

    private boolean fetch(Response response, boolean isSucceedContinue, long finished, long total) throws DownloadStopException, DownloadRetryException {
        if (mTask.getScheduleIntervalTimeByMs() >= 0) {
            long startTime = android.os.SystemClock.elapsedRealtime();
            // 设置进度计算初始时间
            mTask.setProgressStatisticsLatestTime(startTime);
            if (mTask.isShowRealTimeInfo()) {
                // 设置速度计算初始时间
                mTask.setSpeedStatisticsLatestTime(startTime);
                mTask.setSpeedStatisticsLatestSize(finished);
            }
        }

        // fetching datum
        InputStream inputStream;
        final IDownloadOutputStream outputStream = getOutputStream(isSucceedContinue, total);
        mOutputStream = outputStream;
        try {
            // Step 1, get input stream
            inputStream = response.body().byteStream();
            mInputStream = inputStream;
            byte[] buff = new byte[C.DownLoadConfig.BUFFER_SIZE];
            int byteCount;
            long speed;
            boolean showProgress;
            long nowTime;
            long intervalTime;
            boolean isFirst = true;

            int minCalculateSpeedTimeLength = Math.max(mTask.getScheduleIntervalTimeByMs(), C.DownLoadConfig.SPEED_STATISTICS_INTERVAL_TIME);

            while (true) {
                // Step 2, read from input stream.
                byteCount = inputStream.read(buff);
                if (byteCount == -1) {
                    break;
                }
                // if finished size > total size,download failed
                if (total != TOTAL_VALUE_IN_CHUNKED_RESOURCE && finished + byteCount > total) {
                    throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_GREATER_THAN_TOTAL_SIZE, DownloadUtils.formatString("finish[%d] is greater than total[%d]", finished, total));
                }
                if (checkCancel()) {
                    FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "拉取数据", "任务已取消", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                    return false;
                }
                // Step 3, writ to file
                outputStream.write(buff, 0, byteCount);
//                outputStream.sync();

                // step 4, check is canceled
                if (checkCancel()) {
                    FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "拉取数据", "任务已取消", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                    return false;
                }
                // Step 5, adapter sofar
                finished += byteCount;
                mRetryTimes = 0;
                mSpecialRetries =0;

                // Step 6, callback on progressing
                // 如果间隔时间< 0,则表示不回调进度方法
                showProgress = false;
                speed = mTask.getSpeed();
                if (mTask.getScheduleIntervalTimeByMs() >= 0) {
                    nowTime = android.os.SystemClock.elapsedRealtime();

                    // 计算速度
                    intervalTime = nowTime - mTask.getSpeedStatisticsLatestTime();
                    if (mTask.isShowRealTimeInfo() && (isFirst || intervalTime >= minCalculateSpeedTimeLength)) {
                        if (0 != intervalTime) {
                            speed = ((finished - mTask.getSpeedStatisticsLatestSize()) * C.DownLoadConfig.ONE_THOUSAND_MILLISECONDS) / intervalTime;
                        }

                        if (speed < 0) {
                            speed = 0;
                        }
                        mTask.setSpeedStatisticsLatestTime(nowTime);
                        mTask.setSpeedStatisticsLatestSize(finished);
                    }

                    // 是否通知进度更新
                    intervalTime = nowTime - mTask.getProgressStatisticsLatestTime();
                    if (intervalTime >= mTask.getScheduleIntervalTimeByMs()) {
                        showProgress = true;
                        mTask.setProgressStatisticsLatestTime(nowTime);
                    }
                }
                // 总大小有效，第一次或者下载已完成大小等于总大小的情况(即已下载完成)，必须回调一次
                // 保证下载第一次和最后一次进度会回调
                if (isFirst || (total != TOTAL_VALUE_IN_CHUNKED_RESOURCE && finished == total)) {
                    showProgress = true;
                }
                if (isFirst) {
                    isFirst = false;
                }

                onProgress(finished, total, speed, showProgress, true);

                // Step 7, check state
                // if finished size equals total size, don't check state
                if (total != TOTAL_VALUE_IN_CHUNKED_RESOURCE && finished == total) {
                    LogUtil.d(DownloadUtils.formatString(" runnable[%s] is download end(finished=total[%s]), don't check state ", getRunnableId(), finished));
                }

            }   // end while

            if (total != finished && total != TOTAL_VALUE_IN_CHUNKED_RESOURCE) {
                LogUtil.e(" download complete but finish not equal total size ");
                throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_NOT_EQUAL_TOTAL, DownloadUtils.formatString("finish[%d] not equal total[%d]", finished, total));
            }

            // Step 9, Compare between the downloaded so far bytes with the total bytes.
            // 认证网络下, 下载的是网页; 针对小文件下载下来后, 内容包含html的作为失败处理
            if (isAuthenticationNetworkFile(mTask.getTempFilePath())) {
                throw new DownloadAuthenticationFileException(ErrorCode.Values.DOWNLOAD_AUTHENTICATION_FILE, "download file is authentivation net web file");
            }

            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("开始下载", "拉取数据", "拉取结束", "下载成功").setUniqueTag(getRunnableId()).setExtras(mTask).build());

            // Step 10, rename the temp file to the completed file.
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("下载成功", "重命名").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            renameTempFile();

            // Step 11 callback completed
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("重命名", "执行下载成功回调", "下载流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            onDownloadComplete(total);

            return true;

        } catch (SocketTimeoutException e) {
            throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_SOCKET_TIME_OUT, DownloadUtils.formatString("SocketTimeoutException: " + e.getMessage() + ", finish[%d] total[%d]", finished, total), e);
        } catch (IOException e) {
            // Read the file over standard -1 error, but still think that the download is complete
            if (total != TOTAL_VALUE_IN_CHUNKED_RESOURCE && finished == total) {
                LogUtil.w(e, DownloadUtils.formatString(" Read file occur an io exception , but the download is already complete.  runnable[%s] finished=total[%s] ", getRunnableId(), finished));
                renameTempFile();
                onDownloadComplete(total);
                return true;
            } else {
                String errorMsg = e.getMessage();
                if (!TextUtils.isEmpty(errorMsg)) {
                    if (errorMsg.contains("write failed: ENOSPC (No space left on device)")) {
                        throw new DownloadOutOfSpaceException(ErrorCode.Values.DOWNLOAD_OUT_OF_SPACE, DownloadUtils.formatString(" write failed: ENOSPC (No space left on device), finish[%d] total[%d]", finished, total), e);
                    }
                }
                throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_READ_OR_WRITE_IO_EXCEPTION, DownloadUtils.formatString("IOException: " + e.getMessage() + ", finish[%d] total[%d]", finished, total), e);
            }
        }
    }

    private boolean checkCancel() {
        if (isCanceled()) {
            LogUtil.i(DownloadUtils.formatString(" runnable[%s] already canceled state[%s]", getRunnableId(), mTask.getState()));
            return true;
        }
        if (mTask.isTaskIsDeleted()) {
            LogUtil.i(DownloadUtils.formatString(" runnable[%s] already deleted state[%s]", getRunnableId(), mTask.getState()));
            return true;
        }
        return false;
    }

    private IDownloadOutputStream getOutputStream(final boolean append, final long totalBytes)
        throws DownloadStopException, DownloadRetryException {
        // check fileName
        String fileName = mTask.getFileName();
        if (fileName.length() > 255) {
            throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_FILE_NAME_LENGTH_MORE_THAN_255, DownloadUtils.formatString(" invalid file name[%s], length > 255", fileName));
        }
        if (fileName.contains(File.separator)) {
            throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_FILE_NAME_ILLEGAL, DownloadUtils.formatString(" file name[%s] cannot contain illegal characters: /", fileName));
        }

        final String saveDir = mTask.getSavePath();
        File dir = new File(saveDir);

        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_SAVE_DIR_IS_FILE, DownloadUtils.formatString("found invalid internal destination path[%s]," + " & path is file", saveDir));
            }
        } else {
            if (!dir.mkdirs()) {
                throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_MKDIRS_FAILED, DownloadUtils.formatString(" mkdirs [%s] failed ", saveDir));
            }
        }

        final String tempPath = mTask.getTempFilePath();
        if (TextUtils.isEmpty(tempPath)) {
            throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_TEMP_FILE_PATH_IS_NULL, "found invalid internal destination path, empty");
        }
        if (LogUtil.isDebug()) {
            LogUtil.d(" temp file path: " + tempPath);
        }
        File file = new File(tempPath);

        if (file.exists() && file.isDirectory()) {
            throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_TEMP_FILE_IS_DIR, DownloadUtils.formatString("found invalid internal destination path[%s]," + " & path is directory[%B]", tempPath, file.isDirectory()));
        }

        if (file.exists() && !append) {
            FileUtil.deleteFile(file);
        }

        if (!file.exists()) {
            try {
                if (!FileUtil.createNewFile(file)) {
                    throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_CREATE_TEMP_FILE_ERROR, DownloadUtils.formatString("create new file error  %s", file.getAbsolutePath()));
                }
            } catch (IOException e) {
                throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_CREATE_TEMP_FILE_IO_EXCEPTION, "create new file[" + tempPath + "] io exception ", e);
            }

        }

        IDownloadOutputStream outputStream;
        try {
            outputStream = new DownloadRandomAccess(file);
        } catch (FileNotFoundException e) {
            throw new DownloadRetryException(ErrorCode.Values.DOWNLOAD_CREATE_TEMP_FILE_NO_FOUND, "create file not found exception ", e);
        }

        // check the available space bytes whether enough or not.
        if (totalBytes > 0) {
            final long breakpointBytes = file.length();
            final long requiredSpaceBytes = totalBytes - breakpointBytes;

            final long freeSpaceBytes = DownloadUtils.getFreeSpaceBytes(tempPath);

            if (freeSpaceBytes < requiredSpaceBytes) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    LogUtil.e(e, " close output stream error ");
                }
                // throw a out of space exception.
                throw new DownloadOutOfSpaceException(ErrorCode.Values.DOWNLOAD_OUT_OF_SPACE, DownloadUtils.formatString("The file is too large to store, breakpoint in bytes: " +
                    " %d, required space in bytes: %d, but free space in bytes: " + "%d", breakpointBytes, requiredSpaceBytes, freeSpaceBytes)
                );
            } else if (DownloadInitHelper.getInstance().getGlobalConfig().isFilePreAllocation()) {
                // pre allocate.
                try {
                    outputStream.setLength(totalBytes);
                } catch (IllegalAccessException e) {
                    throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_SET_LENGTH_ILLEGAL_ACCESS_EXCEPTION, " set file length IllegalAccessException ", e);
                } catch (IOException e) {
                    throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_SET_LENGTH_IO_EXCEPTION, " set file length IOException ", e);
                }
            }
        }

        if (append) {
            try {
                outputStream.seek(mTask.getFinishSize());
            } catch (IllegalAccessException e) {
                throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_SEEK_FILE_ILLEGAL_ACCESS_EXCEPTION, " seek file IllegalAccessException ", e);
            } catch (IOException e) {
                throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_SEEK_FILE_IO_EXCEPTION, " seek file IOException ", e);
            }
        }

        return outputStream;
    }

    private void processAbnormalStatus(final DownloadInnerTask download, final Response response) throws DownloadStopException, DownloadRetryException, DownloadRedirectException {
        int responseCode = response.code();
        String msg = DownloadUtils.formatString("%s response code %d, range[%d] isn't make sense, so delete the dirty file[%s], and try to redownload it from byte-0.",
            getRunnableId(), responseCode, download.getFinishSize(), download.getTempFilePath());
        LogUtil.i(msg);
        if (mRevisedInterval) {
            throw new DownloadRetryException(ErrorCode.getHTTPResponseErrorCode(responseCode), msg);
        }
        mRevisedInterval = true;

        switch (responseCode) {
            case HTTP_REQUESTED_RANGE_NOT_SATISFIABLE:
                download.deleteAllFiles();
                LogUtil.w(msg);
                throw new DownloadRetryException(ErrorCode.getHTTPResponseErrorCode(responseCode), msg);
            case 503://HttpStatus.SC_SERVICE_UNAVAILABLE
            case 408://HttpStatus.SC_REQUEST_TIMEOUT
            case 504://HttpStatus.SC_GATEWAY_TIMEOUT
                LogUtil.w("");
                throw new DownloadRetryException(ErrorCode.getHTTPResponseErrorCode(responseCode), msg);
            case HTTP_PERM_REDIRECT:// 308
            case HTTP_TEMP_REDIRECT: //307
                // "If the 307 or 308 status code is received in response to a request other than GET
                // or HEAD, the user agent MUST NOT automatically redirect the request"
                //如果不是get和head 那么就不能自动转发
                // fall-through
            case HTTP_MULT_CHOICE: //300
            case HTTP_MOVED_PERM:// 301
            case HTTP_MOVED_TEMP://302
            case HTTP_SEE_OTHER: //303
                // 重定向Okhttp自己会处理
//                handleRedirect(responseCode, response, request, client);
                break;
            default:
                throw new DownloadRetryException(ErrorCode.getHTTPResponseErrorCode(responseCode), msg);
        }
    }


    private boolean isAuthenticationNetworkFile(String filePath) {
        File needCheckFile = new File(filePath);
        // 认证网络的文件一般是个网页, 比较小, 当是小文件时, 才进行校验
        if (needCheckFile.length() > 10 * 1024) {
            return false;
        }

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(filePath);
            byte[] buffer = new byte[1024];
            fis.read(buffer);
            String content = new String(buffer);

            if (content.contains("html")) {
                return true;
            }
        } catch (FileNotFoundException e) {
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseableUtil.close(fis);
        }

        return false;
    }

    private void checkFile() {
        if (isCanceled() || mHasError) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "非待校验状态文件", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }

        if (mTask.getTaskPhase() == DownloadInnerTask.TASK_PHASE_DOWNLOAD || mTask.getTaskPhase() == DownloadInnerTask.TASK_PHASE_CHECK) {
            processCheckFile();
        }
    }

    private void processCheckFile() {
        DownloadInnerTask download = mTask;
        TaskParamInfo paramInfo = download.getTaskParamInfo();
        boolean shouldCheck = paramInfo.needCheckFile();
        LogUtil.d("runnable[" + getRunnableId() + "] must check download file " + shouldCheck);
        if (!shouldCheck) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "不需校验", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }

        // 设置进入校验阶段
        download.setTaskPhase(DownloadInnerTask.TASK_PHASE_CHECK);

//            IValidator validator = mManager.getFileValidator(download.getCheckType());
        IValidator validator = mValidator;
        if (validator == null) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "检验失败", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            onCheckError(new DownloadCheckException(ErrorCode.Values.CHECK_VALIDATOR_NO_FOUND, "validator is null which check type = " + download.getCheckType()));
            return;
        }
        try {
            // 验证校验码是否有效
            if (!validator.isKeyValid(download.getCheckCode())) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "检验失败", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                onCheckError(new DownloadCheckException(ErrorCode.Values.CHECK_KEY_IS_INVALID, "validator is null which check type = " + download.getCheckType()));
                return;
            }
            LogUtil.i("-------> type is" + download.getCheckType() + ", code is " + download.getCheckCode());
            validator.check(download.getTargetFilePath(), download.getCheckType(), download.getCheckCode(), new ICheckCallback() {
                @Override
                public void onStart() {
                    onCheckStart();
                }

                @Override
                public void onProgress(long totalSize, long finishedSize) {
                    onCheckProgress(totalSize, finishedSize);
                }

                @Override
                public void onSuccess() {
                    FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "检验成功", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                    onCheckSuccess();
                }

                @Override
                public void onError(DownloadCheckException e) {
                    FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "检验失败", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                    onCheckError(e);
                }
            });
        } catch (Throwable throwable) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("校验文件", "检验失败", "校验流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            onCheckError(new DownloadCheckException(ErrorCode.Values.CHECK_RUN_ERROR, "Check file  not catch exceptions: ", throwable));
        }
    }


    private void unpackFile() {
        if (isCanceled() || mHasError) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "非待解压状态文件", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }
        processUnpack();
    }

    private void processUnpack() {
        DownloadInnerTask download = mTask;
        LogUtil.d("runnable[" + getRunnableId() + "] must unpack download file " + download.isAutoUnpack());
        if (!download.isAutoUnpack()) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "不需解压", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }

        // 设置进入解压阶段
        download.setTaskPhase(DownloadInnerTask.TASK_PHASE_UNPACK);

        String sourceFilePath = download.getTargetFilePath();
        if (TextUtils.isEmpty(sourceFilePath)) {
            onUnpackError(new DownloadUnpackException(ErrorCode.Values.UNPACK_PATH_IS_EMPTY, "unpack the file, but path is empty savePath[" + sourceFilePath + "]"));
            return;
        }
        IDownloadUnpacker unpacker = mUnpacker;
        if (unpacker == null) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "解压失败", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            onUnpackError(new DownloadUnpackException(ErrorCode.Values.UNPACK_UNPACKER_IS_NULL, "unpack the file, but unpacker is null! if you don't need to unpack," + "please call TaskParam method setAutoUnpack(false)"));
            return;
        }
        try {
            if (!unpacker.isSupport(sourceFilePath)) {
                FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "解压失败", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                onUnpackError(new DownloadUnpackException(ErrorCode.Values.UNPACK_NONSUPPORT, "unpack the file, but unpacker is null! if you don't need to unpack," + "please call TaskParam method setAutoUnpack(false)"));
                return;
            }

            unpacker.unpack(sourceFilePath, download.getUnpackPath(), download.isDeleteSourceAfterUnpack(),
                new IDownloadUnpackCallback() {
                    @Override
                    public void onStart(String targetPath) {
                        onUnpackStart(targetPath);
                    }

                    @Override
                    public void onSuccess() {
                        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "解压成功", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                        onUnpackSuccess();
                    }

                    @Override
                    public void onProgress(long totalSize, long finishedSize) {
                        onUnpackProgress(totalSize, finishedSize);
                    }

                    @Override
                    public void onError(DownloadUnpackException e) {
                        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "解压失败", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
                        mHasError = true;
                        onUnpackError(e);
                    }
                });
        } catch (Throwable throwable) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("解压文件", "解压失败", "解压流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            onUnpackError(new DownloadUnpackException(ErrorCode.Values.UNPACK_RUN_ERROR, "download unpack unchecked error: ", throwable));
        }
    }

    private boolean checkPermission(String permission) {
        return DownloadUtils.checkPermission(DownloadInitHelper.getInstance().getAppContext(), permission);
    }

    private void throwNetworkException(int flag) throws DownloadNetworkException {
        String errorCode = NetworkUtil.getNetworkErrorCode(flag);
        throw new DownloadNetworkException(errorCode, "can't start runnable, because network not ok, flag: " + flag + ", error code: " + errorCode);
    }

    private boolean isRoamingAllowed() {
        return mTask.isAllowRoaming();
    }

    private void onStarted() {
        if (checkCancel()) {
            FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("准备下载", "任务已取消", "准备流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
            return;
        }
        LogUtil.d(DownloadUtils.formatString(" runnable[%s] started", getRunnableId()));
        mTask.setState(Status.DOWNLOAD_STARTED);
        mDbManager.updateDownloadStatus(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadStartedMsg(mTask));
        FlowMonitor.getOperate().addFlow(new IFlowBean.Builder().setFlow("准备下载", "准备成功", "准备流程结束").setUniqueTag(getRunnableId()).setExtras(mTask).build());
    }

    private void onConnected(final boolean resuming, final long total, final String etag, final String filename) {
        if (checkCancel()) {
            return;
        }
        LogUtil.d(DownloadUtils.formatString(" runnable[%s] connected, resuming[%s], total[%s], file name[%s]", getRunnableId(), resuming, total, filename));
        mTask.setFileName(filename);
        mTask.setETag(etag)
            .setTotalSize(total)
            .setState(Status.DOWNLOAD_CONNECTED);
        mDbManager.updateDownloadParamAndStatus(mTask.getTaskParamInfo(), mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadConnectedMsg(mTask, resuming));
    }

    @Override
    public void onCancel() {
        // do nothing
    }

    private void onProgress(final long finished, final long total, final long speed, final boolean showProgress, final boolean asyn) {

        if (checkCancel()) {
            return;
        }
        LogUtil.d(DownloadUtils.formatString(" runnable[%s] downloading [%s] / [%s]  ", getRunnableId(), finished, total));

        mTask.setState(Status.DOWNLOAD_PROGRESS)
            .setFinishSize(finished)
            .setTotalSize(total)
            .setSpeed(speed);

        mDbManager.updateDownloadProgress(mTask.getTaskStateInfo(), asyn);

        if (checkCancel()) {
            return;
        }

        if (showProgress) {
            mManager.receiveMessage(MessageFactory.createDownloadingMsg(mTask));
        }
    }

    private void onDownloadComplete(final long total) {
        LogUtil.d(DownloadUtils.formatString(" runnable[%s] download completed total[%s]  ", getRunnableId(), total));
        mTask.setState(Status.DOWNLOAD_SUCCESS)
            .setTotalSize(total)
            .setSpeed(0);

        mDbManager.updateDownloadSuccess(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadSuccessMsg(mTask));
    }

    private void onRetry(DownloadBaseException e, int retryTimes) {
        if (checkCancel()) {
            return;
        }
        LogUtil.w(e, ErrorCode.format(e.getErrorCode(), " runnable[" + getRunnableId() + "] error, to retry[" + retryTimes + "] "));
        mTask.setState(Status.DOWNLOAD_RETRY)
            .setSpeed(0)
            .setRetryTime(retryTimes)
            .setErrorCode(e.getErrorCode())
            .setException(e);

        mDbManager.updateDownloadRetry(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadRetryMsg(mTask));
    }

    private void onNetworkError(String errorCde, DownloadBaseException e) {
        if (checkCancel()) {
            return;
        }
        LogUtil.w(e, ErrorCode.format(errorCde, " runnable[" + getRunnableId() + "] network error! "));
        setHasError(true);
        mTask.setState(Status.DOWNLOAD_PAUSE)
            .setSpeed(0)
            .setErrorCode(e.getErrorCode())
            .setException(e);

        mDbManager.updateDownloadFailure(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadPauseMsg(mTask));
        FlowMonitor.getOperate().addInfo(new IFlowBean.Builder().setInfo(MessageFactory.createDownloadPauseMsg(mTask).toString()).setUniqueTag(String.valueOf(getRunnableId())).build());
    }

    private void onOutOfSpaceError(String errorCde, DownloadBaseException e) {
        if (checkCancel()) {
            return;
        }
        LogUtil.w(e, ErrorCode.format(errorCde, " runnable[" + getRunnableId() + "] out of space error! "));
        setHasError(true);
        mTask.setState(Status.DOWNLOAD_PAUSE)
            .setSpeed(0)
            .setErrorCode(e.getErrorCode())
            .setException(e);

        mDbManager.updateDownloadFailure(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadPauseMsg(mTask));
        FlowMonitor.getOperate().addInfo(new IFlowBean.Builder().setInfo(MessageFactory.createDownloadPauseMsg(mTask).toString()).setUniqueTag(String.valueOf(getRunnableId())).build());
    }

    private void onDownloadError(DownloadBaseException e) {
        if (checkCancel()) {
            return;
        }
        onDownloadError(e.getErrorCode(), e);
    }

    private void onDownloadError(String errorCde, Throwable throwable) {
        if (checkCancel()) {
            return;
        }
        LogUtil.e(throwable, ErrorCode.format(errorCde, " runnable[" + getRunnableId() + "] download error! "));
        setHasError(true);
        mTask.setState(Status.DOWNLOAD_FAILURE)
            .setSpeed(0)
            .setErrorCode(errorCde)
            .setException(throwable);

        mDbManager.updateDownloadFailure(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createDownloadFailureMsg(mTask));
        FlowMonitor.getOperate().addInfo(new IFlowBean.Builder().setInfo(MessageFactory.createDownloadFailureMsg(mTask).toString()).setUniqueTag(String.valueOf(getRunnableId())).build());
    }

    private void onCheckStart() {
        LogUtil.i(" runnable[" + getRunnableId() + "] check started ");
        mTask.setState(Status.CHECK_STARTED)
            .setFinishSize(0);

        mDbManager.updateDownloadStatus(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createCheckStartedMsg(mTask));
    }

    private void onCheckProgress(long totalSize, long finishedSize) {
        if (LogUtil.isDebug()) {
            LogUtil.d(" runnable[" + getRunnableId() + "] unpacking");
        }
        mTask.setState(Status.CHECK_PROGRESS)
            .setTotalSize(totalSize)
            .setFinishSize(finishedSize);

        mDbManager.updateDownloadProgress(mTask.getTaskStateInfo(), false);
        mManager.receiveMessage(MessageFactory.createCheckingMsg(mTask));
    }

    private void onCheckSuccess() {
        LogUtil.i(" runnable[" + getRunnableId() + "] check success ");
        mTask.setState(Status.CHECK_SUCCESS);

        mDbManager.updateCheckSuccess(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createCheckSuccessMsg(mTask));
    }

    private void onCheckError(DownloadBaseException e) {
        onCheckError(e.getErrorCode(), e);
    }

    private void onCheckError(String errorCde, Throwable throwable) {
        LogUtil.e(throwable, ErrorCode.format(errorCde, " runnable[" + getRunnableId() + "] check error! "));
        setHasError(true);
        mTask.setState(Status.CHECK_FAILURE)
            .setErrorCode(errorCde)
            .setException(throwable);

        mDbManager.updateCheckFailure(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createCheckFailureMsg(mTask));
        FlowMonitor.getOperate().addInfo(new IFlowBean.Builder().setInfo(MessageFactory.createCheckFailureMsg(mTask).toString()).setUniqueTag(String.valueOf(getRunnableId())).build());
    }

    private void onUnpackStart(String targetPath) {
        LogUtil.i(" runnable[" + getRunnableId() + "] unpack started ");
        mTask.setUnpackPath(targetPath)
            .setState(Status.UNPACK_STARTED)
            .setFinishSize(0);

        mDbManager.updateUnpackParamAndStatus(mTask.getTaskParamInfo(), mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createUnpackStartedMsg(mTask));
    }

    private void onUnpackProgress(long totalSize, long finishedSize) {
        if (LogUtil.isDebug()) {
            LogUtil.d(" runnable[" + getRunnableId() + "] unpacking");
        }
        mTask.setState(Status.UNPACK_PROGRESS)
            .setTotalSize(totalSize)
            .setFinishSize(finishedSize);

        mDbManager.updateDownloadProgress(mTask.getTaskStateInfo(), false);
        mManager.receiveMessage(MessageFactory.createUnpackingMsg(mTask));
    }

    private void onUnpackSuccess() {
        LogUtil.i(" runnable[" + getRunnableId() + "] unpack success ");
        mTask.setState(Status.UNPACK_SUCCESS);

        mDbManager.updateUnpackSuccess(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createUnpackSuccessMsg(mTask));
    }

    private void onUnpackError(DownloadBaseException e) {
        onUnpackError(e.getErrorCode(), e);
    }

    private void onUnpackError(String errorCde, Throwable throwable) {
        LogUtil.e(throwable, ErrorCode.format(errorCde, " runnable[" + getRunnableId() + "] unpack error! "));
        setHasError(true);
        mTask.setState(Status.UNPACK_FAILURE)
            .setErrorCode(errorCde)
            .setException(throwable);

        mDbManager.updateUnpackFailure(mTask.getTaskStateInfo());
        mManager.receiveMessage(MessageFactory.createUnpackFailureMsg(mTask));
        FlowMonitor.getOperate().addInfo(new IFlowBean.Builder().setInfo(MessageFactory.createUnpackFailureMsg(mTask).toString()).setUniqueTag(String.valueOf(getRunnableId())).build());
    }

    private void setHasError(boolean hasError) {
        mHasError = hasError;
    }

    @Override
    public void onFinish() {
        //LogUtil.i(" runnable["+getRunnableId()+"] task["+mTask.getGenerateId()+"] finished");
    }


    private long getBreakpointPosition() {
        final long taskFinishSize = mTask.getFinishSize();
        if (taskFinishSize <= 0) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s the downloaded-record is zero.", mTask.getGenerateId()));
            return 0;
        }

        // 如果没有设置保存文件名称，第一次获取将返回null。正常请求获取到文件名后才返回真实文件名
        String tempFilePath = mTask.getTempFilePath();
        if (TextUtils.isEmpty(tempFilePath)) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s temp path == null", mTask.getGenerateId()));
            return 0;
        }

        File tempFile = new File(tempFilePath);
        if (!FileUtil.isFileExist(tempFile)) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s file not suit", mTask.getGenerateId()));
            return 0;
        }

        final long tempFileLength = tempFile.length();
        if (tempFileLength < taskFinishSize) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s dirty data fileLength[%d] sofar[%d]", mTask.getGenerateId(), tempFileLength, taskFinishSize));

            return 0;
        }

        return taskFinishSize;
    }

    /**
     * 检测是否可以断点恢复下载
     */
    private void checkIsResumeAvailable() {
        DownloadInnerTask requestTask = mTask;
        final boolean outputStreamSupportSeek = true;
        if (isBreakpointAvailable(getRunnableId(), requestTask, outputStreamSupportSeek)) {
            this.isResumeDownloadAvailable = true;
        } else {
            this.isResumeDownloadAvailable = false;
            requestTask.deleteAllFiles();
        }
    }

    private boolean isBreakpointAvailable(String runnableId, DownloadInnerTask requestTask, boolean outputStreamSupportSeek) {
        if (!outputStreamSupportSeek) {
            LogUtil.d(DownloadUtils.formatString(" runnable[%s] can't continue, outputStreamSupportSeek == %s", runnableId, outputStreamSupportSeek));
            return false;
        }
        if (requestTask == null) {
            LogUtil.d(DownloadUtils.formatString(" runnable[%s] can't continue, model == null", runnableId));
            return false;
        }
        // 如果没有设置保存文件名称，第一次获取将返回null。正常请求获取到文件名后才返回真实文件名
        if (requestTask.getTempFilePath() == null) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s temp path == null", runnableId));
            return false;
        }

        return isBreakpointAvailable(runnableId, requestTask, requestTask.getTempFilePath(), outputStreamSupportSeek);
    }

    public static boolean isBreakpointAvailable(final String runnableId, final DownloadInnerTask requestTask, final String path, final boolean outputStreamSupportSeek) {
        if (path == null) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s path = null", runnableId));
            return false;
        }

        File file = new File(path);
        final boolean isExists = file.exists();
        final boolean isDirectory = file.isDirectory();

        if (!isExists || isDirectory) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s file not suit, exists[%B], directory[%B]", runnableId, isExists, isDirectory));
            return false;
        }

        final long fileLength = file.length();

        if (requestTask.getFinishSize() == 0) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s the downloaded-record is zero.", runnableId));
            return false;
        }

        if (fileLength < requestTask.getFinishSize() || (requestTask.getTotalSize() != -1  // not chunk transfer encoding data
            && (fileLength > requestTask.getTotalSize() || requestTask.getFinishSize() >= requestTask.getTotalSize()))) {
            // dirty data.
            LogUtil.d(DownloadUtils.formatString("can't continue %s dirty data" + " fileLength[%d] sofar[%d] total[%d]", runnableId, fileLength, requestTask.getFinishSize(), requestTask.getTotalSize()));
            return false;
        }

        if (!outputStreamSupportSeek && requestTask.getTotalSize() == fileLength) {
            LogUtil.d(DownloadUtils.formatString("can't continue %s, because of the output stream doesn't support seek, but the task has already pre-allocated, so we only can download it from the very beginning.", runnableId));
            return false;
        }

        return true;
    }

    private void addHeader(Request.Builder builder) {
        /*final Headers additionHeaders = null;
        if (header != null) {
            if (FileDownloadProperties.getImpl().PROCESS_NON_SEPARATE) {
                *//**
         * In case of the FileDownloadService is not in separate process, the
         * function {@link FileDownloadHeader#writeToParcel} would never be invoked, so
         * {@link FileDownloadHeader#checkAndInitValues} would never be invoked.
         *
         * Instead, we can use {@link FileDownloadHeader#headerBuilder} directly.
         *//*
                additionHeaders = header.getHeaders();
            } else {
                *//**
         * In case of the FileDownloadService is in separate process to UI process,
         * the headers value would be carried by the parcel with
         * {@link FileDownloadHeader#checkAndInitValues()} .
         *//*
                if (header.getNamesAndValues() != null) {
                    additionHeaders = Headers.of(header.getNamesAndValues());
                } else {
                    additionHeaders = null;
                }
            }

            if (additionHeaders != null) {
                LogUtil.v(DownloadUtils.formatString("%s add outside header: %s", getRunnableId(), additionHeaders));
                builder.headers(additionHeaders);
            }
        }*/

        if (isResumeDownloadAvailable) {
            if (!TextUtils.isEmpty(mTask.getETag())) {
                builder.addHeader("If-Match", mTask.getETag());
            }
            builder.addHeader("Range", DownloadUtils.formatString("bytes=%d-", mTask.getFinishSize()));
        }
    }

    private String findEtag(Response response) {
        final String newEtag = response.header("Etag");
        if (LogUtil.isDebug()) {
            LogUtil.d(DownloadUtils.formatString("etag find by header %s %s", getRunnableId(), newEtag));
        }
        return newEtag;
    }

    private String findFilename(Response response) {
        String fileName = getFileName(response);
        String fileExtension = getFileExtension(response);

        String fullFileName;
        if (!fileName.endsWith(fileExtension) && fileExtension.length() > 0) {
            fullFileName = String.format("%s.%s", fileName, fileExtension);
        } else {
            fullFileName = fileName;
        }
        LogUtil.i(DownloadUtils.formatString(" find file name[%s], extension[%s]", fileName, fileExtension));

        // 保存新的文件名
        mTask.setFileName(fullFileName);
        // 保存新的文件后缀名
        mTask.setFileExtension(fileExtension);
        return fullFileName;
    }

    private String getFileExtension(Response response) {
        String fileExtension = mTask.getFileExtension();
        if (TextUtils.isEmpty(fileExtension)) {
            // get file extension from find name
            String tempName = FileUtil.findFileName(mTask.getUrl(), response.header("Content-Disposition"), response.header("Content-Location"));
            if (!TextUtils.isEmpty(tempName)) {
                int dotIndex = tempName.lastIndexOf('.');
                if (dotIndex != -1) {
                    fileExtension = tempName.substring(dotIndex);
                    LogUtil.i(DownloadUtils.formatString(" find file extension[%s]", fileExtension));
                }
            }

            if (TextUtils.isEmpty(fileExtension)) {
                // get file extension from mime type
                String mimeType = response.header("Content-Type");
                fileExtension = FileUtil.findExtensionFromMimeType(mimeType, true);
                LogUtil.i(DownloadUtils.formatString(" find file extension[%s] from mimeType[%s]", fileExtension, mimeType));
            }
        }

        if (!TextUtils.isEmpty(fileExtension)) {
            fileExtension = fileExtension.replaceAll("^[.]+", "");
        }
        fileExtension = fileExtension == null ? "" : fileExtension;

        return fileExtension;
    }

    private String getFileName(Response response) {
        String fileName = mTask.getFileName();
        if (!TextUtils.isEmpty(fileName)) {
            return fileName;
        }

        String findFileName = FileUtil.findFileName(mTask.getUrl(), response.header("Content-Disposition"), response.header("Content-Location"));
        LogUtil.i("getting filename from find [" + findFileName + "]");
        if (!TextUtils.isEmpty(findFileName)) {
            // 过滤掉文件名开头以及结尾的“.”
            fileName = findFileName.replaceAll("^[.]+|[.]+$", "");
        } else {
            fileName = DownloadUtils.generateFileName(mTask.getUrl());
            LogUtil.w("end filename is empty, generate new [" + fileName + "]");
        }
        return fileName;
    }

    private void renameTempFile() throws DownloadStopException {
        final String tempPath = mTask.getTempFilePath();
        final String targetPath = mTask.getTargetFilePath();

        final File tempFile = new File(tempPath);
        try {
            // 检测缓存文件是否存在
            if (!tempFile.exists()) {
                throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_CACHE_FILE_NO_EXITS, DownloadUtils.formatString("Download cache file([%s]) not found, whether has been deleted?", tempPath));
            }

            final File targetFile = new File(targetPath);
            // 如果目标文件存在，删除占用的目标的文件
            if (targetFile.exists()) {
                final long oldTargetFileLength = targetFile.length();
                if (!FileUtil.deleteFile(targetFile)) {
                    throw new DownloadStopException(ErrorCode.Values.DOWNLOAD_DELETE_TARGET_FILE_ERROR, DownloadUtils.formatString("Can't delete the old file([%s], [%d]), " + "so can't replace it with the new downloaded one.", targetPath, oldTargetFileLength));
                } else {
                    LogUtil.w(DownloadUtils.formatString("The target file([%s], [%d]) will be replaced with" + " the new downloaded file[%d]", targetPath, oldTargetFileLength, tempFile.length()));
                }
            }
            // 重命名，去掉缓存文件后缀
            if (!FileUtil.rename(tempFile, targetFile)) {
                throw new DownloadStopException(
                    ErrorCode.Values.DOWNLOAD_RENAME_FILE_ERROR, DownloadUtils.formatString("Can't rename the  temp downloaded file(%s) to the target file(%s)", tempPath, targetPath));
            }
        } finally {
            if (tempFile.exists()) {
                if (!FileUtil.deleteFile(tempFile)) {
                    LogUtil.w(DownloadUtils.formatString("delete the temp file(%s) failed, on completed downloading.", tempPath));
                }
            }
        }
    }
}
