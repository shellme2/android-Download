package com.eebbk.bfc.sdk.download.db;

import android.content.ComponentName;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.io.FileNotFoundException;

/**
 * Desc: 下载数据内容提供者
 * Author: llp
 * Create Time: 2016-10-23 19:40
 * Email: jacklulu29@gmail.com
 */

public class DownloadContentProvider extends ContentProvider {

    private static final int TASKS_QUERY_CODE = 10;
    private static final int TASK_QUERY_CODE = 20;

    private final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private DatabaseHelper mDatabaseHelper;
    private DatabaseWrapper mDatabaseWrapper;

    public DownloadContentProvider (){
        super();
    }

    /**
     * build a task uri from the task id
     * @param uri   uri
     * @param id 任务GENERATE_ID
     * @return Uri
     */
    public static Uri buildTaskUri(@NonNull final Uri uri, final String id){
        final Uri.Builder builder = uri.buildUpon();
        builder.appendPath(id);
        return builder.build();
    }

    public static void notifyTaskChanged(@NonNull Context appContext, @NonNull final Uri uri, final String id){
        final Uri taskUri = buildTaskUri(uri, id);
        final ContentResolver resolver = appContext.getApplicationContext().getContentResolver();
        resolver.notifyChange(taskUri, null);
    }

    @Override
    public boolean onCreate() {
        DownloadProviderConfig config = new DownloadProviderConfig(getContext());
        sUriMatcher.addURI(config.authority, DownloadProviderConfig.TASK_QUERY, TASKS_QUERY_CODE);
        sUriMatcher.addURI(config.authority, DownloadProviderConfig.TASK_QUERY + "/*", TASK_QUERY_CODE);
        mDatabaseHelper = getDatabase();
        return true;
    }

    private DatabaseWrapper getDatabaseWrapper() {
        if (mDatabaseWrapper == null) {
            mDatabaseWrapper = mDatabaseHelper.getDatabase();
        }
        return mDatabaseWrapper;
    }

    protected DatabaseHelper getDatabase() {
        String distinguishOldDbName = getDistinguishOldDbName();

        if(TextUtils.isEmpty(distinguishOldDbName)) {
            return DatabaseHelper.getInstance(getContext());
        } else {
            boolean isDistinguishOldDbName = "distinguishOldDbName".equalsIgnoreCase(distinguishOldDbName);
            return DatabaseHelper.getInstance(getContext(), isDistinguishOldDbName);
        }
    }

    @Override
    public @Nullable Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                                  @Nullable String selection, @Nullable String[] selectionArgs,
                                  @Nullable String sortOrder) {
        final SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        String[] queryArgs = selectionArgs;
        final int match = sUriMatcher.match(uri);
        String groupBy = null;
        String limit = null;
        switch (match){
            case TASKS_QUERY_CODE: {
                queryBuilder.setTables(DatabaseHelper.DOWNLOAD_TASK_VIEW);
                break;
            }
            case TASK_QUERY_CODE: {
                queryBuilder.setTables(DatabaseHelper.DOWNLOAD_TASK_VIEW);
                if(uri.getPathSegments().size() == 2){
                    queryBuilder.appendWhere(DownloadTaskColumns.GENERATE_ID + "=?");
                    // get the task id from uri
                    queryArgs = prependArgs(queryArgs, uri.getPathSegments().get(1));
                } else {
                    throw new IllegalArgumentException("Malformed URI " + uri);
                }
                break;
            }
            default:{
                throw new IllegalArgumentException("Unknown URI " + uri);
            }
        }

        final Cursor cursor = getDatabaseWrapper().query(queryBuilder, projection, selection,
                queryArgs, groupBy, null, sortOrder, limit);
        //cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public @Nullable String getType(@NonNull Uri uri){
        String result;
        switch (sUriMatcher.match(uri)){
            case TASKS_QUERY_CODE:{
                result = "vnd.android.cursor.dir/vpn.bbk.bfcdownload.tasks";
                break;
            }
            case TASK_QUERY_CODE: {
                result = "vnd.android.cursor.item/vpn.bbk.bfcdownload.task";
                break;
            }
            default:{
                throw new IllegalArgumentException("Unknown URI: " + uri);
            }
        }
        return result;
    }

    @Override
    public @Nullable ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode,
                                  @Nullable CancellationSignal signal) throws FileNotFoundException {
        throw new IllegalArgumentException("openFile not supported: " + uri);
    }

    @Override
    public @Nullable Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new IllegalStateException("Insert not supported " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                               @Nullable String[] selectionArgs) {
        throw new IllegalArgumentException("Delete not supported: " + uri);
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                               @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new IllegalArgumentException("Update not supported: " + uri);
    }

    /**
     * Prepends new arguments to the existing argument list.
     *
     * @param oldArgList The current list of arguments. May be {@code null}
     * @param args The new arguments to prepend
     * @return A new argument list with the given arguments prepended
     */
    private String[] prependArgs(final String[] oldArgList, final String... args) {
        if (args == null || args.length == 0) {
            return oldArgList;
        }
        final int oldArgCount = (oldArgList == null ? 0 : oldArgList.length);
        final int newArgCount = args.length;

        final String[] newArgs = new String[oldArgCount + newArgCount];
        System.arraycopy(args, 0, newArgs, 0, newArgCount);
        if (oldArgCount > 0) {
            System.arraycopy(oldArgList, 0, newArgs, newArgCount, oldArgCount);
        }
        return newArgs;
    }

    /**
     * 跟“老版本下载器”区分数据库名
     * 老版本数据名和当前新版本数据库名默认是一样的（downloads.db），
     * 有些应用会通过老版本下载器把数据库创建在应用内，导致跟新版本数据库的版本号和表字段都不一致。
     * 若为"distinguishOldDbName",则在新数据库名后加“-new”，例：downloads-new.db,
     */
    @NonNull
    private String getDistinguishOldDbName() {
        String distinguishOldDbName;
        try {
            PackageManager pm = getContext().getPackageManager();
            ComponentName componentName = new ComponentName(getContext(), DownloadContentProvider.class);
            ProviderInfo providerInfo = pm.getProviderInfo(componentName, 0);
            distinguishOldDbName = providerInfo.loadLabel(pm).toString();
        } catch (Exception e) {
            e.printStackTrace();
            distinguishOldDbName = "";
        }
        return distinguishOldDbName;
    }
}
