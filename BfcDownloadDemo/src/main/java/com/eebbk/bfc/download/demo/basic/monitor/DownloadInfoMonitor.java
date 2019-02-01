package com.eebbk.bfc.download.demo.basic.monitor;

import com.eebbk.bfc.common.app.SharedPreferenceUtils;
import com.eebbk.bfc.common.tools.DateUtils;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.downloadmanager.ITask;
import com.eebbk.bfc.sequence.SequenceTools;
import com.eebbk.bfc.sequence.json.TypeAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by lzy on 2018/8/28.
 */
public class DownloadInfoMonitor {

    private static List<DownloadInfo> allTask = new ArrayList<>();
    private static List<DownloadInfo> downloadSuccessTask = new ArrayList<>();
    private static List<DownloadInfo> downloadFailTask = new ArrayList<>();

    public static void insertToAllTask(ITask iTask) {
        for (DownloadInfo downloadInfo : allTask) {
            if (downloadInfo.getId() == iTask.getId()) {
                return;
            }
        }
        allTask.add(parseToInfo(iTask));
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("allTaskInfo", SequenceTools.serialize(allTask));
    }

    public static void insertToSuccessTask(ITask iTask) {
        for (DownloadInfo downloadInfo : downloadSuccessTask) {
            if (downloadInfo.getId() == iTask.getId()) {
                return;
            }
        }
        downloadSuccessTask.add(parseToInfo(iTask));
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadSuccessTaskInfo", SequenceTools.serialize(downloadSuccessTask));
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadFinishTime", System.currentTimeMillis());
    }

    public static void insertToFailTask(ITask iTask) {
        for (DownloadInfo downloadInfo : downloadFailTask) {
            if (downloadInfo.getId() == iTask.getId()) {
                return;
            }
        }
        downloadFailTask.add(parseToInfo(iTask));
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadFailTaskInfo", SequenceTools.serialize(downloadFailTask));
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadFinishTime", System.currentTimeMillis());
    }

    public static void setDownloadStartTimeFlag() {
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadStartTime", System.currentTimeMillis());
    }

    public static void clearDownloadInfo() {
        allTask.clear();
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("allTaskInfo", "");

        downloadSuccessTask.clear();
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadSuccessTaskInfo", "");

        downloadFailTask.clear();
        SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("downloadFailTaskInfo", "");
    }

    public static void updateInfoFromSP() {
        try {
            allTask = SequenceTools.deserialize(SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("allTaskInfo", ""), new TypeAgent<List<DownloadInfo>>() {
            }.getType());
            downloadSuccessTask = SequenceTools.deserialize(SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("downloadSuccessTaskInfo", ""), new TypeAgent<List<DownloadInfo>>() {
            }.getType());
            downloadFailTask = SequenceTools.deserialize(SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("downloadFailTaskInfo", ""), new TypeAgent<List<DownloadInfo>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (allTask == null) {
                allTask = new ArrayList<>();
            }
            if (downloadSuccessTask == null) {
                downloadSuccessTask = new ArrayList<>();
            }
            if (downloadFailTask == null) {
                downloadFailTask = new ArrayList<>();
            }
        }
    }

    public static String getDownloadInfo() {
        long totalDownloadSize = 0L;
        for (DownloadInfo downloadInfo : downloadSuccessTask) {
            totalDownloadSize += downloadInfo.getFileSize();
        }

        StringBuilder errorInfo = new StringBuilder();
        for (DownloadInfo downloadInfo : downloadFailTask) {
            errorInfo.append(downloadInfo.getFileName()).append(" > ").append(downloadInfo.getErrorCode()).append("\n");
        }

        long finishTime = SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("downloadFinishTime", 0L);
        long startTime = SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("downloadStartTime", 0L);
        String downloadTimeCnt = "";
        if (finishTime - startTime > 0) {
            int hour = (int) ((finishTime - startTime) / 1000 / 60 / 60);
            int minute = (int) ((finishTime - startTime - hour * 60 * 60 * 1000) / 1000 / 60);
            downloadTimeCnt = hour + "hour " + minute + "minutes";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("下载任务：").append(allTask.size()).append("个\n");
        builder.append("下载中任务：").append(allTask.size() - downloadSuccessTask.size() - downloadFailTask.size()).append("个\n");
        builder.append("已下载成功：").append(downloadSuccessTask.size()).append("个\n");
        builder.append("已下载总文件大小：").append(DownloadUtils.formatFileSize(totalDownloadSize)).append("\n");
        builder.append("开始下载时间：").append(DateUtils.format(startTime, "yyyy-MM-dd HH:mm:ss")).append("\n");
        builder.append("下载结束时间：").append(DateUtils.format(finishTime, "yyyy-MM-dd HH:mm:ss")).append("\n");
        builder.append("下载时长：").append(downloadTimeCnt).append("\n");
        if (finishTime - startTime > 1000) {
            builder.append("平均下载速度：").append(DownloadUtils.getSpeedString(totalDownloadSize / ((finishTime - startTime) / 1000), "", 2)).append("\n");
        }
        builder.append("已下载失败：").append(downloadFailTask.size()).append("个\n");
        builder.append("失败详情：\n").append(errorInfo.toString());
        return builder.toString();
    }

    private static DownloadInfo parseToInfo(ITask iTask) {
        return new DownloadInfo(iTask);
    }
}
