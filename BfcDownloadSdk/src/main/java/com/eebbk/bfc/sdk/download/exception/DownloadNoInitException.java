package com.eebbk.bfc.sdk.download.exception;

/**
 * Desc: 没有初始化或者context丢失
 * Author: llp
 * Create Time: 2016-10-10 18:02
 * Email: jacklulu29@gmail.com
 */

public class DownloadNoInitException extends IllegalArgumentException {

    public DownloadNoInitException(){
        super("must call DownloadController.init(...) to init first!");
    }

}
