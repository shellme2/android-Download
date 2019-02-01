package com.eebbk.bfc.sdk.download.db;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.eebbk.bfc.sdk.behavior.utils.ListUtils;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.DownloadInnerTask;
import com.eebbk.bfc.sdk.download.DownloadManager;
import com.eebbk.bfc.sdk.download.db.data.TaskParamInfo;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;
import com.eebbk.bfc.sdk.download.util.CloseableUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Desc: 下载数据库业务实现类
 * Author: llp
 * Create Time: 2016-10-07 16:57
 * Email: jacklulu29@gmail.com
 */

public class DatabaseModeImpl implements IDatabaseMode {

    private DownloadProviderConfig mConfig;
    private DatabaseHelper mDatabaseHelper;
    private Context mAppContext;

    public DatabaseModeImpl(Context appContext) {
        this.mAppContext = appContext;
        this.mConfig = DownloadInitHelper.getInstance().getProviderConfig();
        this.mDatabaseHelper = DatabaseHelper.getInstance(appContext);
    }

    @Override
    public DownloadInnerTask find(DownloadManager manager, int generateId) {
        if (null == mConfig) {
            LogUtil.w(" find task but mConfig is null! ");
            return null;
        }
        final ContentResolver resolver = this.mAppContext.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(DownloadContentProvider.buildTaskUri(mConfig.taskUri, String.valueOf(generateId)), null, null, null, null);
            if (cursor == null) {
                LogUtil.w(" find task but cursor is null! ");
                return null;
            }
            if (cursor.getCount() < 1) {
                return null;
            }
            if (cursor.moveToFirst()) {
                return new DownloadInnerTask(cursor);
            }
            return null;
        } finally {
            CloseableUtil.close(cursor);
        }
    }

    @Override
    public List<DownloadInnerTask> find(DownloadManager manager, int[] generateIds) {
        if (null == mConfig) {
            LogUtil.w(" find task but mConfig is null! ");
            return null;
        }
        final ContentResolver resolver = this.mAppContext.getContentResolver();
        Cursor cursor = null;
        List<DownloadInnerTask> downloadInnerTaskList = new ArrayList<>();
        DownloadInnerTask requestTask;
        try {
            cursor = resolver.query(mConfig.taskUri, null, null, null, null);
            if (cursor == null) {
                LogUtil.w(" find task but cursor is null! ");
                return null;
            }

            if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                do {
                    int queryId = cursor.getInt(cursor.getColumnIndex(DownloadTaskColumns.GENERATE_ID));
                    for(int  id:generateIds){
                        if(id == queryId) {
                            LogUtil.i(" find task to delete id: " + id);
                            requestTask = new DownloadInnerTask(cursor);
                            downloadInnerTaskList.add(requestTask);
                        }
                    }
                } while (cursor.moveToNext());
            }
        } finally {
            CloseableUtil.close(cursor);
        }
        return downloadInnerTaskList;
    }

    @Override
    public int insert(TaskParamInfo taskParamInfo, TaskStateInfo taskStateInfo) {
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        databaseWrapper.beginTransaction();
        try {
            long paramId = databaseWrapper.insert(DatabaseHelper.PARAMS_TABLE, null, taskParamInfo.toContentValues());
            if (paramId <= 0) {
                LogUtil.e(" insert param info[" + taskParamInfo.generateId + "] failed! ");
                return 0;
            }
            taskStateInfo.paramId = String.valueOf(paramId);
            long taskId = databaseWrapper.insert(DatabaseHelper.TASKS_TABLE, null, taskStateInfo.toContentValues());
            if (taskId <= 0) {
                LogUtil.e(" insert state info[" + taskParamInfo.generateId + "] failed! ");
                return 0;
            }
            databaseWrapper.setTransactionSuccessful();
        } finally {
            databaseWrapper.endTransaction();
        }

        return 1;
    }

    public int updateDownloadNetworkTypes(int generateId, String paramsId, int networkTypes) {
        if (TextUtils.isEmpty(paramsId)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.ParamsColumns.NETWORK_TYPES, networkTypes);

        String selection = DatabaseHelper.ParamsColumns._ID + "=?";
        String[] selectionArgs = new String[]{paramsId};
        int count = databaseWrapper.update(DatabaseHelper.PARAMS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update generateId:[" + generateId + "] param:[" + paramsId + "] networkTypes:[" + networkTypes + "] success! ");
        } else {
            LogUtil.e(" update generateId:[" + generateId + "] param:[" + paramsId + "] network types:[" + networkTypes + "] failed! ");
        }
        return count;
    }

    public int updateDownloadStatus(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update info[" + taskStateInfo.id + "] taskPhase[" + taskStateInfo.taskPhase + "] status[" + taskStateInfo.state + "] success! ");
        } else {
            LogUtil.e(" update info[" + taskStateInfo.id + "]  taskPhase[" + taskStateInfo.taskPhase + "] status[" + taskStateInfo.state + "] failed! ");
        }
        return count;
    }

    public int updateDownloadParamAndStatus(int generateId, String paramsId, String fileName, String fileExtension, String savePath, int networkTypes, TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(paramsId) || TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        databaseWrapper.beginTransaction();
        try {
            final ContentValues values = new ContentValues();
            values.put(DatabaseHelper.ParamsColumns.FILE_NAME, fileName);
            values.put(DatabaseHelper.ParamsColumns.FILE_EXTENSION, fileExtension);
            values.put(DatabaseHelper.ParamsColumns.SAVE_PATH, savePath);
            values.put(DatabaseHelper.ParamsColumns.NETWORK_TYPES, networkTypes);

            String selection = DatabaseHelper.ParamsColumns._ID + "=?";
            String[] selectionArgs = new String[]{paramsId};
            int count = databaseWrapper.update(DatabaseHelper.PARAMS_TABLE, values, selection, selectionArgs);

            if (count > 0) {
                LogUtil.d(" update generateId:[" + generateId + "] param:[" + paramsId + "] fileName:[" + fileName + "] fileExtension:[" + fileExtension + "] savePath:[" + savePath + "] networkTypes:[" + networkTypes + "] success! ");
            } else {
                LogUtil.e(" update generateId:[" + generateId + "] param:[" + paramsId + "] fileName:[" + fileName + "] fileExtension:[" + fileExtension + "] savePath:[" + savePath + "] networkTypes:[" + networkTypes + "] failed! ");
                return 0;
            }

            final ContentValues values1 = new ContentValues();
            values1.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
            values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
            values1.put(DatabaseHelper.TasksColumns.TOTAL_SIZE, taskStateInfo.totalSize);
            values1.put(DatabaseHelper.TasksColumns.FINISH_SIZE, taskStateInfo.finishSize);
            //values1.put(DatabaseHelper.TasksColumns.SPEED, taskStateInfo.speed);
            //values1.put(DatabaseHelper.TasksColumns.LAST_TIME, taskStateInfo.lastTime);
            values1.put(DatabaseHelper.TasksColumns.RETRY_TIME, taskStateInfo.retryTime);
            values1.put(DatabaseHelper.TasksColumns.ETAG, taskStateInfo.eTag);
            values1.put(DatabaseHelper.TasksColumns.ERROR_CODE, taskStateInfo.errorCode);
            values1.put(DatabaseHelper.TasksColumns.EXCEPTION, LogUtil.getStackTraceString(taskStateInfo.exception));

            selection = DatabaseHelper.TasksColumns._ID + "=?";
            selectionArgs = new String[]{taskStateInfo.id};
            count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values1, selection, selectionArgs);

            if (count > 0) {
                LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
            } else {
                LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
                return 0;
            }

            databaseWrapper.setTransactionSuccessful();
        } finally {
            databaseWrapper.endTransaction();
        }

        return 1;
    }

    public int updateDownloadProgress(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TOTAL_SIZE, taskStateInfo.totalSize);
        values.put(DatabaseHelper.TasksColumns.FINISH_SIZE, taskStateInfo.finishSize);
        //values.put(DatabaseHelper.TasksColumns.SPEED, taskStateInfo.speed);
        //values.put(DatabaseHelper.TasksColumns.LAST_TIME, taskStateInfo.lastTime);

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(DownloadUtils.formatString(" update stateInfo id[%s] state[%s] total[%s] finish[%s] success! ", taskStateInfo.id, taskStateInfo.state, taskStateInfo.totalSize, taskStateInfo.finishSize));
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    public int updateDownloadRetry(TaskStateInfo taskStateInfo) {
        return updateDownloadFailure(taskStateInfo);
    }

    public int updateDownloadFailure(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.TOTAL_SIZE, taskStateInfo.totalSize);
        values.put(DatabaseHelper.TasksColumns.FINISH_SIZE, taskStateInfo.finishSize);
        //values.put(DatabaseHelper.TasksColumns.SPEED, taskStateInfo.speed);
        //values.put(DatabaseHelper.TasksColumns.LAST_TIME, taskStateInfo.lastTime);
        values.put(DatabaseHelper.TasksColumns.RETRY_TIME, taskStateInfo.retryTime);
        values.put(DatabaseHelper.TasksColumns.DOWNLOAD_FINISH_TIME, System.currentTimeMillis());
        values.put(DatabaseHelper.TasksColumns.ERROR_CODE, taskStateInfo.errorCode);
        values.put(DatabaseHelper.TasksColumns.EXCEPTION, LogUtil.getStackTraceString(taskStateInfo.exception));

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    public int updateDownloadSuccess(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.TOTAL_SIZE, taskStateInfo.totalSize);
        values.put(DatabaseHelper.TasksColumns.FINISH_SIZE, taskStateInfo.finishSize);
        //values.put(DatabaseHelper.TasksColumns.SPEED, taskStateInfo.speed);
        //values.put(DatabaseHelper.TasksColumns.LAST_TIME, taskStateInfo.lastTime);
        values.put(DatabaseHelper.TasksColumns.DOWNLOAD_FINISH_TIME, System.currentTimeMillis());

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    public int updateCheckFailure(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.CHECK_FINISH_TIME, System.currentTimeMillis());
        values.put(DatabaseHelper.TasksColumns.ERROR_CODE, taskStateInfo.errorCode);
        values.put(DatabaseHelper.TasksColumns.EXCEPTION, LogUtil.getStackTraceString(taskStateInfo.exception));

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    public int updateCheckSuccess(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.CHECK_FINISH_TIME, System.currentTimeMillis());

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    public int updateUnpackParamAndStatus(int generateId, String paramsId, String unpackPath, TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(paramsId) || TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        databaseWrapper.beginTransaction();
        try {
            final ContentValues values = new ContentValues();
            values.put(DatabaseHelper.ParamsColumns.UNPACK_PATH, unpackPath);

            String selection = DatabaseHelper.ParamsColumns._ID + "=?";
            String[] selectionArgs = new String[]{paramsId};
            int count = databaseWrapper.update(DatabaseHelper.PARAMS_TABLE, values, selection, selectionArgs);

            if (count > 0) {
                LogUtil.d(" update generateId:[" + generateId + "] param:[" + paramsId + "] unpackPath:[" + unpackPath + "] success! ");
            } else {
                LogUtil.e(" update generateId:[" + generateId + "] param:[" + paramsId + "] unpackPath:[" + unpackPath + "] failed! ");
                return 0;
            }

            final ContentValues values1 = new ContentValues();
            values1.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
            values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
            values1.put(DatabaseHelper.TasksColumns.TOTAL_SIZE, taskStateInfo.totalSize);
            values1.put(DatabaseHelper.TasksColumns.FINISH_SIZE, taskStateInfo.finishSize);
            //values1.put(DatabaseHelper.TasksColumns.SPEED, taskStateInfo.speed);
            //values1.put(DatabaseHelper.TasksColumns.LAST_TIME, taskStateInfo.lastTime);
            values1.put(DatabaseHelper.TasksColumns.ERROR_CODE, taskStateInfo.errorCode);
            values1.put(DatabaseHelper.TasksColumns.EXCEPTION, LogUtil.getStackTraceString(taskStateInfo.exception));

            selection = DatabaseHelper.TasksColumns._ID + "=?";
            selectionArgs = new String[]{taskStateInfo.id};
            count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values1, selection, selectionArgs);

            if (count > 0) {
                LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
            } else {
                LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
                return 0;
            }

            databaseWrapper.setTransactionSuccessful();
        } finally {
            databaseWrapper.endTransaction();
        }

        return 1;
    }

    public int updateUnpackFailure(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.UNPACK_FINISH_TIME, System.currentTimeMillis());
        values.put(DatabaseHelper.TasksColumns.ERROR_CODE, taskStateInfo.errorCode);
        values.put(DatabaseHelper.TasksColumns.EXCEPTION, LogUtil.getStackTraceString(taskStateInfo.exception));

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    public int updateUnpackSuccess(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.UNPACK_FINISH_TIME, System.currentTimeMillis());

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    @Override
    public int updateDownloadState(TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return 0;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        final ContentValues values = new ContentValues();
        values.put(DatabaseHelper.TasksColumns.STATE, taskStateInfo.state);
        values.put(DatabaseHelper.TasksColumns.TASK_PHASE, taskStateInfo.taskPhase);
        values.put(DatabaseHelper.TasksColumns.TOTAL_SIZE, taskStateInfo.totalSize);
        values.put(DatabaseHelper.TasksColumns.FINISH_SIZE, taskStateInfo.finishSize);
        //values.put(DatabaseHelper.TasksColumns.SPEED, taskStateInfo.speed);
        //values.put(DatabaseHelper.TasksColumns.LAST_TIME, taskStateInfo.lastTime);
        values.put(DatabaseHelper.TasksColumns.RETRY_TIME, taskStateInfo.retryTime);
        values.put(DatabaseHelper.TasksColumns.ETAG, taskStateInfo.eTag);
        values.put(DatabaseHelper.TasksColumns.DOWNLOAD_FINISH_TIME, taskStateInfo.downloadFinishTime);
        values.put(DatabaseHelper.TasksColumns.CHECK_FINISH_TIME, taskStateInfo.checkFinishTime);
        values.put(DatabaseHelper.TasksColumns.UNPACK_FINISH_TIME, taskStateInfo.unpackFinishTime);
        values.put(DatabaseHelper.TasksColumns.ERROR_CODE, taskStateInfo.errorCode);
        values.put(DatabaseHelper.TasksColumns.EXCEPTION, LogUtil.getStackTraceString(taskStateInfo.exception));

        String selection = DatabaseHelper.TasksColumns._ID + "=?";
        String[] selectionArgs = new String[]{taskStateInfo.id};
        int count = databaseWrapper.update(DatabaseHelper.TASKS_TABLE, values, selection, selectionArgs);
        if (count > 0) {
            LogUtil.d(" update stateInfo " + taskStateInfo + " success! ");
        } else {
            LogUtil.e(" update stateInfo[" + taskStateInfo.id + "] failed! ");
        }
        return count;
    }

    @Override
    public boolean delete(int generateId, String paramsId, TaskStateInfo taskStateInfo) {
        if (TextUtils.isEmpty(paramsId) || TextUtils.isEmpty(taskStateInfo.id)) {
            LogUtil.e(" task has no id, please load first! ");
            return false;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        databaseWrapper.beginTransaction();
        try {
            String whereArgs = DatabaseHelper.TasksColumns._ID + "=? ";
            String[] args = new String[]{taskStateInfo.id};
            int count = databaseWrapper.delete(DatabaseHelper.TASKS_TABLE, whereArgs, args);
            LogUtil.i(" delete task[" + generateId + "], state info[" + taskStateInfo.id + "] count: " + count);
            whereArgs = DatabaseHelper.ParamsColumns._ID + "=? ";
            args = new String[]{paramsId};
            count = databaseWrapper.delete(DatabaseHelper.PARAMS_TABLE, whereArgs, args);
            LogUtil.i(" delete task[" + generateId + "], param id[" + paramsId + "] count: " + count);
            databaseWrapper.setTransactionSuccessful();
        } finally {
            databaseWrapper.endTransaction();
        }
        return true;
    }

    @Override
    public boolean delete(List<DownloadInnerTask> downloadInnerTaskList) {
        if (ListUtils.isEmpty(downloadInnerTaskList)) {
            LogUtil.e(" task has no id, please load first! ");
            return false;
        }
        DatabaseWrapper databaseWrapper = mDatabaseHelper.getDatabase();
        databaseWrapper.beginTransaction();
        try {
            List<String> taskIds = new ArrayList<>();
            List<String> paramIds = new ArrayList<>();
            for (DownloadInnerTask innerTask : downloadInnerTaskList) {
                taskIds.add(innerTask.getTaskStateInfo().id);
                paramIds.add(innerTask.getTaskParamInfo().paramsId);
            }
            int count = databaseWrapper.delete(DatabaseHelper.TASKS_TABLE, DatabaseHelper.TasksColumns._ID + " in (" + TextUtils.join(",", taskIds) + ")", null);
            LogUtil.i(" delete tasks, state infos count: " + count);
            count = databaseWrapper.delete(DatabaseHelper.PARAMS_TABLE, DatabaseHelper.ParamsColumns._ID + " in (" + TextUtils.join(",", paramIds) + ")", null);
            LogUtil.i(" delete tasks, params count: " + count);
            databaseWrapper.setTransactionSuccessful();
        } finally {
            databaseWrapper.endTransaction();
        }
        return true;
    }
}
