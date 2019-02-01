package com.eebbk.bfc.sdk.download.unpack;

import android.os.Build;

import com.eebbk.bfc.sdk.download.exception.DownloadUnpackException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.dm.util.FileExtractor;

import java.io.File;

/**
 * Desc: 默认解压器
 * Author: llp
 * Create Time: 2016-10-11 10:01
 * Email: jacklulu29@gmail.com
 */

public class DownloadDefaultUnpacker extends DownloadBaseUnpacker {

    public static final String ZIP_SUFFIX = ".zip";
    public static final String RAR_SUFFIX = ".rar";

    @Override
    public boolean unpackByParseTargetPath(String sourceFilePath, String targetPath, boolean deleteSourceAfterUnpack)
            throws DownloadUnpackException {
        if(LogUtil.isDebug()){
            LogUtil.e(" CUP ABI: " + Build.CPU_ABI);
        }
        if(LogUtil.isDebug()){
            LogUtil.d(" unpack file sourceFilePath[" + sourceFilePath + "] targetPath [" + targetPath + "]");
        }

        File file = new File(sourceFilePath);
        if (!file.exists()) {
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(1), " unpack source file["+sourceFilePath+"] not found ");
        }

        if (file.length() <= 0) {
            // 删除源文件
            FileUtil.deleteFile(sourceFilePath);
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(2), " can't unpack file[length=" + file.length() +"], delete file[" + file + "]");
        }

        try {
            // 回调进度
            unpackProgress(file.length(), 0);

            if (FileExtractor.fileExtractor(sourceFilePath, targetPath)) {
                LogUtil.i("unpack success, auto delete source file[" + deleteSourceAfterUnpack + "]");
                if(deleteSourceAfterUnpack){
                    // 删除源文件
                    FileUtil.deleteFile(sourceFilePath);
                }
                return true;
            } else {
                LogUtil.e("unpack failed, delete unpack temp files");
                // 删除解压后的文件夹
                FileUtil.deleteFileAndDir(targetPath);
                return false;
            }

        } catch (Exception e) {
            LogUtil.e("unpack failed, delete unpack temp files");
            // 删除解压后的文件夹
            FileUtil.deleteFileAndDir(targetPath);
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(3), " unpack error ", e);
        }
    }

    @Override
    public boolean isSupport(String filePath) {
        if (filePath != null) {
            String lowCaseString = filePath.toLowerCase();
            if (lowCaseString.endsWith(ZIP_SUFFIX) || lowCaseString.endsWith(RAR_SUFFIX)) {
                return true;
            }
        }
        LogUtil.w(" no support unpack file[" + filePath + "]");
        return false;
    }

    public static class DownloadBaseUnpackerCreator implements DownloadBaseUnpacker.Creator {

        @Override
        public DownloadBaseUnpacker create() {
            return new DownloadDefaultUnpacker();
        }
    }
}
