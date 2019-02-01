package com.eebbk.bfc.sdk.download.db;

import android.content.Context;
import android.net.Uri;

/**
 * 下载数据库共享配置，由于是动态生成，跨进程需要各自进程初始化此配置
 * Author: llp
 * Create Time: 2016-12-14 10:42
 * Email: jacklulu29@gmail.com
 */

public class DownloadProviderConfig {

    // download task query
    static final String TASK_QUERY = "task";
    String authority;
    public Uri taskUri;

    public DownloadProviderConfig (Context context){
        final String packageName = context.getPackageName();
        authority = packageName + ".bfc.download.DownloadContentProvider";
        String contentAuthority = "content://" + authority + '/';
        taskUri = Uri.parse(contentAuthority + TASK_QUERY);
    }

}
