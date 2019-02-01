package com.eebbk.bfc.download.demo.limit.model;

import android.content.Context;

import com.eebbk.bfc.download.demo.util.SharedPreferencesUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-11-13 19:38
 * Email: jacklulu29@gmail.com
 */

public class SingleTaskCycleDownloadModel {

    private static final String SINGLE_TASK_SHARE_CONFIG = "single_task_cycle_download_config";
    private static final String SHARE_KEY_TASK_ID = "task_id";
    private static final String SHARE_KEY_START_TIME = "start_time";
    private static final String SHARE_KEY_END_TIME = "end_time";
    private static final String SHARE_KEY_PAUSE_TIMES = "pause_times";
    private static final String SHARE_KEY_RETRY_TIMES = "retry_times";
    private static final String SHARE_KEY_SUCCESS_TIMES = "success_times";
    private static final String SHARE_KEY_FAILURE_TIMES = "failure_times";
    private static final String SHARE_KEY_IS_STOP = "is_stop";
    private SharedPreferencesUtil mSharePfe;

    public SingleTaskCycleDownloadModel(Context context){
        mSharePfe = new SharedPreferencesUtil(context, SINGLE_TASK_SHARE_CONFIG);
    }

    public void saveTaskId(int taskId){
        mSharePfe.putInt(SHARE_KEY_TASK_ID, taskId);
    }

    public int getTaskId(){
        return mSharePfe.getInt(SHARE_KEY_TASK_ID, ITask.INVALID_GENERATE_ID);
    }

    public void saveStartTime(long time){
        mSharePfe.putLong(SHARE_KEY_START_TIME, time);
    }

    public long getStartTime(){
        return mSharePfe.getLong(SHARE_KEY_START_TIME, 0);
    }

    public void saveSuccessTimes(int times){
        mSharePfe.putInt(SHARE_KEY_SUCCESS_TIMES, times);
    }

    public int getSuccessTimes(){
        return mSharePfe.getInt(SHARE_KEY_SUCCESS_TIMES, 0);
    }

    public void saveFailureTimes(int times){
        mSharePfe.putInt(SHARE_KEY_FAILURE_TIMES, times);
    }

    public int getFailureTimes(){
        return mSharePfe.getInt(SHARE_KEY_FAILURE_TIMES, 0);
    }

    public void savePauseTimes(int times){
        mSharePfe.putInt(SHARE_KEY_PAUSE_TIMES, times);
    }

    public int getPauseTimes(){
        return mSharePfe.getInt(SHARE_KEY_PAUSE_TIMES, 0);
    }

    public void saveRetryTimes(int times){
        mSharePfe.putInt(SHARE_KEY_RETRY_TIMES, times);
    }

    public int getRetryTimes(){
        return mSharePfe.getInt(SHARE_KEY_RETRY_TIMES, 0);
    }

    public void saveEndTime(long time){
        mSharePfe.putLong(SHARE_KEY_END_TIME, time);
    }

    public long getEndTime(){
        return mSharePfe.getLong(SHARE_KEY_END_TIME, 0);
    }

    public void saveIsStop(boolean isStop){
        mSharePfe.putBoolean(SHARE_KEY_IS_STOP, isStop);
    }

    public boolean getIsStop(){
        return mSharePfe.getBoolean(SHARE_KEY_IS_STOP, true);
    }
}
