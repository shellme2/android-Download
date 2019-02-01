package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;

/**
 * Desc: 进度消息
 * Author: llp
 * Create Time: 2016-11-01 17:11
 * Email: jacklulu29@gmail.com
 */

public class ProgressMsg extends SizeMsg {

    private long mSpeed;

    public ProgressMsg(int id, String moduleName, int state, long totalSize, long finishedSize, long speed) {
        super(id, moduleName, state, totalSize, finishedSize);
        this.mSpeed = speed;
    }

    public long getSpeed() {
        return mSpeed;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeLong(this.mSpeed);
    }

    protected ProgressMsg(Parcel in) {
        super(in);
        this.mSpeed = in.readLong();
    }

    public static final Creator<ProgressMsg> CREATOR = new Creator<ProgressMsg>() {
        @Override
        public ProgressMsg createFromParcel(Parcel source) {
            return new ProgressMsg(source);
        }

        @Override
        public ProgressMsg[] newArray(int size) {
            return new ProgressMsg[size];
        }
    };
}
