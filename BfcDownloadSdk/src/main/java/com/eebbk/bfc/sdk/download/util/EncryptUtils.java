package com.eebbk.bfc.sdk.download.util;

import android.support.annotation.StringDef;

import com.eebbk.bfc.sdk.behavior.aidl.utils.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据摘要,加密界面工具类
 * 目前只有md5摘要的算法
 * <p>
 * Created by Simon on 2016/9/29.
 */
public class EncryptUtils {
    private static final String TAG = "BfcCommon_EncryptUtils";

    public final static String TYPE_MD5 = "MD5";
    public final static String SHA256 = "sha256";

    /******************** 存储相关常量 ********************/
    /**
     * Byte与Byte的倍数
     */
    public static final int BYTE = 1;
    /**
     * KB与Byte的倍数
     */
    public static final int KB = 1024;
    /**
     * MB与Byte的倍数
     */
    public static final int MB = 1024 * KB;
    /**
     * GB与Byte的倍数
     */
    public static final int GB = 1024 * MB;

    @StringDef({TYPE_MD5, SHA256})
    @Retention(RetentionPolicy.CLASS)
    @interface DigestType {
    }

    private EncryptUtils() {
    }

    /**
     * 获取摘要算法
     */
    public static MessageDigest getDigest(@DigestType String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (final NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * 获取md5摘要算法
     */
    public static MessageDigest getMd5Digest() {
        return getDigest(TYPE_MD5);
    }

    /**
     * MD5加密
     *
     * @param data 明文字节数组
     * @return 16进制密文
     */
    public static String md5Hex(byte[] data) {
        byte[] md5 = getMd5Digest().digest(data);
        return StringUtils.bytes2HexString(md5);
    }

    /**
     * 对字符串求md5
     *
     * @param data 明文字符串
     * @return 16进制密文
     */
    public static String md5Hex(String data) {
        return md5Hex(data.getBytes());
    }

    /**
     * 获取数据流数据的md5
     *
     * @param is 读取数据的输入流
     * @throws IOException 读取过程中, 有io异常 会抛出
     */
    public static String md5Hex(InputStream is) throws IOException {
        if (is == null) return null;

        byte[] md5 = updateDigest(getMd5Digest(), is).digest();
        return StringUtils.bytes2HexString(md5);
    }


    /**
     * 用指定的摘要算法,对输入数据求摘要
     *
     * @param digest 摘要算法
     * @param data   输入的数据流
     * @throws IOException 读取过程中, 有io异常 会抛出
     */
    public static MessageDigest updateDigest(final MessageDigest digest, final InputStream data) throws IOException {
        final byte[] buffer = new byte[KB];
        int read = data.read(buffer, 0, KB);

        try {
            while (read > -1) {
                digest.update(buffer, 0, read);
                read = data.read(buffer, 0, KB);
            }
        } finally {
            FileUtils.closeIO(data);
        }

        return digest;
    }

    /**
     * MD5加密文件大文件
     *
     * @param file 文件
     * @return 文件的MD5校验码
     */
    public static String md5Hex_BigFile(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        return md5Hex(fis);
    }

    /**
     * MD5加密文件 小文件
     *
     * @param file 文件
     * @return 文件的MD5校验码
     */
    public static String md5Hex_SmallFile(File file) {
        String md5Str = "";
        FileInputStream fis = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

            fis = new FileInputStream(file);
            byte[] buffer = new byte[(int) file.length()];
            fis.read(buffer);
            byte[] md5Bytes = messageDigest.digest(buffer);
            md5Str = MD5.bytesToHex(md5Bytes);
        } catch (Exception e) {
            LogUtil.e("----> md5Hex_SmallFile MD5ExValidator Error :" + e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
            }
        }

        return md5Str;
    }


    public static List<String> md5Hex_ArraysFile(File file) {
        String md5Str = "";
        FileInputStream fis = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            List<String> md5Arrs = new ArrayList<>();
            int readlen = 15 * MB;
            fis = new FileInputStream(file);

            while (readlen >= 15 * MB ) {
                if ((int) file.length() - readlen * md5Arrs.size() < readlen) {
                    readlen = (int) file.length() - readlen * md5Arrs.size();
                }

                byte[] buffer = new byte[readlen];
                fis.read(buffer);
                byte[] md5Bytes = messageDigest.digest(buffer);
                md5Str = MD5.bytesToHex(md5Bytes);
                md5Arrs.add(md5Str);
            }
            return md5Arrs;
        } catch (Exception e) {
            LogUtil.e("----> md5Hex_SmallFile MD5ExValidator Error :" + e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                    fis = null;
                }
            } catch (IOException e) {
            }
        }

        return null;
    }

}
