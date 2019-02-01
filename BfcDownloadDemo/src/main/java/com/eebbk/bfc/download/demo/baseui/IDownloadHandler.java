package com.eebbk.bfc.download.demo.baseui;

/**
 * Desc: 下载操作接口
 * Author: llp
 * Create Time: 2016-10-25 3:16
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadHandler {

    void onStartBtnClick();

    void onPauseBtnClick();

    void onResumeBtnClick();

    void onRestartBtnClick();

    void onLookInfoBtnClick();

    void onNetworkChanged(int networkTypes);

    void onRegisterListenerBtnClick();

    void onUnregisterListenerBtnClick();

    void onRegisterListenerByTagBtnClick(String tag);

    void onUnregisterListenerByTagBtnClick(String tag);

    void onDeleteByDefault();

    void onDeleteAllFile();

    void onDeleteWithoutFile();
}
