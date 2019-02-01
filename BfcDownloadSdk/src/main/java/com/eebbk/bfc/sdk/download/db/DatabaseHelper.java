package com.eebbk.bfc.sdk.download.db;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.eebbk.bfc.sdk.download.util.LogUtil;


/**
 * 功能：下载日志数据库
 *
 */
public class DatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "DatabaseHelper";
	private static final String DATABASE_NAME = "downloads.db";
	private static final String DATABASE_NAME_NEW = "downloads-new.db";
	private static final int DATABASE_VERSION = 4;

	// table
	public static final String PARAMS_TABLE = "task_params";
	public static final String TASKS_TABLE = "tasks";
	public static final String THREADS_TABLE = "threads";

	// view
	public static final String DOWNLOAD_TASK_VIEW = "download_task";

	public static class ParamsColumns implements BaseColumns {
		/**
		 * 根据url和保存地址生成的唯一id
		 */
		public static final String GENERATE_ID = "generate_id";
		/**
		 * 下载地址
		 */
		public static final String URL = "url";
		/**
		 * 文件名
		 */
		public static final String FILE_NAME = "file_name";
		/**
		 * 文件后缀名
		 */
		public static final String FILE_EXTENSION = "file_extension";
		/**
		 * 文件保存路径
		 */
		public static final String SAVE_PATH = "save_path";
		/**
		 * 预设文件大小
		 */
		public static final String PRESET_FILE_SIZE = "preset_file_size";
		/**
		 * 自动检测文件大小是否符合预设文件大小
		 */
		public static final String AUTO_CHECK_SIZE = "auto_check_size";
		/**
		 * 优先级
		 */
		public static final String PRIORITY = "priority";
		/**
		 * 文件校验类型
		 */
		public static final String CHECK_TYPE = "check_type";
		/**
		 * 文件校验码
		 */
		public static final String CHECK_CODE = "check_code";
		/**
		 * 是否进行文件校验
		 */
		public static final String CHECK_ENABLE = "check_enable";
		/**
		 * 可用网络类型
		 */
		public static final String NETWORK_TYPES = "network_types";
		/**
		 * 是否需求排队
		 */
		public static final String NEED_QUEUE = "need_queue";
		/**
		 * 预留字段
		 */
		public static final String RESERVER = "reserver";
		/**
		 * 扩展字段
		 */
		public static final String EXTRAS_MAP = "extras_map";
		/**
		 * 是否显示通知
		 */
		public static final String NOTIFICATION_VISIBILITY = "notification_visibility";
		/**
		 * 是否允许修改保存路径
		 */
		public static final String ALLOW_ADJUST_SAVE_PATH = "allow_adjust_save_path";
		/**
		 * 是否显示实时状态（下载速度、剩余时间）
		 */
		public static final String SHOW_REAL_TIME_INFO = "show_real_time_info";
		/**
		 * 下载进度回调间隔时间，单位毫秒。
		 * 同时会影响到下载速度、剩余时间的统计以及回调显示
		 */
		public static final String MIN_PROGRESS_TIME = "min_progress_time";
		/**
		 * 是否自动解压
		 */
		public static final String AUTO_UNPACK = "auto_unpack";
		/**
		 * 解压文件保存路径
		 */
		public static final String UNPACK_PATH = "unpack_path";
		/**
		 * 解压后是否自动删除源文件
		 */
		public static final String DELETE_SOURCE_AFTER_UNPACK = "delete_source_after_unpack";
		/**
		 * 删除未下载完成任务时自动删除缓存文件
		 */
		public static final String DELETE_NO_END_TASK_AND_CACHE = "delete_no_end_task_and_cache";
		/**
		 * 删除已下载完成任务时自动删除文件
		 */
		public static final String DELETE_END_TASK_AND_CACHE = "delete_end_task_and_cache";
		/**
		 * 下载任务开启多线程数量
		 */
		public static final String DOWNLOAD_THREADS = "download_threads";
		/**
		 * 是否允许漫游
		 */
		public static final String DOWNLOAD_ALLOW_ROAMING = "download_allow_roaming";

		/**
		 * 模块名称
		 * add by llp , db version 2
		 */
		public static final String MODULE_NAME = "module_name";

	}

	public static class TasksColumns implements BaseColumns {
		/**
		 * 任务ID
		 */
		public static final String PARAM_ID = "param_id";
		/**
		 * 状态（下载、校验、解压）
		 */
		public static final String STATE = "state";
		/**
		 * 文件总大小
		 */
		public static final String TOTAL_SIZE = "total_size";
		/**
		 *  已完成大小（下载、校验、解压）
		 */
		public static final String FINISH_SIZE = "finish_size";
		/**
		 * 速度（下载、校验、解压）
		 */
		public static final String SPEED = "speed";
		/**
		 * 剩余时间（下载、校验、解压）
		 */
		public static final String LAST_TIME = "last_time";
		/**
		 * 下载重试次数
		 */
		public static final String RETRY_TIME = "retry_time";
		/**
		 * 任务创建时间
		 */
		public static final String BUILD_TIME = "build_time";
		/**
		 * 请求头缓存标记
		 */
		public static final String ETAG = "etag";
		/**
		 * 下载完成时间
		 */
		public static final String DOWNLOAD_FINISH_TIME = "download_finish_time";
		/**
		 * 校验完成时间
		 */
		public static final String CHECK_FINISH_TIME = "check_finish_time";
		/**
		 * 解压完成时间
		 */
		public static final String UNPACK_FINISH_TIME = "unpack_finish_time";
		/**
		 * 错误码
		 */
		public static final String ERROR_CODE = "error_code";
		/**
		 * 异常
		 */
		public static final String EXCEPTION = "exception";

		/**
		 * 任务阶段：下载、校验、解压
		 * add by llp , db version 3
		 */
		public static final String TASK_PHASE = "task_phase";

	}

	public static class ThreadsColumns implements BaseColumns {
		/**
		 * 线程ID
		 */
		public static final String TASK_ID = "task_id";
		/**
		 * 开始位置
		 */
		public static final String START_POSITION = "start_position";
		/**
		 * 结束位置
		 */
		public static final String END_POSITION = "end_position";
		/**
		 * 已完成大小
		 */
		public static final String FINISH_SIZE = "finish_size";
		/**
		 * 下载重试次数
		 */
		public static final String RETRY_TIME = "retry_time";
		/**
		 * 错误码
		 */
		public static final String ERROR_CODE = "error_code";
		/**
		 * 异常
		 */
		public static final String EXCEPTION = "exception";
	}



	// download list table SQL
	private static final String CREATE_PARAMS_TABLE_SQL =
			"CREATE TABLE " + PARAMS_TABLE + "("
					+ ParamsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ParamsColumns.GENERATE_ID + " INT, "
					+ ParamsColumns.URL + " TEXT, "
					+ ParamsColumns.FILE_NAME + " TEXT, "
					+ ParamsColumns.FILE_EXTENSION + " TEXT, "
					+ ParamsColumns.SAVE_PATH + " TEXT, "
					+ ParamsColumns.PRESET_FILE_SIZE + " INT DEFAULT(-1), "
					+ ParamsColumns.AUTO_CHECK_SIZE + " INT DEFAULT(1), "
					+ ParamsColumns.PRIORITY + " INT DEFAULT(0), "
					+ ParamsColumns.CHECK_TYPE + " TEXT, "
					+ ParamsColumns.CHECK_CODE + " TEXT, "
					+ ParamsColumns.CHECK_ENABLE + " TEXT, "
					+ ParamsColumns.NETWORK_TYPES + " INT, "
					+ ParamsColumns.NEED_QUEUE + " INT DEFAULT(1), "
					+ ParamsColumns.RESERVER + " TEXT, "
					+ ParamsColumns.EXTRAS_MAP + " TEXT, "
					+ ParamsColumns.NOTIFICATION_VISIBILITY + " INT DEFAULT(0), "
					+ ParamsColumns.ALLOW_ADJUST_SAVE_PATH + " INT DEFAULT(0), "
					+ ParamsColumns.SHOW_REAL_TIME_INFO + " INT DEFAULT(1), "
					// add by  llp, db version 4
					+ ParamsColumns.MIN_PROGRESS_TIME + " INT DEFAULT(750), "

					+ ParamsColumns.AUTO_UNPACK + " INT DEFAULT (0), "
					+ ParamsColumns.UNPACK_PATH + " TEXT, "
					+ ParamsColumns.DELETE_SOURCE_AFTER_UNPACK + " INT DEFAULT(1), "
					+ ParamsColumns.DELETE_NO_END_TASK_AND_CACHE + " INT DEFAULT(1), "
					+ ParamsColumns.DELETE_END_TASK_AND_CACHE + "  INT DEFAULT(0), "
					+ ParamsColumns.DOWNLOAD_THREADS + " INT DEFAULT(1), "
					+ ParamsColumns.DOWNLOAD_ALLOW_ROAMING + " INT DEFAULT(0), "

					// add by  llp, db version 2
					+ ParamsColumns.MODULE_NAME + " TEXT "
					+ ");";

	// download thread list table SQL
	private static final String CREATE_TASKS_TABLE_SQL =
			"CREATE TABLE " + TASKS_TABLE + "("
					+ TasksColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ TasksColumns.PARAM_ID + " INT DEFAULT(0), "
					+ TasksColumns.STATE + " INT DEFAULT(0), "

					// add by llp, db version 3
					+ TasksColumns.TASK_PHASE + " INT DEFAULT(1), "

					+ TasksColumns.TOTAL_SIZE + " INT DEFAULT(0), "
					+ TasksColumns.FINISH_SIZE + " INT DEFAULT(0), "
					+ TasksColumns.SPEED + " INT DEFAULT(0), "
					+ TasksColumns.LAST_TIME + " INT DEFAULT(-1), "
					+ TasksColumns.RETRY_TIME + " INT DEFAULT(0), "
					+ TasksColumns.BUILD_TIME + " INT DEFAULT(-1), "
					+ TasksColumns.ETAG + " TEXT, "
					+ TasksColumns.DOWNLOAD_FINISH_TIME + " INT DEFAULT(-1), "
					+ TasksColumns.CHECK_FINISH_TIME + " INT DEFAULT(-1), "
					+ TasksColumns.UNPACK_FINISH_TIME + " INT DEFAULT(-1), "
					+ TasksColumns.ERROR_CODE + " TEXT, "
					+ TasksColumns.EXCEPTION + " TEXT "
					+ ");";

	// download thread list table SQL
	private static final String CREATE_THREADS_TABLE_SQL =
			"CREATE TABLE " + THREADS_TABLE + "("
					+ ThreadsColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ ThreadsColumns.TASK_ID + " INT DEFAULT(0), "
					+ ThreadsColumns.START_POSITION + " INT DEFAULT(0), "
					+ ThreadsColumns.END_POSITION + " INT DEFAULT(0), "
					+ ThreadsColumns.FINISH_SIZE + " INT DEFAULT(0), "
					+ ThreadsColumns.RETRY_TIME + " INT DEFAULT(0), "
					+ ThreadsColumns.ERROR_CODE + " TEXT, "
					+ ThreadsColumns.EXCEPTION + " TEXT "
					+ ");";

	private static final String VIEW_PARAMS_COLUMNS_PROJECTION =
			PARAMS_TABLE + '.' + ParamsColumns.GENERATE_ID +
					" as " + DownloadTaskColumns.GENERATE_ID + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.URL
					+ " as " + DownloadTaskColumns.URL + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.FILE_NAME
					+ " as " + DownloadTaskColumns.FILE_NAME + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.FILE_EXTENSION
					+ " as " + DownloadTaskColumns.FILE_EXTENSION + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.SAVE_PATH
					+ " as " + DownloadTaskColumns.SAVE_PATH + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.PRESET_FILE_SIZE
					+ " as " + DownloadTaskColumns.PRESET_FILE_SIZE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.AUTO_CHECK_SIZE
					+ " as " + DownloadTaskColumns.AUTO_CHECK_SIZE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.PRIORITY
					+ " as " + DownloadTaskColumns.PRIORITY + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.CHECK_TYPE
					+ " as " + DownloadTaskColumns.CHECK_TYPE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.CHECK_CODE
					+ " as " + DownloadTaskColumns.CHECK_CODE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.CHECK_ENABLE
					+ " as " + DownloadTaskColumns.CHECK_ENABLE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.NETWORK_TYPES
					+ " as " + DownloadTaskColumns.NETWORK_TYPES + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.NEED_QUEUE
					+ " as " + DownloadTaskColumns.NEED_QUEUE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.RESERVER
					+ " as " + DownloadTaskColumns.RESERVER + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.EXTRAS_MAP
					+ " as " + DownloadTaskColumns.EXTRAS_MAP + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.NOTIFICATION_VISIBILITY
					+ " as " + DownloadTaskColumns.NOTIFICATION_VISIBILITY + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.ALLOW_ADJUST_SAVE_PATH
					+ " as " + DownloadTaskColumns.ALLOW_ADJUST_SAVE_PATH + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.SHOW_REAL_TIME_INFO
					+ " as " + DownloadTaskColumns.SHOW_REAL_TIME_INFO + ", "
					// add by llp, db version 4
					+ PARAMS_TABLE + '.' + ParamsColumns.MIN_PROGRESS_TIME
					+ " as " + DownloadTaskColumns.MIN_PROGRESS_TIME + ", "

					+ PARAMS_TABLE + '.' + ParamsColumns.AUTO_UNPACK
					+ " as " + DownloadTaskColumns.AUTO_UNPACK + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.UNPACK_PATH
					+ " as " + DownloadTaskColumns.UNPACK_PATH + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.DELETE_SOURCE_AFTER_UNPACK
					+ " as " + DownloadTaskColumns.DELETE_SOURCE_AFTER_UNPACK + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.DELETE_NO_END_TASK_AND_CACHE
					+ " as " + DownloadTaskColumns.DELETE_NO_END_TASK_AND_CACHE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.DELETE_END_TASK_AND_CACHE
					+ " as " + DownloadTaskColumns.DELETE_END_TASK_AND_CACHE + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.DOWNLOAD_THREADS
					+ " as " + DownloadTaskColumns.DOWNLOAD_THREADS + ", "
					+ PARAMS_TABLE + '.' + ParamsColumns.DOWNLOAD_ALLOW_ROAMING
					+ " as " + DownloadTaskColumns.DOWNLOAD_ALLOW_ROAMING

					// add by llp, db version 2
					+ ", " + PARAMS_TABLE + '.' + ParamsColumns.MODULE_NAME
					+ " as " + DownloadTaskColumns.MODULE_NAME
			;

	private static final String VIEW_TASKS_COLUMNS_PROJECTION =
			TASKS_TABLE + '.' + TasksColumns._ID
					+ " as " + DownloadTaskColumns._ID + ", "
					+ TASKS_TABLE + '.' + TasksColumns.PARAM_ID
					+ " as " + DownloadTaskColumns.PARAM_ID + ", "
					+ TASKS_TABLE + '.' + TasksColumns.STATE
					+ " as " + DownloadTaskColumns.STATE + ", "
					+ TASKS_TABLE + '.' + TasksColumns.TOTAL_SIZE
					+ " as " + DownloadTaskColumns.TOTAL_SIZE + ", "
					+ TASKS_TABLE + '.' + TasksColumns.FINISH_SIZE
					+ " as " + DownloadTaskColumns.FINISH_SIZE + ", "
					+ TASKS_TABLE + '.' + TasksColumns.SPEED
					+ " as " + DownloadTaskColumns.SPEED + ", "
					+ TASKS_TABLE + '.' + TasksColumns.LAST_TIME
					+ " as " + DownloadTaskColumns.LAST_TIME + ", "
					+ TASKS_TABLE + '.' + TasksColumns.RETRY_TIME
					+ " as " + DownloadTaskColumns.RETRY_TIME + ", "
					+ TASKS_TABLE + '.' + TasksColumns.BUILD_TIME
					+ " as " + DownloadTaskColumns.BUILD_TIME + ", "
					+ TASKS_TABLE + '.' + TasksColumns.ETAG
					+ " as " + DownloadTaskColumns.ETAG + ", "
					+ TASKS_TABLE + '.' + TasksColumns.DOWNLOAD_FINISH_TIME
					+ " as " + DownloadTaskColumns.DOWNLOAD_FINISH_TIME + ", "
					+ TASKS_TABLE + '.' + TasksColumns.CHECK_FINISH_TIME
					+ " as " + DownloadTaskColumns.CHECK_FINISH_TIME + ", "
					+ TASKS_TABLE + '.' + TasksColumns.UNPACK_FINISH_TIME
					+ " as " + DownloadTaskColumns.UNPACK_FINISH_TIME + ", "
					+ TASKS_TABLE + '.' + TasksColumns.ERROR_CODE
					+ " as " + DownloadTaskColumns.ERROR_CODE + ", "
					+ TASKS_TABLE + '.' + TasksColumns.EXCEPTION
					+ " as " + DownloadTaskColumns.EXCEPTION

					// add by llp, db version 3
					+ ", " + TASKS_TABLE + '.' + TasksColumns.TASK_PHASE
					+ " as " + DownloadTaskColumns.TASK_PHASE
			;

	private static String[] TASKS_COLUMNS_PROJECTION = {
			DownloadTaskColumns.GENERATE_ID,
			DownloadTaskColumns.URL,
			DownloadTaskColumns.FILE_NAME,
			DownloadTaskColumns.FILE_EXTENSION,
			DownloadTaskColumns.SAVE_PATH,
			DownloadTaskColumns.PRESET_FILE_SIZE,
			DownloadTaskColumns.AUTO_CHECK_SIZE,
			DownloadTaskColumns.PRIORITY,
			DownloadTaskColumns.CHECK_TYPE,
			DownloadTaskColumns.CHECK_CODE,
			DownloadTaskColumns.CHECK_ENABLE,
			DownloadTaskColumns.NETWORK_TYPES,
			DownloadTaskColumns.NEED_QUEUE,
			DownloadTaskColumns.RESERVER,
			DownloadTaskColumns.EXTRAS_MAP,
			DownloadTaskColumns.NOTIFICATION_VISIBILITY,
			DownloadTaskColumns.ALLOW_ADJUST_SAVE_PATH,
			DownloadTaskColumns.SHOW_REAL_TIME_INFO,
			// add by llp, db version 4
			DownloadTaskColumns.MIN_PROGRESS_TIME,
			DownloadTaskColumns.AUTO_UNPACK,
			DownloadTaskColumns.UNPACK_PATH,
			DownloadTaskColumns.DELETE_SOURCE_AFTER_UNPACK,
			DownloadTaskColumns.DELETE_NO_END_TASK_AND_CACHE,
			DownloadTaskColumns.DELETE_END_TASK_AND_CACHE,
			DownloadTaskColumns.DOWNLOAD_THREADS,
			DownloadTaskColumns.DOWNLOAD_ALLOW_ROAMING,
			// add by llp, db version 2
			DownloadTaskColumns.MODULE_NAME,

			DownloadTaskColumns._ID,
			DownloadTaskColumns.PARAM_ID,
			DownloadTaskColumns.STATE,
			DownloadTaskColumns.TOTAL_SIZE,
			DownloadTaskColumns.FINISH_SIZE,
			DownloadTaskColumns.SPEED,
			DownloadTaskColumns.LAST_TIME,
			DownloadTaskColumns.RETRY_TIME,
			DownloadTaskColumns.BUILD_TIME,
			DownloadTaskColumns.ETAG,
			DownloadTaskColumns.DOWNLOAD_FINISH_TIME,
			DownloadTaskColumns.CHECK_FINISH_TIME,
			DownloadTaskColumns.UNPACK_FINISH_TIME,
			DownloadTaskColumns.ERROR_CODE,
			DownloadTaskColumns.EXCEPTION,

			// add by llp, db version 2
			DownloadTaskColumns.TASK_PHASE,
	};

	public static String[] getTasksColumnsProjection() {
		return TASKS_COLUMNS_PROJECTION;
	}

	// List of all our SQL tables
	private static final String[] CREATE_TABLE_SQLS = new String[] {
			CREATE_PARAMS_TABLE_SQL,
			CREATE_TASKS_TABLE_SQL,
			CREATE_THREADS_TABLE_SQL,
	};

	private static final String DOWNLOAD_TASK_VIEW_SQL = "CREATE VIEW " +
			DOWNLOAD_TASK_VIEW + " AS SELECT " +
			VIEW_PARAMS_COLUMNS_PROJECTION + ", " +
			VIEW_TASKS_COLUMNS_PROJECTION +
			" FROM " + PARAMS_TABLE +
			" LEFT JOIN " + TASKS_TABLE + " ON(" +
			PARAMS_TABLE + "." + ParamsColumns._ID + "=" +
			TASKS_TABLE + "." + TasksColumns.PARAM_ID + ")";

	// List of all our views
	private static final String[] CREATE_VIEW_SQLS = new String[] {
			DOWNLOAD_TASK_VIEW_SQL,
	};

	private final Context mApplicationContext;
	private static final Object sLock = new Object();
	private static DatabaseHelper sHelperInstance = null;
	private final Object mDatabaseWrapperLock = new Object();
	private DatabaseWrapper mDatabaseWrapper;           // Protected by mDatabaseWrapperLock.
	private final DatabaseUpgradeHelper mUpgradeHelper = new DatabaseUpgradeHelper();
	private static boolean mIsDistinguishOldDbName = false;
	
	private DatabaseHelper(Context context) {
		super(context, getDbName(), null, DATABASE_VERSION);
		mApplicationContext = context;
	}

	public static DatabaseHelper getInstance(final Context context) {
		synchronized (sLock){
			if(sHelperInstance == null) {
				sHelperInstance = new DatabaseHelper(context.getApplicationContext());
			}
			return sHelperInstance;
		}
	}

	public static DatabaseHelper getInstance(final Context context, final boolean isDistinguishOldDbName) {
		synchronized (sLock){
			if(sHelperInstance == null) {
				mIsDistinguishOldDbName = isDistinguishOldDbName;
				sHelperInstance = new DatabaseHelper(context.getApplicationContext());
			}
			return sHelperInstance;
		}
	}

	/**
	 * Get the (singleton) instance of @{link DatabaseWrapper}.
	 * <p>The database is always opened as a writeable database.
	 * @return The current (or a new) DatabaseWrapper instance.
	 */
	public DatabaseWrapper getDatabase() {
		// We prevent the main UI thread from accessing the database here since we have to allow
		// public access to this class to enable sub-packages to access data.
		synchronized (mDatabaseWrapperLock) {
			if (mDatabaseWrapper == null) {
				mDatabaseWrapper = new DatabaseWrapper(mApplicationContext, getWritableDatabase());
			}
			return mDatabaseWrapper;
		}
	}
	
	/*public static SQLiteDatabase getReadableDatabase(Context context) throws SQLiteException {
		return getInstance(context).getReadableDatabase();
	}
	
	public static SQLiteDatabase getWritableDatabase(Context context) throws SQLiteException {
		return getInstance(context).getWritableDatabase();
	}*/

	@Override
	public void onCreate(SQLiteDatabase db) {
		// Recreate the whole database.
		createDatabase(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		mUpgradeHelper.doOnUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * Drops and recreates all tables.
	 */
	public static void rebuildTables(final SQLiteDatabase db) {
		// Drop tables first, then views, and indices.
		dropAllTables(db);
		dropAllViews(db);

		// Recreate the whole database.
		createDatabase(db);
	}

	private static void createDatabase(final SQLiteDatabase db) {
		createAllTables(db);
		createAllViews(db);
		// Enable foreign key constraints
		//db.execSQL("PRAGMA foreign_keys=ON;");
	}

	private static void createAllTables(final SQLiteDatabase db){
		for (final String sql : CREATE_TABLE_SQLS) {
			db.execSQL(sql);
		}
	}

	/**
	 * Drops all user-defined tables from the given database.
	 */
	private static void dropAllTables(final SQLiteDatabase db) {
		dropTable(db, PARAMS_TABLE);
		dropTable(db, TASKS_TABLE);
		dropTable(db, THREADS_TABLE);
	}

	public static void createAllViews(final SQLiteDatabase db){
		for (final String sql : CREATE_VIEW_SQLS) {
			db.execSQL(sql);
		}
	}

	/**
	 * Drops all user-defined views from the given database.
	 */
	public static void dropAllViews(final SQLiteDatabase db) {
		dropView(db, DOWNLOAD_TASK_VIEW, false);
	}

	private static void dropTable(final SQLiteDatabase db, String tableName){
		final String dropPrefix = "DROP TABLE IF EXISTS ";
		try {
			db.execSQL(dropPrefix + tableName);
		} catch (final SQLException ex) {
			LogUtil.e(ex, TAG, "unable to drop table " + tableName + " ");
		}
	}

	private static void dropView(final SQLiteDatabase db, final String viewName,
								 final boolean throwOnFailure) {
		final String dropPrefix = "DROP VIEW IF EXISTS ";
		try {
			db.execSQL(dropPrefix + viewName);
		} catch (final SQLException ex) {
			LogUtil.e(ex, "unable to drop view " + viewName + " ");
			if (throwOnFailure) {
				throw ex;
			}
		}
	}

	/**
	 * 获取数据库名
	 * 老版本数据名和当前新版本数据库名默认是一样的（downloads.db），
	 * 有些应用会通过老版本下载器把数据库创建在应用内，导致跟新版本数据库的版本号和表字段都不一致。
	 * 如果isDistinguishOldDbName为true，则创建downloads-new.db
	 */
	private static String getDbName() {
		return mIsDistinguishOldDbName ? DATABASE_NAME_NEW : DATABASE_NAME;
	}
}
