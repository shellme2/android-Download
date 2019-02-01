package com.eebbk.bfc.sdk.download.da;

/**
 * @描述： 获取迅雷下载信息类
 */
class XYCDNDownloadInfo {
    /**
     * url : http://res.hoisin.coocaatv.com/video/20180413/20180413155540371684.ts
     * ip : 157.255.128.27
     * down_cdn : 647776
     * down_peer : 11042816
     * down_cdn_speed : 95
     * down_peer_speed : 0
     */
    private String url;
    private String ip;
    private int down_cdn;
    private int down_peer;
    private int down_cdn_speed;
    private int down_peer_speed;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public int getDown_cdn() {
        return down_cdn;
    }

    public void setDown_cdn(int down_cdn) {
        this.down_cdn = down_cdn;
    }

    public int getDown_peer() {
        return down_peer;
    }

    public void setDown_peer(int down_peer) {
        this.down_peer = down_peer;
    }

    public int getDown_cdn_speed() {
        return down_cdn_speed;
    }

    public void setDown_cdn_speed(int down_cdn_speed) {
        this.down_cdn_speed = down_cdn_speed;
    }

    public int getDown_peer_speed() {
        return down_peer_speed;
    }

    public void setDown_peer_speed(int down_peer_speed) {
        this.down_peer_speed = down_peer_speed;
    }
}
