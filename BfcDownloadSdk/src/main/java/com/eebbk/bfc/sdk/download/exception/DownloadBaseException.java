package com.eebbk.bfc.sdk.download.exception;

/**
 * Desc: 下载异常基类
 * Author: llp
 * Create Time: 2016-10-11 21:05
 * Email: jacklulu29@gmail.com
 */

public class DownloadBaseException extends Exception {

    private String mErrorCode;
    /**
     * Constructs a new {@code Exception} that includes the current stack trace.
     * @param errorCode    异常序号，错误码
     */
    public DownloadBaseException(String errorCode) {
        mErrorCode = errorCode;
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param errorCode 异常序号，错误码
     * @param detailMessage
     *            the detail message for this exception.
     */
    public DownloadBaseException(String errorCode, String detailMessage) {
        super(detailMessage);
        mErrorCode = errorCode;
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace, the
     * specified detail message and the specified cause.
     *
     * @param errorCode 异常序号，错误码
     * @param detailMessage
     *            the detail message for this exception.
     * @param throwable
     *            the cause of this exception.
     */
    public DownloadBaseException(String errorCode, String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
        mErrorCode = errorCode;
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified cause.
     *
     * @param errorCode 异常序号，错误码
     * @param throwable
     *            the cause of this exception.
     */
    public DownloadBaseException(String errorCode, Throwable throwable) {
        super(throwable);
        mErrorCode = errorCode;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public DownloadBaseException setErrorCode(String errorCode) {
        this.mErrorCode = errorCode;
        return this;
    }
}
