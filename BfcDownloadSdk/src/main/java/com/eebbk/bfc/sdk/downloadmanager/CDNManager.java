package com.eebbk.bfc.sdk.downloadmanager;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.eebbk.bfc.common.app.AppUtils;
import com.eebbk.bfc.common.app.SharedPreferenceUtils;
import com.eebbk.bfc.common.devices.DeviceUtils;
import com.eebbk.bfc.common.devices.NetUtils;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.da.DownloadDACollect;
import com.eebbk.bfc.sdk.download.message.ErrorMsg;
import com.eebbk.bfc.sdk.download.net.CdnFlagBean;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sequence.SequenceTools;
import com.onething.xyvod.XYVodSDK;

import java.io.IOException;

import okhttp3.CacheControl;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * CDN 信息管理器
 */
public class CDNManager {

    private CDNManager() {
    }

    /**
     * 初始化星域sdk
     */
    public static void initXYVodSDK(boolean isDebug) {
        XYVodSDK.INIT();
        LogUtil.i("XYVodSDK init version: " + XYVodSDK.GET_VERSION());
//        if (isDebug) {
//        XYVodSDK.setLogEnable(1);
//        }
    }

    /**
     * 使用星域sdk转换url
     *
     * @param url 原始的url
     * @return 转换过后的url
     */
    public static String url_REWRITE(String url) {
//        新版sdk已增加对https的支持，放开https下载
//        if (url.startsWith("https:")) {
//            // 星域so不支持https下载，但是对https地址转换的时候会在尾部添加 xyop=download
//            return url;
//        }
        return XYVodSDK.URL_REWRITE(url, XYVodSDK.DOWNLOAD_MODE);
    }

    /**
     * CDN请求缓存是否有效
     *
     * @return false 失效,true 有效
     */
    private static boolean isValid(Context context) {
        long startTime = SharedPreferenceUtils.getInstance(context.getApplicationContext()).get("BFC_DOWNLOAD_CDN_Start_Cache_TIME", 0L);
        long currentTime = System.currentTimeMillis();
        if (0L != startTime) {
            //缓存是否大于6小时
            if ((currentTime - startTime) / (1000 * 60 * 60) >= 6) {
                return false;
            }
            String cacheInfo = SharedPreferenceUtils.getInstance(context.getApplicationContext()).get("CDN_TYPE_INFO", "");
            return !TextUtils.isEmpty(cacheInfo);
        }
        return false;
    }

