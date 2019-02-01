package com.eebbk.bfc.sdk.download.exception;

import java.util.Locale;

/**
 * Desc: 错误码列表
 * Author: llp
 * Create Time: 2016-09-26 16:48
 * Email: jacklulu29@gmail.com
 */

public class ErrorCode {

    /**
     * 下载项目编号
     */
    public static final String PROJECT = "03";

    /**
     * 模块名称
     */
    public static class Module {

        /**
         * 下载模块
         */
        public static final String DOWNLOAD = "01";
        /**
         * 校验模块
         */
        public static final String CHECK = "02";
        /**
         * 解压模块
         */
        public static final String UNPACK = "03";
        /**
         * 版本信息模块
         */
        public static final String VERSION = "04";
        /**
         * 其他
         */
        public static final String OTHER = "05";
    }

    /**
     * 具体异常编号
     */
    public static class ErrorNumber {
        public static final String UNKNOWN = "0001";
    }

    public static final String HEAD_DOWNLOAD = PROJECT + Module.DOWNLOAD;

    public static final String HEAD_CHECK = PROJECT + Module.CHECK;

    public static final String HEAD_UNPACK = PROJECT + Module.UNPACK;

    public static final String HEAD_VERSION = PROJECT + Module.VERSION;

    public static final String HEAD_OTHER = PROJECT + Module.OTHER;

    public static final class Values {
        public static final String DOWNLOAD_UNKNOWN                   = HEAD_DOWNLOAD + ErrorNumber.UNKNOWN;
        public static final String DOWNLOAD_PAUSE_BY_USER             = HEAD_DOWNLOAD + "0002";
        public static final String DOWNLOAD_PAUSE_BY_AUTO             = HEAD_DOWNLOAD + "0003";
        public static final String DOWNLOAD_STATE_IS_NO_WAIT          = HEAD_DOWNLOAD + "0004";
        public static final String DOWNLOAD_NO_NETWORK_PERMISSION     = HEAD_DOWNLOAD + "0005";
        public static final String DOWNLOAD_REQUEST_IS_NULL           = HEAD_DOWNLOAD + "0006";
        public static final String DOWNLOAD_NETWORK_NO_CONNECTION     = HEAD_DOWNLOAD + "0007";
        public static final String DOWNLOAD_NETWORK_UNUSABLE_DUE_TO_SIZE = HEAD_DOWNLOAD + "0008";
        public static final String DOWNLOAD_NETWORK_RECOMMENDED_UNUSABLE_DUE_TO_SIZE = HEAD_DOWNLOAD + "0009";
        public static final String DOWNLOAD_NETWORK_TYPE_DISALLOWED_BY_REQUESTOR = HEAD_DOWNLOAD + "0010";
        public static final String DOWNLOAD_NETWORK_BLOCKED           = HEAD_DOWNLOAD + "0011";
        public static final String DOWNLOAD_TEMP_FILE_PATH_IS_NULL    = HEAD_DOWNLOAD + "0012";
        public static final String DOWNLOAD_TEMP_FILE_IS_DIR          = HEAD_DOWNLOAD + "0013";
        public static final String DOWNLOAD_CREATE_TEMP_FILE_ERROR    = HEAD_DOWNLOAD + "0014";
        public static final String DOWNLOAD_CREATE_TEMP_FILE_IO_EXCEPTION    = HEAD_DOWNLOAD + "0015";
        public static final String DOWNLOAD_CREATE_TEMP_FILE_NO_FOUND    = HEAD_DOWNLOAD + "0016";
        public static final String DOWNLOAD_OUT_OF_SPACE              = HEAD_DOWNLOAD + "0017";
        public static final String DOWNLOAD_SEEK_FILE_IO_EXCEPTION    = HEAD_DOWNLOAD + "0018";
        public static final String DOWNLOAD_SEEK_FILE_ILLEGAL_ACCESS_EXCEPTION = HEAD_DOWNLOAD + "0019";
        public static final String DOWNLOAD_SET_LENGTH_ILLEGAL_ACCESS_EXCEPTION = HEAD_DOWNLOAD + "0020";
        public static final String DOWNLOAD_SET_LENGTH_IO_EXCEPTION   = HEAD_DOWNLOAD + "0021";
        public static final String DOWNLOAD_NOT_EQUAL_TOTAL           = HEAD_DOWNLOAD + "0022";
        public static final String DOWNLOAD_READ_OR_WRITE_IO_EXCEPTION   = HEAD_DOWNLOAD + "0023";
        public static final String DOWNLOAD_DELETE_TARGET_FILE_ERROR  = HEAD_DOWNLOAD + "0024";
        public static final String DOWNLOAD_RENAME_FILE_ERROR         = HEAD_DOWNLOAD + "0025";
        public static final String DOWNLOAD_REMOTE_EXCEPTION = HEAD_DOWNLOAD + "0026";
        public static final String DOWNLOAD_GREATER_THAN_TOTAL_SIZE   = HEAD_DOWNLOAD + "0027";
        public static final String DOWNLOAD_GET_TASK_CURSOR_IS_NULL   = HEAD_DOWNLOAD + "0028";
        public static final String DOWNLOAD_NETWORK_CANNOT_USE_ROAMING= HEAD_DOWNLOAD + "0029";
        public static final String DOWNLOAD_SOCKET_TIME_OUT           = HEAD_DOWNLOAD + "0030";
        public static final String DOWNLOAD_SAVE_DIR_IS_FILE          = HEAD_DOWNLOAD + "0031";
        public static final String DOWNLOAD_MKDIRS_FAILED             = HEAD_DOWNLOAD + "0032";
        public static final String DOWNLOAD_RELOAD_ALL_TASKS_CURSOR_IS_NULL   = HEAD_DOWNLOAD + "0033";
        public static final String DOWNLOAD_FILE_NAME_LENGTH_MORE_THAN_255   = HEAD_DOWNLOAD + "0034";
        public static final String DOWNLOAD_FILE_NAME_ILLEGAL         = HEAD_DOWNLOAD + "0035";
        public static final String DOWNLOAD_CONNECT_EXCEPTION         = HEAD_DOWNLOAD + "0036";
        public static final String DOWNLOAD_CACHE_FILE_NO_EXITS       = HEAD_DOWNLOAD + "0037";
        public static final String DOWNLOAD_NETWORK_NO_INIT_CONTEXT   = HEAD_DOWNLOAD + "0038";
        public static final String DOWNLOAD_NETWORK_UNKNOWN           = HEAD_DOWNLOAD + "0039";
        public static final String DOWNLOAD_NETWORK_NOT_ALLOW_MOBILE_2_G = HEAD_DOWNLOAD + "0040";
        public static final String DOWNLOAD_NETWORK_REQUEST_NO_ADDRESS = HEAD_DOWNLOAD + "0041";
        public static final String DOWNLOAD_AUTHENTICATION_FILE = HEAD_DOWNLOAD + "0042";
        public static final String DOWNLOAD_NETWORK_REQUEST_NO_ROUTE_TO_HOST = HEAD_DOWNLOAD + "0043";

