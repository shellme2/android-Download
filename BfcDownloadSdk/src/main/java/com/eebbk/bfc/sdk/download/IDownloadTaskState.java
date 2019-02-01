package com.eebbk.bfc.sdk.download;

/**
 * Desc: 设置下载任务临时信息接口，由下载系统调用写入
 * Author: llp
 * Create Time: 2016-09-28 12:47
 * Email: jacklulu29@gmail.com
 */

public interface IDownloadTaskState {

    /**
     * 获取当前状态，包括下载状态、校验状态、解压状态
     * @return 状态值
     */
    int getState();

    /**
     * 获取当前速度值。下载时，显示的是下载速度；校验时，显示的是校验速度；解压时，显示的是解压速度<br/>
     * 速度值保留小数点后面两位<br/>
     * 如果不想转换，可以通过{@link #getSpeedNumber()}获取原始值
     * @return 当前速度值，字符串，经过了转换
     */
    String getSpeed();

    /**
     * 获取当前速度值。 B/S
     * @return 速度值
     */
    long getSpeedNumber();

    /**
     * 获取剩余时间。下载时，显示的是下载剩余时间；校验时，显示的是校验剩余时间；解压时，显示的是解压剩余时间<br/>
     * 如果不想转换，可以通过{@link #getLastTimeSeconds()}获取原始值
     * @return 当前剩余时间，字符串，经过了转换
     */
    String getLastTime();

    /**
     * 获取剩余时间,单位秒
     * @return 剩余时间
     */
    long getLastTimeSeconds();

    /**
     * 获取剩余时间。下载时，显示的是下载剩余时间；校验时，显示的是校验剩余时间；解压时，显示的是解压剩余时间
     * <pre>不建议使用，请使用替换方法{@link #getLastTime()}</pre>
     * @return 剩余时间
     * @deprecated
     */
    String getNeedtime();

    /**
     * 获取已完成大小。下载时，显示的是下载已完成大小；校验时，显示的是校验已完成大小；解压时，显示的是解压已完成大小
     * @return 已完成大小（bytes）
     */
    long getFinishSize();

    /**
     * 获取已完成大小
     * <pre>不建议使用， 请使用替换方法 {@link #getFinishSize()}</pre>
     * @return 已完成大小（bytes）
     * @deprecated
     */
    long getLoadedSize();

    /**
     * 获取错误码
     * @return 错误码
     */
    @Deprecated
    String getReasonCode();

    /**
     * 获取错误码
     * @return 错误码
     */
    String getErrorCode();

    /**
     * 是否用户手动暂停，手动暂停必须手动恢复下载
     * @return true是，false否
     */
    boolean isPauseByUser();

    /**
     * 是否因为网络原因造成暂停，网络正常后会自动恢复下载
     * @return true是，false否
     */
    boolean isPauseByNetwork();

    /**
     * 是否因为存储空间不足造成暂停，清理后必须手动恢复下载
     * @return true是，false否
     */
    boolean isPauseByOutOfSpace();
}
