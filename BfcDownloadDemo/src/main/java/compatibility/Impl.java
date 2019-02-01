package compatibility;


import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Implementation details
 *
 * Exposes constants used to interact with the download manager's
 * content provider.
 * The constants URI ... STATUS are the names of columns in the downloads table.
 * share 包中的常量类需要与 /module/DownloadSystem 中的同名包中的内容保持一致
 *
 * @hide
 */
public final class Impl implements BaseColumns {

    /**
     * The permission to access the download manager
     * 访问下载管理器的权限
     */
    public static final String PERMISSION_ACCESS = "com.eebbk.bfc.permission.ACCESS_BBK_DOWNLOAD_MANAGER";

    /**
     * The permission to access the download manager's advanced functions
     * 
     */
    public static final String PERMISSION_ACCESS_ADVANCED =
            "com.eebbk.bfc.permission.ACCESS_BBK_DOWNLOAD_MANAGER_ADVANCED";

    /**
     * The permission to access the all the downloads in the manager.
     * 访问管理器中所有下载的权限
     */
    public static final String PERMISSION_ACCESS_ALL =
            "com.eebbk.bfc.permission.ACCESS_ALL_BBK_DOWNLOADS";

    /**
     * The permission to directly access the download manager's cache
     * directory
     * 直接访问下载管理器的Cache目录的权限
     */
    public static final String PERMISSION_CACHE = "android.permission.ACCESS_CACHE_FILESYSTEM";

    /**
     * The permission to send broadcasts on download completion
     * 下载完成后发下载完成广播的权限
     */
    public static final String PERMISSION_SEND_INTENTS =
            "com.eebbk.bfc.permission.SEND_BBK_DOWNLOAD_COMPLETED_INTENTS";

    /**
     * The permission to download files to the cache partition that won't be automatically
     * purged when space is needed.
     * 下载文件时，不会去自动释放空间的权限
     */
    public static final String PERMISSION_CACHE_NON_PURGEABLE =
            "com.eebbk.bfc.permission.BBK_DOWNLOAD_CACHE_NON_PURGEABLE";

    /**
     * The permission to download files without any system notification being shown.
     * 不需要任何通知栏的权限
     */
    public static final String PERMISSION_NO_NOTIFICATION =
            "com.eebbk.bfc.permission.BBK_DOWNLOAD_WITHOUT_NOTIFICATION";
    
    /**
     * The content:// URI to access downloads owned by the caller's UID.
     * 
     */
    public static final Uri CONTENT_URI =
            Uri.parse("content://bbk_downloads/my_downloads");

    /**
     * The content URI for accessing all downloads across all UIDs (requires the
     * ACCESS_ALL_BBK_DOWNLOADS permission).
     */
    public static final Uri ALL_DOWNLOADS_CONTENT_URI =
            Uri.parse("content://bbk_downloads/all_downloads");

    /** URI segment to access a publicly accessible downloaded file */
    public static final String PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT = "public_downloads";

    /**
     * The content URI for accessing publicly accessible downloads (i.e., it requires no
     * permissions to access this downloaded file)
     */
    public static final Uri PUBLICLY_ACCESSIBLE_DOWNLOADS_URI =
            Uri.parse("content://bbk_downloads/" + PUBLICLY_ACCESSIBLE_DOWNLOADS_URI_SEGMENT);

    
    /**
     * <pre>
     * add by llp 20160506 对数据库进行业务操作的URI，暂时支持对网络类型修改
     * 后面必须跟上业务码，区分不同业务行为
     * 
     * 使用方式：Uri uri = Uri.parse(Impl.DOWNLOADS_URI_DO_ACTION + "/" + 业务码);
     * 
     * 业务码：{@link #DO_ACTION_MODIFY_NETWORK}
     * 
     * 便于将业务操作Uri与通知Uri区分出来
     * </pre>
     */
    public static final String DOWNLOADS_URI_DO_ACTION = "content://bbk_downloads/do_action";
    /**
     * <pre>
     * add by llp 20160506 操作业务码，表示要修改使用的网络类型
     * 
     * 跟在DOWNLOADS_URI_DO_ACTION后面组成操作Uri
     * 
     * </pre>
     */
    public static final String DO_ACTION_MODIFY_NETWORK = "action_modify_network";
    
