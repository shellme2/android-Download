package com.eebbk.bfc.sdk.download.util;

import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.innosystec.unrar.Archive;
import de.innosystec.unrar.rarfile.FileHeader;

/**
 * Created by Simon on 2017/7/4.
 */

public class UnRar {
    private UnRar() {
    }

    public static boolean unrar(String sourceRar, String destDir) throws Exception {
        boolean isOk = false;
        Archive a = null;
        FileOutputStream fos = null;

        try {
            a = new Archive(new File(sourceRar));
            List<FileHeader> fileHeaderList = a.getFileHeaders();
            for (FileHeader fh : fileHeaderList) {

                String compressFileName = fh.getFileNameW().trim();
                if(!existChinese(compressFileName)) {
                    compressFileName = fh.getFileNameString().trim();
                }

                Log.i("bb", "fh1-->" + compressFileName);
                String destFileName;
                if (!compressFileName.contains(".")) {
                    compressFileName = compressFileName.replaceAll("\\\\", "/");
                    destFileName = destDir + "/" + compressFileName;
                    File dir = new File(destFileName);
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdirs();
                    }
                }

                if (!fh.isDirectory()) {
                    String destDirName;
                    compressFileName = compressFileName.replaceAll("\\\\", "/");
                    destFileName = destDir + "/" + compressFileName;
                    Log.d("bb", "destFileName" + destFileName);
                    destDirName = destFileName.substring(0, destFileName.lastIndexOf("/"));
                    Log.d("bb", "destDirName" + destDirName);
                    File dir = new File(destDirName);
                    if (!dir.exists() || !dir.isDirectory()) {
                        dir.mkdirs();
                    }

                    fos = new FileOutputStream(new File(destFileName));
                    a.extractFile(fh, fos);
                    fos.close();
                    fos = null;
                }
            }

            isOk = true;
            a.close();
            a = null;
            return isOk;
        } catch (Exception var21) {
            Log.i("bb", "e" + var21);
            throw var21;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                    fos = null;
                } catch (Exception var20) {
                    var20.printStackTrace();
                }
            }

            if (a != null) {
                try {
                    a.close();
                    a = null;
                } catch (Exception var19) {
                    Log.i("bb", "ew" + var19);
                    var19.printStackTrace();
                }
            }
        }
    }

    private static boolean existChinese(String str) {
        if(str.isEmpty()) {
            return false;
        }
        String regEx = "[\\u4e00-\\u9fa5]";
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.find();
    }
}
