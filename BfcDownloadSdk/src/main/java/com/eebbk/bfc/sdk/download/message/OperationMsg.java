package com.eebbk.bfc.sdk.download.message;

import android.os.Parcel;

/**
 * Desc: 操作消息
 * Author: llp
 * Create Time: 2016-12-11 19:44
 * Email: jacklulu29@gmail.com
 */

public class OperationMsg extends DownloadBaseMessage {

    private int mOperation;

    public OperationMsg(int id, String moduleName, int operation) {
        super(id, moduleName, -1);
        this.mOperation = operation;
    }

    public int getOperation(){
        return mOperation;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.mOperation);
    }

    protected OperationMsg(Parcel in) {
        super(in);
        this.mOperation = in.readInt();
    }

    public static final Creator<OperationMsg> CREATOR = new Creator<OperationMsg>() {
        @Override
        public OperationMsg createFromParcel(Parcel source) {
            return new OperationMsg(source);
        }

        @Override
        public OperationMsg[] newArray(int size) {
            return new OperationMsg[size];
        }
    };
}
