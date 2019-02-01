package com.eebbk.bfc.sdk.download.check;

import android.text.TextUtils;

import com.eebbk.bfc.sdk.download.exception.DownloadCheckException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.EncryptUtils;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.MD5;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.List;

public class MD5ExValidator extends BaseValidator {

    private static final int KEY_LENGTH = 10;

    private static final int MAX_BLOCK_MD5_FOR_CHECK = 15 * 1024 * 1024;

    private static final int MD5_CHECK_SIZE = 100 * 1024 * 1024;

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

            //md5传进来是一个序列时，默认采用15M分片加载
            if ( md5s.length > 1 ) {
                List<String> md5Arrs = EncryptUtils.md5Hex_ArraysFile(file);
                LogUtil.d("-------> md5Hex_ArraysFile file.length()=" + md5Arrs.toString());
                if ( md5Arrs.size() !=  md5s.length ) {
                    throw new DownloadCheckException(ErrorCode.getCheckErrorCode(3), " check file[" + fileSourcePath + "] md5 failed ");
                } else {
                    for (int idx = 0; idx < md5s.length; idx ++ ) {
                        if ( !TextUtils.equals(md5s[idx], md5Arrs.get(idx))) {
                            throw new DownloadCheckException(ErrorCode.getCheckErrorCode(3), " check file[" + fileSourcePath + "] md5 failed ");
                        }
                    }
                    //完全都符合后，校验通过
                    return true;
                }

             } else {
                // 校验文件 小于300M时快速校验获取MD5,大文件时需要考虑到
                // 认证网络会下载网页, 内容错误
                LogUtil.d("-------> MD5ExValidator file.length()=" + file.length());
                String md5FileStr = "";
                if (file.length() < MD5_CHECK_SIZE) {
                    LogUtil.e("------->md5Hex_SmallFile MD5ExValidator file.length()=" + file.length());
                    md5FileStr = EncryptUtils.md5Hex_SmallFile(file);
                } else {
                    LogUtil.e("------->md5Hex_BigFile MD5ExValidator file.length()=" + file.length());
                    md5FileStr = EncryptUtils.md5Hex_BigFile(file);
                }
                LogUtil.e("------->MD5 FILE CHECK = md5FileStr ->" + md5FileStr +", check MD5:" + md5s[0] +", fileSourcePath ->" + fileSourcePath);
                if (md5FileStr.equalsIgnoreCase(md5s[0])) {
                    return true;
                } else {
                    throw new DownloadCheckException(ErrorCode.getCheckErrorCode(3), " check file[" + fileSourcePath + "] md5 failed ");
                }
            }
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

    public static class MD5ExValidatorCreator implements BaseValidator.Creator {
        @Override
        public BaseValidator create() {
            return new MD5ExValidator();
        }

        @Override
        public String getType() {
            return ITask.CheckType.MD5_EX;
        }
    }
}
