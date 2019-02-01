package com.eebbk.bfc.sdk.download.net;

/**
 * Desc: 下载网络类型
 * Author: llp
 * Create Time: 2016-09-26 17:59
 * Email: jacklulu29@gmail.com
 */

public interface NetworkType {

    /**
     * 未知网络
     */
    int NETWORK_UNKNOWN = 0;

    /**
     * 移动网络（4G/3G/2G...）
     */
    int NETWORK_MOBILE = 1;// 1 << 0;

    /**
     * WIFI网络
     */
    int NETWORK_WIFI = 1 << 1;

    /**
     * 蓝牙（暂时不支持）
     */
    int NETWORK_BLUETOOTH = 1 << 2;

    /**
     * 下载默认使用网络
     */
    int DEFAULT_NETWORK = NETWORK_WIFI | NETWORK_BLUETOOTH;

}