        public static final String DOWNLOAD_CONNECT_SOCKET_TIME_OUT           = HEAD_DOWNLOAD + "0044";

        /* static String DOWNLOAD_RENAME_FILE_ERROR         = HEAD_DOWNLOAD + "0025";*/


        public static final String CHECK_UNKNOWN                      = HEAD_CHECK + ErrorNumber.UNKNOWN;
        public static final String CHECK_VALIDATOR_NO_FOUND           = HEAD_CHECK + "0002";
        public static final String CHECK_KEY_IS_INVALID               = HEAD_CHECK + "0003";
        public static final String CHECK_ERROR                        = HEAD_CHECK + "0004";
        public static final String CHECK_RUN_ERROR                    = HEAD_CHECK + "0005";
        public static final String CHECK_FAILED                       =  HEAD_CHECK + "0006";


        public static final String UNPACK_UNKNOWN                     = HEAD_UNPACK + ErrorNumber.UNKNOWN;
        public static final String UNPACK_UNPACKER_IS_NULL            = HEAD_UNPACK + "0002";
        public static final String UNPACK_NONSUPPORT                  = HEAD_UNPACK + "0003";
        public static final String UNPACK_ERROR                       = HEAD_UNPACK + "0004";
        public static final String UNPACK_RUN_ERROR                   = HEAD_UNPACK + "0005";
        public static final String UNPACK_PATH_IS_EMPTY               = HEAD_UNPACK + "0006";

        public static final String VERSION_UNKNOWN                    = HEAD_VERSION + ErrorNumber.UNKNOWN;


        public static final String OTHER_UNKNOWN                      = HEAD_OTHER + ErrorNumber.UNKNOWN;
    }

    /**
     * 生成自定义错误码，用于外部扩展时产生的错误
     *
     * @param errorHeard 错误码头部(错误码左边起第3~4位)
     * @param errorNumber 具体错误码值，最右边三位
     * @return 错误码
     */
    public static String getCustomErrorCode(String errorHeard, int errorNumber){
        return errorHeard + "F" + formatErrorNumber(errorNumber);
    }

    /**
     * 生成校验错误码
     *
     * @param errorNumber 具体错误码值，最右边四位
     * @return 错误码
     */
    public static String getCheckErrorCode(int errorNumber){
        return getCustomErrorCode(HEAD_CHECK, errorNumber);
    }

    /**
     * 生成解压错误码
     *
     * @param errorNumber 具体错误码值，最右边四位
     * @return 错误码
     */
    public static String getUnpackErrorCode(int errorNumber){
        return getCustomErrorCode(HEAD_UNPACK, errorNumber);
    }

    /**
     * 生成下载请求返回code异常情况的错误码
     *
     * @param responseCode 具体错误码值，最右边三位
     * @return 错误码
     */
    public static String getHTTPResponseErrorCode(int responseCode){
        return HEAD_DOWNLOAD + "E" + formatErrorNumber(responseCode);
    }

    private static String formatErrorNumber(int errorNumber){
        return String.format(Locale.getDefault(), "%03d", errorNumber);
    }

    public static String format(String errorCode, String msg){
        return "BFC Download ErrorCode: " + errorCode + "   Msg: " + msg;
    }

}
