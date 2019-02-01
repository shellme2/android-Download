package com.eebbk.bfc.sdk.download.db.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.C;
import com.eebbk.bfc.sdk.download.TaskParam;
import com.eebbk.bfc.sdk.download.db.DatabaseHelper.ParamsColumns;
import com.eebbk.bfc.sdk.download.db.DownloadTaskColumns;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.ExtrasConverter;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.downloadmanager.CDNManager;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Desc: 下载任务配置实体类
 * Author: llp
 * Create Time: 2016-10-05 18:21
 * Email: jacklulu29@gmail.com
 */

public class TaskParamInfo {

    public String paramsId;
    public int generateId;
    public String url;
    public String fileName;
    public String fileExtension;
    public String savePath;
    public long presetFileSize;
    public boolean autoCheckSize;
    public int priority;
    public String checkType;
    public String checkCode;
    public boolean checkEnable;
    public int networkTypes;
    public boolean needQueue;
    public String reserver;
    public HashMap<String, String> extrasMap;
    public int notificationVisibility;
    public boolean allowAdjustSavePath;
    /**
     * 下载进度回调间隔时间，单位毫秒，
     * 同时会影响到下载速度、剩余时间的统计以及回调显示
     */
    public int minProgressTime;
    public boolean showRealTimeInfo;
    public boolean autoUnpack;
    public String unpackPath;
    public boolean deleteSourceAfterUnpack;
    public boolean deleteNoEndTaskAndCache;
    public boolean deleteEndTaskAndCache;
    public int downloadThreads;

    // add by llp, db version 2
    public String moduleName;

    /**
     * 是否允许漫游
     */
    public boolean allowRoaming;

    public TaskParamInfo() {
        paramsId = null;
        generateId = -1;
        url = null;
        fileName = null;
        fileExtension = null;
        savePath = null;
        presetFileSize = -1;
        autoCheckSize = true;
        priority = 0;
        checkType = ITask.CheckType.NON;
        checkCode = null;
        checkEnable = true;
        networkTypes = NetworkType.DEFAULT_NETWORK;
        needQueue = true;
        reserver = null;
        extrasMap = new HashMap<>();
        notificationVisibility = ITask.Notification.VISIBILITY_HIDDEN;
        allowAdjustSavePath = true;
        minProgressTime = C.DownLoadConfig.DEFAULT_MIN_PROGRESS_TIME;
        showRealTimeInfo = false;
        autoUnpack = false;
        unpackPath = null;
        deleteSourceAfterUnpack = true;
        deleteNoEndTaskAndCache = true;
        deleteEndTaskAndCache = false;
        downloadThreads = C.DownLoadConfig.DEFAULT_DOWNLOAD_THREAD_COUNT;
        allowRoaming = false;

        moduleName = null;
    }

    public TaskParamInfo(@NonNull TaskParam param) {
        paramsId = null;
        generateId = param.getGenerateId();
        url = param.getUrl();
        fileName = param.getFileName();
        fileExtension = param.getFileExtension();
        savePath = param.getSavePath();
        presetFileSize = param.getPresetFileSize();
        autoCheckSize = param.isAutoCheckSize();
        priority = param.getPriority();
        checkType = param.getCheckType();
        checkCode = param.getCheckCode();
        checkEnable = param.isCheckEnable();
        networkTypes = param.getNetworkTypes();
        needQueue = param.isNeedQueue();
        reserver = param.getReserver();
        extrasMap = param.getExtrasMap();
        notificationVisibility = param.getNotificationVisibility();
        allowAdjustSavePath = param.hasAllowAdjustSavePath();
        minProgressTime = param.getMinProgressTime();
        showRealTimeInfo = param.isShowRealTimeInfo();
        autoUnpack = param.isAutoUnpack();
        unpackPath = param.getUnpackPath();
        deleteSourceAfterUnpack = param.isDeleteSourceAfterUnpack();
        deleteNoEndTaskAndCache = param.isDeleteNoEndTaskAndCache();
        deleteEndTaskAndCache = param.isDeleteEndTaskAndCache();
        downloadThreads = param.getDownloadThreads();
        allowRoaming = false;

        moduleName = param.getModuleName();
    }

