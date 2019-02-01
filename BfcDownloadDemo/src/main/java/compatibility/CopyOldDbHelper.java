package compatibility;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.db.DatabaseHelper;
import com.eebbk.bfc.sdk.download.db.DatabaseWrapper;
import com.eebbk.bfc.sdk.download.db.DownloadContentProvider;
import com.eebbk.bfc.sdk.download.db.DownloadProviderConfig;
import com.eebbk.bfc.sdk.download.db.data.TaskParamInfo;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.CloseableUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.ExtrasConverter;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * <p>兼容旧版数据帮助类</p>
 * <p>将旧版数据复制到新版数据库中</p>
 * <pre>
 *  使用方式：
 *     String applicationId = BuildConfig.APPLICATION_ID;
 *     CopyOldDbHelper helper = new CopyOldDbHelper(this);
 *     helper.asynCopyData(applicationId);
 *  注意：BuildConfig为应用主module的所有的
 * </pre>
 *
 * Created by Administrator on 2017/2/7.
 */
public class CopyOldDbHelper {

    private Context mAppContext;
    private DownloadProviderConfig mConfig;
    private DatabaseHelper mDatabaseHelper;

    public CopyOldDbHelper(Context context) {
        mAppContext = context.getApplicationContext();
        this.mConfig = DownloadInitHelper.getInstance().getProviderConfig();
        this.mDatabaseHelper = DatabaseHelper.getInstance(this.mAppContext);
    }

