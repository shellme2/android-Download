package com.eebbk.bfc.download.demo.limit.ui;

import android.content.Context;

import com.eebbk.bfc.download.demo.baseui.IBaseView;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-21 17:17
 * Email: jacklulu29@gmail.com
 */

public interface SingleTaskCycleDownloadView extends IBaseView {

    Context getContext();

    void showDataChangedByInit(ITask task);

    void showDataChanged(ITask task);

    void showToast(String msg);

    void onTestStart();

    void onTestStop();

    void showTestInfo(CharSequence charSequence);

}
