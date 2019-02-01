package com.eebbk.bfc.download.demo.util;

import android.text.TextUtils;

import com.eebbk.bfc.bfclog.BfcLog;
import com.eebbk.bfc.bfclog.inner.BfcLogImpl;

/**
 * 日志记录工具类 可以通过修改是否启用调试模式、日志级别、是否记录日志到文件, 灵活的记录日志。
 * 记录日志到文件需要配置权限：android.permission.WRITE_EXTERNAL_STORAGE
 *
 * @author llp
 */
public class L {

    private static final String TAG = "BfcDownloadDemo";

    private L() {
    }

    private static boolean sDebug = false;
    private static BfcLog sBfcLog;

    public static BfcLog buildLog(boolean isDebug, String savePath) {
        BfcLog bfcLog = new BfcLog.Builder()
            .tag(TAG)
            .showLog(true)
            .logLevel(isDebug ? BfcLog.VERBOSE : BfcLog.INFO)
            //.showThreadInfo(debug)
            .showThreadInfo(false)
            .methodCount(0)
            .methodOffset(0)
            .saveLog(!TextUtils.isEmpty(savePath), savePath)
            .build();
        return bfcLog;
    }

    public static void setLog(BfcLog log, boolean isDebug) {
        sDebug = isDebug;
        sBfcLog = log;
    }

    public static synchronized BfcLog getBfcLog() {
        if (sBfcLog == null) {
            sBfcLog = new BfcLogImpl().tag(TAG).method(0).thread(false);
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
}
