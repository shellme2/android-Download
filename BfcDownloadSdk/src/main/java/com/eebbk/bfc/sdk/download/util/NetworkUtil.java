package com.eebbk.bfc.sdk.download.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.exception.DownloadNoInitException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.net.NetworkType;

import java.lang.reflect.Method;

/**
 * Desc: 网络工具类
 * Author: llp
 * Create Time: 2016-10-10 17:59
 * Email: jacklulu29@gmail.com
 */

public class NetworkUtil {

    /** 运营商-未知 */
    public static final int PROVIDER_UNKNOWN = 0;
    /** 运营商-中国移动 */
    public static final int PROVIDER_CHINA_MOBILE = 1;
    /** 运营商-中国联通 */
    public static final int PROVIDER_CHINA_UNICOM = 2;
    /** 运营商-中国电信 */
    public static final int PROVIDER_CHINA_TELECOM = 3;

    /** Current network is GPRS */
    public static final int TYPE_GPRS = 1;
    /** Current network is EDGE */
    public static final int TYPE_EDGE = 2;
    /** Current network is UMTS */
    public static final int TYPE_UMTS = 3;
    /** Current network is CDMA: Either IS95A or IS95B*/
    public static final int TYPE_CDMA = 4;
    /** Current network is EVDO revision 0*/
    public static final int TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A*/
    public static final int TYPE_EVDO_A = 6;
    /** Current network is 1xRTT*/
    public static final int TYPE_1xRTT = 7;
    /** Current network is HSDPA */
    public static final int TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    public static final int TYPE_HSUPA = 9;
    /** Current network is HSPA */
    public static final int TYPE_HSPA = 10;
    /** Current network is iDen */
    public static final int TYPE_IDEN = 11;
    /** Current network is EVDO revision B*/
    public static final int TYPE_EVDO_B = 12;
    /** Current network is LTE */
    public static final int TYPE_LTE = 13;
    /** Current network is eHRPD */
    public static final int TYPE_EHRPD = 14;
    /** Current network is HSPA+ */
    public static final int TYPE_HSPAP = 15;

    /** Unknown network class.*/
    public static final int CLASS_UNKNOWN = 0;
    /** Class of broadly defined "2G" networks.*/
    public static final int CLASS_2_G = 1;
    /** Class of broadly defined "3G" networks.*/
    public static final int CLASS_3_G = 2;
    /** Class of broadly defined "4G" networks.*/
    public static final int CLASS_4_G = 3;

    private NetworkUtil(){
        // private construct
    }

    private static Long sRecommendedMaxBytesOverMobile = -1L;
    private static Long sMaxBytesOverMobile = -1L;
    private static Boolean sNetworkRoaming = null;

    private static final String DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE = "download_manager_recommended_max_bytes_over_mobile";
    private static final String DOWNLOAD_MAX_BYTES_OVER_MOBILE = "download_manager_max_bytes_over_mobile";

    /**
     * The network is usable for the given download.
     */
    public static final int NETWORK_OK = 1;

    /**
     * There is no network connectivity.
     */
    public static final int NETWORK_NO_CONNECTION = 2;

    /**
     * The download exceeds the maximum size for this network.
     */
    public static final int NETWORK_UNUSABLE_DUE_TO_SIZE = 3;

    /**
     * The download exceeds the recommended maximum size for this network, the
     * user must confirm for this download to proceed without WiFi.
     */
    public static final int NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE = 4;

    /**
     * The current connection is roaming, and the download can't proceed over a
     * roaming connection.
     */
    public static final int NETWORK_CANNOT_USE_ROAMING = 5;

    /**
     * The app requesting the download specific that it can't use the current
     * network connection.
     */
    public static final int NETWORK_TYPE_DISALLOWED_BY_REQUESTOR = 6;

    /**
     * Current network is blocked for requesting application.
     */
    public static final int NETWORK_BLOCKED = 7;
    /**
     * unknown network, maybe get network info error
     */
    public static final int NETWORK_NO_INIT_CONTEXT = 8;

    public static final int NETWORK_NOT_ALLOW_MOBILE_2_G = 9;

    public static NetworkInfo getActiveNetworkInfo(Context appContext){
        ConnectivityManager connectivity = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            LogUtil.w("couldn't get connectivity manager");
            return null;
        }

