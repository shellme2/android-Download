package com.eebbk.bfc.download.demo.net_test;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Simon on 2017/6/5.
 */

public class PingInfo implements Parcelable {
    private String mUrl;
    private int mIntervalTime;
    private int mTimeout;

    public PingInfo(String url, int intervalTime, int timeout) {
        mUrl = url;
        mIntervalTime = intervalTime;
        mTimeout = timeout;
    }


    public String getUrl() {
        return mUrl;
    }

    public int getIntervalTime() {
        return mIntervalTime;
    }

    public int getTimeout() {
        return mTimeout;
    }


    protected PingInfo(Parcel in) {
        mUrl = in.readString();
        mIntervalTime = in.readInt();
        mTimeout = in.readInt();
    }

    public static final Creator<PingInfo> CREATOR = new Creator<PingInfo>() {
        @Override
        public PingInfo createFromParcel(Parcel in) {
            return new PingInfo(in);
        }

        @Override
        public PingInfo[] newArray(int size) {
            return new PingInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mUrl);
        dest.writeInt(mIntervalTime);
        dest.writeInt(mTimeout);
    }
}