    public void asynCopyData(final String applicationId){
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                copyData(applicationId);
                mAppContext = null;
                mConfig = null;
                mDatabaseHelper = null;
            }
        });
        thread.start();
    }

    public void copyData(String moduleName){
        LogUtil.i(" start copy old bfc data to new bfc! ");
        Cursor cursor = queryOldData();
        if(cursor == null){
            LogUtil.w(" query old data cursor is null! ");
            return;
        }
        try {
            if(cursor.getCount() < 1){
                LogUtil.i(" query old data count=0 ");
                return;
            }
            if(cursor.moveToFirst()){
                TaskParamInfo taskParamInfo = null;
                TaskStateInfo taskStateInfo = null;
                do {
                    taskParamInfo = getTaskParamInfo(cursor, moduleName);
                    if(taskParamInfo == null){
                        continue;
                    }
                    if(findInNewDb(taskParamInfo)){
                        LogUtil.w(" task[" + taskParamInfo + "] is already in new db!");
                        continue;
                    }
                    taskStateInfo = getTaskStateInfo(cursor);
                    int index = insertDataToNewDb(taskParamInfo, taskStateInfo);
                    if(index > 0){
                        LogUtil.i(" insert data["+taskParamInfo+"] to new db success! ");
                    } else {
                        LogUtil.e(" insert data["+taskParamInfo+"] to new db failed ");
                    }
                } while (cursor.moveToNext());
            } else {
                LogUtil.w(" query old data move to first false! ");
            }
        } finally {
            CloseableUtil.close(cursor);
        }
    }

    public TaskParamInfo getTaskParamInfo(Cursor cursor, String moduleName){
        TaskParamInfo info = new TaskParamInfo();
        //int priority = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_PRIORITY));
        String url = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_URI));
        String filename = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_TITLE));
        int notificationVisibility = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_VISIBILITY));
        boolean allowAdjustSavePath = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_ALLOW_ADJUST_SAVEPATH)) != 0;
        boolean needQueue = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_NEED_QUEUE)) != 0;
        String md5 = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_MD5));
        String fileExtension = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_FILE_EXTENSION));
        String strExtras = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_EXTRAS));

        HashMap<String, String> extras = null;
        try {
            extras = ExtrasConverter.decode(strExtras);
        } catch (UnsupportedEncodingException e) {
            LogUtil.e(e, "decode extras error");
        }

        String uri = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_FILE_NAME_HINT));
        //uri = DownloadManagerPro.decodeUTF8(uri);
        String savePath = Uri.parse(uri).getPath();
        if (!TextUtils.isEmpty(fileExtension) && !savePath.endsWith(fileExtension)) {
            if (!fileExtension.startsWith(".")) {
                savePath += ".";
            }
            savePath += fileExtension;
        }

        String reserver = cursor.getString(cursor.getColumnIndex(Impl.COLUMN_RESERVE));
        //int networkTypes = cursor.getInt(cursor.getColumnIndex(Impl.COLUMN_ALLOWED_NETWORK_TYPES));

        if(TextUtils.isEmpty(url) || TextUtils.isEmpty(savePath)){
            LogUtil.w(" create taskParam error, url:[" + url + "] savePath:[" + savePath + "]");
            return null;
        }

        info.generateId = DownloadUtils.generateId(url, savePath);
        info.url = url;
        info.savePath = savePath;
        info.fileName = filename;
        info.fileExtension = fileExtension;
        info.checkType = ITask.CheckType.MD5;
        info.checkCode = md5;
        info.extrasMap = extras;
        info.reserver = reserver;
        info.networkTypes = NetworkType.DEFAULT_NETWORK;//networkTypes;
        info.notificationVisibility = notificationVisibility;
        info.allowAdjustSavePath = allowAdjustSavePath;
        info.needQueue = needQueue;
        info.moduleName = moduleName;

        return info;
    }

    public TaskStateInfo getTaskStateInfo(Cursor cursor){
        TaskStateInfo info = new TaskStateInfo();

        long fileSize = cursor.getLong(cursor.getColumnIndex("total_size"));
        if (-1 == fileSize) {
            fileSize = cursor.getLong(cursor.getColumnIndex(Impl.COLUMN_FILE_SIZE));
        }
        long loadedSize = cursor.getLong(cursor.getColumnIndex("bytes_so_far"));

        info.state = Status.DOWNLOAD_SUCCESS;
        info.totalSize = fileSize;
        info.finishSize = loadedSize;
        info.buildTime = cursor.getLong(cursor.getColumnIndex("last_modified_timestamp"));
        info.downloadFinishTime = System.currentTimeMillis();

        return info;
    }

    public boolean findInNewDb(TaskParamInfo taskParamInfo){
        final ContentResolver resolver = this.mAppContext.getContentResolver();
        final Cursor cursor = resolver.query(DownloadContentProvider.buildTaskUri(mConfig.taskUri, String.valueOf(taskParamInfo.generateId)),null,null,null,null);
        if(cursor == null){
            LogUtil.w(" find task but cursor is null! ");
            return false;
        }
        try {
            if(cursor.getCount() < 1){
                return false;
            }
            if(cursor.moveToFirst()){
                return true;
            }
            return false;
        } finally {
            CloseableUtil.close(cursor);
        }
    }

    public Cursor queryOldData(){
        ContentResolver resolver = mAppContext.getContentResolver();
        Uri uri = Uri.parse("content://bbk_downloads/my_downloads");
        String[] projection = new String[] {
                Impl._ID,
                Impl._DATA + " AS " + "local_filename",
                Impl.COLUMN_MEDIAPROVIDER_URI,
                Impl.COLUMN_DESTINATION,
                Impl.COLUMN_TITLE,
                Impl.COLUMN_DESCRIPTION,
                Impl.COLUMN_URI,
                Impl.COLUMN_STATUS,
                Impl.COLUMN_FILE_NAME_HINT,
                Impl.COLUMN_MIME_TYPE + " AS " + "media_type",
                Impl.COLUMN_TOTAL_BYTES + " AS " + "total_size",
                Impl.COLUMN_LAST_MODIFICATION + " AS " + "last_modified_timestamp",
                Impl.COLUMN_CURRENT_BYTES + " AS " + "bytes_so_far",
                Impl.COLUMN_PRIORITY,
                Impl.COLUMN_FILE_SIZE,
                Impl.COLUMN_FILE_EXTENSION,
                Impl.COLUMN_RESERVE,
                Impl.COLUMN_CURRENT_SPEED,
                Impl.COLUMN_EXTRAS,
                Impl.COLUMN_MD5,
                Impl.COLUMN_ALLOW_ADJUST_SAVEPATH,
                Impl.COLUMN_VISIBILITY,
                Impl.COLUMN_NEED_QUEUE,
                //Impl.COLUMN_ALLOWED_NETWORK_TYPES
        };
        String selection =
                //Impl.COLUMN_NOTIFICATION_PACKAGE + "='" + packageName +"'" +
                //" and " +
                Impl.COLUMN_STATUS + "=? ";
        String[] args = {
                "200",
        };
        Cursor cursor = resolver.query(uri, projection, selection, args, null);
        return cursor;
    }

    public int insertDataToNewDb(TaskParamInfo taskParamInfo, TaskStateInfo taskStateInfo){
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        databaseWrapper.beginTransaction();
        try {
            long paramId = databaseWrapper.insert(DatabaseHelper.PARAMS_TABLE, null, taskParamInfo.toContentValues());
            if(paramId <= 0){
                LogUtil.e(" insert param info[" + taskParamInfo.generateId +"] failed! ");
                return 0;
            }
            taskStateInfo.paramId = String.valueOf(paramId);
            long taskId = databaseWrapper.insert(DatabaseHelper.TASKS_TABLE, null, taskStateInfo.toContentValues());
            if(taskId <= 0){
                LogUtil.e(" insert state info[" + taskParamInfo.generateId +"] failed! ");
                return 0;
            }
            databaseWrapper.setTransactionSuccessful();
        } finally {
            databaseWrapper.endTransaction();
        }
        return 1;
    }

}
