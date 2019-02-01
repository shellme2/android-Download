package com.eebbk.bfc.sdk.download;

import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc: 动态修改网络类型，比如动态添加移动网络使用权限
 * Author: llp
 * Create Time: 2016年5月5日 下午2:53:44
 * Email: jacklulu29@gmail.com
 */
public interface INetworkAccessController {

	/**
	 * 动态设置可以使用的网络类型（权限） <br/>
	 * <pre>网络类型包括：{@link com.eebbk.bfc.sdk.download.net.NetworkType#NETWORK_WIFI}、
	 * {@link com.eebbk.bfc.sdk.download.net.NetworkType#NETWORK_MOBILE}、
	 * {@link com.eebbk.bfc.sdk.download.net.NetworkType#NETWORK_BLUETOOTH}</pre>
	 * @param networkTypes 网络类型
	 * @param tasks 任务
	 */
	void setNetworkTypes(int networkTypes, ITask... tasks);
	
	/**
	 * 动态设置可以使用的网络类型（权限） <br/>
	 * * <pre>网络类型包括：{@link com.eebbk.bfc.sdk.download.net.NetworkType#NETWORK_WIFI}、
	 * {@link com.eebbk.bfc.sdk.download.net.NetworkType#NETWORK_MOBILE}、
	 * {@link com.eebbk.bfc.sdk.download.net.NetworkType#NETWORK_BLUETOOTH}</pre>
	 * @param networkTypes 网络类型
	 * @param ids 任务id
	 */
	void setNetworkTypes(int networkTypes, int... ids);
}
