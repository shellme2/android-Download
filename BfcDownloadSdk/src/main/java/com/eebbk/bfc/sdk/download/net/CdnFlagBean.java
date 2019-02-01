package com.eebbk.bfc.sdk.download.net;

import java.util.List;

/**
 * 用于提供CDN服务切换服务
 */
public class CdnFlagBean {
    /**
     * stateCode : 0
     * stateInfo : 成功
     * data : {"flag":"2","whiteFlag":"1","apkList":null,"domainList":["www.eebbk.com"],"urlList":null}
     */
    private String stateCode;
    private String stateInfo;
    private DataBean data;

    public String getStateCode() {
        return stateCode;
    }

    public void setStateCode(String stateCode) {
        this.stateCode = stateCode;
    }

    public String getStateInfo() {
        return stateInfo;
    }

    public void setStateInfo(String stateInfo) {
        this.stateInfo = stateInfo;
    }

    public DataBean getData() {
        return data;
    }

    public void setData(DataBean data) {
        this.data = data;
    }

    public static class DataBean {
        //标记位 区分使用不同CDN的标志。 1：默认CDN 2：星云 3：其他扩展服务商
        private String flag;
        //标记位 白名单 所使用的CDN服务标志。 1：默认CDN 2：星云 3：其他扩展服务商
        //为扩展标签，标志与flag不一致，则白名单中的相关资源走whiteFlag标记的CDN服务。
        private String whiteFlag;
        //应用名单
        private List<String> apkList;
        //下载地址名单
        private List<String> urlList;
        //域名名单
        private List<String> domainList;

        public String getFlag() {
            return flag;
        }

        public void setFlag(String flag) {
            this.flag = flag;
        }

        public String getWhiteFlag() {
            return whiteFlag;
        }

        public void setWhiteFlag(String whiteFlag) {
            this.whiteFlag = whiteFlag;
        }

        public List<String> getApkList() {
            return apkList;
        }

        public void setApkList(List<String> apkList) {
            this.apkList = apkList;
        }

        public List<String> getUrlList() {
            return urlList;
        }

        public void setUrlList(List<String> urlList) {
            this.urlList = urlList;
        }

        public List<String> getDomainList() {
            return domainList;
        }

        public void setDomainList(List<String> domainList) {
            this.domainList = domainList;
        }

        public boolean isInApkList(String apkPackageName) {
            return null != apkList && apkList.contains(apkPackageName);
        }

        public boolean isInurlList(String url) {
            return null != urlList && urlList.contains(url);
        }

        public boolean isInDomainList(String domain) {
            return null != domainList && domainList.contains(domain);
        }

        @Override
        public String toString() {
            return "DataBean{" +
                "flag='" + flag + '\'' +
                ", whiteFlag='" + whiteFlag + '\'' +
                ", apkList=" + apkList +
                ", urlList=" + urlList +
                ", domainList=" + domainList +
                '}';
        }
    }
}
