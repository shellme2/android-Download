package com.eebbk.bfc.sdk.download.da;

import java.io.Serializable;

/**
 * bfc下载文件链接成功,采集bean
 */
class DownloadBaseInfoBean implements Serializable {
    private String appName;
    private String appPkg;
    private String appVersion;
    private String url;
    private String cdnType;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPkg() {
        return appPkg;
    }

    public void setAppPkg(String appPkg) {
        this.appPkg = appPkg;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCdnType() {
        return cdnType;
    }

    public void setCdnType(String cdnType) {
        this.cdnType = cdnType;
    }
}
