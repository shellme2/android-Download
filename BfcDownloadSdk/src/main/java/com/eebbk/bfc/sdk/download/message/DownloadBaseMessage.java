package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Desc: 基本信息
 * Author: llp
 * Create Time: 2016-10-20 15:05
 * Email: jacklulu29@gmail.com
 */

public class DownloadBaseMessage implements Parcelable {

    private int mId;
    private String mModuleName;
    private int mState;

    public DownloadBaseMessage(final int id, final String moduleName, final int state){
        this.mId = id;
        this.mModuleName = moduleName;
        this.mState = state;
    }

    public int getId() {
        return mId;
    }

    public int getState() {
        return mState;
    }

    public String getModuleName(){
        return mModuleName;
    }

    @Override
    public String toString() {
        return "[ id: " + mId + ", state: " + mState+ " ]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mId);
        dest.writeString(this.mModuleName);
        dest.writeInt(this.mState);
    }

    protected DownloadBaseMessage(Parcel in) {
        this.mId = in.readInt();
        this.mModuleName = in.readString();
        this.mState = in.readInt();
    }

    public static final Creator<DownloadBaseMessage> CREATOR = new Creator<DownloadBaseMessage>() {
        @Override
        public DownloadBaseMessage createFromParcel(Parcel source) {
            return new DownloadBaseMessage(source);
        }

        @Override
        public DownloadBaseMessage[] newArray(int size) {
            return new DownloadBaseMessage[size];
        }
    };
}
