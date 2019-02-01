package com.eebbk.bfc.sdk.download;

/**
 * Desc: 常量
 * Author: llp
 * Create Time: 2016-09-26 21:07
 * Email: jacklulu29@gmail.com
 */

public final class C {

    public static class DownloadMode {

        /**
         * 最低级级别下载模式，仅能下载，无法查看进度，中断必须从头下载
         */
        public static final int LOW = 0;
        /**
         * 普通下载模式，支持查看进度，中断必须从头下载
         */
        public static final int NORMAL = 1;
        /**
         * 断点下载模式，支持查看进度，支持断点下载
         */
        public static final int BREAK_POINT = 2;

    }

    public static class DownLoadConfig {
        /**
         * 初始默认同时下载任务数
         */
        public static final int DEFAULT_DOWNLOAD_TASK_COUNT = 1;
        /**
         * 断点下载模式下，默认开启多线程下载个数
         */
        public static final int DEFAULT_DOWNLOAD_THREAD_COUNT = 2;
        /**
         * 默认最大下载失败重试次数
         */
        public static final int DEFAULT_MAX_RETRIES = 7;
        /**
         * 下载中超时或IO异常的最大下载失败重试次数
         */
        public static final int DEFAULT_MAX_SPECIAL_RETRIES = 20;
        /**
         * 默认最大重定向次数
         */
        public static final int DEFAULT_MAX_REDIRECTS = 5;
        /**
         * 默认最大不排队任务数量，超过将不能添加
         */
        public static final int DEFAULT_MAX_NO_NEED_QUEUE_TASKS = 5;
        /** The maximum number of rows in the database (FIFO) */
        public static final int DEFAULT_MAX_DOWNLOADS = 1000;
        /** The minimum amount of time that has to elapse before the progress bar gets updated, in ms */
        public static final int DEFAULT_MIN_PROGRESS_TIME = 1250;
        /** The minimum amount of progress that has to be done before the progress bar gets updated */
        public static final int MIN_PROGRESS_STEP = 4096;
        /** The buffer size used to stream the data */
        public static final int BUFFER_SIZE = 4096;
        /**
         * 速度统计间隔时间(毫秒)
         */
        public static final int SPEED_STATISTICS_INTERVAL_TIME = 1000;
        /**
         * 1000毫秒
         */
        public static final int ONE_THOUSAND_MILLISECONDS = 1000;
        /**
         * 闲置时停止下载服务的延迟时间
         */
        public static final int STOP_SERVICE_DELAY_TIME_IF_IDLE =  60 * 1000; // 1 minutes

        /** The MIME type of APKs */
        public static final String MIME_TYPE_APK = "application/vnd.android.package";
    }

}
