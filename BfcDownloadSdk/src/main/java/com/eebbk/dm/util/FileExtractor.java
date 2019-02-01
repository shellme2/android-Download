package com.eebbk.dm.util;

import android.content.Context;

import com.eebbk.bfc.sdk.download.util.LogUtil;

import java.io.UnsupportedEncodingException;

/**
 * MD5 计算 后面要移动到工具包中 
 */
public class FileExtractor {

    private static final String FORMAT_UTF8 = "UTF-8";
    private  FileExtractor() {
		
	}
    
    public static void loadLib(Context context) {
        System.loadLibrary("md5");
        System.loadLibrary("FileExtractor");
        System.loadLibrary("JNIUtil");
        System.loadLibrary("FileExtractor-jni");
    }

    public static boolean fileExtractor(String sourcePath, String targetPath) {
        byte[] sourcePathBytes = null;
        byte[] targetPathBytes = null;

        LogUtil.i("start unpack file sourcePath[" + sourcePath + "] targetPath[" + targetPath+"]");

        try {
            sourcePathBytes = sourcePath.getBytes(FORMAT_UTF8);
            targetPathBytes = targetPath.getBytes(FORMAT_UTF8);
        } catch (UnsupportedEncodingException e) {
            LogUtil.e(e, " unpack error ");
            return false;
        }

        boolean isFileextractorNative = false;

        isFileextractorNative = fileextractorNative(
                sourcePathBytes,
                sourcePathBytes.length,
                targetPathBytes,
                targetPathBytes.length);

        LogUtil.i("end unpack result[" + isFileextractorNative + "]");

        return isFileextractorNative;
    }

    public static byte[] calcMD5(String sourcePath) {
        byte[] sourcePathBytes = null;
        int sourceLen = 0;
        try {
            sourcePathBytes = sourcePath.getBytes(FORMAT_UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        if(sourcePathBytes != null){
        	sourceLen = sourcePathBytes.length;
        }

        return calcMD5Native(sourcePathBytes, sourceLen);
    }

    public static byte[] calcMD5NoBlock(String sourcePath) {
        byte[] sourcePathBytes = null;
        int sourceLen = 0;
        try {
            sourcePathBytes = sourcePath.getBytes(FORMAT_UTF8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        
        if(sourcePathBytes != null){
        	sourceLen = sourcePathBytes.length;
        }

        return calcMD5NativeNoBlock(sourcePathBytes, sourceLen);
    }

    static {
        System.loadLibrary("md5");
        System.loadLibrary("FileExtractor");
        System.loadLibrary("JNIUtil");
        System.loadLibrary("FileExtractor-jni");
    }

    private native static boolean fileextractorNative(byte[] sourcePath, int sourcePathLen,
            byte[] targetPath, int targetPathLen);

    private native static byte[] calcMD5Native(byte[] sourcePath, int sourcePathLen);

    private native static byte[] calcMD5NativeNoBlock(byte[] sourcePath, int sourcePathLen);
}
