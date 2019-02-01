package com.eebbk.bfc.download.demo.basic.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.basic.presenter.MultiTaskPresenter;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;
import java.util.List;

public class MultiTaskAdapter extends BaseAdapter {
	
	private Context mContext = null;
	private LayoutInflater mInflater;
	private List< ITask > mItems;
	private MultiTaskPresenter mPresenter;
	private Handler mHandler;

	public MultiTaskAdapter(Context mContext, MultiTaskPresenter presenter) {
		this.mContext = mContext;
		this.mInflater = LayoutInflater.from(mContext);
		mPresenter = presenter;
		mHandler = new Handler();
	}
	
	public synchronized void setItems( List< ITask> items ){
		if(items != null){
			mItems = (List<ITask>) ((ArrayList<ITask>)items).clone();
		}
		notifyDataSetChanged();
	}
	
	public synchronized List< ITask> getItems( ){
		return mItems;
	}
  
	
	@Override
	public View getView( final int position, View convertView, ViewGroup parent ) {
		ViewHolder viewHolder = null;
		if(convertView != null){
			viewHolder = (ViewHolder)convertView.getTag();
		}else {
			viewHolder = onCreateViewHolder(parent, position);
		}
		onBindViewHolder(viewHolder, position);
		viewHolder.rootView.setTag(viewHolder);
		return viewHolder.rootView;
	}
	
