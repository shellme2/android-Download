package com.eebbk.bfc.download.demo.basic.model;

import android.content.Context;

import com.eebbk.bfc.download.demo.util.SharedPreferencesUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-11-13 19:38
 * Email: jacklulu29@gmail.com
 */

public class SingleTaskModel {

    private static final String SINGLE_TASK_SHARE_CONFIG = "single_task_config";
    private static final String SHARE_KEY_TASK_ID = "task_id";
    private SharedPreferencesUtil mSharePfe;

    public SingleTaskModel(Context context){
        mSharePfe = new SharedPreferencesUtil(context, SINGLE_TASK_SHARE_CONFIG);
    }

    public void saveTaskId(int taskId){
        mSharePfe.putInt(SHARE_KEY_TASK_ID, taskId);
    }

    public int getTaskId(){
        return mSharePfe.getInt(SHARE_KEY_TASK_ID, ITask.INVALID_GENERATE_ID);
    }

}