    /**
     * 进行CDN类型请求
     */
    public static void requestForCDNType(final Context context) {
        if (isValid(context)) {
            LogUtil.i("CDN-->requestForCDNType, config can use, not need to request.");
            return;
        }

        if (!NetUtils.isConnected(context)) {
            LogUtil.e("CDN-->requestForCDNType, no net connect.");
            return;
        }

        String osVersion = DeviceUtils.getSystemVersion().replaceAll("　", " ");
        String deviceModel = DeviceUtils.getModel().replaceAll("　", " ");

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
            .addHeader("machineId", DeviceUtils.getMachineId(context))
            .addHeader("apkPackageName", context.getPackageName())
            .addHeader("apkVersionCode", String.valueOf(AppUtils.getVersionCode(context)))
            .addHeader("deviceModel", deviceModel)
            .addHeader("deviceOSVersion", osVersion)
            .url("http://safe.eebbk.net/app/CdnSwitch/getCdnSwitch")
//                .url("http://test.eebbk.net/safe/app/CdnSwitch/getCdnSwitch")
            .post(RequestBody.create(null, ""))
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                try {
                    //更新连续失败次数
                    int failedNum = SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).get("CDN_REQUEST_FAILED_NUM", 0);
                    failedNum += 1;
                    LogUtil.e("CDN-->requestForCDNType fail: " + e.getMessage() + ", failCnt: " + failedNum);
                    SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("CDN_REQUEST_FAILED_NUM", failedNum);
                    DownloadDACollect.requestCdnFlagError(e.getMessage());
                } catch (Exception e1) {
                    DownloadDACollect.requestCdnFlagError(e.getMessage());
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = null;
                if (response.body() != null) {
                    s = response.body().string();
                }
                LogUtil.i("CDN-->requestForCDNType success: " + s);
                processCdnFlagBean(s, true);
            }
        });
    }

    /**
     * 将星域转换过的url切换回原始的url
     */
    public static String transformSourceUrl(String url) {
        return XYVodSDK.URL_REWRITE_BACK(url);
    }

    /**
     * 是否是星域转换过的url
     *
     * @param url 待判断的url
     * @return 结果
     */
    public static boolean isXYVodUrl(String url) {
        return XYVodSDK.IF_XYVOD_URL(url);
    }


    /**
     * 上传星域下载失败信息并覆盖下载渠道配置信息
     */
    public static void uploadXYDownloadError(final Context context, String url, String cdnType, ErrorMsg errorMsg) {
        if (!NetUtils.isConnected(context)) {
            LogUtil.e("CDN-->uploadXYDownloadError, no net connect.");
            return;
        }

        if (TextUtils.isEmpty(url)) {
            LogUtil.e("CDN-->uploadXYDownloadError, url is empty.");
            return;
        }

        if (TextUtils.isEmpty(cdnType)) {
            LogUtil.e("CDN-->uploadXYDownloadError, cdnType is empty.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String machineId = DeviceUtils.getMachineId(context);

        String osVersion = DeviceUtils.getSystemVersion().replaceAll("　", " ");
        String deviceModel = DeviceUtils.getModel().replaceAll("　", " ");

        String appVersion = String.valueOf(AppUtils.getVersionCode(context));

        StringBuilder failureInfo = new StringBuilder();
        String ip = "";
        try {
            String[] stacks = errorMsg.getThrowable().toString().split("\n");
            failureInfo.append(stacks[0]).append("\n");
            for (String stack : stacks) {
                if (stack.contains("Caused by:")) {
                    failureInfo.append(stack).append("\n");
                }
            }
            WifiManager wifiManager = (WifiManager) DownloadInitHelper.getInstance().getAppContext().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            if (wifiManager != null) {
                int ipAddress = wifiManager.getConnectionInfo().getIpAddress();
                ip = (ipAddress & 0xFF) + "." + ((ipAddress >> 8) & 0xFF) + "." + ((ipAddress >> 16) & 0xFF) + "." + (ipAddress >> 24 & 0xFF);
            }
        } catch (Exception ignore) {
        }

        RequestBody requestBody = new FormBody.Builder()
            .add("cdnType", cdnType)
            .add("url", url)
            .add("deviceModel", deviceModel)
            .add("osVersion", osVersion)
            .add("machineId", machineId)
            .add("bfcVersionCode", String.valueOf(SDKVersion.getSDKInt()))
            .add("failureCode", errorMsg.getErrorCode())
            .add("failureInfo", failureInfo.toString())
            .add("ip", ip)
            .add("domainName", DownloadUtils.getDomain(url))
            .build();

        Request request = new Request.Builder()
            .addHeader("machineId", machineId)
            .addHeader("apkPackageName", context.getPackageName())
            .addHeader("apkVersionCode", appVersion)
            .addHeader("deviceModel", deviceModel)
            .addHeader("deviceOSVersion", osVersion)
            .url("http://safe.eebbk.net/app/CdnSwitch/reportDownloadFailueInfo")
//            .url("http://test.eebbk.net/safe/app/CdnSwitch/reportDownloadFailueInfo")
//            .url("http://172.28.194.33:9040/app/CdnSwitch/reportDownloadFailueInfo")
            .post(requestBody)
            .cacheControl(CacheControl.FORCE_NETWORK)
            .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                LogUtil.e("CDN-->uploadXYDownloadError fail " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String s = null;
                if (response.body() != null) {
                    s = response.body().string();
                }
                LogUtil.i("CDN-->uploadXYDownloadError success: " + s);
                processCdnFlagBean(s, false);
            }
        });
    }

    private static void processCdnFlagBean(String info, boolean needUpload) {
        try {
            CdnFlagBean cdnFlagBean = SequenceTools.deserialize(info, CdnFlagBean.class);
            if (cdnFlagBean == null || cdnFlagBean.getData() == null) {
                if (needUpload) {
                    DownloadDACollect.requestCdnFlagError(info);
                }
                return;
            }
            CdnFlagBean.DataBean dataBean = cdnFlagBean.getData();
            //缓存
            SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("CDN_TYPE_INFO", SequenceTools.serialize(dataBean));
            SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("CDN_REQUEST_FAILED_NUM", 0);
            SharedPreferenceUtils.getInstance(DownloadInitHelper.getInstance().getAppContext()).put("BFC_DOWNLOAD_CDN_Start_Cache_TIME", System.currentTimeMillis());
        } catch (Exception e) {
            LogUtil.e("CDN-->processCdnFlagBean fail: " + e.getMessage());
            DownloadDACollect.requestCdnFlagError(info);
        }
    }
}
