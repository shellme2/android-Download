/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.eebbk.bfc.sdk.download.db;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteCantOpenDatabaseException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDiskIOException;
import android.database.sqlite.SQLiteFullException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.database.sqlite.SQLiteReadOnlyDatabaseException;
import android.database.sqlite.SQLiteStatement;
import android.util.SparseArray;

import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.Locale;
import java.util.Stack;
import java.util.regex.Pattern;

public class DatabaseWrapper {
    private static final String TAG = "DatabaseWrapper";

    private final SQLiteDatabase mDatabase;
    private final Context mContext;
    /**
     * to regex matching queries to see query plans. For example, ".*" to show all query plans.
     */
    private final String mExplainQueryPlanRegexp;

    private final SparseArray<SQLiteStatement> mCompiledStatements;

    static class TransactionData {
        long time;
        boolean transactionSuccessful;
    }

    // track transaction on a per thread basis
    private static ThreadLocal<Stack<TransactionData>> sTransactionDepth =
        new ThreadLocal<Stack<TransactionData>>() {
            @Override
            public Stack<TransactionData> initialValue() {
                return new Stack<>();
            }
        };

    private static String[] sFormatStrings = new String[]{"took %d ms to %s", "   took %d ms to %s", "      took %d ms to %s",};

    DatabaseWrapper(final Context context, final SQLiteDatabase db) {
        mExplainQueryPlanRegexp = null;
        mDatabase = db;
        mContext = context;
        mCompiledStatements = new SparseArray<>();
    }

    public SQLiteStatement getStatementInTransaction(final int index, final String statement) {
        // Use transaction to serialize access to statements
        SQLiteStatement compiled = mCompiledStatements.get(index);
        if (compiled == null) {
            compiled = mDatabase.compileStatement(statement);
            mCompiledStatements.put(index, compiled);
        }
        return compiled;
    }

    public Context getContext() {
        return mContext;
    }

    public void beginTransaction() {
        final long t1 = System.currentTimeMillis();

        // push the current time onto the transaction stack
        final TransactionData f = new TransactionData();
        f.time = t1;
        sTransactionDepth.get().push(f);

        mDatabase.beginTransaction();
    }

    public void setTransactionSuccessful() {
        final TransactionData f = sTransactionDepth.get().peek();
        f.transactionSuccessful = true;
        mDatabase.setTransactionSuccessful();
    }

