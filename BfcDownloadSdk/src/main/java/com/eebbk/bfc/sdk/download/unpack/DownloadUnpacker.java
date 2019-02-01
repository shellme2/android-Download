package com.eebbk.bfc.sdk.download.unpack;

import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.exception.DownloadUnpackException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.FileUtil;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import com.eebbk.bfc.sdk.download.util.MediaUtils;
import com.eebbk.bfc.sdk.download.util.UnRar;

import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.FileHeader;

import java.io.File;
import java.util.List;

/**
 * Desc: 自定义解压器
 * Author: llp
 * Create Time: 2016-11-07 22:41
 * Email: jacklulu29@gmail.com
 */

public class DownloadUnpacker extends DownloadBaseUnpacker {

    @Override
    public boolean unpackByParseTargetPath(String sourceFilePath, String targetPath, boolean deleteSourceAfterUnpack)
            throws DownloadUnpackException {
        File file = new File(sourceFilePath);
        if (!file.exists()) {
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(1), " unpack source file[" + sourceFilePath + "] not found ");
        }

        if (file.length() <= 0) {
            // 删除源文件
            FileUtil.deleteFile(sourceFilePath);
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(2), " can't unpack file[length=" + file.length() + "], delete file[" + file + "]");
        }

        try {
            // 回调进度
            unpackProgress(file.length(), 0);

            if (fileExtractor(sourceFilePath, targetPath)) {
                LogUtil.i("unpack success, auto delete source file[" + deleteSourceAfterUnpack + "]");
                if (deleteSourceAfterUnpack) {
                    // 删除源文件
                    FileUtil.deleteFile(sourceFilePath);
                }
                // 解压成功后扫描目标文件夹, 加入媒体库
                // TODO: 2017/5/23 解压和扫描媒体库整体 可以考虑一起移动到FileUtils中
                if (DownloadInitHelper.getInstance().getGlobalConfig().isNeedScanMedia()) {
                    MediaUtils.scanFile(DownloadInitHelper.getInstance().getAppContext(), targetPath);
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

    private boolean fileExtractor(String sourceFilePath, String targetPath) throws DownloadUnpackException {
        // 解压代码写这里
        boolean result ;
        if (sourceFilePath.endsWith("rar")) {
            try {
                result = UnRar.unrar(sourceFilePath, targetPath);
            } catch (Exception e) {
                LogUtil.e(e, "unRar error");
                throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(5), " unRar error! ");
            }
        } else if (sourceFilePath.endsWith("zip")) {
            try {
                unZip(sourceFilePath, targetPath);
                result = true;
            } catch (Exception e) {
                LogUtil.e(e, "unZip error");
                throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(6), " unZip error! ");
            }
        } else {
            throw new DownloadUnpackException(ErrorCode.getUnpackErrorCode(7), " unSupport error! ");
        }
        // 返回结果，解压成功返回true，失败返回false
        return result;
    }

    @Override
    public boolean isSupport(String filePath) {
        // 该文件是否支持解压
        return filePath.endsWith("rar") || filePath.endsWith("zip");
    }

    public static class Creator implements DownloadBaseUnpacker.Creator {

        @Override
        public DownloadBaseUnpacker create() {
            return new DownloadUnpacker();
        }
    }

    private void unZip(String filePath, String destDir) throws ZipException {
        ZipFile zipFile = new ZipFile(filePath);

        List<FileHeader> fileHeaders = zipFile.getFileHeaders();
        int fileHeadersSize = fileHeaders.size();
        if (fileHeadersSize != 0 && !fileHeaders.get(fileHeadersSize - 1).isFileNameUTF8Encoded()) {
            zipFile = new ZipFile(filePath); //必须重新new一个ZipFile，否则setFileNameCharset不生效
            zipFile.setFileNameCharset("GBK");
        }

        File toDir = new File(destDir);
        if (toDir.isDirectory() && !toDir.exists()) {
            toDir.mkdirs();
        }

        zipFile.extractAll(toDir.getPath());
    }
}
