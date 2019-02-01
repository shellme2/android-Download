package com.eebbk.bfc.download.demo.basic.ui;

import com.eebbk.bfc.download.demo.baseui.IBaseView;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.List;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-25 14:46
 * Email: jacklulu29@gmail.com
 */

public interface IMultiTaskView extends IBaseView {

    int SEARCH_ALL = 0;
    int SEARCH_BY_ID = 1;
    int SEARCH_BY_STATUS = 2;
    int SEARCH_BY_EXTRAS = 3;

    void showTaskChanged(ITask task);

    List<ITask> getData();

    void setListData(String moduleName, List<ITask> data);

    void showToast(String msg);

    void closeSearchPanel();

    String getSearchModuleName();

    int getSearchType();

    int getSearchId();

    int getSearchStatus();

    String[]  getSearchExtraKeys();

    String[]  getSearchExtraValues();
}
