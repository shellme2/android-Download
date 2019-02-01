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

import android.database.sqlite.SQLiteDatabase;

import com.eebbk.bfc.sdk.download.util.LogUtil;

public class DatabaseUpgradeHelper {

    private static final String TAG = "DatabaseUpgradeHelper";

    public DatabaseUpgradeHelper(){
        // do nothing
    }

    public void doOnUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        if (oldVersion == newVersion) {
            return;
        }

        for(int i = oldVersion + 1; i <= newVersion; i ++){
            onUpgrade(db, i);
        }

        LogUtil.i(TAG, "Database upgrade started from version " + oldVersion + " to " + newVersion);
        // Add future upgrade code here
    }

    private void onUpgrade(final SQLiteDatabase db, int version){
        switch(version){
            case 2:
            case 3:
                onUpgrade2(db, version-1, version);
                break;
            case 4:
                onUpgrade4(db, version-1, version);
                break;
            default:
                break;
        }
    }

    private void onUpgrade2(final SQLiteDatabase db, final int oldVersion, final int newVersion){
        onDowngrade(db, oldVersion, newVersion);
    }

    private void onUpgrade4(final SQLiteDatabase db, final int oldVersion, final int newVersion){
        String sqlStr = "ALTER TABLE " + DatabaseHelper.PARAMS_TABLE +
                " ADD COLUMN " + DatabaseHelper.ParamsColumns.MIN_PROGRESS_TIME + " INT DEFAULT(750);";
        db.execSQL(sqlStr);
        if(LogUtil.isDebug()){
            LogUtil.d(TAG, " sql: " + sqlStr);
        }
        DatabaseHelper.dropAllViews(db);
        DatabaseHelper.createAllViews(db);
        LogUtil.i(TAG, "Database downgrade requested for version " + oldVersion + " version " + newVersion + " upgrade!");
    }

    public void onDowngrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
        DatabaseHelper.rebuildTables(db);
        LogUtil.i(TAG, "Database downgrade requested for version " + oldVersion + " version " + newVersion + ", forcing db rebuild!");
    }
}
