package com.eebbk.bfc.download.demo.util;


import com.eebbk.bfc.download.demo.util.L;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class SharedPreferencesUtil {
    private Context mContext;
    private Editor mEditor;
    private SharedPreferences mPreferences;
    private String mFileName = "";
    private int mMode = 0;
    private static final String TAG = SharedPreferencesUtil.class.getSimpleName();

    /*public SharedPreferencesUtil(Context context) {
        this.mContext = context;
        this.mPreferences = context.getSharedPreferences(MoKeyApplication.SHARE_EASY_TOUCH, Context.MODE_PRIVATE);
        this.mEditor = this.mPreferences.edit();
        mFileName = MoKeyApplication.SHARE_EASY_TOUCH;
        mMode = Context.MODE_PRIVATE;
        L.d(TAG," create SharedPreferencesUtil; name : " + mFileName + "; mode : " + mMode);
    }*/
    
    public SharedPreferencesUtil(Context context, String fileName){
    	this.mContext = context;
        this.mPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        this.mEditor = this.mPreferences.edit();
        mFileName = fileName;
        mMode = Context.MODE_PRIVATE;
        L.d(TAG," create SharedPreferencesUtil; name : " + mFileName + "; mode : " + mMode);
    }
    
    public SharedPreferencesUtil(Context context, String fileName, int mode){
    	this.mContext = context;
        this.mPreferences = context.getSharedPreferences(fileName, mode);
        this.mEditor = this.mPreferences.edit();
        mFileName = fileName;
        mMode = mode;
		L.d(TAG," create SharedPreferencesUtil; name : " + mFileName + "; mode : " + mMode);
    }

    // 读写配置文件
    public void putString(String name, String value) {
 		mEditor.putString(name, value);
 		mEditor.apply();
 		
 		L.d(TAG, " put key : "+name+", value : "+value+" to file : "+mFileName);
 	}

    public void putLong(String name, Long value) {
 		mEditor.putLong(name, value);
		mEditor.apply();
 		
 		L.d(TAG, " put key : "+name+", value : "+value+" to file : "+mFileName);
 	}

    public void putInt(String name, int value) {
 		mEditor.putInt(name, value);
 		mEditor.apply();
 		
 		L.d(TAG, " put key : "+name+", value : "+value+" to file : "+mFileName);
 	}

    public void putBoolean(String name, Boolean value) {
 		mEditor.putBoolean(name, value);
 		mEditor.apply();
 		
 		L.d(TAG, " put key : "+name+", value : "+value+" to file : "+mFileName);
 	}
    
    public void remove(String name) {
 		mEditor.remove(name);
 		mEditor.apply();
 		
 		L.d(TAG, " remove key : "+name+" from file : "+mFileName);
 	}
    
    public void clear(){
    	mEditor.clear();
    	mEditor.apply();
 		
 		L.d(TAG, " clear file : "+mFileName);
    }

    public long getLong(String key) {
 		return mPreferences.getLong(key, 0);
 	}

    public int getInt(String key) {
 		return mPreferences.getInt(key, 0);
 	}

    public Boolean getBoolean(String key) {
 		return mPreferences.getBoolean(key, false);
 	}

    public String getString(String key) {
 		return mPreferences.getString(key, "");
 	}
    
    public long getLong(String key, long defValue) {
 		return mPreferences.getLong(key, defValue);
 	}

    public int getInt(String key, int defValue) {
 		return mPreferences.getInt(key, defValue);
 	}

    public Boolean getBoolean(String key, boolean defValue) {
 		return mPreferences.getBoolean(key, defValue);
 	}

    public String getString(String key, String defValue) {
 		return mPreferences.getString(key, defValue);
 	}
    
    public Editor getEditor(){
    	return mEditor;
    }
}

