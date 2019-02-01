package com.eebbk.bfc.sdk.download.util;

import android.text.TextUtils;

import com.eebbk.bfc.bfclog.BfcLog;
import com.eebbk.bfc.sdk.downloadmanager.SDKVersion;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * 日志记录工具类 可以通过修改是否启用调试模式、日志级别、是否记录日志到文件, 灵活的记录日志。
 * 记录日志到文件需要配置权限：android.permission.WRITE_EXTERNAL_STORAGE
 *
 * @author llp
 */
public class LogUtil {

    private static final String TAG = SDKVersion.getLibraryName();

    private LogUtil() {
        // private construct
    }

    private static boolean sDebug = false;
    private static BfcLog sBfcLog;
    private static boolean sShowThreadInfo = false;

    /**
     * 创建BfcLog日志记录器
     *
     * @param isDebug  是否调试模式
     * @param savePath 日志保存地址，设置为null不保存，如果保存到sd卡，必须声明读写权限
     * @return 日志记录器
     */
    public static BfcLog buildLog(boolean isDebug, String savePath, String tagSuffix) {
        BfcLog bfcLog = new BfcLog.Builder()
            .tag(TextUtils.isEmpty(tagSuffix) ? TAG : TAG + tagSuffix)
            .showLog(true)
            .logLevel(isDebug ? BfcLog.VERBOSE : BfcLog.INFO)
            //.showThreadInfo(debug)
            .showThreadInfo(sShowThreadInfo)
            .methodCount(isDebug ? 1 : 0)
            .methodOffset(1)
            .saveLog(isDebug && !TextUtils.isEmpty(savePath), savePath)
            .build();
        return bfcLog;
    }

    /**
     * 设置日志记录器
     *
     * @param log     日志对象
     * @param isDebug 是否调试模式
     */
    public static void setLog(BfcLog log, boolean isDebug) {
        sDebug = isDebug;
        sBfcLog = log;
    }

    private static synchronized BfcLog getBfcLog() {
        if (sBfcLog == null) {
            sBfcLog = new BfcLog.Builder()
                .tag(TAG)
                .showLog(true)
                .logLevel(BfcLog.VERBOSE)
                .showThreadInfo(sShowThreadInfo)
                .methodCount(0)
                .build();
        }
        return sBfcLog;
    }

    public static boolean isDebug() {
        return sDebug;
    }

    public static void v(String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().v(combineLogMsg(msg));
        }
    }

    public static void d(String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().d(combineLogMsg(msg));
        }
    }

    public static void i(String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().i(combineLogMsg(msg));
        }
    }

    public static void w(String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().w(combineLogMsg(msg));
        }
    }

    public static void w(Throwable tr, String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().w(tr, combineLogMsg(msg));
        }
    }

    public static void e(String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().e(combineLogMsg(msg));
        }
    }

    public static void e(Throwable tr, String... msg) {
        if (getBfcLog() != null) {
            getBfcLog().e(tr, combineLogMsg(msg));
        }
    }

    private static String combineLogMsg(String... msg) {
        if (null == msg)
            return null;

        StringBuilder sb = new StringBuilder();
        for (String s : msg) {
            sb.append(s);
            sb.append(" ");
        }
        return sb.toString();

    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

}
