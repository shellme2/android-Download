package com.eebbk.bfc.sdk.download;

import android.app.Service;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-07 20:18
 * Email: jacklulu29@gmail.com
 */

public class DownloadServiceProxy {

    private static final class DownloadServiceProxyHolder {
        private static final Service sService = new DownloadServiceProxy.Build().build();
    }

    public static Service getDownloadService(){
        return DownloadServiceProxyHolder.sService;
    }

    private static final class Build {

        private Build(){

        }

        public Service build(){
            return null;
        }

    }



}
