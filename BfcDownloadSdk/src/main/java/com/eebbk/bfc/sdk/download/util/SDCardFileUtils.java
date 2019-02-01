package com.eebbk.bfc.sdk.download.util;

import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * 文件操作类（SD卡上的检查文件是否存在、创建目录、创建文件、删除文件、删除文件夹等操作）
 * 
 * */
public class SDCardFileUtils {

	private static final String TAG = "SDCardFileUtils";
	public final static String SDCARD_PATH = Environment.getExternalStorageDirectory().toString();
	public final static String DISK_B_PATH = Environment.getExternalStorageDirectory().toString();
	private static long sLastTime = 0;

	private SDCardFileUtils(){
		// private construct
	}

	public static boolean canClick() {
		long thisTime = System.currentTimeMillis();
		long temp = thisTime - sLastTime;
		final long clickTap = 1500;
		
		sLastTime = thisTime;
		return temp >= clickTap;
	}

	/**
	 * 在SD卡上创建文件 “aFileName 如test.xml”
	 * 
	 * @param aFileName
	 *            文件名
	 * @return 文件句柄
	 * @throws IOException
	 */
	public static File createSDFile(String aFileName) throws IOException {
		File mFile = null;

		if (checkFileSystemIsOk() && null != aFileName) {
			mFile = new File(getSDPath() + aFileName);
			if(!mFile.createNewFile()){
				Log.e(TAG, "createNewFile 失败");
			}
		}

		return mFile;
	}

	/**
	 * 在SD卡上创建目录
	 * 
	 * @param aDirName
	 *            目录名称
	 * @return 文件句柄
	 */
	public static File creatSDDir(String aDirName) {
		File mDir = null;

		if (checkFileSystemIsOk() && null != aDirName) {
			mDir = new File(getSDPath() + aDirName);
			if(!mDir.mkdirs()){
				Log.e(TAG, "mDir.mkdirs() 失败");
			}
		}

		return mDir;
	}

	/**
	 * 判断SD卡上的文件夹是否存在
	 * 
	 * @param aFilePath
	 *            文件名
	 * @return true 文件存在，false 文件不存在
	 */
	public static boolean isFileExist(String aFilePath) {
		File mFile = null;
		boolean mIsExist = false;

		if (checkFileSystemIsOk() && null != aFilePath) {
			mFile = new File(aFilePath);
			mIsExist = mFile.exists();
		}

		return mIsExist;
	}

	/**
	 * 删掉文件
	 * @param aFilePath 文件路径
	 */
	public static void removeFile(String aFilePath) {
		if (null != aFilePath && isFileExist(aFilePath)) {
			File mFile = new File(aFilePath);
			if(!mFile.delete()){
				Log.e(TAG, "mFile.delete 删除是失败");
			}
		}
	}

	/**
	 * 功能描述： 检查文件系统是否OK
	 * 
	 * @return true Ok
	 */
	private static boolean checkFileSystemIsOk() {
		boolean mIsOk = false;

		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			mIsOk = true;
		}

		return mIsOk;
	}

	/**
	 * 功能描述： 检查SD卡是否存在
	 * @return 是否
	 */
	public static boolean checkSdCardSystemIsOk() {
		return Environment.getExternalStorageState().equals(SDCardFileUtils.SDCARD_PATH);
	}

	/**
	 * 得到SD卡的路径
	 * @return SD卡路径
	 */
	public static String getSDPath() {
		return SDCARD_PATH;
	}

	/**
	 * 得到flash/B盘的路径
	 * 
	 * @return 家教机盘路径
	 */
	public static String getFlashBPath() {
		return DISK_B_PATH;
	}
	
	/**
	 * 判断磁盘空间是否足够
	 * @param path 指定路径
	 * @param sizeMb 所需空间
	 * @return true 有 false 无
	 */
	public static boolean isAvailableSpace(String path, int sizeMb) {
		final long memoryMMeasure = 1024;
		boolean ishasSpace = false;

		StatFs statFs = new StatFs(path);

		long blockSize = statFs.getBlockSize();
		long blocks = statFs.getAvailableBlocks();
		long availableSpare = (blocks * blockSize) / (memoryMMeasure * memoryMMeasure);

		if (availableSpare > sizeMb) {
			ishasSpace = true;
		}

		return ishasSpace;
	}

	/**
	 * 判断外部TF卡是否存在
	 * 
	 * @return 是否
	 */
	public static boolean isSDCardExits() {
		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
			Log.d(TAG,"sdcard is mounted");
			return true;
		} else {
			Log.w(TAG,"sdcard no mounted!");
			return false;
		}
	}
}
