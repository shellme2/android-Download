package com.eebbk.bfc.sdk.download.da;

import android.text.TextUtils;

import com.eebbk.bfc.common.app.AppUtils;
import com.eebbk.bfc.common.devices.NetUtils;
import com.eebbk.bfc.sdk.behavior.BehaviorCollector;
import com.eebbk.bfc.sdk.behavior.control.collect.encapsulation.entity.attr.AidlCustomAttr;
import com.eebbk.bfc.sdk.behavior.control.collect.encapsulation.entity.event.ClickEvent;
import com.eebbk.bfc.sdk.behavior.db.constant.BFCColumns;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.message.ErrorMsg;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.downloadmanager.BuildConfig;
import com.eebbk.bfc.sdk.downloadmanager.CDNManager;
import com.eebbk.bfc.sdk.downloadmanager.ITask;
import com.eebbk.bfc.sdk.downloadmanager.SDKVersion;
import com.eebbk.bfc.sequence.SequenceTools;
import com.onething.xyvod.XYVodSDK;

import java.util.HashMap;
import java.util.Map;

/**
 * @author zhua
 * 下载大数据埋点类
 */
public class DownloadDACollect {

    private static final String CDN_TYPE_XY = "星域";
    private static final String CDN_TYPE_ORIGINAL = "原始CDN";
    private static final String MODULE_DOWNLOAD = "下载功能";
    private static final String MODULE_REQUEST_CDN_FLAG = "渠道判断";
    private static final String FUNCTION_DOWNLOAD_INFO = "下载信息";
    private static final String FUNCTION_DOWNLOAD_ERROR = "下载失败";
    private static final String FUNCTION_DOWNLOAD_SUCCESS = "下载成功";
    private static final String FUNCTION_REQUEST_CDN_FLAG_FAIL = "获取渠道标识失败";
    private static final String FUNCTION_REQUEST_CDN_FLAG_SUCCESS = "获取渠道标识成功";
    private static final String FUNCTION_DOWNLOAD_START = "开始下载";

    private static String appName = "";
    private static String appPkg = "";
    private static String appVersion = "";

    private DownloadDACollect() {
    }

    public static void downloadStart(DownloadInnerTask downloadInnerTask) {
        if (downloadInnerTask == null) {
            return;
        }
        DownloadBaseInfoBean downloadStartBean = new DownloadBaseInfoBean();
        downloadStartBean.setUrl(downloadInnerTask.getUrl());
        String cdnType = CDNManager.isXYVodUrl(downloadInnerTask.getUrl()) ? CDN_TYPE_XY : CDN_TYPE_ORIGINAL;
        downloadStartBean.setCdnType(cdnType);

        sendClickEvent(MODULE_DOWNLOAD, FUNCTION_DOWNLOAD_START, downloadStartBean);
    }

    public static void downloadSuccess(ITask iTask) {
        if (iTask == null) {
            return;
        }
        DownloadBaseInfoBean downloadSuccessBean = new DownloadBaseInfoBean();
        downloadSuccessBean.setUrl(iTask.getRealUrl());
        String cdnType = CDNManager.isXYVodUrl(iTask.getRealUrl()) ? CDN_TYPE_XY : CDN_TYPE_ORIGINAL;
        downloadSuccessBean.setCdnType(cdnType);

        sendClickEvent(MODULE_DOWNLOAD, FUNCTION_DOWNLOAD_SUCCESS, downloadSuccessBean);

        downloadInfo(iTask);
    }

    private static void downloadInfo(ITask iTask) {
        if (iTask == null) {
            return;
        }
        DownloadInfoBean downloadInfoBean = new DownloadInfoBean();
        downloadInfoBean.setUrl(iTask.getUrl());
        downloadInfoBean.setDownFileSize(iTask.getFileSize());

        String getInfo = XYVodSDK.GET_INFO(iTask.getUrl());
        XYCDNDownloadInfo xyDownloadInfo = null;
        String urlHost = "";
        String ip = "";
        try {
            xyDownloadInfo = SequenceTools.deserialize(getInfo, XYCDNDownloadInfo.class);
            ip = NetUtils.getIpAddress();
            urlHost = DownloadUtils.getDomain(iTask.getUrl());
        } catch (Exception e) {
            e.printStackTrace();
        }

        String cdnType = TextUtils.isEmpty(getInfo) ? CDN_TYPE_ORIGINAL : CDN_TYPE_XY;
        downloadInfoBean.setCdnType(cdnType);

        downloadInfoBean.setUrlHost(urlHost);
        downloadInfoBean.setIp(ip);

        downloadInfoBean.setDataPeerFromXY(xyDownloadInfo == null ? "-1" : String.valueOf(xyDownloadInfo.getDown_peer()));
        downloadInfoBean.setDataPeerFromCDN(xyDownloadInfo == null ? "-1" : String.valueOf(xyDownloadInfo.getDown_cdn()));

        sendClickEvent(MODULE_DOWNLOAD, FUNCTION_DOWNLOAD_INFO, downloadInfoBean);
    }

