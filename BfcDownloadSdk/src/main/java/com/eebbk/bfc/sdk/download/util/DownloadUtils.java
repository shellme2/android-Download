package com.eebbk.bfc.sdk.download.util;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.StatFs;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Formatter;
import java.util.Locale;

/**
 * Desc: 下载工具类
 * Author: llp
 * Create Time: 2016-10-08 19:53
 * Email: jacklulu29@gmail.com
 */

public class DownloadUtils {

    public final static int GB = 1024 * 1024 * 1024;
    public final static long MB = 1024 * 1024L;
    public final static int KB = 1024;

    private DownloadUtils(){
        // private construct
    }

    /**
     * @param url  The downloading URL.
     * @param path The absolute file path.
     * @return The download id.
     */
    public static int generateId(final String url, final String path) {
        return generateId(url, path, false);
    }

    /**
     * @param url  The downloading URL.
     * @param path If {@code pathAsDirectory} is {@code true}, {@code path} would be the absolute
     *             directory to place the file;
     *             If {@code pathAsDirectory} is {@code false}, {@code path} would be the absolute
     *             file path.
     * @return The download id.
     */
    public static int generateId(final String url, final String path, final boolean pathAsDirectory) {
        if (pathAsDirectory) {
            return md5(formatString("%sp%s@dir", url, path)).hashCode();
        } else {
            return md5(formatString("%sp%s", url, path)).hashCode();
        }
    }

