package com.eebbk.bfc.download.demo.util;

import android.graphics.Color;
import android.os.Environment;
import android.support.annotation.ColorInt;
import android.text.Html;

import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.util.ExtrasConverter;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-23 10:40
 * Email: jacklulu29@gmail.com
 */

public class DemoUtil {

    private DemoUtil(){
        // private construct
    }

    public static String getStatusStr(int state){
        String statusStr = "状态： - - ";
        switch (state){
            case Status.DOWNLOAD_WAITING:
                statusStr = "状态： 等待下载 ";
                break;
            case Status.DOWNLOAD_STARTED:
                statusStr = "状态： 开始下载 ";
                break;
            case Status.DOWNLOAD_CONNECTED:
                statusStr = "状态： 下载已连接 ";
                break;
            case Status.DOWNLOAD_PROGRESS:
                statusStr = "状态： 下载中... ";
                break;
            case Status.DOWNLOAD_PAUSE:
                statusStr = "状态： 下载暂停 ";
                break;
            case Status.DOWNLOAD_RETRY:
                statusStr = "状态： 下载重试中... ";
                break;
            case Status.DOWNLOAD_FAILURE:
                statusStr = "状态： 下载失败 ";
                break;
            case Status.DOWNLOAD_SUCCESS:
                statusStr = "状态： 下载完成 ";
                break;
            case Status.CHECK_STARTED:
                statusStr = "状态： 校验开始 ";
                break;
            case Status.CHECK_PROGRESS:
                statusStr = "状态： 校验中... ";
                // no support
                break;
            case Status.CHECK_FAILURE:
                statusStr = "状态： 校验失败 ";
                break;
            case Status.CHECK_SUCCESS:
                statusStr = "状态： 校验成功 ";
                break;
            case Status.UNPACK_STARTED:
                statusStr = "状态： 解压开始 ";
                break;
            case Status.UNPACK_PROGRESS:
                statusStr = "状态： 解压中... ";
                break;
            case Status.UNPACK_FAILURE:
                statusStr = "状态： 解压失败 ";
                break;
            case Status.UNPACK_SUCCESS:
                statusStr = "状态： 解压成功 ";
                break;
            default:
                break;
        }
        return statusStr;
    }

    public static String getStatusStr2(int state){
        String statusStr;
        switch (state){
            case Status.DOWNLOAD_INVALID:
                statusStr = "未开始";
                break;
            case Status.DOWNLOAD_WAITING:
                statusStr = "等待下载";
                break;
            case Status.DOWNLOAD_STARTED:
                statusStr = "下载已开始";
                break;
            case Status.DOWNLOAD_CONNECTED:
                statusStr = "下载已连接";
                break;
            case Status.DOWNLOAD_PROGRESS:
                statusStr = "下载中...";
                break;
            case Status.DOWNLOAD_PAUSE:
                statusStr = "下载暂停";
                break;
            case Status.DOWNLOAD_RETRY:
                statusStr = "下载重试中...";
                break;
            case Status.DOWNLOAD_FAILURE:
                statusStr = "下载失败";
                break;
            case Status.DOWNLOAD_SUCCESS:
                statusStr = "下载完成";
                break;
            case Status.CHECK_STARTED:
                statusStr = "校验开始";
                break;
            case Status.CHECK_PROGRESS:
                statusStr = "校验中...";
                // no support
                break;
            case Status.CHECK_FAILURE:
                statusStr = "校验失败";
                break;
            case Status.CHECK_SUCCESS:
                statusStr = "校验成功";
                break;
            case Status.UNPACK_STARTED:
                statusStr = "解压开始";
                break;
            case Status.UNPACK_PROGRESS:
                statusStr = "解压中...";
                break;
            case Status.UNPACK_FAILURE:
                statusStr = "解压失败";
                break;
            case Status.UNPACK_SUCCESS:
                statusStr = "解压成功";
                break;
            default:
                statusStr = "未知状态";
                break;
        }
        return statusStr;
    }

    public static String getStatusBtnStr(int state){
        String statusStr;
        switch (state){
            case Status.DOWNLOAD_INVALID:
                statusStr = "下载";
                break;
            case Status.DOWNLOAD_WAITING:
                statusStr = "暂停（等待下载中）";
                break;
            case Status.DOWNLOAD_STARTED:
                statusStr = "暂停（下载已开始）";
                break;
            case Status.DOWNLOAD_CONNECTED:
                statusStr = "暂停（下载已连接)";
                break;
            case Status.DOWNLOAD_PROGRESS:
                statusStr = "暂停（下载中...)";
                break;
            case Status.DOWNLOAD_PAUSE:
                statusStr = "继续（下载暂停中）";
                break;
            case Status.DOWNLOAD_RETRY:
                statusStr = "暂停（下载重试中）";
                break;
            case Status.DOWNLOAD_FAILURE:
                statusStr = "下载失败，点击重新下载";
                break;
            case Status.DOWNLOAD_SUCCESS:
                statusStr = "下载成功,点击重新下载";
                break;
            case Status.CHECK_STARTED:
                statusStr = "校验开始";
                break;
            case Status.CHECK_PROGRESS:
                statusStr = "校验中...";
                // no support
                break;
            case Status.CHECK_FAILURE:
                statusStr = "校验失败";
                break;
            case Status.CHECK_SUCCESS:
                statusStr = "校验成功";
                break;
            case Status.UNPACK_STARTED:
                statusStr = "解压开始";
                break;
            case Status.UNPACK_PROGRESS:
                statusStr = "解压中...";
                break;
            case Status.UNPACK_FAILURE:
                statusStr = "解压失败";
                break;
            case Status.UNPACK_SUCCESS:
                statusStr = "解压成功";
                break;
            default:
                statusStr = "未知状态";
                break;
        }
        return statusStr;
    }

