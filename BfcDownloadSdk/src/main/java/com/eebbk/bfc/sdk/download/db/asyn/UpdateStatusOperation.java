package com.eebbk.bfc.sdk.download.db.asyn;

/**
 * Desc: 更新状态
 * Author: llp
 * Create Time: 2017-02-06 16:47
 * Email: jacklulu29@gmail.com
 */

public abstract class UpdateStatusOperation implements DbOperation {

    @Override
    public boolean canReplace(DbOperation OldOperation) {
        return false;
    }

    @Override
    public String getMessage() {
        return "";
    }

    @Override
    public Object getData() {
        return null;
    }
}
