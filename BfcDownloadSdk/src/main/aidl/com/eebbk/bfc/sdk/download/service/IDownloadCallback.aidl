// IDownloadCallback.aidl
package com.eebbk.bfc.sdk.download.service;

import com.eebbk.bfc.sdk.download.message.DownloadBaseMessage;

interface IDownloadCallback {

    oneway void callback(in DownloadBaseMessage msg);
}