    public TaskParam copyDataToTaskParam(@NonNull TaskParam taskParam) {
        taskParam
            .setGenerateId(generateId)
            .setInnerCopyUrl(url)
            .setFileName(fileName)
            .setFileExtension(fileExtension)
            .setSavePath(savePath)
            .setPresetFileSize(presetFileSize)
            .setAutoCheckSize(autoCheckSize)
            .setPriority(priority)
            .setCheckType(checkType)
            .setCheckCode(checkCode)
            .setCheckEnable(checkEnable)
            .setNetworkTypes(networkTypes)
            .setNeedQueue(needQueue)
            .setReserver(reserver)
            .setExtrasMap(extrasMap)
            .setNotificationVisibility(notificationVisibility)
            .setAllowAdjustSavePath(allowAdjustSavePath)
            .setMinProgressTime(minProgressTime)
            .setShowRealTimeInfo(showRealTimeInfo)
            .setAutoUnpack(autoUnpack)
            .setUnpackPath(unpackPath)
            .setDeleteSourceAfterUnpack(deleteSourceAfterUnpack)
            .setDeleteNoEndTaskAndCache(deleteNoEndTaskAndCache)
            .setDeleteEndTaskAndCache(deleteEndTaskAndCache)
            .setDownloadThreads(downloadThreads)
            .setModuleName(moduleName);
        return taskParam;
    }

    public void bind(Cursor cursor) {
        paramsId = cursor.getString(cursor.getColumnIndex(ParamsColumns._ID));
        generateId = cursor.getInt(cursor.getColumnIndex(ParamsColumns.GENERATE_ID));
        url = cursor.getString(cursor.getColumnIndex(ParamsColumns.URL));
        fileName = cursor.getString(cursor.getColumnIndex(ParamsColumns.FILE_NAME));
        fileExtension = cursor.getString(cursor.getColumnIndex(ParamsColumns.FILE_EXTENSION));
        savePath = cursor.getString(cursor.getColumnIndex(ParamsColumns.SAVE_PATH));
        presetFileSize = cursor.getLong(cursor.getColumnIndex(ParamsColumns.PRESET_FILE_SIZE));
        autoCheckSize = cursor.getInt(cursor.getColumnIndex(ParamsColumns.AUTO_CHECK_SIZE)) == 1;
        priority = cursor.getInt(cursor.getColumnIndex(ParamsColumns.PRIORITY));
        checkType = cursor.getString(cursor.getColumnIndex(ParamsColumns.CHECK_TYPE));
        checkCode = cursor.getString(cursor.getColumnIndex(ParamsColumns.CHECK_CODE));
        checkEnable = cursor.getInt(cursor.getColumnIndex(ParamsColumns.CHECK_ENABLE)) == 1;
        networkTypes = cursor.getInt(cursor.getColumnIndex(ParamsColumns.NETWORK_TYPES));
        needQueue = cursor.getInt(cursor.getColumnIndex(ParamsColumns.NEED_QUEUE)) == 1;
        reserver = cursor.getString(cursor.getColumnIndex(ParamsColumns.RESERVER));
        extrasMap = decodeExtrasMap(cursor.getString(cursor.getColumnIndex(ParamsColumns.EXTRAS_MAP)));
        notificationVisibility = cursor.getInt(cursor.getColumnIndex(ParamsColumns.NOTIFICATION_VISIBILITY));
        allowAdjustSavePath = cursor.getInt(cursor.getColumnIndex(ParamsColumns.ALLOW_ADJUST_SAVE_PATH)) == 1;
        minProgressTime = cursor.getInt(cursor.getColumnIndex(ParamsColumns.MIN_PROGRESS_TIME));
        showRealTimeInfo = cursor.getInt(cursor.getColumnIndex(ParamsColumns.SHOW_REAL_TIME_INFO)) == 1;
        autoUnpack = cursor.getInt(cursor.getColumnIndex(ParamsColumns.AUTO_UNPACK)) == 1;
        unpackPath = cursor.getString(cursor.getColumnIndex(ParamsColumns.UNPACK_PATH));
        deleteSourceAfterUnpack = cursor.getInt(cursor.getColumnIndex(ParamsColumns.DELETE_SOURCE_AFTER_UNPACK)) == 1;
        deleteNoEndTaskAndCache = cursor.getInt(cursor.getColumnIndex(ParamsColumns.DELETE_NO_END_TASK_AND_CACHE)) == 1;
        deleteEndTaskAndCache = cursor.getInt(cursor.getColumnIndex(ParamsColumns.DELETE_END_TASK_AND_CACHE)) == 1;
        downloadThreads = cursor.getInt(cursor.getColumnIndex(ParamsColumns.DOWNLOAD_THREADS));
        allowRoaming = false;
        // add by llp, db version 2
        moduleName = cursor.getString(cursor.getColumnIndex(ParamsColumns.MODULE_NAME));
    }

