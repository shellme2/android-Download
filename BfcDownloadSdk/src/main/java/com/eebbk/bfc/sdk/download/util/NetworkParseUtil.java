package com.eebbk.bfc.sdk.download.util;

import com.eebbk.bfc.sdk.download.net.NetworkType;

/**
 * Desc: 网络类型解析、获取、权限判断、添加、移除帮助类
 * Author: llp
 * Create Time: 2016年5月6日 下午10:32:53
 * Email: jacklulu29@gmail.com
 */
public class NetworkParseUtil {
	
	/**
	 * 是否可使用Wifi网络类型，默认支持
	 * 
	 * @param networkTypes 网络类型
	 * @return true支持，false不支持
	 */
	public static boolean containsWifi(int networkTypes){
		return (networkTypes & NetworkType.NETWORK_WIFI) != 0;
	}
	
	/**
	 * 是否可使用移动数据网络类型
	 * 
	 * @param networkTypes 网络类型
	 * @return true支持，false不支持
	 */
	public static boolean containsMobile(int networkTypes){
		return (networkTypes & NetworkType.NETWORK_MOBILE) != 0;
	}
	
	/**
	 * 是否可使用蓝牙网络类型
	 * 
	 * @param networkTypes 网络类型
	 * @return true支持，false不支持
	 */
	public static boolean containsBluetooth(int networkTypes){
		return (networkTypes & NetworkType.NETWORK_BLUETOOTH) != 0;
	}
	
	/**
	 * 将多种网络类型转换叠加为一个值，方便保存和使用
	 * <pre>
	 * 网络类型：
	 * {@link NetworkType#NETWORK_WIFI} wifi
	 * {@link NetworkType#NETWORK_MOBILE} mobile
	 * {@link NetworkType#NETWORK_BLUETOOTH} bluetooth
	 * 
	 * 用法：int types = convertNetworkTypes(NetworkType.NETWORK_WIFI, NetworkType.NETWORK_MOBILE);
	 * </pre>
	 * 
	 * @param networkTypes 网络类型
	 * @return 叠加后的网络类型值
	 */
	public static int convertNetworkTypes(int... networkTypes){
		int result = NetworkType.DEFAULT_NETWORK;
		if(networkTypes == null || networkTypes.length < 1){
			return result;
		}
		if(networkTypes.length == 1){
			result |= networkTypes[0];
		}else{
			for(int type : networkTypes){
				result |= type;
			}
		}
		return result;
	}
	
	/**
	 * add by llp 20160429 10:03
	 * 校验网络类型，默认只允许使用wifi
	 * <pre>
	 * {@link NetworkType#NETWORK_WIFI }
	 * {@link NetworkType#NETWORK_MOBILE }
	 * {@link NetworkType#NETWORK_BLUETOOTH }
	 * </pre>
	 * 可以叠加使用，比如: wifi+mobile的值 = {@link NetworkType#NETWORK_WIFI } | {@link NetworkType#NETWORK_MOBILE }
	 * 
	 * @param networkTypes 网络类型
	 */
	public static int checkNetworkTypes(int networkTypes) {
		// 默认只允许使用wifi
		int networkFlags = NetworkType.DEFAULT_NETWORK;
		if (networkTypes > 0) {
			// 如果要求使用wifi
			if ((networkTypes & NetworkType.NETWORK_WIFI) != 0) {
				// is default add
				networkFlags |= NetworkType.NETWORK_WIFI;
			}
			// 如果要求使用移动数据
			if ((networkTypes & NetworkType.NETWORK_MOBILE) != 0) {
				networkFlags |= NetworkType.NETWORK_MOBILE;
			}
			// 如果要求使用蓝牙网络
			if ((networkTypes & NetworkType.NETWORK_BLUETOOTH) != 0) {
				networkFlags |= NetworkType.NETWORK_BLUETOOTH;
			}
			// to add other network
		}

		return networkFlags;
	}
	
	/**
	 * 在原来的网络类型上加上某种网络类型
	 * 
	 * <pre>
	 * {@link NetworkType#NETWORK_WIFI }
	 * {@link NetworkType#NETWORK_MOBILE }
	 * {@link NetworkType#NETWORK_BLUETOOTH }
	 * </pre>
	 * 
	 * @param curNetworkTypes 原来的网络类型
	 * @param newNetworkType 要加上的网络类型
	 * @return 最终的网络类型值
	 */
	public static int addNetworkType(int curNetworkTypes, int newNetworkType){
		return curNetworkTypes | newNetworkType;
	}
	
	/**
	 * 从原来的网络类型里面移除某种网络类型
	 * <pre>
	 * {@link NetworkType#NETWORK_WIFI }
	 * {@link NetworkType#NETWORK_MOBILE }
	 * {@link NetworkType#NETWORK_BLUETOOTH }
	 * </pre>
	 * @param curNetworkTypes 原来的网络类型
	 * @param removeNetworkType 要移除的网络类型
	 * 
	 * @return 最终的网络类型值
	 */
	public static int removeNetworkType(int curNetworkTypes, int removeNetworkType){
		return curNetworkTypes & ~removeNetworkType;
	}

}
