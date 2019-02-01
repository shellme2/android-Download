package com.eebbk.bfc.sdk.download.db.asyn;

import com.eebbk.bfc.sdk.download.db.IDatabaseMode;
import com.eebbk.bfc.sdk.download.db.data.TaskStateInfo;

/**
 * Desc: 更新进度
 * Author: llp
 * Create Time: 2017-02-06 16:47
 * Email: jacklulu29@gmail.com
 */
public class UpdateProgressOperation implements DbOperation {
	
	private IDatabaseMode mDatabaseMode;
	private TaskStateInfo mInfo;
	
	public UpdateProgressOperation(IDatabaseMode databaseMode, TaskStateInfo taskStateInfo) {
		this.mDatabaseMode = databaseMode;
		this.mInfo = new TaskStateInfo(taskStateInfo);
	}

	@Override
	public void operation() {
		if(mDatabaseMode != null && mInfo != null){
			mDatabaseMode.updateDownloadProgress(mInfo);
			clear();
		}
	}

	@Override
	public void clear() {
		mDatabaseMode = null;
		mInfo = null;
	}

	@Override
	public boolean canReplace(DbOperation oldOperation) {
		if(oldOperation == null || !(oldOperation instanceof UpdateProgressOperation)){
			return false;
		}
		Object obj = oldOperation.getData();
		if(obj == null || !(obj instanceof TaskStateInfo)){
			return false;
		}
		TaskStateInfo oldInfo = (TaskStateInfo)obj;
		return mInfo.id.equals(oldInfo.id) && mInfo.paramId.equals(oldInfo.paramId);
	}

	@Override
	public String getMessage() {
		return mInfo != null ? " [id: " +mInfo.id + " paramId:" + mInfo.paramId +  " finishSize: " +mInfo.finishSize + "]": "";
	}

	@Override
	public Object getData() {
		return mInfo;
	}
	
}
