package com.eebbk.bfc.download.demo.basic.monitor;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Created by lzy on 2018/8/28.
 */
public class DownloadInfo {
    private int id;
    private String fileName;
    private long fileSize;
    private String url;
    private String errorCode;

    public DownloadInfo(ITask iTask) {
        this.id = iTask.getId();
        this.fileName = iTask.getFileName();
        this.fileSize = iTask.getFileSize();
        this.url = iTask.getUrl();
        this.errorCode = iTask.getErrorCode();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }
}