    /**
     * Broadcast Action: this is sent by the download manager to the app
     * that had initiated a download when that download completes. The
     * download's content: uri is specified in the intent's data.
     * 下载完成的ACTION
     */
    public static final String ACTION_DOWNLOAD_COMPLETED =
            "android.intent.action.DOWNLOAD_COMPLETED";

    /**
     * Broadcast Action: this is sent by the download manager to the app
     * that had initiated a download when the user selects the notification
     * associated with that download. The download's content: uri is specified
     * in the intent's data if the click is associated with a single download,
     * or Downloads.CONTENT_URI if the notification is associated with
     * multiple downloads.
     * Note: this is not currently sent for downloads that have completed
     * successfully.
     * 点击通知栏的ACTION
     */
    public static final String ACTION_NOTIFICATION_CLICKED =
            "android.intent.action.DOWNLOAD_NOTIFICATION_CLICKED";
    
	/**
	 * BBK download info for Extras data
	 * <P>Type: TEXT</P>
	 * <P>Owner can Init/Read</P>
	 * 
	 * added by xym 2014/07/16
	 */
	public static final String COLUMN_EXTRAS = "extras";
	
	/**
	 * file size just for display
	 * <P>Type: INTEGER</P>
     * <P>Owner can Init/Read</P>
	 */
	public static final String	COLUMN_FILE_SIZE = "file_size";
	
	/**
	 * file extension for APP checksum
	 * <P>Type: TEXT</P>
     * <P>Owner can Init/Read</P>
	 */
	public static final String  COLUMN_FILE_EXTENSION  = "fill_extension";
		
	/**
	 * reserve data,maybe a JSON string
	 */
	public static final String COLUMN_RESERVE = "reserve";
	
    /**
     * The name of the column containing the URI of the data being downloaded.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_URI = "uri";

    /**
     * The name of the column containing application-specific data.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_APP_DATA = "entity";

    /**
     * The name of the column containing the flags that indicates whether
     * the initiating application is capable of verifying the integrity of
     * the downloaded file. When this flag is set, the download manager
     * performs downloads and reports success even in some situations where
     * it can't guarantee that the download has completed (e.g. when doing
     * a byte-range request without an ETag, or when it can't determine
     * whether a download fully completed).
     * <P>Type: BOOLEAN</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_NO_INTEGRITY = "no_integrity";

    /**
     * The name of the column containing the filename that the initiating
     * application recommends. When possible, the download manager will attempt
     * to use this filename, or a variation, as the actual name for the file.
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_FILE_NAME_HINT = "hint";

    /**
     * The name of the column containing the filename where the downloaded data
     * was actually stored.
     * <P>Type: TEXT</P>
     * <P>Owner can Read</P>
     */
    public static final String _DATA = "_data";
    
    /**
     * The name of the column containing the priority that indicates it's download order.
     * add by ly20050516@gmail.com
     * 
     * <P>Type: INTEGER</P>
     * <P>Owner can INTEGER</P>
     */
    public static final String COLUMN_PRIORITY = "priority";

    /**
     * The name of the column containing the MIME type of the downloaded data.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_MIME_TYPE = "mimetype";

    /**
     * The name of the column containing the flag that controls the destination
     * of the download. See the DESTINATION_* constants for a list of legal values.
     * <P>Type: INTEGER</P>
     * <P>Owner can INTEGER</P>
     */
    public static final String COLUMN_DESTINATION = "destination";

