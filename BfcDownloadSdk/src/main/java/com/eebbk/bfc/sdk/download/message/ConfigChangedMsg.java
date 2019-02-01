package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;

/**
 * Desc: 任务配置信息更改信息
 * Author: llp
 * Create Time: 2016-11-01 17:11
 * Email: jacklulu29@gmail.com
 */

public class ConfigChangedMsg extends DownloadBaseMessage {

    private int mNetworkTypes;

    public ConfigChangedMsg(int id, String moduleName, int networkTypes) {
        super(id, moduleName, -1);
        this.mNetworkTypes = networkTypes;
    }

    public int getNetworkTypes() {
        return mNetworkTypes;
    }

    @Override
    public int getState() {
        throw new IllegalArgumentException(" no support ! ");
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mNetworkTypes);
    }

    protected ConfigChangedMsg(Parcel in) {
        super(in);
        this.mNetworkTypes = in.readInt();
    }

    public static final Creator<ConfigChangedMsg> CREATOR = new Creator<ConfigChangedMsg>() {
        @Override
        public ConfigChangedMsg createFromParcel(Parcel source) {
            return new ConfigChangedMsg(source);
        }

        @Override
        public ConfigChangedMsg[] newArray(int size) {
            return new ConfigChangedMsg[size];
        }
    };
}
