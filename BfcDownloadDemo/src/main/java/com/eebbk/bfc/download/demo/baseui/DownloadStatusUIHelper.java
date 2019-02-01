package com.eebbk.bfc.download.demo.baseui;

import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.sdk.download.util.DownloadUtils;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-25 2:36
 * Email: jacklulu29@gmail.com
 */

public class DownloadStatusUIHelper {

    private TextView mStatusTv;
    private TextView mTotalSizeTv;
    private TextView mFinishedSizeTv;
    private TextView mSpeedTv;
    private TextView mLastTimeTv;
    private TextView mErrorCodeTv;
    private TextView mExceptionTv;

    public DownloadStatusUIHelper(){

    }

    public void bindView(View rootView){
        initView(rootView);
    }

    private void initView(View rootView){
        mStatusTv = findView(rootView, R.id.status_tv);
        mTotalSizeTv = findView(rootView, R.id.total_size_tv);
        mFinishedSizeTv = findView(rootView, R.id.finished_size_tv);
        mSpeedTv = findView(rootView, R.id.speed_tv);
        mLastTimeTv = findView(rootView, R.id.last_time_tv);
        mErrorCodeTv = findView(rootView, R.id.reason_code_tv);
        mExceptionTv = findView(rootView, R.id.exception_tv);
    }

    public void updateView(ITask task){
        mStatusTv.setText(DemoUtil.getStatusStr(task.getState()));
        showTotalView(task.getFileSize());
        showFinishedView(task.getFinishSize());
        mSpeedTv.setText("速度：" + task.getSpeed());
        mLastTimeTv.setText("剩余时间：" + task.getLastTime());
        mErrorCodeTv.setText("错误码：" + task.getReasonCode());
        mExceptionTv.setText("异常：" + LogUtil.getStackTraceString(task.getException()));
    }

    private void showTotalView(long size){
        showTotalView(size == -1 ? " - - ": String.valueOf(size), size == -1 ? " - - ": DownloadUtils.formatFileSize(size));
    }

    private void showFinishedView(long size){
        showFinishedView(String.valueOf(size), DownloadUtils.formatFileSize(size));
    }

    private void showTotalView(String size, String formatStr){
        mTotalSizeTv.setText("总大小：" + size + " Bytes (" + formatStr + ")");
    }

    private void showFinishedView(String size, String formatStr){
        mFinishedSizeTv.setText("已完成：" + size + " Bytes (" + formatStr + ")");
    }

    private <T extends View> T findView(View rootView, int resId){
        return (T)rootView.findViewById(resId);
    }

}