    /**
     * The name of the column containing the flags that controls whether the
     * download is displayed by the UI. See the VISIBILITY_* constants for
     * a list of legal values.
     * <P>Type: INTEGER</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_VISIBILITY = "visibility";

    /**
     * The name of the column containing the current control state  of the download.
     * Applications can write to this to control (pause/resume) the download.
     * the CONTROL_* constants for a list of legal values.
     * <P>Type: INTEGER</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_CONTROL = "control";

    /**
     * The name of the column containing the current status of the download.
     * Applications can read this to follow the progress of each download. See
     * the STATUS_* constants for a list of legal values.
     * <P>Type: INTEGER</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_STATUS = "status";

    /**
     * The name of the column containing the date at which some interesting
     * status changed in the download. Stored as a System.currentTimeMillis()
     * value.
     * <P>Type: BIGINT</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_LAST_MODIFICATION = "lastmod";

    /**
     * The name of the column containing the package name of the application
     * that initiating the download. The download manager will send
     * notifications to a component in this package when the download completes.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_NOTIFICATION_PACKAGE = "notificationpackage";

    /**
     * The name of the column containing the component name of the class that
     * will receive notifications associated with the download. The
     * package/class combination is passed to
     * Intent.setClassName(String,String).
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_NOTIFICATION_CLASS = "notificationclass";

    /**
     * If extras are specified when requesting a download they will be provided in the intent that
     * is sent to the specified class and package when a download has finished.
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_NOTIFICATION_EXTRAS = "notificationextras";

    /**
     * The name of the column contain the values of the cookie to be used for
     * the download. This is used directly as the value for the Cookie: HTTP
     * header that gets sent with the request.
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_COOKIE_DATA = "cookiedata";

    /**
     * The name of the column containing the user agent that the initiating
     * application wants the download manager to use for this download.
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_USER_AGENT = "useragent";

    /**
     * The name of the column containing the referer (sic) that the initiating
     * application wants the download manager to use for this download.
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_REFERER = "referer";

    /**
     * The name of the column containing the total size of the file being
     * downloaded.
     * <P>Type: INTEGER</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_TOTAL_BYTES = "total_bytes";

    /**
     * The name of the column containing the size of the part of the file that
     * has been downloaded so far.
     * <P>Type: INTEGER</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_CURRENT_BYTES = "current_bytes";
    
    /**
     * The name of the column express the speed of the current downloaded.
     * <P>Type: INTEGER</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_CURRENT_SPEED = "current_speed";
    
    
    /**
     * The name of the column for check validity or integrity of the download file
     * <P>Type: TEXT</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_MD5 = "md5";
    
    /**
     * The name of the column for check validity or integrity of the download MD5 buffer
     * 
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_MD5_BUFFER = "md5_buffer";
    
    /**
     * The name of the column for check validity or integrity of the download MD5 count
     * 
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_MD5_COUNT = "md5_count";
    /**
     * The name of the column for check validity or integrity of the download MD5 buffer
     * 
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_MD5_STATE = "md5_state";
    /**
     * The name of the column where the initiating application can provide the
     * UID of another application that is allowed to access this download. If
     * multiple applications share the same UID, all those applications will be
     * allowed to access this download. This column can be updated after the
     * download is initiated. This requires the permission
     * com.eebbk.bfc.permission.ACCESS_BBK_DOWNLOAD_MANAGER_ADVANCED.
     * <P>Type: INTEGER</P>
     * <P>Owner can Init</P>
     */
    public static final String COLUMN_OTHER_UID = "otheruid";

    /**
     * The name of the column where the initiating application can provided the
     * title of this download. The title will be displayed ito the user in the
     * list of downloads.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_TITLE = "title";

    /**
     * The name of the column where the initiating application can provide the
     * description of this download. The description will be displayed to the
     * user in the list of downloads.
     * <P>Type: TEXT</P>
     * <P>Owner can Init/Read/Write</P>
     */
    public static final String COLUMN_DESCRIPTION = "description";

