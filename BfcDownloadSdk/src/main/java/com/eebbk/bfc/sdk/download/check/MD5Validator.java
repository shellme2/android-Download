package com.eebbk.bfc.sdk.download.check;

import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.exception.DownloadCheckException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.MD5;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;

/**
 * Desc: MD5校验
 * Author: llp
 * Create Time: 2016-10-11 16:07
 * Email: jacklulu29@gmail.com
 */

public class MD5Validator extends BaseValidator {

    private static final int KEY_LENGTH = 10;

    private static final int MAX_BLOCK_MD5_FOR_CHECK = 15 * 1024 * 1024;

    private static final int MD5_CHECK_SIZE = 300 * 1024;

    @Override
    public boolean isKeyValid(String checkCode) {
        return checkCode != null && !TextUtils.isEmpty(checkCode) && checkCode.length() >= KEY_LENGTH;
    }

    @Override
    public boolean checkFile(String fileSourcePath, String checkType, String checkCode) throws DownloadCheckException {
        int mCurrentBytes = 0;
        int mTotalBytes = 0;
        long mMD5count = 0;
//        MD5 md5 = new MD5();
        String[] md5s = checkCode.split(";");

        if (LogUtil.isDebug()) {
            LogUtil.d(" check file sourceFilePath[" + fileSourcePath + "] checkType [" + checkType + "] checkCode[" + checkCode + "]");
        }

        File file = new File(fileSourcePath);
        if (!file.exists()) {
            throw new DownloadCheckException(ErrorCode.getCheckErrorCode(1), " check source file[" + fileSourcePath + "] not found ");
        }

        if (file.length() <= 0) {
            // 删除源文件
            FileUtil.deleteFile(fileSourcePath);
            throw new DownloadCheckException(ErrorCode.getCheckErrorCode(2), " can't check file[length=" + file.length() + "], delete file[" + file + "]");
        }

        try {
            // 进度回调
            checkProgress(file.length(), 0);
            /*if(mTotalBytes <= 0 ){
                throw new DownloadCheckException(ErrorCode.getCheckErrorCode(1), " file total bytes <= 0! ");
            }*/
            // 校验文件
            // TODO: 2016/11/6

            // 如果文件小于300kb 才进行校验, 全文件校验, 需要的时间太长
            // 认证网络会下载网页, 内容错误
            LogUtil.d(" file.length()=" + file.length());
            if (file.length() < MD5_CHECK_SIZE) {
                FileInputStream fis = null;
                try {
                    MessageDigest messageDigest = MessageDigest.getInstance("MD5");

                    fis = new FileInputStream(file);
                    byte[] buffer = new byte[(int) file.length()];
                    fis.read(buffer);
                    byte[] md5Bytes = messageDigest.digest(buffer);
                    String md5Str = MD5.bytesToHex(md5Bytes);

                    if (md5Str.equalsIgnoreCase(md5s[0])) {
                        return true;
                    } else {
                        throw new DownloadCheckException(ErrorCode.getCheckErrorCode(3), " check file[" + fileSourcePath + "] md5 failed ");
                    }
                } catch (Exception e) {
                    throw new DownloadCheckException(ErrorCode.getCheckErrorCode(3), " check file[" + fileSourcePath + "] md5 failed ", e);
                } finally {
                    try {
                        if (fis != null) {
                            fis.close();
                            fis = null;
                        }
                    } catch (IOException e) {
                    }
                }
            }


            return true;
        } catch (Exception e) {
            throw new DownloadCheckException(ErrorCode.getCheckErrorCode(2), " check error! ", e);
        }
    }

    private void checkDownloadMD5(long mTotalBytes, long mCurrentBytes, long mMD5count, MD5 mMD5, byte[] buff, int bytesRead, String[] mMD5Code) throws Exception {
        long current = mCurrentBytes + bytesRead;
        long md5D = current / MAX_BLOCK_MD5_FOR_CHECK;

        LogUtil.d("checkDownloadMD5  current: " + current + ", md5D: " + md5D
            + ", state.mTotalBytes: " + mTotalBytes
            + ", state.mMD5count: " + mMD5count);

        if (current == mTotalBytes) {
            mMD5.update(buff, bytesRead);
            mMD5.finalBuffer();
            String strMD5 = mMD5.digestMD5();

            LogUtil.d("checkDownloadMD5  strMD5: " + strMD5);

            if (!checkoutMD5((int) current
                / MAX_BLOCK_MD5_FOR_CHECK, strMD5, mMD5Code)) {
                throw new Exception("download data error for md5 error");
            }
            mMD5.init();
        } else if (md5D >= mMD5count) {
            int pos = (int) ((mMD5count * MAX_BLOCK_MD5_FOR_CHECK) - mCurrentBytes);
            byte[] buffFront = new byte[pos];
            mMD5.memcpy(buffFront, buff, 0, 0, pos);
            mMD5.update(buffFront, pos);
            mMD5.finalBuffer();
            String strMD5 = mMD5.digestMD5();
            if (!checkoutMD5((int) (mMD5count - 1), strMD5, mMD5Code)) {
                throw new Exception("download data error for md5 error");
            }
            //mMD5count++;
            int remain = bytesRead - pos;
            byte[] buffBehind = new byte[remain];
            mMD5.memcpy(buffBehind, buff, 0, pos, remain);
            mMD5.init();
            mMD5.update(buffBehind, remain);
        } else {
            mMD5.update(buff, bytesRead);
        }
    }

    private boolean checkoutMD5(int pos, String strMD5, String[] mMD5) {
        if (mMD5 == null || strMD5 == null || pos < 0 || pos >= mMD5.length
            || TextUtils.isEmpty(strMD5) || TextUtils.isEmpty(mMD5[pos])) {
            return false;
        }
        LogUtil.d("origin MD5 is:" + mMD5[pos] + ",new MD5 is:" + strMD5);

        return strMD5.equalsIgnoreCase(mMD5[pos]);
    }

    public static class MD5ValidatorCreator implements BaseValidator.Creator {
        @Override
        public BaseValidator create() {
            return new MD5Validator();
        }

        @Override
        public String getType() {
            return ITask.CheckType.MD5;
        }
    }
}
