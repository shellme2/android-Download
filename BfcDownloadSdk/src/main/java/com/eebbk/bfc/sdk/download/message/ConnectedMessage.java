package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;

/**
 * Desc: 下载连接消息
 * Author: llp
 * Create Time: 2016-10-20 15:50
 * Email: jacklulu29@gmail.com
 */

public class ConnectedMessage extends SizeMsg {

    private boolean mResuming;
    private String mFileName;
    private String mFileExtension;

    public ConnectedMessage(int id, String moduleName, int state, boolean resuming,
                            long totalSize, long finishedSize,
                            String fileName, String fileExtension) {
        super(id, moduleName, state, totalSize, finishedSize);
        this.mResuming = resuming;
        this.mFileName = fileName;
        this.mFileExtension = fileExtension;
    }

    public boolean isResuming() {
        return mResuming;
    }

    public String getFileName() {
        return mFileName;
    }

    public String getFileExtension() {
        return mFileExtension;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeByte(this.mResuming ? (byte) 1 : (byte) 0);
        dest.writeString(this.mFileName);
        dest.writeString(this.mFileExtension);
    }

    protected ConnectedMessage(Parcel in) {
        super(in);
        this.mResuming = in.readByte() != 0;
        this.mFileName = in.readString();
        this.mFileExtension = in.readString();
    }

    public static final Creator<ConnectedMessage> CREATOR = new Creator<ConnectedMessage>() {
        @Override
        public ConnectedMessage createFromParcel(Parcel source) {
            return new ConnectedMessage(source);
        }

        @Override
        public ConnectedMessage[] newArray(int size) {
            return new ConnectedMessage[size];
        }
    };
}
