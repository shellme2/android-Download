package com.eebbk.bfc.sdk.download.db.asyn;

import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.util.LinkedList;

public class DownloadAsynDbTask {
	
	private static final String TAG = "DownloadAsynDbTask";
	
	private final Thread mDbTask;
	private boolean mIsDestroy = false;
	private long mStartTime = 0;
	private long mDestroyTime = 0;
	
	/** The linked Task Queue. */
	private final LinkedList<DbOperation> mEntityQueue;
	
	public DownloadAsynDbTask() {
		mIsDestroy = false;
		mEntityQueue = new LinkedList<DbOperation>();
		
		mDbTask = new DownloaderDbThread();
		mDbTask.start();
		mStartTime = System.currentTimeMillis();
	}
	
	public void destroy(){
		mIsDestroy = true;
		synchronized (mDbTask) {
			mDbTask.notify();
		}
	}

	public void add(DbOperation operation){
		if(operation == null){
			LogUtil.w(TAG, "operation is null!");
			return;
		}
		synchronized (mEntityQueue) {
			//DownloaderEntity cEntity = checkContains(mEntityQueue, entity);
			int index = checkContains(mEntityQueue, operation);
			if(index >= 0){
				DbOperation cOperation = mEntityQueue.get(index);
				if(cOperation != null){
					//mEntityQueue.remove(cEntity);
					try {
						mEntityQueue.set(index, operation);
					} catch (Exception e) {
						LogUtil.e(e, TAG, " replace old error! ");
					}
					cOperation = null;
				}else{
					//LogUtil.v(TAG, " replace operation is null, Add operation: " + operation.getMessage());
					mEntityQueue.add(operation);
				}
			}else{
				//LogUtil.v(TAG, " Add operation: " + operation.getMessage());
				mEntityQueue.add(operation);
			}

			//mEntityQueue.add(entity);
			synchronized (mDbTask) {
				mDbTask.notify();
			}
		}
	}

	public DbOperation getOperationInList(DbOperation operation){
		if(operation == null){
			LogUtil.w(TAG, "operation is null!");
			return null;
		}
		synchronized (mEntityQueue) {
			//DownloaderEntity cEntity = checkContains(mEntityQueue, entity);
			int index = checkContains(mEntityQueue, operation);
			if(index >= 0){
				DbOperation cOperation = mEntityQueue.remove(index);
				if(LogUtil.isDebug() && cOperation != null){
					LogUtil.d(TAG, " removoe old operation: " + cOperation.getMessage() + " \n new operation: " + operation.getMessage());
				}
				return cOperation;
			}
		}
		return null;
	}
	
	public int checkContains(LinkedList<DbOperation> list, DbOperation newOperation){
		DbOperation oldOperation = null;
		int r = -1;
		int size = list.size();
		if(size > 0){
			for(int i=0; i < size; i ++){
				oldOperation = list.get(i);
				if(newOperation.canReplace(oldOperation)){
					r = i;
					break;
				}
			}
		}
		return r;
	}
	
	public DbOperation get(){
		DbOperation result = null;
		int size = -1;
		synchronized (mEntityQueue) {
			size = mEntityQueue.size();
			if(size > 0){
                result = mEntityQueue.pop();
			}
		}
		
		if(size > 0 && result == null && mEntityQueue.size() > 0){
			LogUtil.e(TAG, " pop operation is null!");
			return get();
		}
		
		return result;
	}
	
	private class DownloaderDbThread extends Thread {
		
		@Override
		public void run() {
			while(true){
				DbOperation operation = get();
				if(operation != null){
					if(mIsDestroy){
						if(mDestroyTime == 0){
							mDestroyTime = System.currentTimeMillis();
						}
						//LogUtil.d(TAG, "is destroy, but has operation: " + operation.getMessage());
					}else{
						//LogUtil.v(TAG, "DO: " + operation.getMessage());
					}
					operation.operation();
				}else{
					if(mIsDestroy){
						// 线程结束
						LogUtil.i("asyn db task is destroy!");
						long delyTime = 0;
						long endTime = System.currentTimeMillis();
						if(mDestroyTime != 0){
							delyTime = endTime - mDestroyTime;
						}
						if(LogUtil.isDebug()){
							LogUtil.d(TAG, " delay time: " + delyTime + " process time: " + (endTime - mStartTime));
						}
						return;
					}else{
						// 等待
						try {
							//LogUtil.d(TAG, "no operation, waiting!");
							//long endTime = System.currentTimeMillis();
							//LogUtil.i(TAG, " db process time: " + (endTime - mStartTime));
							synchronized (this) {
								wait();
							}
						} catch (InterruptedException e) {
							LogUtil.d(TAG, "interrupted, do next!");
						}
					}
				}
			}
		}
		
	}
}
