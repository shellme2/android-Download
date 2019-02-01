package com.eebbk.bfc.download.demo.baseui;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-25 2:36
 * Email: jacklulu29@gmail.com
 */

public class DownloadHandlerPanelUIHelper implements View.OnClickListener {

    private IDownloadHandler mHandler;

    private Button mStartBtn;
    private Button mPauseBtn;
    private Button mResumeBtn;
    private Button mRestartBtn;
    private Button mLookInfoBtn;

    private Button mEditNetworkBtn;
    private CheckBox mNetworkWifiChx;
    private CheckBox mNetworkMobileChx;
    private CheckBox mNetworkBluetoothChx;

    private Button mRegisterListenerBtn;
    private Button mUnregisterListenerBtn;
    private EditText mTagEtv;
    private Button mRegisterListenerByTagBtn;
    private Button mUnregisterListenerByTagBtn;

    private Button mDeleteBtn;
    private Spinner mDeleteTypeSpinner;

    private int mDeleteType = 0;
    //private int mState = Status.DOWNLOAD_INVALID;

    public DownloadHandlerPanelUIHelper(IDownloadHandler handler){
        this.mHandler = handler;
    }

    public void bindView(View rootView, Context context){
        initView(rootView, context);
        initListener();
    }

    public void onDataChanged(ITask task){
        if(task == null){
            mDeleteBtn.setEnabled(false);
        } else {
            mDeleteBtn.setEnabled(true);
        }
    }

    private void initView(View rootView, Context context){
        mStartBtn = findView(rootView, R.id.start_btn);
        mPauseBtn = findView(rootView, R.id.pause_btn);
        mResumeBtn = findView(rootView, R.id.resume_btn);
        mRestartBtn = findView(rootView, R.id.restart_btn);
        mLookInfoBtn = findView(rootView, R.id.look_info_btn);

        mEditNetworkBtn = findView(rootView, R.id.edit_network_btn);
        mNetworkWifiChx = findView(rootView, R.id.network_wifi_chx);
        mNetworkMobileChx = findView(rootView, R.id.network_mobile_chx);
        mNetworkBluetoothChx = findView(rootView, R.id.network_bluetooth_chx);

        mRegisterListenerBtn = findView(rootView, R.id.register_listener_btn);
        mUnregisterListenerBtn = findView(rootView, R.id.unregister_listener_btn);
        mTagEtv = findView(rootView, R.id.tag_etv);
        mRegisterListenerByTagBtn = findView(rootView, R.id.register_listener_by_tag_btn);
        mUnregisterListenerByTagBtn = findView(rootView, R.id.unregister_listener_by_tag_btn);

        mDeleteBtn = findView(rootView, R.id.delete_btn);
        mDeleteTypeSpinner = findView(rootView, R.id.delete_type_spinner);
        String[] deleteTypesArray = rootView.getResources().getStringArray(R.array.delete_types_array);
        ArrayAdapter<String> deleteAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                deleteTypesArray);
        mDeleteTypeSpinner.setAdapter(deleteAdapter);

    }

    private void initListener(){
        mStartBtn.setOnClickListener(this);
        mPauseBtn.setOnClickListener(this);
        mResumeBtn.setOnClickListener(this);
        mRestartBtn.setOnClickListener(this);
        mLookInfoBtn.setOnClickListener(this);

        mEditNetworkBtn.setOnClickListener(this);

        mRegisterListenerBtn.setOnClickListener(this);
        mUnregisterListenerBtn.setOnClickListener(this);
        mRegisterListenerByTagBtn.setOnClickListener(this);
        mUnregisterListenerByTagBtn.setOnClickListener(this);

        mDeleteBtn.setOnClickListener(this);

        mPauseBtn.setOnClickListener(this);

        mDeleteTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mDeleteType = position;
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

    }

    public final static String WAKE_LOCK_TAG = "com.eebbk.wakelock.IDLE_ALLOW.downloadServiceWakeLock";
    public final static long WAKE_LOCK_HELD_TIME = 11 * 60 * 60 * 1000;
    public void onClick(View v) {
        if(mHandler == null){
            return;
        }
        switch (v.getId()){
            case R.id.start_btn:
                mHandler.onStartBtnClick();
                break;
            case R.id.pause_btn:
                mHandler.onPauseBtnClick();
                break;
            case R.id.resume_btn:
                mHandler.onResumeBtnClick();
                break;
            case R.id.restart_btn:
                mHandler.onRestartBtnClick();
                break;
            case R.id.look_info_btn:
                mHandler.onLookInfoBtnClick();
                break;
            case R.id.edit_network_btn:
                processNetworkClick();
                break;
            case R.id.register_listener_btn:
                mHandler.onRegisterListenerBtnClick();
                break;
            case R.id.unregister_listener_btn:
                mHandler.onUnregisterListenerBtnClick();
                break;
            case R.id.register_listener_by_tag_btn:
                mHandler.onRegisterListenerByTagBtnClick(mTagEtv.getText().toString());
                break;
            case R.id.unregister_listener_by_tag_btn:
                mHandler.onUnregisterListenerByTagBtnClick(mTagEtv.getText().toString());
                break;
            case R.id.delete_btn:
                processDeleteClick();
                break;
            default:
                break;
        }
    }

    private void processNetworkClick(){
        mHandler.onNetworkChanged(getNetworkTypes());
    }

    public int getNetworkTypes() {
        boolean wifiEnable = mNetworkWifiChx.isChecked();
        boolean mobileEnable = mNetworkMobileChx.isChecked();
        boolean bluetoothEnable = mNetworkBluetoothChx.isChecked();
        int networkTypes = NetworkType.NETWORK_UNKNOWN;
        if(wifiEnable){
            networkTypes |= NetworkType.NETWORK_WIFI;
        }
        if(mobileEnable){
            networkTypes |= NetworkType.NETWORK_MOBILE;
        }
        if(bluetoothEnable){
            networkTypes |= NetworkType.NETWORK_BLUETOOTH;
        }
        return networkTypes;
    }

    private void processDeleteClick(){
        switch (mDeleteType){
            case 0:
                mHandler.onDeleteByDefault();
                break;
            case 1:
                mHandler.onDeleteWithoutFile();
                break;
            case 2:
                mHandler.onDeleteAllFile();
                break;
            default:
                break;
        }
    }

    private <T extends View> T findView(View rootView, int resId){
        return (T)rootView.findViewById(resId);
    }

}
