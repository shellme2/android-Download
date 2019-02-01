package com.eebbk.bfc.sdk.download.util;

import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.downloadmanager.CDNManager;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Desc: 文件操作工具类
 * Author: llp
 * Create Time: 2016-10-17 17:34
 * Email: jacklulu29@gmail.com
 */

public class FileUtil {

    private static final Pattern CONTENT_DISPOSITION_PATTERN =
            //Pattern.compile("attachment;\\s*filename\\s*=\\s*\"([^\"]*)\"");
            Pattern.compile("\\s*filename\\s*=\\s*\"([^\"]*)\"");
    private static final char START_CTRLCODE = 0x00;
    private static final char END_CTRLCODE = 0x1f;
    private static final char QUOTEDBL = 0x22;
    private static final char ASTERISK = 0x2A;
    private static final char SLASH = 0x2F;
    private static final char COLON = 0x3A;
    private static final char LESS = 0x3C;
    private static final char GREATER = 0x3E;
    private static final char QUESTION = 0x3F;
    private static final char BACKSLASH = 0x5C;
    private static final char BAR = 0x7C;
    private static final char DEL = 0x7F;
    private static final char UNDERSCORE = 0x5F;

    /** The MIME type of special DRM files */
    public static final String MIMETYPE_DRM_MESSAGE = "application/vnd.oma.drm.message";
    public static final String EXTENSION_INTERNAL_FWDL = ".fl";
    /**
     * The default extension for html files if we can't get one at the HTTP
     * level
     */
    public static final String DEFAULT_DL_HTML_EXTENSION = ".html";

    /**
     * The default extension for text files if we can't get one at the HTTP
     * level
     */
    public static final String DEFAULT_DL_TEXT_EXTENSION = ".txt";

    /**
     * The default extension for binary files if we can't get one at the HTTP
     * level
     */
    public static final String DEFAULT_DL_BINARY_EXTENSION = ".bin";

    private FileUtil(){
    }

    // 文件(不管文件夹)是否存在
    public static boolean isFileExist(File file){
        return file != null && file.exists() && file.isFile();
    }

    /**
     * 创建文件, 如果创建成功,则通知媒体库扫描
     * @throws IOException
     */
    public static boolean createNewFile(File file) throws IOException {
        try {
            if (file.createNewFile()){
                if(DownloadInitHelper.getInstance().getGlobalConfig().isNeedScanMedia()) {
                    MediaUtils.scanFile(DownloadInitHelper.getInstance().getAppContext(), file);
                }
                return true;
            }
        } catch (Exception e) {
            throw new IOException("create file failed", e);
        }
        return false;
    }

    /**
     * 重命名文件同时更新媒体库
     */
    public static boolean rename(File srcFile, File desFile){
        if (srcFile.renameTo(desFile)){
            MediaUtils.removeFile(DownloadInitHelper.getInstance().getAppContext(), srcFile);
            if(DownloadInitHelper.getInstance().getGlobalConfig().isNeedScanMedia()) {
                MediaUtils.scanFile(DownloadInitHelper.getInstance().getAppContext(), desFile);
            }
            return true;
        }
        return false;
    }

    /**
     * 删除文件  同时更新媒体库
     */
    public static boolean deleteFile(File file){
        if (file.delete()){
            MediaUtils.removeFile(DownloadInitHelper.getInstance().getAppContext(), file);
            return true;
        }

        LogUtil.w(" delete file path=" + file.getAbsolutePath() + ", failed!" );
        return false;
    }

    /**
     * delete file
     *
     * @param path file path
     * @return true成功，false失败
     */
    public static boolean deleteFile(String path){
        if (TextUtils.isEmpty(path)){
            LogUtil.w(" delete file fail, path=null!");
            return false;
        }

        final File file = new File(path);

        if (!file.exists()) {
            LogUtil.w(" delete file fail, not exists, path=" + path);
            return false;
        }
        return deleteFile(file);
    }