    public void endTransaction() {
        final TransactionData f = sTransactionDepth.get().pop();
        if (!f.transactionSuccessful) {
            LogUtil.w(TAG, "endTransaction without setting successful");
            for (final StackTraceElement st : (new Exception()).getStackTrace()) {
                LogUtil.w(TAG, "    " + st.toString());
            }
        }
        try {
            mDatabase.endTransaction();
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to endTransaction");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
    }

    public void yieldTransaction() {
        mDatabase.yieldIfContendedSafely();
    }

    public void insertWithOnConflict(final String searchTable, final String nullColumnHack, final ContentValues initialValues, final int conflictAlgorithm) {
        try {
            mDatabase.insertWithOnConflict(searchTable, nullColumnHack, initialValues, conflictAlgorithm);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to insertWithOnConflict");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
    }

    @TargetApi(11)
    private void explainQueryPlan(final SQLiteQueryBuilder qb, final SQLiteDatabase db, final String[] projection, final String selection, final String[] queryArgs, final String groupBy, final String having, final String sortOrder, final String limit) {
        final String queryString = qb.buildQuery(projection, selection, groupBy, null, sortOrder, limit);
        explainQueryPlan(db, queryString, queryArgs);
    }

    private void explainQueryPlan(final SQLiteDatabase db, final String sql, final String[] queryArgs) {
        if (!Pattern.matches(mExplainQueryPlanRegexp, sql)) {
            return;
        }
        final Cursor planCursor = db.rawQuery("explain query plan " + sql, queryArgs);
        try {
            if (planCursor != null && planCursor.moveToFirst()) {
                final int detailColumn = planCursor.getColumnIndex("detail");
                final StringBuilder sb = new StringBuilder();
                do {
                    sb.append(planCursor.getString(detailColumn));
                    sb.append("\n");
                } while (planCursor.moveToNext());
                if (sb.length() > 0) {
                    sb.setLength(sb.length() - 1);
                }
                LogUtil.v(TAG, "for query " + sql + "\nplan is: " + sb.toString());
            }
        } catch (final Exception e) {
            LogUtil.e(e, TAG, "Query plan failed ");
        } finally {
            if (planCursor != null) {
                planCursor.close();
            }
        }
    }

    public Cursor query(final String searchTable, final String[] projection, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy, final String limit) {
        if (mExplainQueryPlanRegexp != null) {
            final SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(searchTable);
            explainQueryPlan(qb, mDatabase, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
        }

        return mDatabase.query(searchTable, projection, selection, selectionArgs, groupBy, having, orderBy, limit);
    }

    public Cursor query(final String searchTable, final String[] columns, final String selection, final String[] selectionArgs, final String groupBy, final String having, final String orderBy) {
        return query(searchTable, columns, selection, selectionArgs, groupBy, having, orderBy, null);
    }

    public Cursor query(final SQLiteQueryBuilder qb, final String[] projection, final String selection, final String[] queryArgs, final String groupBy, final String having, final String sortOrder, final String limit) {
        if (mExplainQueryPlanRegexp != null) {
            explainQueryPlan(qb, mDatabase, projection, selection, queryArgs, groupBy, having, sortOrder, limit);
        }
        return qb.query(mDatabase, projection, selection, queryArgs, groupBy, having, sortOrder, limit);
    }

    @TargetApi(11)
    public long queryNumEntries(final String table, final String selection, final String[] selectionArgs) {
        return DatabaseUtils.queryNumEntries(mDatabase, table, selection, selectionArgs);
    }

    public Cursor rawQuery(final String sql, final String[] args) {
        if (mExplainQueryPlanRegexp != null) {
            explainQueryPlan(mDatabase, sql, args);
        }
        return mDatabase.rawQuery(sql, args);
    }

    public int update(final String table, final ContentValues values, final String selection, final String[] selectionArgs) {
        int count = 0;
        try {
            count = mDatabase.update(table, values, selection, selectionArgs);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to update");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        } catch (SQLiteReadOnlyDatabaseException ex) {
            LogUtil.e(ex, TAG, "Database SQLiteReadOnlyDatabaseException, unable to update");
        } catch (Exception e) {
            LogUtil.e(e, TAG, "Database Exception, unable to update");
        }
        return count;
    }

    public int delete(final String table, final String whereClause, final String[] whereArgs) {
        int count = 0;
        try {
            count = mDatabase.delete(table, whereClause, whereArgs);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to delete");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        } catch (SQLiteCantOpenDatabaseException ex) {
            LogUtil.e(ex, TAG, "Database SQLiteCantOpenDatabaseException, unable to update");
        }
        return count;
    }

    public long insert(final String table, final String nullColumnHack, final ContentValues values) {
        long rowId = -1;
        try {
            rowId = mDatabase.insert(table, nullColumnHack, values);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to insert");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
        return rowId;
    }

    public long replace(final String table, final String nullColumnHack, final ContentValues values) {
        long rowId = -1;
        try {
            rowId = mDatabase.replace(table, nullColumnHack, values);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to replace");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
        return rowId;
    }

    public void setLocale(final Locale locale) {
        mDatabase.setLocale(locale);
    }

    public void execSQL(final String sql, final String[] bindArgs) {
        try {
            mDatabase.execSQL(sql, bindArgs);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to execSQL");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
    }

    public void execSQL(final String sql) {
        try {
            mDatabase.execSQL(sql);
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to execSQL");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
    }

    @TargetApi(11)
    public int execSQLUpdateDelete(final String sql) {
        final SQLiteStatement statement = mDatabase.compileStatement(sql);
        int rowsUpdated = 0;
        try {
            rowsUpdated = statement.executeUpdateDelete();
        } catch (SQLiteFullException ex) {
            LogUtil.e(ex, TAG, "Database full, unable to execSQLUpdateDelete");
        } catch (SQLiteDiskIOException ex) {
            LogUtil.e(ex, TAG, "Database DiskIOException, unable to update");
        }
        return rowsUpdated;
    }

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }
}