    /**
     * The name of the column indicating whether the download was requesting through the public
     * API.  This controls some differences in behavior.
     * <P>Type: BOOLEAN</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_IS_PUBLIC_API = "is_public_api";

    /**
     * The name of the column holding a bitmask of allowed network types.  This is only used for
     * public API downloads.
     * <P>Type: INTEGER</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_ALLOWED_NETWORK_TYPES = "allowed_network_types";

    /**
     * The name of the column indicating whether roaming connections can be used.  This is only
     * used for public API downloads.
     * <P>Type: BOOLEAN</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_ALLOW_ROAMING = "allow_roaming";

    /**
     * The name of the column indicating whether metered connections can be used.  This is only
     * used for public API downloads.
     * <P>Type: BOOLEAN</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_ALLOW_METERED = "allow_metered";
    
    public static final String COLUMN_NEED_QUEUE = "need_queue";

    /**
     * Whether or not this download should be displayed in the system's Downloads UI.  Defaults
     * to true.
     * <P>Type: INTEGER</P>
     * <P>Owner can Init/Read</P>
     */
    public static final String COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI = "is_visible_in_downloads_ui";

    /**
     * If true, the user has confirmed that this download can proceed over the mobile network
     * even though it exceeds the recommended maximum size.
     * <P>Type: BOOLEAN</P>
     */
    public static final String COLUMN_BYPASS_RECOMMENDED_SIZE_LIMIT =
        "bypass_recommended_size_limit";

    /**
     * Set to true if this download is deleted. It is completely removed from the database
     * when MediaProvider database also deletes the metadata asociated with this downloaded file.
     * <P>Type: BOOLEAN</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_DELETED = "deleted";
    
    public static final String COLUMN_DELETED_FILE = "deleted_file";

    /**
     * The URI to the corresponding entry in MediaProvider for this downloaded entry. It is
     * used to delete the entries from MediaProvider database when it is deleted from the
     * downloaded list.
     * <P>Type: TEXT</P>
     * <P>Owner can Read</P>
     */
    public static final String COLUMN_MEDIAPROVIDER_URI = "mediaprovider_uri";

    /**
     * The column that is used to remember whether the media scanner was invoked.
     * It can take the values: null or 0(not scanned), 1(scanned), 2 (not scannable).
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_MEDIA_SCANNED = "scanned";

    /**
     * The column with errorMsg for a failed downloaded.
     * Used only for debugging purposes.
     * <P>Type: TEXT</P>
     */
    public static final String COLUMN_ERROR_MSG = "errorMsg";

    /**
     *  This column stores the source of the last update to this row.
     *  This column is only for internal use.
     *  Valid values are indicated by LAST_UPDATESRC_* constants.
     * <P>Type: INT</P>
     */
    public static final String COLUMN_LAST_UPDATESRC = "lastUpdateSrc";

    public static final String COLUMN_ALLOW_ADJUST_SAVEPATH = "allow_adjust_save_path";
    
    /**
     * default value for {@link #COLUMN_LAST_UPDATESRC}.
     * This value is used when this column's value is not relevant.
     */
    public static final int LAST_UPDATESRC_NOT_RELEVANT = 0;

    /**
     * One of the values taken by {@link #COLUMN_LAST_UPDATESRC}.
     * This value is used when the update is NOT to be relayed to the DownloadService
     * (and thus spare DownloadService from scanning the database when this change occurs)
     */
    public static final int LAST_UPDATESRC_DONT_NOTIFY_DOWNLOADSVC = 1;

    /*
     * Lists the destinations that an application can specify for a download.
     */

    /**
     * This download will be saved to the external storage. This is the
     * default behavior, and should be used for any file that the user
     * can freely access, copy, delete. Even with that destination,
     * unencrypted DRM files are saved in secure internal storage.
     * Downloads to the external destination only write files for which
     * there is a registered handler. The resulting files are accessible
     * by filename to all applications.
     */
    public static final int DESTINATION_EXTERNAL = 0;

