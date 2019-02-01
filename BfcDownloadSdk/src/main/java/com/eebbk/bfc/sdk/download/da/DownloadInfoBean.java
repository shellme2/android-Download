package com.eebbk.bfc.sdk.download.da;

class DownloadInfoBean extends DownloadBaseInfoBean {
    private String urlHost;
    private String ip;
    private long downFileSize;
    private String dataPeerFromXY;
    private String dataPeerFromCDN;

    public String getUrlHost() {
        return urlHost;
    }

    public void setUrlHost(String urlHost) {
        this.urlHost = urlHost;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public long getDownFileSize() {
        return downFileSize;
    }

    public void setDownFileSize(long downFileSize) {
        this.downFileSize = downFileSize;
    }

    public String getDataPeerFromXY() {
        return dataPeerFromXY;
    }

    public void setDataPeerFromXY(String dataPeerFromXY) {
        this.dataPeerFromXY = dataPeerFromXY;
    }

    public String getDataPeerFromCDN() {
        return dataPeerFromCDN;
    }

    public void setDataPeerFromCDN(String dataPeerFromCDN) {
        this.dataPeerFromCDN = dataPeerFromCDN;
    }
}