	public void onBindViewHolder(final ViewHolder viewHolder, final int position) {
		final ITask taskInfo = (ITask) getItem( position );
		if(taskInfo == null){
			return;
		}
		// 显示进度条
		viewHolder.progress.setIndeterminate(false);
		viewHolder.progress.setMax(100);
		viewHolder.progress.setProgress(taskInfo.getFileSize()>0&&taskInfo.getFinishSize()>0 ? (int)(taskInfo.getFinishSize() * 100/taskInfo.getFileSize()) : 0);

		String fileName = taskInfo.getFileName() == null? taskInfo.getUrl() : taskInfo.getFileName();
		String tip = taskInfo.getFileSize() == -1 ? " 此文件不支持进度" : "";
		viewHolder.nameText.setText(fileName + tip);
		// 显示下载速度、剩余时间
		if(taskInfo.isShowRealTimeInfo()){
			viewHolder.speed.setText(taskInfo.getSpeed() + " " + taskInfo.getLastTime());
			if(viewHolder.speed.getVisibility() != View.VISIBLE){
				viewHolder.speed.setVisibility(View.VISIBLE);
			}
		} else {
			if(viewHolder.speed.getVisibility() != View.GONE){
				viewHolder.speed.setText("");
				viewHolder.speed.setVisibility(View.GONE);
			}
		}

		int downloadState = taskInfo.getState();
		String btnStr = DemoUtil.getStatusBtnStr(downloadState);
		viewHolder.stateBt.setText(btnStr);
		viewHolder.stateBt.setTextColor(DemoUtil.getStatusBtnColor(downloadState));
		viewHolder.stateBt.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				processStateBtnClick(taskInfo);
			}
		});
		
		viewHolder.selBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPresenter.deleteTask(taskInfo);				
			}
		});
		viewHolder.selBtOnly.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPresenter.deleteTaskWithoutFile(taskInfo);
			}
		});
		viewHolder.selBtAll.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mPresenter.deleteTaskAndAllFile(taskInfo);
			}
		});
		viewHolder.showBt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				show(position);
			}
		});
		
		int networkType = taskInfo.getNetworkTypes();
		if(!NetworkParseUtil.containsMobile(networkType)){
			viewHolder.networkMobile.setText("+移动网络");
		} else {
			viewHolder.networkMobile.setText("-移动网络");
		}
		viewHolder.networkMobile.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int networkType = taskInfo.getNetworkTypes();
				if(!NetworkParseUtil.containsMobile(networkType)){
					// 添加移动网络
					mPresenter.addMobileNet(taskInfo);
				} else {
					// 移除移动网络
					mPresenter.removeMobileNet(taskInfo);
				}
				// 设置网络类型存在异步，数据更新可能会延迟，这里延迟200重现同步数据
				mHandler.postDelayed(new Runnable() {
					@Override
					public void run() {
						if(mPresenter != null){
							mPresenter.refreshTaskAndNotify(taskInfo);
						}
					}
				}, 200);
			}
		});
	}

	private void processStateBtnClick(final ITask task){
		switch (task.getState()) {
			case Status.DOWNLOAD_INVALID:
				mPresenter.addTask(task);
				break;
			case Status.DOWNLOAD_WAITING:
			case Status.DOWNLOAD_STARTED:
			case Status.DOWNLOAD_CONNECTED:
			case Status.DOWNLOAD_PROGRESS:
			case Status.DOWNLOAD_RETRY:
				mPresenter.pauseTask(task);
				break;
			case Status.DOWNLOAD_PAUSE:
				mPresenter.resumeTask(task);
				break;
			case Status.DOWNLOAD_FAILURE:
			case Status.DOWNLOAD_SUCCESS:
				mPresenter.restartTask(task);
				break;
			case Status.CHECK_STARTED:
			case Status.CHECK_PROGRESS:
			case Status.CHECK_FAILURE:
			case Status.CHECK_SUCCESS:
			case Status.UNPACK_STARTED:
			case Status.UNPACK_PROGRESS:
			case Status.UNPACK_FAILURE:
			case Status.UNPACK_SUCCESS:
				// can't do anything,but delete task
				break;
			default:
				break;
		}
	}
	
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
		ViewHolder viewHolder;
		//View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.ad_item_layout, viewGroup, false);
		View view = mInflater.inflate(R.layout.item_downlist, null);
		viewHolder = new ViewHolder(view);
		
		return viewHolder;
	}
	
	public static class ViewHolder {
		View rootView;
		ProgressBar progress;
		TextView nameText;
		TextView speed;
		Button stateBt;
		Button selBt;
		Button selBtOnly;
		Button selBtAll;
		Button showBt;
		Button networkMobile;
		
		public ViewHolder(View itemView) {
			rootView = itemView;
			progress = ( ProgressBar) rootView.findViewById(R.id.download_progress);
			nameText = ( TextView )rootView.findViewById(R.id.download_filename);
			speed = ( TextView )rootView.findViewById(R.id.download_speed);
			stateBt = ( Button )rootView.findViewById(R.id.download_state);
			selBt = ( Button )rootView.findViewById(R.id.download_delete);
			selBtOnly = ( Button )rootView.findViewById(R.id.download_delete_no_file);
			selBtAll = (Button) rootView.findViewById(R.id.download_delete_all);
			showBt =  ( Button )rootView.findViewById(R.id.download_show);
			networkMobile  =  ( Button )rootView.findViewById(R.id.bt_network_mobile);
		}
	}

	@Override
	public long getItemId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	private void show(int position){
		ITask task = (ITask) getItem(position);
		if(!mPresenter.refreshTask(task)){
			ToastUtil.showToast(mContext, "刷新数据失败，任务可能已被删除");
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
		builder.setMessage(DemoUtil.getTaskInfo(task))
				.setCancelable(true)
				.show();
	}

	@Override
	public int getCount() {
		if( null != mItems ){
			return mItems.size();
		}
		else{
			return 0;
		}
	}


	@Override
	public Object getItem(int index) {
		if( null != mItems && index < mItems.size()){
			return mItems.get(index);
		} else{
			return null;
		}
	}
	
	public void clear( ){
		if( null != mItems ){
			mItems.clear();
		}
	}

	/**
	 *  局部刷新
	 * @param view
	 * @param itemIndex
	 */
	public void updateView(View view, int itemIndex) {
		if (view == null) {
			return;
		}
		//从view中取得holder 78
		ViewHolder holder = (ViewHolder) view.getTag();
		onBindViewHolder(holder, itemIndex);
	}
}