    /**
     * This download will be saved to the download manager's private
     * partition. This is the behavior used by applications that want to
     * download private files that are used and deleted soon after they
     * get downloaded. All file types are allowed, and only the initiating
     * application can access the file (indirectly through a content
     * provider). This requires the
     * com.eebbk.bfc.permission.ACCESS_BBK_DOWNLOAD_MANAGER_ADVANCED permission.
     */
    public static final int DESTINATION_CACHE_PARTITION = 1;

    /**
     * This download will be saved to the download manager's private
     * partition and will be purged as necessary to make space. This is
     * for private files (similar to CACHE_PARTITION) that aren't deleted
     * immediately after they are used, and are kept around by the download
     * manager as long as space is available.
     */
    public static final int DESTINATION_CACHE_PARTITION_PURGEABLE = 2;

    /**
     * This download will be saved to the download manager's private
     * partition, as with DESTINATION_CACHE_PARTITION, but the download
     * will not proceed if the user is on a roaming data connection.
     */
    public static final int DESTINATION_CACHE_PARTITION_NOROAMING = 3;

    /**
     * This download will be saved to the location given by the file URI in
     * {@link #COLUMN_FILE_NAME_HINT}.
     */
    public static final int DESTINATION_FILE_URI = 4;

    /**
     * This download will be saved to the system cache ("/cache")
     * partition. This option is only used by system apps and so it requires
     * android.permission.ACCESS_CACHE_FILESYSTEM permission.
     */
    public static final int DESTINATION_SYSTEMCACHE_PARTITION = 5;

    /**
     * This download was completed by the caller (i.e., NOT downloadmanager)
     * and caller wants to have this download displayed in Downloads App.
     */
    public static final int DESTINATION_NON_DOWNLOADMANAGER_DOWNLOAD = 6;

    /**
     * This download is allowed to run.
     */
    public static final int CONTROL_RUN = 0;

    /**
     * This download must pause at the first opportunity.
     */
    public static final int CONTROL_PAUSED = 1;
    
    /**
     * This download pause for connectivity change to mobile.
     */
    public static final int CONTROL_PAUSED_FOR_CONNECTED_TO_MOBILE = 2;

    /*
     * Lists the states that the download manager can set on a download
     * to notify applications of the download progress.
     * The codes follow the HTTP families<br>
     */
    public static final int[] INFORMATIONAL_STATUS_CODE = new int[]{100,200};
    public static final int[] SUCCESS_STATUS_CODE = new int[]{200,300};
    public static final int[] CLIENT_STATUS_CODE = new int[]{400,500};
    public static final int[] SERVER_STATUS_CODE = new int[]{500,600};
    

    /**
     * add by llp 20160506 下载状态初始值（默认值）
     */
    public static final int STATUS_INIT = 0;

    /**
     * We don't handle redirect error,at the moment.
     */
    public static final int STATUS_UNHANDLED_REDIRECT_ERROR = 188;

    /**
     * This download couldn't be completed because no external storage
     * device was found.  Typically, this is because the external SD card is not
     * mounted.Maybe this status just be appropriate for our PAD 
     * 
     * added by ly20050516@gmail.com
     */
    public static final int STATUS_EXTERNAL_DEVICE_NOT_FOUND_ERROR = 189;
    /**
     * This download hasn't stated yet
     */
    public static final int STATUS_PENDING = 190;

    /**
     * This download has started
     */
    public static final int STATUS_RUNNING = 192;

    /**
     * This download has been paused by the owning app.
     */
    public static final int STATUS_PAUSED_BY_APP = 193;
    
    /**
     * This download has been paused for connectivity change to mobile.
     */
    public static final int STATUS_PAUSED_FOR_CONNECTED_TO_MOBILE = 101;

    /**
     * This download encountered some network error and is waiting before retrying the request.
     */
    public static final int STATUS_WAITING_TO_RETRY = 194;

    /**
     * This download is waiting for network connectivity to proceed.
     */
    public static final int STATUS_WAITING_FOR_NETWORK = 195;

