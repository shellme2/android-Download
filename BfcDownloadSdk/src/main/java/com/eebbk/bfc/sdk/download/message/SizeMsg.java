package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-11-01 17:19
 * Email: jacklulu29@gmail.com
 */

public class SizeMsg extends DownloadBaseMessage {

    private long mFinishedSize;
    private long mTotalSize;

    public SizeMsg(int id, String moduleName, int state, long totalSize, long finishedSize) {
        super(id, moduleName, state);
        this.mTotalSize = totalSize;
        this.mFinishedSize = finishedSize;
    }

    public long getFinishedSize() {
        return mFinishedSize;
    }

    public long getTotalSize() {
        return mTotalSize;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mFinishedSize);
        dest.writeLong(this.mTotalSize);
    }

    protected SizeMsg(Parcel in) {
        super(in);
        this.mFinishedSize = in.readLong();
        this.mTotalSize = in.readLong();
    }

    public static final Creator<SizeMsg> CREATOR = new Creator<SizeMsg>() {
        @Override
        public SizeMsg createFromParcel(Parcel source) {
            return new SizeMsg(source);
        }

        @Override
        public SizeMsg[] newArray(int size) {
            return new SizeMsg[size];
        }
    };
}
