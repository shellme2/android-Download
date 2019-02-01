package com.eebbk.bfc.sdk.downloadmanager;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import com.eebbk.bfc.sdk.download.DownloadInitHelper;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.sdk.download.db.DownloadTaskColumns;
import com.eebbk.bfc.sdk.download.util.ExtrasConverter;
import com.eebbk.bfc.sdk.download.util.LogUtil;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This class may be used to filter download manager queries.
 * 下载任务 查询过滤器使用
 */
public class Query {

    /**
     * 排序，升序
     */
    public static final String ORDER_ASC = "ASC";

    /**
     * 排序，降序
     */
    public static final String ORDER_DESC = "DESC";

    public static final String COLUMN_TASK_CREATE_TIME = DownloadTaskColumns.BUILD_TIME;
    public static final String COLUMN_TOTAL_SIZE_BYTES = DownloadTaskColumns.TOTAL_SIZE;
    public static final String COLUMN_ID = DownloadTaskColumns.PARAM_ID;

    private long[] mIds = null;
    
    private int[] mState;
    private String mOrderByColumn = DownloadTaskColumns.BUILD_TIME;
    private String mOrder = ORDER_ASC;
    /**
     * 模块名
     */
    private String mModuleName;
    private HashMap<String, String> mExtras = new HashMap<>();

    public Query addFilterByExtras(String[] keys , String[] values){
    	if(keys == null || values == null || keys.length != values.length){
    		try {
        		throw new IllegalArgumentException("set filter by extras and the argument is illegal!");
			} catch (Exception e) {
                LogUtil.w(e, " set filter by extras and the argument is illegal! ");
				return this;
			}
    	}
    	for(int i = 0; i < keys.length; i++){
    		if(keys[i] == null){
    			continue;
    		}
    		mExtras.put(keys[i], values[i]);
    	}
    	return this;
    }
    
    public Query addFilterByExtras(String key , String value){
    	if(TextUtils.isEmpty(key)){
    		try {
        		throw new IllegalArgumentException("set filter by extras add the key is null!");
			} catch (Exception e) {
                LogUtil.e(e, " set filter by extras add the key is null! ");
				return this;
			}
    	}
        mExtras.put(key, value);
      	return this;
    }
    /**
     * 按id过滤，只查询指定id的任务
     * @return this object
     */
    public Query setFilterById(long... ids) {
        mIds = ids;
        return this;
    }

    /**
     * 设置状态值过滤，详细状态值可查看{@link Status}
     * @param state 装置
     * @return 查询对象
     */
    public Query setFilterByStatus(int... state) {
        mState = state;
        return this;
    }

    /**
     * 设置排序，排序字段目前支持：任务对应数据中的ID {@link #COLUMN_ID}<br/>
     * 任务创建时间： {@link #COLUMN_TASK_CREATE_TIME}<br/>
     * 文件总大小： {@link #COLUMN_TOTAL_SIZE_BYTES}<br/>
     * 排序关键字为升序 {@link #ORDER_ASC}、降序 {@link #ORDER_DESC}，默认按任务创建时间降序排列<br/>
     * @param column 排序字段
     * @param order 排序关键字
     * @return 查询对象
     */
    public Query orderBy(String column, String order) {
        if (!ORDER_ASC.equals(order) && !ORDER_DESC.equals(order)) {
            throw new IllegalArgumentException("Invalid order key: " + order);
        }
        if(!COLUMN_TASK_CREATE_TIME.equals(column)
                && !COLUMN_TOTAL_SIZE_BYTES.equals(column)
                && !COLUMN_ID.equals(column)){
            throw new IllegalArgumentException("Cannot order by " + column);
        }
        mOrderByColumn = column;
        mOrder = order;
        return this;
    }

    /**
     * 设置模块名
     * @param moduleName 模块名
     */
    public void setModuleName(String moduleName){
        mModuleName = moduleName;
    }

    private String getString(String[]  strSrc){
        if(strSrc == null || strSrc.length == 0){
            return "";
        }
        StringBuilder str = new StringBuilder();
        for(String string : strSrc){
            str.append(string);
            str.append(" ");
        }
        return str.toString();
    }
    

