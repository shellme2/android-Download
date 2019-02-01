package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;

/**
 * Desc: 错误信息
 * Author: llp
 * Create Time: 2016-11-01 17:11
 * Email: jacklulu29@gmail.com
 */

public class ErrorMsg extends SizeMsg {

    private int mRetries;
    private String mErrorCode;
    private Throwable mThrowable;

    public ErrorMsg(int id, String moduleName, int state, long totalSize, long finishedSize, int retries, String errorCode, Throwable throwable) {
        super(id, moduleName, state, totalSize, finishedSize);
        this.mRetries = retries;
        this.mErrorCode = errorCode;
        this.mThrowable = throwable;
    }

    public int getRetries() {
        return mRetries;
    }

    public String getErrorCode() {
        return mErrorCode;
    }

    public Throwable getThrowable() {
        return mThrowable;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mRetries);
        dest.writeString(this.mErrorCode);
        dest.writeSerializable(this.mThrowable);
    }

    protected ErrorMsg(Parcel in) {
        super(in);
        this.mRetries = in.readInt();
        this.mErrorCode = in.readString();
        this.mThrowable = (Throwable) in.readSerializable();
    }

    public static final Creator<ErrorMsg> CREATOR = new Creator<ErrorMsg>() {
        @Override
        public ErrorMsg createFromParcel(Parcel source) {
            return new ErrorMsg(source);
        }

        @Override
        public ErrorMsg[] newArray(int size) {
            return new ErrorMsg[size];
        }
    };

    @Override
    public String toString() {
        return "ErrorMsg{" +
            "mRetries=" + mRetries +
            ", mErrorCode='" + mErrorCode + '\'' +
            ", mThrowable=" + mThrowable +
            '}';
    }
}
