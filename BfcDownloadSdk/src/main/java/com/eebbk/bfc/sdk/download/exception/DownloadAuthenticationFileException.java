package com.eebbk.bfc.sdk.download.exception;

import com.eebbk.bfc.sdk.download.exception.DownloadStopException;

/**
 * 下载的文件, 是在认证网络中下载的html网页
 * <p>
 * 理论上下载库不应该处理这个问题的
 *
 * errorCode = ErrorCode.Values.DOWNLOAD_AUTHENTICATION_FILE
 * Created by Simon on 2017/6/6.
 */

public class DownloadAuthenticationFileException extends DownloadStopException {
    public DownloadAuthenticationFileException(String errorCode) {
        super(errorCode);
    }

    public DownloadAuthenticationFileException(String errorCode, String detailMessage) {
        super(errorCode, detailMessage);
    }

    public DownloadAuthenticationFileException(String errorCode, String detailMessage, Throwable throwable) {
        super(errorCode, detailMessage, throwable);
    }

    public DownloadAuthenticationFileException(String errorCode, Throwable throwable) {
        super(errorCode, throwable);
    }
}