    /**
     * 开始查询，下载器内部使用，外部请勿调用
     * @param resolver ContentResolver
     * @param projection 表字段集合
     * @param baseUri 查询uri
     * @return cursor 结果游标
     * 
     */
    public Cursor runQuery(ContentResolver resolver, String[] projection, Uri baseUri) {
        Uri uri = baseUri;
        List<String> selectionParts = new ArrayList<>();
        String[] selectionArgs = null;

        if (mIds != null) {
            selectionParts.add(getWhereClauseForIds(mIds));
            selectionArgs = getWhereArgsForIds(mIds);
        }

        if(mExtras != null && !mExtras.isEmpty()){
        	selectionParts.add(getWhereClauseForExtras(mExtras));

            String[] args = null;
			try {
				args = getWhereArgsForExtras(mExtras);
			} catch (UnsupportedEncodingException e) {
                LogUtil.e(e, " get where args unsupported encoding exception ");
			}
			
			int baseLen = 0;
			int argsLen = 0;
			
			if(args != null){
				argsLen = args.length;
			}
			
			if(selectionArgs != null){
				baseLen = selectionArgs.length;
			}
            String[] whereArgs = new String[baseLen + argsLen];
            int i = 0;
            for(i = 0;selectionArgs != null && i <  baseLen;i ++){
                whereArgs[i] = selectionArgs[i];
            }
            for(int j = 0;i < whereArgs.length && j < argsLen;i++,j ++){
                whereArgs[i] = args[j];
            }
            selectionArgs = whereArgs;
        }

        if (mState != null && mState.length > 0) {
            List<String> parts = new ArrayList<>();
            for(int state: mState){
                parts.add(statusClause("=", state));
            }
            if(!parts.isEmpty()){
                selectionParts.add("(" + joinStrings(" OR ", parts) + ")");
            }
        }

        if(TextUtils.isEmpty(mModuleName)){
            mModuleName = DownloadInitHelper.getInstance().getDefaultModuleName();
        }
        selectionParts.add(DownloadTaskColumns.MODULE_NAME + "='" + mModuleName +"'");

        String selection = joinStrings(" AND ", selectionParts);
        if(LogUtil.isDebug()){
            LogUtil.i("selection : " + selection + "  selectionArgs : " + getString(selectionArgs));
        }
        String orderBy = mOrderByColumn + " " + mOrder;

        return resolver.query(uri, projection, selection, selectionArgs, orderBy);
    }

    private String getWhereClauseForIds(long[] ids) {
        StringBuilder whereClause = new StringBuilder();
        whereClause.append("(");
        for (int i = 0; i < ids.length; i++) {
            if (i > 0) {
                whereClause.append("OR ");
            }
            whereClause.append(DownloadTaskColumns.GENERATE_ID);
            whereClause.append(" = ? ");
        }
        whereClause.append(")");
        return whereClause.toString();
    }

    private String[] getWhereArgsForIds(long[] ids) {
        String[] whereArgs = new String[ids.length];
        for (int i = 0; i < ids.length; i++) {
            whereArgs[i] = Long.toString(ids[i]);
        }
        return whereArgs;
    }

    private String getWhereClauseForExtras(@NonNull HashMap<String, String> map)  {
        if(map.isEmpty()){
            return null;
        }
        StringBuilder whereClause = new StringBuilder();

        Iterator<?> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            iter.next();
            whereClause.append(DownloadTaskColumns.EXTRAS_MAP);
            whereClause.append(" LIKE ? ");

            if (iter.hasNext()) {
                whereClause.append(" AND ");
            }
        }

        return whereClause.toString();
    }

    /**
     * Get the selection args for a clause
     * @throws UnsupportedEncodingException
     */
    private String[] getWhereArgsForExtras(@NonNull HashMap<String, String> map) throws UnsupportedEncodingException {
        if(map.isEmpty()){
            return null;
        }
        String[] whereArgs = new String[map.size()];
        Iterator<?> iter = map.entrySet().iterator();
        int i = 0;
        while (iter.hasNext()) {
            @SuppressWarnings("rawtypes")
            Map.Entry entry = (Map.Entry) iter.next();
            whereArgs[i] = "%" + ExtrasConverter.encodeFormat((String) entry.getKey(), (String) entry.getValue()) + "%";
            i++;
        }

        return whereArgs;
    }
    
    private String joinStrings(String joiner, Iterable<String> parts) {
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (String part : parts) {
            if (!first) {
                builder.append(joiner);
            }
            builder.append(part);
            first = false;
        }
        return builder.toString();
    }

    private String statusClause(String operator, int value) {
        return DownloadTaskColumns.STATE + operator + "'" + value + "'";
    }
    
}