    /**
     * 删除某个文件夹下的所有文件夹和文件
     *
     * @param deletePath 要删除的路径
     * @return boolean true成功，false失败
     */
    public static boolean deleteFileAndDir(String deletePath) {
        try {
            File file = new File(deletePath);
            // 当且仅当此抽象路径名表示的文件存在，且是一个目录时，返回 true
            boolean result = file.exists();
            LogUtil.i(" delete path: " + deletePath + " exists[" + result + "]");
            if (result) {
                if (!file.isDirectory()) {
                    result = file.delete();
                    LogUtil.i(" delete path: " + file.getPath() + " result [" + result + "]");
                } else {
                    String[] fileList = file.list();
                    if (fileList != null && fileList.length > 0) {
                        for (String filePath : fileList) {
                            if (!TextUtils.isEmpty(filePath)) {
                                File delFile = new File(deletePath + File.separator + filePath);
                                if (!delFile.isDirectory()) {
                                    result = delFile.delete();
                                    MediaUtils.removeFile(DownloadInitHelper.getInstance().getAppContext(), delFile);
                                    LogUtil.i(" delete path: " + delFile.getPath() + " result [" + result + "]");
                                } else if (delFile.isDirectory()) {
                                    deleteFileAndDir(deletePath + File.separator + filePath);
                                }
                            }
                        }
                    }
                    result = file.delete();
                    LogUtil.i(" delete path: " + file.getPath() + " result [" + result + "]");
                }
            }
        } catch (Exception e){
            LogUtil.e(e, " delete file and dir error ");
        }
        return true;
    }

    /**
     * <pre>查找文件名称
     * 首先从请求头content-disposition中查找，
     * 如果没有，则从content-location中查找，
     * 如果content-location没有，则从url中查找，
     * 如果url中也没有，则通过url生成md5串来作为文件名称
     * </pre>
     * @param url 下载地址
     * @param contentDisposition 请求头中contentDisposition
     * @param contentLocation 请求头中contentLocation
     * @return 文件名
     */
    public static @NonNull String findFileName(String url, String contentDisposition, String contentLocation) {
        String filename = null;
        // If we couldn't do anything with the hint, move toward the content disposition
        if (contentDisposition != null) {
            filename = parseContentDisposition(contentDisposition);
            if (!TextUtils.isEmpty(filename)) {
                int index = filename.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = filename.substring(index);
                }
            }
            LogUtil.d("getting filename from content-disposition [" + contentDisposition + "] fileName[" + filename+"]");
        }