        final NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
        if (activeInfo == null) {
            LogUtil.w("network is not available");
        }
        return activeInfo;
    }

    public static int getActiveNetworkType(Context appContext){
        ConnectivityManager connectivity = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity == null) {
            LogUtil.w("couldn't get connectivity manager");
            return -1;
        }
        final NetworkInfo activeInfo = connectivity.getActiveNetworkInfo();
        if (activeInfo == null || !activeInfo.isConnected()) {
            LogUtil.w("network is not available");
            return -1;
        }
        return translateNetworkTypeToApiFlag(activeInfo.getType());
    }

    public static void networkChanged(){
        sRecommendedMaxBytesOverMobile = -1L;
        sMaxBytesOverMobile = -1L;
        sNetworkRoaming = null;
    }

    public static boolean isNetworkRoaming(NetworkInfo info) {
        boolean isMobile = (info != null && info.getType() == ConnectivityManager.TYPE_MOBILE);
        if(!isMobile){
            return false;
        }
        if(sNetworkRoaming != null){
            return sNetworkRoaming;
        }
        Object obj = null;
        try {
            Class<TelephonyManager> c = TelephonyManager.class;
            Method method = c.getMethod("getDefault");
            method.setAccessible(true);
            obj = method.invoke(c);
        } catch (Exception e) {
            LogUtil.w(" check is network roaming error: " + e);
        }
        boolean isRoaming = false;
        if(obj != null ){
            final TelephonyManager telephonyManager = (TelephonyManager)obj;
            isRoaming = telephonyManager.isNetworkRoaming();
        }
        if(LogUtil.isDebug()){
            LogUtil.d( " network is roaming["+isRoaming+"]");
        }
        sNetworkRoaming = isRoaming;
        return isRoaming;
    }

    public static boolean isActiveNetworkMetered(NetworkInfo info) {
        return info != null && !isNetworkTypeWIFI(info.getType());
    }

    public static boolean isNetworkTypeWIFI(int networkType) {
        return networkType == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Returns maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     *
     * @param context the {@link Context} to use for accessing the {@link android.content.ContentResolver}
     * @return maximum size, in bytes, of downloads that may go over a mobile connection; or null if
     * there's no limit
     */

    public static Long getMaxBytesOverMobile(Context context) {
        if(sMaxBytesOverMobile == null || sMaxBytesOverMobile != -1){
            return sMaxBytesOverMobile;
        }
        try {
            sMaxBytesOverMobile = Settings.Secure.getLong(context.getContentResolver(),
                    DOWNLOAD_MAX_BYTES_OVER_MOBILE);
            LogUtil.i(" max bytes over mobile = " + sMaxBytesOverMobile);
            return sMaxBytesOverMobile;
        } catch (Settings.SettingNotFoundException exc) {
            sMaxBytesOverMobile = null;
            return null;
        }
    }

    /**
     * Returns recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.  The user will have the option to bypass
     * this limit.
     *
     * @param context the {@link Context} to use for accessing the {@link android.content.ContentResolver}
     * @return recommended maximum size, in bytes, of downloads that may go over a mobile
     * connection; or null if there's no recommended limit.
     */
    //modify xym 2014.7.3 add this tag
    public static Long getRecommendedMaxBytesOverMobile(Context context) {
        if(sRecommendedMaxBytesOverMobile == null || sRecommendedMaxBytesOverMobile != -1){
            return sRecommendedMaxBytesOverMobile;
        }
        try {
            sRecommendedMaxBytesOverMobile = Settings.Secure.getLong(context.getContentResolver(),
                    /**Settings.Global.*/DOWNLOAD_RECOMMENDED_MAX_BYTES_OVER_MOBILE);
            LogUtil.i(" recommended max bytes over mobile = " + sRecommendedMaxBytesOverMobile);
            return sRecommendedMaxBytesOverMobile;
        } catch (Settings.SettingNotFoundException exc) {
            sRecommendedMaxBytesOverMobile = null;
            return null;
        }
    }

    public static int checkNetwork(long totalBytes, int allowNetworkTypes, boolean isRoamingAllowed, boolean isAllowMobile2g){
        if(DownloadInitHelper.getInstance().getAppContext() == null){
            LogUtil.e(new DownloadNoInitException(), " check network error! ");
            return NETWORK_NO_INIT_CONTEXT;
        }
        NetworkInfo info = getActiveNetworkInfo(DownloadInitHelper.getInstance().getAppContext());
        return checkNetwork(info, totalBytes, allowNetworkTypes, isRoamingAllowed, isAllowMobile2g);
    }

    public static int checkNetwork(NetworkInfo info, long totalBytes, int allowNetworkTypes,
                                   boolean isRoamingAllowed, boolean isAllowMobile2g){
        if(DownloadInitHelper.getInstance().getAppContext() == null){
            LogUtil.e(new DownloadNoInitException(), " check network error! ");
            return NETWORK_NO_INIT_CONTEXT;
        }
        boolean mAllowMetered = true;
        int result;
        if (info == null || !info.isConnected()) {
            result = NETWORK_NO_CONNECTION;
        }/* else if (NetworkInfo.DetailedState.BLOCKED.equals(info.getDetailedState())) {
            result = NETWORK_BLOCKED;
        } */else if (!isRoamingAllowed && isNetworkRoaming(info)) {
            result = NETWORK_CANNOT_USE_ROAMING;
        } else if (!mAllowMetered && isActiveNetworkMetered(info)) {
            result = NETWORK_TYPE_DISALLOWED_BY_REQUESTOR;
        }else if(!checkIsNetworkTypeAllowed(translateNetworkTypeToApiFlag(info.getType()), allowNetworkTypes)){
            return NETWORK_TYPE_DISALLOWED_BY_REQUESTOR;
        }else {
            // 判断是否允许使用2g网络
            if(!isAllowMobile2g && info.getType() == ConnectivityManager.TYPE_MOBILE){
                TelephonyManager telephonyManager = (TelephonyManager) DownloadInitHelper.getInstance().getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
                int mobileProvider = getMobileProvider(telephonyManager);
                if((mobileProvider == PROVIDER_UNKNOWN || mobileProvider == PROVIDER_CHINA_TELECOM) &&
                        getNetworkClass(telephonyManager.getNetworkType()) == CLASS_2_G){
                    return NETWORK_NOT_ALLOW_MOBILE_2_G;
                }
        }
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                // 检测是否超过网络允许下载的文件大小
                return checkSizeAllowedForNetwork(totalBytes);
            }
            return NETWORK_OK;
            // 检测是否超过网络允许下载的文件大小
//            return checkSizeAllowedForNetwork(totalBytes);
        }
        if(LogUtil.isDebug()){
            LogUtil.d(" check is network type : " + result);
        }
        return result;
    }

    public static int getMobileProvider(TelephonyManager telephonyManager) {
        int provider = PROVIDER_UNKNOWN;
        try {
            String imsi = telephonyManager.getSubscriberId();
            if (imsi == null) {
                if (TelephonyManager.SIM_STATE_READY == telephonyManager.getSimState()) {
                    String operator = telephonyManager.getSimOperator();
                    if (operator != null) {
                        if (operator.equals("46000")
                                || operator.equals("46002")
                                || operator.equals("46007")) {
                            provider = PROVIDER_CHINA_MOBILE;
                        } else if (operator.equals("46001")
                                || operator.equals("46006")) {
                            provider = PROVIDER_CHINA_UNICOM;
                        } else if (operator.equals("46003")
                                || operator.equals("46005")
                                || operator.equals("46011")) {
                            provider = PROVIDER_CHINA_TELECOM;
                        }
                    }
                }
            } else {
                if (imsi.startsWith("46000") || imsi.startsWith("46002")
                        || imsi.startsWith("46007")) {
                    provider = PROVIDER_CHINA_MOBILE;
                } else if (imsi.startsWith("46001")
                        || imsi.startsWith("46006")) {
                    provider = PROVIDER_CHINA_UNICOM;
                } else if (imsi.startsWith("46003")
                        || imsi.startsWith("46005")
                        || imsi.startsWith("46011")) {
                    provider = PROVIDER_CHINA_TELECOM;
                }
            }
        } catch (Exception e) {
            LogUtil.e(e, " get mobile provider error ");
        }
        return provider;
    }

    public static int getNetworkClass(int networkType){
        switch (networkType) {
            case TYPE_GPRS:
            case TYPE_EDGE:
            case TYPE_CDMA:
            case TYPE_1xRTT:
            case TYPE_IDEN:
                return CLASS_2_G;
            case TYPE_UMTS:
            case TYPE_EVDO_0:
            case TYPE_EVDO_A:
            case TYPE_HSDPA:
            case TYPE_HSUPA:
            case TYPE_HSPA:
            case TYPE_EVDO_B:
            case TYPE_EHRPD:
            case TYPE_HSPAP:
                return CLASS_3_G;
            case TYPE_LTE:
                return CLASS_4_G;
            default:
                return CLASS_UNKNOWN;
        }
    }

    /**
     * Check if this download can proceed over the given network type.
     *
     * @param activeNetworkType
     *            a constant from ConnectivityManager.TYPE_*.
     * @param allowNetworkTypes 网络类型
     * @return true is allow
     */
    public static boolean checkIsNetworkTypeAllowed(int activeNetworkType, int allowNetworkTypes){
        if((allowNetworkTypes & activeNetworkType) == 0){
            LogUtil.w(DownloadUtils.formatString(" current network type[%s] disallowed configuration of the network types[%s] ",
                    activeNetworkType, allowNetworkTypes));
            return false;
        }
        return true;
    }

    /**
     * Check if the download's size prohibits it from running over the current
     * network.
     *
     * @return one of the NETWORK_* constants
     */
    public static int checkSizeAllowedForNetwork(long totalBytes) {
        if(DownloadInitHelper.getInstance().getAppContext() == null){
            LogUtil.e(new DownloadNoInitException(), " check network error! ");
            return NETWORK_NO_INIT_CONTEXT;
        }
        Long maxBytesOverMobile = getMaxBytesOverMobile(DownloadInitHelper.getInstance().getAppContext());
        if (maxBytesOverMobile != null && totalBytes > maxBytesOverMobile) {
            return NETWORK_UNUSABLE_DUE_TO_SIZE;
        }
        Long recommendedMaxBytesOverMobile = getRecommendedMaxBytesOverMobile(DownloadInitHelper.getInstance().getAppContext());
        if (recommendedMaxBytesOverMobile != null
                && totalBytes > recommendedMaxBytesOverMobile) {
            return NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE;
        }
        return NETWORK_OK;
    }


    /**
     * Translate a ConnectivityManager.TYPE_* constant to the corresponding
     * DownloadManager.Request.NETWORK_* bit flag.
     */
    public static int translateNetworkTypeToApiFlag(int networkType) {
        switch (networkType) {
            case ConnectivityManager.TYPE_MOBILE:
                return NetworkType.NETWORK_MOBILE;

            case ConnectivityManager.TYPE_WIFI:
                return NetworkType.NETWORK_WIFI;

            case ConnectivityManager.TYPE_BLUETOOTH:
                return NetworkType.NETWORK_BLUETOOTH;

            default:
                return NetworkType.NETWORK_UNKNOWN;
        }
    }

    /**
     * 获取异常网络具体的错误码，但增加异常网络错误码时，
     * 请修改暂停判断条件{@link DownloadUtils#isPauseByNetwork(int, String)}
     * @param flag 网络状态错误标识
     * @return 对应的错误码
     */
    public static String getNetworkErrorCode(int flag){
        String errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_UNKNOWN;
        switch (flag){
            case NetworkUtil.NETWORK_NO_CONNECTION:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_NO_CONNECTION;
                break;
            case NetworkUtil.NETWORK_UNUSABLE_DUE_TO_SIZE:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_UNUSABLE_DUE_TO_SIZE;
                break;
            case NetworkUtil.NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE;
                break;
            case NetworkUtil.NETWORK_CANNOT_USE_ROAMING:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_CANNOT_USE_ROAMING;
                break;
            case NetworkUtil.NETWORK_TYPE_DISALLOWED_BY_REQUESTOR:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_TYPE_DISALLOWED_BY_REQUESTOR;
                break;
            case NetworkUtil.NETWORK_BLOCKED:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_BLOCKED;
                break;
            case NetworkUtil.NETWORK_NO_INIT_CONTEXT:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_NO_INIT_CONTEXT;
                break;
            case NetworkUtil.NETWORK_NOT_ALLOW_MOBILE_2_G:
                errorCode = ErrorCode.Values.DOWNLOAD_NETWORK_NOT_ALLOW_MOBILE_2_G;
                break;
            default:
                break;
        }
        return errorCode;
    }
}