    private static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 should be supported?", e);
        }

        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();
    }

    public static String formatString(final String msg, Object... args) {
        try {
            return String.format(Locale.ENGLISH, msg, args);
        } catch (Exception e){
            if(LogUtil.isDebug()){
                throw  e;
            } else {
                LogUtil.w(e, " format str error ");
                return msg;
            }
        }
    }

    public static boolean checkPermission(Context context, String permission){
        final int perm = context.checkCallingOrSelfPermission(permission);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * @param targetPath The target path for the download task.
     * @return The temp path is {@code targetPath} in downloading status; The temp path is used for
     * storing the file not completed downloaded yet.
     */
    public static String getTempPath(final String targetPath, final String moduleName) {
        return formatString("%s_%s.downloading", targetPath, moduleName);
    }

    /**
     * @param path            If {@code pathAsDirectory} is true, the {@code path} would be the
     *                        absolute directory to settle down the file;
     *                        If {@code pathAsDirectory} is false, the {@code path} would be the
     *                        absolute file path.
     * @param pathAsDirectory whether the {@code path} is a directory.
     * @param filename        the file's name.
     * @param fileExtension   the file's extension
     * @return the absolute path of the file. If can't find by params, will return {@code null}.
     */
    public static String getTargetFilePath(String path, boolean pathAsDirectory, String filename, String fileExtension) {
        if (path == null) {
            return null;
        }

        if (pathAsDirectory) {
            if (filename == null) {
                return null;
            }
            String filePath = generateFilePath(path, filename);
            /*if(fileExtension != null && fileExtension.length() > 0){
                int dotIndex = filePath.lastIndexOf('.');
                boolean missingExtension = dotIndex < 0 || dotIndex < filePath.lastIndexOf('/');
                if(missingExtension){
                    filePath = filePath + "." + fileExtension;
                } else {
                    filePath = filePath.substring(0, dotIndex) + "." + fileExtension;
                }
            }*/
            return filePath;
        } else {
            return path;
        }
    }

    /**
     * @see #getTargetFilePath(String, boolean, String, String)
     */
    public static String generateFilePath(String directory, String filename) {
        if (filename == null) {
            throw new IllegalStateException("can't generate real path, the file name is null");
        }

        if (directory == null) {
            throw new IllegalStateException("can't generate real path, the directory is null");
        }

        return formatString("%s%s%s", directory, File.separator, filename);
    }

    public static String generateFileName(final String url) {
        return md5(url);
    }

    public static long getFreeSpaceBytes(final String path) {
        long freeSpaceBytes;
        final StatFs statFs = new StatFs(path);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            freeSpaceBytes = statFs.getAvailableBytes();
        } else {
            //noinspection deprecation
            freeSpaceBytes = statFs.getAvailableBlocks() * (long) statFs.getBlockSize();
        }

        return freeSpaceBytes;
    }

    /**
     * 获取速度显示字符
     *
     * @param speed 速度
     * @return 经过转换的速度显示字符串
     */
    public static String getSpeedString(long speed, String defaultStr, int bigDecimal) {
        final int memoryMeasure = 1024;
        if (speed < 0){
            return defaultStr;
        }
        String speedString = "";
        if (speed < memoryMeasure) {
            speedString = speed + "B/s";
        } else if (memoryMeasure <= speed && speed <= memoryMeasure * memoryMeasure) {
            speedString = (formatNumberStr(speed / (double)memoryMeasure, bigDecimal)) + "KB/s";
        } else {
            speedString = (formatNumberStr(speed /  (double)memoryMeasure / (double)memoryMeasure , bigDecimal)) + "MB/s";
        }
        return speedString;
    }

    /**
     * 转换显示剩余时间，格式： 小时：分钟：秒
     *
     * @param totalSeconds 总时间
     * @return 时间字符串
     */
    public static String formatLastTime(long totalSeconds, String defaultStr) {
        if (totalSeconds < 0) {
            return defaultStr;
        }

        StringBuilder formatBuilder = new StringBuilder();
        @SuppressWarnings("resource")
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        final int secondMeasure = 60;
        final int hourMeasure = 24;
        long seconds = totalSeconds % secondMeasure;
        long minutes = (totalSeconds / secondMeasure) % secondMeasure;
        long hours = totalSeconds / (secondMeasure*secondMeasure);
        formatBuilder.setLength(0);

        if (hours > 0 && hours < (hourMeasure - 1)) {
            return formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString();
        } else if (hours >= (hourMeasure - 1)) {
            return formatter.format("%d:%02d:%02d", hourMeasure - 1, minutes, seconds).toString();
        } else {
            return formatter.format("%02d:%02d", minutes, seconds).toString();
        }
    }

    /**
     * 转换显示进度,比如98.00
     *
     * @param total 总大小
     * @param finished 已完成大小
     * @param maxValues 进度最大值，此值 < 0 将不进行检测
     * @param bigDecimal     保留位数
     * @return 经过友好转换的进度显示
     */
    public static String formatProgress(double total, double finished, double maxValues, int bigDecimal){
        double progress = 0;
        if(total != 0){
            progress = (finished / total ) * 100;
        }
        if(maxValues > 0){
            if(progress > maxValues){
                progress = maxValues;
            }
        }
        return formatNumberStr(progress, 2);
    }

    /**
     * Convert app size unit from byte to "xx MB" or "xx GB" string.
     *
     * @param size Size in byte.
     * @return Formated string.
     */
    public static String formatFileSize(long size) {
        String unit;
        if (size >= GB) {
            unit = String.format(Locale.US, "%.02f GB", (float)size / (float)GB);
        } else if (size >= MB && size < GB) {
            unit = String.format(Locale.US, "%.02f MB", (float)size / (float)MB);
        } else {
            unit = String.format(Locale.US, "%.02f KB", (float)size / (float)KB);
        }

        return unit;
    }

    /**
     * 对数值进行转换，保留小数点后bigDecimal位的数字，不足则补0
     *
     * @param number 数值
     * @param bigDecimal 保留位数
     * @return 转换过后的值
     */
    private static String formatNumberStr(double number, int bigDecimal) {
        if(bigDecimal < 0){
            bigDecimal = 0;
        }
        return String.format(Locale.US, "%.0"+bigDecimal+"f", number);
    }

    /**
     * 是否需要校验文件，将根据配置的参数以及校验开关决定
     *
     * @return true需要，false不需要
     */
    public static boolean needCheckFile(boolean checkEnable, String checkType, String checkCode){
        return checkEnable
                && !TextUtils.isEmpty(checkType)
                && !ITask.CheckType.NON.equals(checkType)
                && !TextUtils.isEmpty(checkCode);
    }

    public static boolean isTaskFinished(int state, boolean checkEnable, String checkType, String checkCode, boolean isAutoUnpack){
        if(state == Status.UNPACK_SUCCESS
                || state == Status.DOWNLOAD_FAILURE
                || state == Status.CHECK_FAILURE
                || state == Status.UNPACK_FAILURE){
            return true;
        }else if(state == Status.DOWNLOAD_SUCCESS){
            if(!needCheckFile(checkEnable, checkType, checkCode) && !isAutoUnpack){
                return true;
            }
        } else if(state == Status.CHECK_SUCCESS){
            if(!isAutoUnpack){
                return true;
            }
        }
        return false;
    }

    public static boolean isPauseByUser(int status, String errorCode){
        return status == Status.DOWNLOAD_PAUSE && ErrorCode.Values.DOWNLOAD_PAUSE_BY_USER.equals(errorCode);
    }

    public static boolean isPauseByOutOfSpace(int status, String errorCode){
        return status == Status.DOWNLOAD_PAUSE && ErrorCode.Values.DOWNLOAD_OUT_OF_SPACE.equals(errorCode);
    }

    /**
     * 判断是否因为网络原因造成暂停，详细可以查看{@link NetworkUtil#getNetworkErrorCode(int)}
     * @param status 下载状态
     * @param errorCode 错误码
     * @return true表示因为网络原因暂停
     */
    public static boolean isPauseByNetwork(int status, String errorCode){
        return status == Status.DOWNLOAD_PAUSE &&
                (ErrorCode.Values.DOWNLOAD_NETWORK_UNKNOWN.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_NO_CONNECTION.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_UNUSABLE_DUE_TO_SIZE.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_CANNOT_USE_ROAMING.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_TYPE_DISALLOWED_BY_REQUESTOR.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_BLOCKED.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_NO_INIT_CONTEXT.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_REQUEST_NO_ADDRESS.equals(errorCode)
                        || ErrorCode.Values.DOWNLOAD_NETWORK_REQUEST_NO_ROUTE_TO_HOST.equals(errorCode)
                );
    }

    /**
     * 获取域名
     * @param url url
     * @return 域名
     */
    public static String getDomain(String url){
        String domain = "";
        try {
            domain = new URL(url).getHost();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return domain;
    }
}
