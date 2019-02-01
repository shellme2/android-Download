package com.eebbk.bfc.sdk.download.exception;

/**
 * Desc: 暂停异常，由于网络或者其他的原因
 * Author: llp
 * Create Time: 2016-10-11 21:04
 * Email: jacklulu29@gmail.com
 */

public class DownloadPauseException extends DownloadStopException {

    /**
     * Constructs a new {@code Exception} that includes the current stack trace.
     * @param errorCode    异常序号，错误码
     */
    public DownloadPauseException(String errorCode) {
        super(errorCode);
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified detail message.
     *
     * @param errorCode 异常序号，错误码
     * @param detailMessage
     *            the detail message for this exception.
     */
    public DownloadPauseException(String errorCode, String detailMessage) {
        super(errorCode, detailMessage);
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
    public DownloadPauseException(String errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    /**
     * Constructs a new {@code Exception} with the current stack trace and the
     * specified cause.
     *
     * @param errorCode 异常序号，错误码
     * @param throwable
     *            the cause of this exception.
     */
    public DownloadPauseException(String errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }

}
