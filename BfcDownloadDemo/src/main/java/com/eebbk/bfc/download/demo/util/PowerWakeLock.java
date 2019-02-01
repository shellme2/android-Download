package com.eebbk.bfc.download.demo.util;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import static com.eebbk.bfc.sdk.download.thread.DownloadBaseRunnable.WAKE_LOCK_HELD_TIME;

/**
 * 需要权限<uses-permission android:name="android.permission.WAKE_LOCK"/>
 */
public class PowerWakeLock{
	private WakeLock mWakeLock = null;
	
	/**
	 * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
	 * PARTIAL_WAKE_LOCK: 保持CPU 运转，屏幕和键盘灯可以关闭。
	 * SCREEN_DIM_WAKE_LOCK：   保持CPU 运转，保持屏幕显示，但可以变暗，允许键盘灯关闭。
	 * SCREEN_BRIGHT_WAKE_LOCK：保持CPU 运转，允许保持屏幕高亮显示，允许键盘灯关闭。
	 * FULL_WAKE_LOCK：   保持CPU 运转，保持屏幕和键盘灯都高亮显示。
	 * ACQUIRE_CAUSES_WAKEUP：  当获取锁后，立刻亮屏，典型地使用在通知中，以让用户立刻查看。
	 * ON_AFTER_RELEASE：   在释放锁（release()）后，手机屏幕仍会继续亮一会儿。
	 */
	@SuppressWarnings( { "static-access", "deprecation" } )
	public void acquireWakeLock( Context context ){
		if ( null == mWakeLock ){
			PowerManager pm = ( PowerManager ) context.getSystemService( context.POWER_SERVICE );
			mWakeLock = pm.newWakeLock( PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService" );
			if ( null != mWakeLock ){
				mWakeLock.acquire(WAKE_LOCK_HELD_TIME);
			}
		}
	}
	
	
	/**
	 * 释放设备电源锁
	 */
	public void releaseWakeLock( ){
		if ( null != mWakeLock && mWakeLock.isHeld( ) ){
			mWakeLock.release( );
			mWakeLock = null;
		}
	}
}
