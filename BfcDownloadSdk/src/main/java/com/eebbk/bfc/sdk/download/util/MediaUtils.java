package com.eebbk.bfc.sdk.download.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.File;

/**
 * 媒体库工具类
 * <p>
 * Created by Simon on 2017/5/23.
 */

public class MediaUtils {

    /**
     * 通知媒体库扫描文件
     */
    public static void scanFile(Context context, String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            return;
        }
        File file = new File(filePath);
        scanFile(context, file);
    }

    /**
     * 通知媒体库扫描文件
     */
    public static void scanFile(Context context, File file) {
        if (context == null || file == null) {
            return;
        }
        LogUtil.i("通知媒体库添加文件 filePath: " + file.getAbsolutePath());
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(file));
        context.sendBroadcast(intent);
    }

    /**
     * 从媒体库中删除文件
     */
    public static void removeFile(Context context, File file) {
        removeFile(context, file.getAbsolutePath());
    }

    /**
     * 从媒体库中删除文件
     */
    public static void removeFile(Context context, String filePath) {
        if (context == null || TextUtils.isEmpty(filePath)){
            return;
        }
        try {
            LogUtil.i("通知媒体库删除文件 filepath: " + filePath);
            Uri uri = MediaStore.Files.getContentUri("external");
            context.getContentResolver().delete(uri, MediaStore.Files.FileColumns.DATA + "=?", new String[]{filePath});
        } catch (Exception e) {
        }
    }

}