    /**
     * 下载失败埋点统计
     */
    public static void downloadError(ITask iTask, ErrorMsg errorMsg) {
        if (null == iTask || errorMsg == null) {
            return;
        }
        DownloadErrorBean downloadErrorBean = new DownloadErrorBean();
        downloadErrorBean.setUrl(iTask.getUrl());
        String cdnType = CDNManager.isXYVodUrl(iTask.getRealUrl()) ? CDN_TYPE_XY : CDN_TYPE_ORIGINAL;
        downloadErrorBean.setCdnType(cdnType);

        downloadErrorBean.setErrorCode(errorMsg.getErrorCode());
        downloadErrorBean.setErrorMsg(errorMsg.getThrowable().getLocalizedMessage());

        sendClickEvent(MODULE_DOWNLOAD, FUNCTION_DOWNLOAD_ERROR, downloadErrorBean);

        if (TextUtils.equals(cdnType, CDN_TYPE_XY)) {
            CDNManager.uploadXYDownloadError(DownloadInitHelper.getInstance().getAppContext(), iTask.getUrl(), cdnType, errorMsg);
        }
    }

    /**
     * CDN渠道获取信息失败
     */
    public static void requestCdnFlagError(String cdnJson) {
        sendClickEvent(MODULE_REQUEST_CDN_FLAG, FUNCTION_REQUEST_CDN_FLAG_FAIL, cdnJson);
    }


    /**
     * CDN渠道获取信息失败成功
     */
    public static void requestCdnFlagSuccess(String cdnJson) {
        sendClickEvent(MODULE_REQUEST_CDN_FLAG, FUNCTION_REQUEST_CDN_FLAG_SUCCESS, cdnJson);
    }

    private static void sendClickEvent(String modelDetail, String functionName, DownloadBaseInfoBean infoBean) {
        if (TextUtils.isEmpty(appName)) {
            appName = AppUtils.getAppName(DownloadInitHelper.getInstance().getAppContext());
        }
        if (TextUtils.isEmpty(appVersion)) {
            appVersion = AppUtils.getVersionName(DownloadInitHelper.getInstance().getAppContext());
        }

        if (TextUtils.isEmpty(appPkg)) {
            appPkg = AppUtils.getPackageName(DownloadInitHelper.getInstance().getAppContext());
        }
        infoBean.setAppName(appName);
        infoBean.setAppPkg(appPkg);
        infoBean.setAppVersion(appVersion);

        sendClickEvent(modelDetail, functionName, SequenceTools.serialize(infoBean));
    }

    private static void sendClickEvent(String modelDetail, String functionName, String extend) {
        ClickEvent clickEvent = new ClickEvent();
        clickEvent.moduleDetail = modelDetail;
        clickEvent.functionName = functionName;
        clickEvent.extend = extend;

        AidlCustomAttr attr = new AidlCustomAttr();
        Map<String, String> attrMap = new HashMap<>();
        attrMap.put(BFCColumns.COLUMN_AA_MODULENAME, SDKVersion.getLibraryName());
        attrMap.put(BFCColumns.COLUMN_AA_APPVER, SDKVersion.getVersionName());
        attrMap.put(BFCColumns.COLUMN_AA_PACKAGENAME, BuildConfig.APPLICATION_ID);
        attr.setMap(attrMap);
        clickEvent.addAttr(attr);

        LogUtil.i("DownloadDACollect:", functionName, ">", clickEvent.extend);
        BehaviorCollector.getInstance().clickEvent(clickEvent);
    }
}
