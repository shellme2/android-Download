package com.eebbk.bfc.sdk.download.message;

/**
 * Desc: 消息接收器
 * Author: llp
 * Create Time: 2016-10-22 20:10
 * Email: jacklulu29@gmail.com
 */

public interface IMessageReceiver {

    void onMessageReceive(DownloadBaseMessage message);

}