    public void bindFromDownloadTask(Cursor cursor) {
        paramsId = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.PARAM_ID));
        generateId = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.GENERATE_ID));
        url = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.URL));
        //此处是从db取出的url,如果url是星域的url则进行再次转换避免端口不同导致下载失败问题
        if (!TextUtils.isEmpty(url) && CDNManager.isXYVodUrl(url)) {
            url = CDNManager.url_REWRITE(CDNManager.transformSourceUrl(url));
        }
        fileName = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.FILE_NAME));
        fileExtension = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.FILE_EXTENSION));
        savePath = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.SAVE_PATH));
        presetFileSize = cursor.getLong(cursor.getColumnIndex(DownloadTaskColumns.PRESET_FILE_SIZE));
        autoCheckSize = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.AUTO_CHECK_SIZE)) == 1;
        priority = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.PRIORITY));
        checkType = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.CHECK_TYPE));
        checkCode = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.CHECK_CODE));
        checkEnable = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.CHECK_ENABLE)) == 1;
        networkTypes = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.NETWORK_TYPES));
        needQueue = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.NEED_QUEUE)) == 1;
        reserver = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.RESERVER));
        extrasMap = decodeExtrasMap(cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.EXTRAS_MAP)));
        notificationVisibility = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.NOTIFICATION_VISIBILITY));
        allowAdjustSavePath = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.ALLOW_ADJUST_SAVE_PATH)) == 1;
        minProgressTime = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.MIN_PROGRESS_TIME));
        showRealTimeInfo = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.SHOW_REAL_TIME_INFO)) == 1;
        autoUnpack = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.AUTO_UNPACK)) == 1;
        unpackPath = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.UNPACK_PATH));
        deleteSourceAfterUnpack = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.DELETE_SOURCE_AFTER_UNPACK)) == 1;
        deleteNoEndTaskAndCache = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.DELETE_NO_END_TASK_AND_CACHE)) == 1;
        deleteEndTaskAndCache = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.DELETE_END_TASK_AND_CACHE)) == 1;
        downloadThreads = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.DOWNLOAD_THREADS));
        allowRoaming = false;
        // add by llp, db version 2
        moduleName = cursor.getString(cursor.getColumnIndex(DownloadTaskColumns.MODULE_NAME));
    }

    public ContentValues toContentValues() {
        final ContentValues values = new ContentValues();
        values.put(ParamsColumns.GENERATE_ID, generateId);
        values.put(ParamsColumns.URL, url);
        values.put(ParamsColumns.FILE_NAME, fileName);
        values.put(ParamsColumns.FILE_EXTENSION, fileExtension);
        values.put(ParamsColumns.SAVE_PATH, savePath);
        values.put(ParamsColumns.PRESET_FILE_SIZE, presetFileSize);
        values.put(ParamsColumns.AUTO_CHECK_SIZE, autoCheckSize ? 1 : 0);
        values.put(ParamsColumns.PRIORITY, priority);
        values.put(ParamsColumns.CHECK_TYPE, checkType);
        values.put(ParamsColumns.CHECK_CODE, checkCode);
        values.put(ParamsColumns.CHECK_ENABLE, checkEnable ? 1 : 0);
        values.put(ParamsColumns.NETWORK_TYPES, networkTypes);
        values.put(ParamsColumns.NEED_QUEUE, needQueue);
        values.put(ParamsColumns.RESERVER, reserver);
        values.put(ParamsColumns.EXTRAS_MAP, encodeExtrasMap());
        values.put(ParamsColumns.NOTIFICATION_VISIBILITY, notificationVisibility);
        values.put(ParamsColumns.ALLOW_ADJUST_SAVE_PATH, allowAdjustSavePath ? 1 : 0);
        values.put(ParamsColumns.MIN_PROGRESS_TIME, minProgressTime);
        values.put(ParamsColumns.SHOW_REAL_TIME_INFO, showRealTimeInfo ? 1 : 0);
        values.put(ParamsColumns.AUTO_UNPACK, autoUnpack ? 1 : 0);
        values.put(ParamsColumns.UNPACK_PATH, unpackPath);
        values.put(ParamsColumns.DELETE_SOURCE_AFTER_UNPACK, deleteSourceAfterUnpack ? 1 : 0);
        values.put(ParamsColumns.DELETE_NO_END_TASK_AND_CACHE, deleteNoEndTaskAndCache ? 1 : 0);
        values.put(ParamsColumns.DELETE_END_TASK_AND_CACHE, deleteEndTaskAndCache ? 1 : 0);
        values.put(ParamsColumns.DOWNLOAD_THREADS, downloadThreads);
        values.put(ParamsColumns.DOWNLOAD_ALLOW_ROAMING, allowRoaming ? 1 : 0);

        // add by llp, db version 2
        values.put(ParamsColumns.MODULE_NAME, moduleName);
        return values;
    }

    public TaskParamInfo setGenerateId(int generateId) {
        this.generateId = generateId;
        return this;
    }

    /**
     * 是否需要校验文件，将根据配置的参数以及校验开关决定
     *
     * @return true需要，false不需要
     */
    public boolean needCheckFile() {
        return DownloadUtils.needCheckFile(checkEnable, checkType, checkCode);
    }

    private String getExtrasMapString() {
        if (extrasMap == null || extrasMap.isEmpty()) {
            return "";
        }
        StringBuilder str = new StringBuilder();
        Iterator<?> iter = extrasMap.entrySet().iterator();
        str.append("(");
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            str.append(key);
            str.append(":");
            str.append(val);
            if (iter.hasNext()) {
                str.append(",");
            }
        }
        str.append(")");

        return str.toString();
    }

    private String encodeExtrasMap() {
        String result = "";
        if (extrasMap == null || extrasMap.isEmpty()) {
            return result;
        }
        try {
            result = ExtrasConverter.encode(extrasMap);
        } catch (UnsupportedEncodingException e) {
            LogUtil.e(e, " ExtrasConverter encode error! ");
        }
        return result;
    }

    private HashMap<String, String> decodeExtrasMap(String encodeStr) {
        HashMap<String, String> extrasMap = new HashMap<>();
        try {
            extrasMap = ExtrasConverter.decode(encodeStr);
        } catch (UnsupportedEncodingException e) {
            LogUtil.e(e, " ExtrasConverter decode error! ");
        }
        return extrasMap;
    }

    /*protected String extrasToString(){
        StringBuilder str = new StringBuilder();
        Iterator<?> iter = extrasMap.entrySet().iterator();
        str.append("(");
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            String key = (String) entry.getKey();
            String val = (String) entry.getValue();
            str.append(key);
            str.append(":");
            str.append(val);
            if(iter.hasNext()){
                str.append(",");
            }
        }
        str.append(")");

        return str.toString();
    }*/

    @Override
    public String toString() {
        return "TaskParamInfo{" +
            "generateId=" + generateId +
            ", url='" + url + '\'' +
            ", fileName='" + fileName + '\'' +
            ", fileExtension='" + fileExtension + '\'' +
            ", savePath='" + savePath + '\'' +
            ", autoCheckSize=" + autoCheckSize +
            ", checkType='" + checkType + '\'' +
            ", checkEnable=" + checkEnable +
            ", networkTypes=" + networkTypes +
            ", extrasMap=" + extrasMap +
            ", autoUnpack=" + autoUnpack +
            ", unpackPath='" + unpackPath + '\'' +
            ", deleteSourceAfterUnpack=" + deleteSourceAfterUnpack +
            ", moduleName='" + moduleName + '\'' +
            '}';
    }
}
