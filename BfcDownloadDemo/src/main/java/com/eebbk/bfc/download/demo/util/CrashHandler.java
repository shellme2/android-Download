package com.eebbk.bfc.download.demo.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * UncaughtException处理类,当程序发生Uncaught异常的时候,由该类来接管程序,并记录发送错误报告.
 *
 * @author way
 */
public class CrashHandler implements UncaughtExceptionHandler {
    private Context mContext;
    private UncaughtExceptionHandler mDefaultHandler;            // 系统默认的UncaughtException处理类
    private static CrashHandler INSTANCE = new CrashHandler();            // CrashHandler实例
    private Map<String, String> info = new HashMap<>();    // 用来存储设备信息和异常信息

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {

    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     * @param context 上下文，ApplicationContext
     */
    public void init(Context context) {
        mContext = context.getApplicationContext();
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// 获取系统默认的UncaughtException处理器
        Thread.setDefaultUncaughtExceptionHandler(this);// 设置该CrashHandler为程序的默认处理器
    }

    /**
     * 当UncaughtException发生时会转入该重写的方法来处理
     */
    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        if (mDefaultHandler != null) {
            handleException(ex);// 如果自定义的没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, ex);// 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
        }
    }

    /**
     * 自定义错误处理,收集错误信息 发送错误报告等操作均在此完成.
     *
     * @param ex 异常信息 <a
     *           href="\"http://www.eoeandroid.com/home.php?mod=space&uid=7300\""
     *           target="\"_blank\"">@return</a> true 如果处理了该异常信息;否则返回false.
     */
    public boolean handleException(final Throwable ex) {
        if (ex == null) return false;
        L.e(ex, " uncaught exception !");
        // 收集设备参数信息
        collectDeviceInfo(mContext);
        // 保存日志文件
        final String exString = getCrashInfoString(ex);
        saveCrashInfo2File(exString, mContext);
        return true;
    }

    /**
     * 收集设备参数信息
     * @param context 上下文，ApplicationContext
     */
    public void collectDeviceInfo(Context context) {
        try {
            PackageManager pm = context.getApplicationContext().getPackageManager();// 获得包管理器
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_ACTIVITIES);// 得到该应用的信息，即主Activity
            if (pi != null) {
                String versionName = pi.versionName == null ? "null" : pi.versionName;
                String versionCode = pi.versionCode + "";
                info.put("versionName", versionName);
                info.put("versionCode", versionCode);
            }
        } catch (NameNotFoundException e) {
            L.e("Error: " + e);
        }

        Field[] fields = Build.class.getDeclaredFields();// 反射机制
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                info.put(field.getName(), field.get("").toString());
            } catch (IllegalArgumentException e) {
                L.e("Error: " + e);
            } catch (IllegalAccessException e) {
                L.e("Error: " + e);
            }
        }
    }

    private String getCrashInfoString(Throwable ex) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : info.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            sb.append(key + "=" + value + "\r\n");
        }
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
        // 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// 记得关闭
        String result = writer.toString();
        sb.append(result);
        return sb.toString();
    }

    @SuppressLint("SimpleDateFormat")
    public static String saveCrashInfo2File(String exString, Context context) {
        long timetamp = System.currentTimeMillis();
        String time = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
        String fileName = "-crash-" + time + "-" + timetamp + ".log";
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            FileOutputStream fos = null;
            try {
                //File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "SynChinese");
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + ".crash" + File.separator + context.getPackageName());
                if ((dir.exists() && dir.isDirectory()) || dir.mkdirs()) {
                    File outfile = new File(dir, fileName);
                    fos = new FileOutputStream(outfile);
                    fos.write(exString.getBytes("utf-8"));
                    return outfile.getAbsolutePath();
                } else {
                    L.e(" create crash dir failed, exists: " + dir.exists());
                }
            } catch (FileNotFoundException e) {
                L.e(e, " file not found exception ");
            } catch (IOException e) {
                L.e(e, " create dir io exception ");
            } finally {
                if(fos != null){
                    try {
                        fos.close();
                    } catch (IOException e) {
                        L.e(e, " close output stream error: ");
                    }
                }
            }
        }
        return null;
    }
}
