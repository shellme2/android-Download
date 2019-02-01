package com.eebbk.bfc.sdk.download;

/**
 * Desc: 设置下载任务配置信息扩展字段操作接口
 * Author: llp
 * Create Time: 2016-09-28 12:47
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadTaskExtra {

    /**
     * 获取扩展信息
     *
     * @param name key
     * @return 值
     */
    String getStringExtra(String name);

    /**
     * 获取扩展信息
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    int getIntExtra(String name, int defaultValue);
    /**
     * 获取扩展信息
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    boolean getBooleanExtra(String name, boolean defaultValue);
    /**
     * 获取扩展信息
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    float getFloatExtra(String name, float defaultValue);
    /**
     * 获取扩展信息
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    double getDoubleExtra(String name, double defaultValue);
    /**
     * 获取扩展信息
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    char getCharExtra(String name, char defaultValue);
    /**
     * 获取扩展信息
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    byte getByteExtra(String name, byte defaultValue);

    /**
     * 获取扩展信息
     *
     * @param name key
     * @return 值
     */
    byte[] getByteArrayExtra(String name);
    /**
     * 获取扩展新
     *
     * @param name key
     * @param defaultValue 默认值
     * @return 值
     */
    short getShortExtra(String name, short defaultValue);

}