    /**
     * This download exceeded a size limit for mobile networks and is waiting for a Wi-Fi
     * connection to proceed.
     */
    public static final int STATUS_QUEUED_FOR_WIFI = 196;

    /**
     * This download couldn't be completed due to insufficient storage
     * space.  Typically, this is because the SD card is full.
     */
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 198;

    /**
     * This download couldn't be completed because no external storage
     * device was found.  Typically, this is because the SD card is not
     * mounted.
     */
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 199;

    /**
     * This download has successfully completed.
     * Warning: there might be other status values that indicate success
     * in the future.
     * Use isSucccess() to capture the entire category.
     */
    public static final int STATUS_SUCCESS = 200;

    /**
     * This request couldn't be parsed. This is also used when processing
     * requests with unknown/unsupported URI schemes.
     */
    public static final int STATUS_BAD_REQUEST = 400;

    /**
     * This download can't be performed because the content type cannot be
     * handled.
     */
    public static final int STATUS_NOT_ACCEPTABLE = 406;

    /**
     * This download cannot be performed because the length cannot be
     * determined accurately. This is the code for the HTTP error "Length
     * Required", which is typically used when making requests that require
     * a content length but don't have one, and it is also used in the
     * client when a response is received whose length cannot be determined
     * accurately (therefore making it impossible to know when a download
     * completes).
     */
    public static final int STATUS_LENGTH_REQUIRED = 411;

    /**
     * This download was interrupted and cannot be resumed.
     * This is the code for the HTTP error "Precondition Failed", and it is
     * also used in situations where the client doesn't have an ETag at all.
     */

    public static final int STATUS_PRECONDITION_FAILED = 412;

    /**
     * This download can't be performed,because the md5 error,it must be retrying
     * The value I give it 470
     * added by ly20050516@gmail.com 2014/04/30
     * */

    public static final int STATUS_HTTP_MD5_ERROR = 470;
    /**
     * The lowest-valued error status that is not an actual HTTP status code.
     */
    public static final int MIN_ARTIFICIAL_ERROR_STATUS = 488;

    /**
     * The requested destination file already exists.
     */
    public static final int STATUS_FILE_ALREADY_EXISTS_ERROR = 488;

    /**
     * Some possibly transient error occurred, but we can't resume the download.
     */
    public static final int STATUS_CANNOT_RESUME = 489;

    /**
     * This download was canceled
     */
    public static final int STATUS_CANCELED = 490;

    /**
     * This download has completed with an error.
     * Warning: there will be other status values that indicate errors in
     * the future. Use isStatusError() to capture the entire category.
     */
    public static final int STATUS_UNKNOWN_ERROR = 491;

    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full.
     * Use the more specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR}
     * and {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
     */
    public static final int STATUS_FILE_ERROR = 492;

    /**
     * This download couldn't be completed because of an HTTP
     * redirect response that the download manager couldn't
     * handle.
     */
    public static final int STATUS_UNHANDLED_REDIRECT = 493;

    /**
     * This download couldn't be completed because of an
     * unspecified unhandled HTTP code.
     */
    public static final int STATUS_UNHANDLED_HTTP_CODE = 494;

    /**
     * This download couldn't be completed because of an
     * error receiving or processing data at the HTTP level.
     */
    public static final int STATUS_HTTP_DATA_ERROR = 495;

    /**
     * This download couldn't be completed because of an
     * HttpException while setting up the request.
     */
    public static final int STATUS_HTTP_EXCEPTION = 496;

    /**
     * This download couldn't be completed because there were
     * too many redirects.
     */
    public static final int STATUS_TOO_MANY_REDIRECTS = 497;

    /**
     * This download has failed because requesting application has been
     * blocked by  NetworkPolicyManager.
     *
     * @hide
     * @deprecated since behavior now uses
     *             {@link #STATUS_WAITING_FOR_NETWORK}
     */
    @Deprecated
    public static final int STATUS_BLOCKED = 498;

    private Impl() {
    	//private init
    }

}