        // If we still have nothing at this point, try the content location
        if (TextUtils.isEmpty(filename) && contentLocation != null) {
            String decodedContentLocation = Uri.decode(contentLocation);
            if (decodedContentLocation != null
                    && !decodedContentLocation.endsWith("/")
                    && decodedContentLocation.indexOf('?') < 0) {
                int index = decodedContentLocation.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedContentLocation.substring(index);
                } else {
                    filename = decodedContentLocation;
                }
                LogUtil.d( "getting filename from content-location [" + decodedContentLocation + "] fileName[" + filename+"]");
            }
        }

        // If all the other http-related approaches failed, use the plain uri
        if (TextUtils.isEmpty(filename)) {
            String originalUrl = url;
            if (!TextUtils.isEmpty(url) && CDNManager.isXYVodUrl(url)) {
                originalUrl = CDNManager.transformSourceUrl(url);
            }
            String decodedUrl = Uri.decode(originalUrl);
            if (decodedUrl != null && !decodedUrl.endsWith("/") && decodedUrl.indexOf('?') < 0) {
                int index = decodedUrl.lastIndexOf('/') + 1;
                if (index > 0) {
                    filename = decodedUrl.substring(index);
                }
            }
            LogUtil.d( "getting filename from uri [" + decodedUrl + "] fileName[" + filename + "]");
        }

        // Finally, if couldn't get filename from URI, get a generic filename
        if (TextUtils.isEmpty(filename)) {
            filename = DownloadUtils.generateFileName(url);
            LogUtil.d( " generate filename[" + filename + "]");
        }

        // The VFAT file system is assumed as target for downloads.
        // Replace invalid characters according to the specifications of VFAT.
        filename = replaceInvalidVfatCharacters(filename);

        return filename;
    }

    /**
     * The same to com.android.providers.downloads.Helpers#parseContentDisposition.
     * </p>
     * Parse the Content-Disposition HTTP Header. The format of the header
     * is defined here: http://www.w3.org/Protocols/rfc2616/rfc2616-sec19.html
     * This header provides a filename for content that is going to be
     * downloaded to the file system. We only support the attachment type.
     */
    public static String parseContentDisposition(String contentDisposition) {
        if (contentDisposition == null) {
            return null;
        }

        try {
            Matcher m = CONTENT_DISPOSITION_PATTERN.matcher(contentDisposition);
            if (m.find()) {
                return m.group(1);
            }
        } catch (IllegalStateException ex) {
            // This function is defined as returning null when it can't parse the header
        }
        return null;
    }

    /**
     * Replace invalid filename characters according to
     * specifications of the VFAT.
     * Package-private due to testing.
     */
    private static String replaceInvalidVfatCharacters(String filename) {
        StringBuilder sb = new StringBuilder();
        char ch;
        boolean isRepetition = false;
        for (int i = 0; i < filename.length(); i++) {
            ch = filename.charAt(i);
            if ((START_CTRLCODE <= ch && ch <= END_CTRLCODE) ||
                    ch == QUOTEDBL ||
                    ch == ASTERISK ||
                    ch == SLASH ||
                    ch == COLON ||
                    ch == LESS ||
                    ch == GREATER ||
                    ch == QUESTION ||
                    ch == BACKSLASH ||
                    ch == BAR ||
                    ch == DEL){
                if (!isRepetition) {
                    sb.append(UNDERSCORE);
                    isRepetition = true;
                }
            } else {
                sb.append(ch);
                isRepetition = false;
            }
        }
        return sb.toString();
    }

    /**
     * Checks if the Media Type needs to be DRM converted
     *
     * @param mimetype Media type of the content
     * @return True if convert is needed else false
     */
    private static boolean isDrmConvertNeeded(String mimetype) {
        return MIMETYPE_DRM_MESSAGE.equals(mimetype);
    }

    /**
     * Modifies the file extension for a DRM Forward Lock file NOTE: This
     * function shouldn't be called if the file shouldn't be DRM converted
     */
    private static String modifyDrmFwLockFileExtension(String filename) {
        if (filename != null) {
            int extensionIndex;
            extensionIndex = filename.lastIndexOf(".");
            if (extensionIndex != -1) {
                filename = filename.substring(0, extensionIndex);
            }
            filename = filename.concat(EXTENSION_INTERNAL_FWDL);
        }
        return filename;
    }

    public static String findExtensionFromMimeType(String mimeType, boolean useDefaults) {
        String extension = null;
        if (mimeType != null) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            if (LogUtil.isDebug()) {
                LogUtil.v( " find extension from mimeType[" + mimeType+"] extension["+extension+"]");
            }
        }
        if (extension == null) {
            if (mimeType != null && mimeType.toLowerCase().startsWith("text/")) {
                if ("text/html".equalsIgnoreCase(mimeType)) {
                    extension = DEFAULT_DL_HTML_EXTENSION;
                } else if (useDefaults) {
                    extension = DEFAULT_DL_TEXT_EXTENSION;
                }
                if (LogUtil.isDebug()) {
                    LogUtil.v( "find extension starts with text, extension["+extension+"]");
                }
            } else if (useDefaults) {
                extension = DEFAULT_DL_BINARY_EXTENSION;
                if (LogUtil.isDebug()) {
                    LogUtil.v( "use default extension["+extension+"]");
                }
            }
        }
        return extension;
    }

    /**
     * 获取默认保存路径
     * @return 保存路径
     */
    public static String getDefaultSaveRootPath() {
        return Environment.getDownloadCacheDirectory().getAbsolutePath();
    }

    /**
     * Java文件操作 从全路径中获取不带后缀的文件名
     */
    public static String getFileNameNoEx(String allSavePath) {
        File targetFile = new File(allSavePath);
        String filename = targetFile.getName();
        LogUtil.d("getFileNameNoEx : " + filename);
        if ((filename != null) && (filename.length() > 0)) {
            int dot = filename.lastIndexOf('.');
            if ((dot > -1) && (dot < (filename.length()))) {
                return filename.substring(0, dot);
            }
        }

        return filename;
    }
}
