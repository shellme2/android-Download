package com.eebbk.bfc.download.demo.check;

import com.eebbk.bfc.sdk.download.check.BaseValidator;
import com.eebbk.bfc.sdk.download.exception.DownloadCheckException;
import com.eebbk.bfc.sdk.download.exception.ErrorCode;
import com.eebbk.bfc.sdk.download.util.FileUtil;

import java.io.File;

/**
 * Desc: 自定义校验类
 * Author: llp
 * Create Time: 2016-11-07 22:28
 * Email: jacklulu29@gmail.com
 */

public class MyValidator extends BaseValidator {

    @Override
    public boolean checkFile(String fileSourcePath, String checkType, String checkCode)
            throws DownloadCheckException {
        File file = new File(fileSourcePath);
        if (!file.exists()) {
            throw new DownloadCheckException(ErrorCode.getUnpackErrorCode(1), " check source file["+fileSourcePath+"] not found ");
        }

        if (file.length() <= 0) {
            // 删除源文件
            FileUtil.deleteFile(fileSourcePath);
            throw new DownloadCheckException(ErrorCode.getUnpackErrorCode(2), " can't check file[length=" + file.length() +"], delete file[" + file + "]");
        }

        try{
            // 进度回调
            checkProgress(file.length(), 0);

            boolean result = true;
            // 校验文件代码写在这

            // 返回校验结果，true为通过，false不通过
            return result;
        } catch (Exception e){
            throw new DownloadCheckException(ErrorCode.getCheckErrorCode(2), " check error! ", e);
        }
    }

    public static class MyValidatorCreator implements BaseValidator.Creator {

        @Override
        public BaseValidator create() {
            return new MyValidator();
        }

        @Override
        public String getType() {
            // 返回校验器类型，随便自己定义
            return "MyValidatorType";
        }
    }
}