    public static @ColorInt int getStatusBtnColor(int state){
        int color = Color.WHITE;
        switch (state){
            case Status.DOWNLOAD_INVALID:
                color = Color.WHITE;
                break;
            case Status.DOWNLOAD_WAITING:
                color = Color.LTGRAY;
                break;
            case Status.DOWNLOAD_STARTED:
                color = Color.CYAN;
                break;
            case Status.DOWNLOAD_CONNECTED:
                color = Color.GREEN;
                break;
            case Status.DOWNLOAD_PROGRESS:
                color = Color.GREEN;
                break;
            case Status.DOWNLOAD_PAUSE:
                color = Color.YELLOW;
                break;
            case Status.DOWNLOAD_RETRY:
                color = Color.MAGENTA;
                break;
            case Status.DOWNLOAD_FAILURE:
                color = Color.RED;
                break;
            case Status.DOWNLOAD_SUCCESS:
                color = 0xFF0E6DB0;
                break;
            case Status.CHECK_STARTED:
                color = Color.WHITE;
                break;
            case Status.CHECK_PROGRESS:
                color = Color.GREEN;
                // no support
                break;
            case Status.CHECK_FAILURE:
                color = Color.RED;
                break;
            case Status.CHECK_SUCCESS:
                color = 0xFF0E6DB0;
                break;
            case Status.UNPACK_STARTED:
                color = Color.WHITE;
                break;
            case Status.UNPACK_PROGRESS:
                color = Color.GREEN;
                break;
            case Status.UNPACK_FAILURE:
                color = Color.RED;
                break;
            case Status.UNPACK_SUCCESS:
                color = 0xFF0E6DB0;
                break;
            default:
                break;
        }
        return color;
    }

    public static CharSequence getTaskInfo(ITask task){
        StringBuilder sb = new StringBuilder();
        sb.append("文件名： " + task.getFileName());
        sb.append("<br> 文件名后缀： " + task.getFileExtension());
        sb.append("<br>" + DemoUtil.getStatusStr(task.getState()));
        sb.append("<br> 总大小： " + task.getFileSize() + " Bytes");
        sb.append("<br> 已完成大小： " + task.getFinishSize() + " Bytes");
        sb.append("<br> 错误码： "  + task.getReasonCode());
        if(task.getErrorCode() != null){
            sb.append("<br>" + ErrorCodeUtil.getErrorStr(task.getReasonCode()));
        }
        sb.append("<br> 异常： " + LogUtil.getStackTraceString(task.getException()));
        sb.append("<br> ");
        sb.append("<br> ---------------------------------------");
        sb.append("<br> ");
        sb.append("<br> 模块名： " + task.getModuleName());
        sb.append("<br> 下载地址： " + task.getUrl());
        sb.append("<br> 保存路径： " + task.getSavePath());
        sb.append("<br> 预设文件大小： " + task.getPresetFileSize());
        sb.append("<br> 自动检测文件大小： " + getBooleanStr(task.isAutoCheckSize()));
        sb.append("<br> 优先级： " + task.getPriority());
        sb.append("<br> 校验类型： " + task.getCheckType());
        sb.append("<br> 校验码： " + task.getCheckCode());
        sb.append("<br> 自动校验： " + getBooleanStr(task.isCheckEnable()));
        sb.append("<br> 扩展字段： " + ExtrasConverter.extrasToString(task.getExtras()));
        sb.append("<br> 自定义字段： " + task.getReserver());
        sb.append("<br> 显示实时速度： " + task.isShowRealTimeInfo());
        sb.append("<br> 进度回调时间： " + task.getMinProgressTime() + "ms");
        sb.append("<br> 自动解压： " + task.isAutoUnpack());
        sb.append("<br> 解压保存路径： " + task.getUnpackPath());
        sb.append("<br> 是否有下载监听： " + getBooleanStr(task.getOnDownloadListener() != null));
        sb.append("<br> 是否有校验监听： " + getBooleanStr(task.getOnCheckListener() != null));
        sb.append("<br> 是否有解压监听： " + getBooleanStr(task.getOnUnpackListener() != null));

        String networkStr = "";
        if(NetworkParseUtil.containsWifi(task.getNetworkTypes())){
            networkStr += " Wifi ";
        }
        if(NetworkParseUtil.containsMobile(task.getNetworkTypes())){
            networkStr += " Mobile ";
        }
        if(NetworkParseUtil.containsBluetooth(task.getNetworkTypes())){
            networkStr += " Bluetooth ";
        }
        sb.append("<br> 支持网络： " + networkStr);
        sb.append("<br> 需要排队： " + getBooleanStr(task.isNeedQueue()));
        sb.append("<br> 删除时删除文件（已完成）： " + getBooleanStr(task.isDeleteNoEndTaskAndCache()));
        sb.append("<br> 删除时删除文件（未完成）： " + getBooleanStr(task.isDeleteEndTaskAndCache()));

        return Html.fromHtml(sb.toString());
    }

    public static String getBooleanStr(boolean value){
        return value ? "是" : "否";
    }


    public static String getSaveLogPath(){
        String fileName = new SimpleDateFormat("yyyy-MM-dd_HH_mm_ss", Locale.getDefault()).format(new Date());
        String saveLogPath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                File.separator + "BfcDownloadDemo" + File.separator + fileName +".log";
        return saveLogPath;
    }
}
