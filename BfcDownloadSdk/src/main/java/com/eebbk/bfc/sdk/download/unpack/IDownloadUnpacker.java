package com.eebbk.bfc.sdk.download.unpack;

/**
 * Desc: 下载文件解压
 * Author: llp
 * Create Time: 2016-10-11 8:51
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadUnpacker {

    /**
     * 判断是否支持本文件解压
     *
     * @param filePath 要解压的文件路径
     * @return true为支持，false不支持
     */
    boolean isSupport(String filePath);

    /**
     * 文件解压，这个方法将会运行在异步线程中，请勿做任何更新UI的操作
     * @param sourceFilePath    原始文件路径
     * @param targetPath 解压文件保存路径
     * @param deleteSourceAfterUnpack    解压后是否删除源文件
     * @param callback 解压回调方法
     */
    void unpack(String sourceFilePath, String targetPath, boolean deleteSourceAfterUnpack, IDownloadUnpackCallback callback);

}
