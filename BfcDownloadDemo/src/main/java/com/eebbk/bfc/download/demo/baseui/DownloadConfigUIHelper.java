package com.eebbk.bfc.download.demo.baseui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.download.C;
import com.eebbk.bfc.sdk.download.net.NetworkType;
import com.eebbk.bfc.sdk.download.util.NetworkParseUtil;
import com.eebbk.bfc.sdk.downloadmanager.DownloadController;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc: 下载配置ui帮助类
 * Author: llp
 * Create Time: 2016-10-23 15:58
 * Email: jacklulu29@gmail.com
 */

public class DownloadConfigUIHelper implements IDownloadTaskConfig, View.OnClickListener {

    private Context mContext;
    private View mRootView;

    private EditText mUrlEtv;
    private TextView mIdTv;
    private EditText mFileNameEtv;
    private EditText mFileExtensionEtv;
    private EditText mSavePathEtv;
    private TextView mPresetFileSizeTv;
    private CheckBox mAutoCheckSizeChx;
    private EditText mPriorityEtv;
    private EditText mCheckTypeEtv;
    private EditText mCheckCodeEtv;
    private CheckBox mCheckEnableChx;
    private TextView mNetworkTypesTv;
    private CheckBox mNetworkWifiChx;
    private CheckBox mNetworkMobileChx;
    private CheckBox mNetworkBluetoothChx;
    private TextView mNeedQueueTv;
    private CheckBox mNeedQueueChx;
    private EditText mReserverEtv;
    private TextView mAddExtrasTv;
    private LinearLayout mExtrasLy;

    private TextView mNotificationTv;
    private CheckBox mAllowAdjustSavePathChx;
    private EditText mMinProgressTimeEtv;
    private CheckBox mShowRealTimeChx;
    private CheckBox mAutoUnpackChx;
    private EditText mUnpackPathEtv;
    private CheckBox mDeleteSourceAfterUnpackChx;
    private CheckBox mDeleteNoEndTaskChx;
    private CheckBox mDeleteEndTaskChx;
    private TextView mDownloadThreadsTv;

    private EditText mEditModuleEtv;

    private ITask mDefaultTask;

    private ArrayList<ExtraItemView> mExtrasViewItems = new ArrayList<>();

    public DownloadConfigUIHelper() {
        String url = "http://apps.wandoujia.com/redirect?signature=6619964&url=http%3A%2F%2Fwap.sogou.com%2Fweb%2Fredir.jsp%3Fappdown%3D1%26docid%3D7314245383295080118%26sourceid%3D-2903356712629349561%26u%3DV14ejE_E-5OVP-Vbw-mQfXaGxEyIXVVj7G3x_nyrRNXiPNCzlaeqGi4_TXDOQWtw15-09Fh3KKPYrEG2lyIFtQ..%26w%3D1467&pn=com.fhvxehy.supermario&md5=3c50710dab7a68299aa41a9701fed01e&apkid=15144502&vc=15&size=9607879&tokenId=bubugao&pos=t%2Fsearch%2Flist%2F%2F%25E8%25B6%2585%25E7%25BA%25A7%25E7%258E%259B%25E4%25B8%25BD2%2F14%2Fnormal";
        String url1 = "http://file.eebbk.net/android-other/cloudIDN/uploaddemo/2014/11/10/700H94607961U/112210204_BCompare-4.0.0.18746.exe";
        String url2 = "http://file.eebbk.net/android-test1/cloudIDN/demo/overwrite/F:FileExplorer.rar";
        String url3 = "https://codeload.github.com/google/guava/zip/master";
        String url4 = "http://dl2.smartisan.cn/app/smartisan_launcherdown1229.apk";
        String url5 = "http://mirror.bit.edu.cn/apache//ant/binaries/apache-ant-1.9.7-bin.zip";
        String url6 = "http://down.eebbk.net/xzzc/h10/yrdd/plfMrdnpqS30omR6YmsyU48capwIRGLb/识字上-新乐学版-步步高小学入学准备-2.tia";
        String url7 = "http://down.eebbk.net/xzzc/h8/tbyw/k7ZmH2ZBKLMoFgKfE3XCQ9S4fg81LePZ/%5b西南师大版%5d四年级语文下册-1.ptr";
        String url8 = "http://7xth7g.com2.z0.glb.qiniucdn.com/20160428/temp/HZGeniusUnit01.zip";
        String url9 = "http://file.eebbk.net/server-test/cloudIDN/test/2017/09/05/114205543_f9b3308049d44506.jpg";
        mDefaultTask = DownloadController.buildTask(url8).build();
    }

    public void bindView(View rootView) {
        initView(rootView);
        mContext = rootView.getContext();
    }

    private void initView(View rootView) {
        this.mRootView = rootView;
        mUrlEtv = findView(rootView, R.id.edit_url_tv);
        mIdTv = findView(rootView, R.id.show_id_tv);
        mFileNameEtv = findView(rootView, R.id.edit_file_name_etv);
        mFileExtensionEtv = findView(rootView, R.id.edit_file_extension_etv);
        mSavePathEtv = findView(rootView, R.id.edit_save_path_etv);
        mPresetFileSizeTv = findView(rootView, R.id.show_preset_file_size_tv);
        mAutoCheckSizeChx = findView(rootView, R.id.edit_auto_check_size_chx);
        mPriorityEtv = findView(rootView, R.id.edit_priority_etv);
        mCheckTypeEtv = findView(rootView, R.id.edit_check_type_etv);
        mCheckCodeEtv = findView(rootView, R.id.edit_check_code_etv);
        mCheckEnableChx = findView(rootView, R.id.edit_check_enable_size_chx);
        mNetworkTypesTv = findView(rootView, R.id.show_network_type_size_tv);
        mNetworkWifiChx = findView(rootView, R.id.network_wifi_chx);
        mNetworkMobileChx = findView(rootView, R.id.network_mobile_chx);
        mNetworkBluetoothChx = findView(rootView, R.id.network_bluetooth_chx);
        mNeedQueueTv = findView(rootView, R.id.need_queue_tv);
        mNeedQueueChx = findView(rootView, R.id.edit_need_queue_chx);
        mReserverEtv = findView(rootView, R.id.edit_reserver_etv);
        mAddExtrasTv = findView(rootView, R.id.extras_add_tv);
        mExtrasLy = findView(rootView, R.id.show_extras_ly);

        mNotificationTv = findView(rootView, R.id.show_notification_tv);
        mAllowAdjustSavePathChx = findView(rootView, R.id.edit_allow_adjust_save_path_chx);
        mShowRealTimeChx = findView(rootView, R.id.edit_show_real_time_info_chx);
        mMinProgressTimeEtv = findView(rootView, R.id.min_progress_time_etv);
        mAutoUnpackChx = findView(rootView, R.id.edit_auto_unpack_chx);
        mUnpackPathEtv = findView(rootView, R.id.edit_unpack_path_etv);
        mDeleteSourceAfterUnpackChx = findView(rootView, R.id.edit_delete_source_after_unpack_chx);
        mDeleteSourceAfterUnpackChx.setEnabled(false);
        mDeleteNoEndTaskChx = findView(rootView, R.id.edit_delete_no_end_task_chx);
        mDeleteEndTaskChx = findView(rootView, R.id.edit_delete_end_task_chx);
        mDownloadThreadsTv = findView(rootView, R.id.show_download_threads_tv);
        mEditModuleEtv = findView(rootView, R.id.edit_module_name_etv);

        mAddExtrasTv.setOnClickListener(this);

        mNetworkWifiChx.setOnClickListener(this);
        mNetworkMobileChx.setOnClickListener(this);
        mNetworkBluetoothChx.setOnClickListener(this);
    }

    public void initSingleTaskPanel() {
        mNeedQueueTv.setVisibility(View.GONE);
        mNeedQueueChx.setVisibility(View.GONE);
    }

    public void loadViewByDefaultTask() {
        updateView(mDefaultTask);
    }

    public void updateView(ITask task) {
        if (task == null) {
            return;
        }
        mUrlEtv.setText(task.getUrl());
        mIdTv.setText(String.valueOf(task.getId()));
        mFileNameEtv.setText(task.getFileName());
        mFileExtensionEtv.setText(task.getFileExtension());
        mSavePathEtv.setText(task.getSavePath());
        mPresetFileSizeTv.setText(String.valueOf(task.getPresetFileSize()));
        mAutoCheckSizeChx.setChecked(task.isAutoCheckSize());
        mPriorityEtv.setText(String.valueOf(task.getPriority()));
        mCheckTypeEtv.setText(task.getCheckType());
        mCheckCodeEtv.setText(task.getCheckCode());
        mCheckCodeEtv.setText("545111BE3D8E98EE061D83DCE1CD1C4F");
        mCheckEnableChx.setChecked(task.isCheckEnable());
        String networkStr = "";
        if (NetworkParseUtil.containsWifi(task.getNetworkTypes())) {
            networkStr += " Wifi ";
        }
        if (NetworkParseUtil.containsMobile(task.getNetworkTypes())) {
            networkStr += " Mobile ";
        }
        if (NetworkParseUtil.containsBluetooth(task.getNetworkTypes())) {
            networkStr += " Bluetooth ";
        }
        mNetworkTypesTv.setText(String.valueOf(task.getNetworkTypes()) + " " + networkStr);
        mNetworkWifiChx.setChecked(NetworkParseUtil.containsWifi(task.getNetworkTypes()));
        mNetworkMobileChx.setChecked(NetworkParseUtil.containsMobile(task.getNetworkTypes()));
        mNetworkBluetoothChx.setChecked(NetworkParseUtil.containsBluetooth(task.getNetworkTypes()));

        mNeedQueueChx.setChecked(task.isNeedQueue());
        mReserverEtv.setText(task.getReserver());
        setExtras(task.getExtras());

        mNotificationTv.setText(String.valueOf(task.getNotificationVisibility()));
        mAllowAdjustSavePathChx.setChecked(false);
        mAllowAdjustSavePathChx.setEnabled(false);
        mShowRealTimeChx.setChecked(task.isShowRealTimeInfo());
        mMinProgressTimeEtv.setText(String.valueOf(task.getMinProgressTime()));
        mAutoUnpackChx.setChecked(task.isAutoUnpack());
        mUnpackPathEtv.setText(task.getUnpackPath());
        mDeleteSourceAfterUnpackChx.setChecked(true);
        mDeleteSourceAfterUnpackChx.setEnabled(false);
        mDeleteNoEndTaskChx.setChecked(task.isDeleteNoEndTaskAndCache());
        mDeleteEndTaskChx.setChecked(task.isDeleteEndTaskAndCache());
        //mDownloadThreadsTv.setText(String.valueOf(task.getDownloadThreads()));
        mDownloadThreadsTv.setText("暂不支持多线程下载");

        mEditModuleEtv.setText(task.getModuleName());
    }

    public void updateNetworkTypes(int networkTypes) {
        updateNetworkText(networkTypes);
        mNetworkWifiChx.setChecked(NetworkParseUtil.containsWifi(networkTypes));
        mNetworkMobileChx.setChecked(NetworkParseUtil.containsMobile(networkTypes));
        mNetworkBluetoothChx.setChecked(NetworkParseUtil.containsBluetooth(networkTypes));
    }

    public void setConfigViewEnable(boolean enable) {
        mUrlEtv.setEnabled(enable);
        mFileNameEtv.setEnabled(enable);
        mFileExtensionEtv.setEnabled(enable);
        mSavePathEtv.setEnabled(enable);
        mAutoCheckSizeChx.setEnabled(enable);
        mPriorityEtv.setEnabled(enable);
        mCheckTypeEtv.setEnabled(enable);
        mCheckCodeEtv.setEnabled(enable);
        mCheckEnableChx.setEnabled(enable);
        mNetworkWifiChx.setEnabled(enable);
        mNetworkMobileChx.setEnabled(enable);
        mNetworkBluetoothChx.setEnabled(enable);
        mNeedQueueChx.setEnabled(enable);
        mReserverEtv.setEnabled(enable);
        mAllowAdjustSavePathChx.setEnabled(false);
        mShowRealTimeChx.setEnabled(enable);
        mMinProgressTimeEtv.setEnabled(enable);
        mAutoUnpackChx.setEnabled(enable);
        mUnpackPathEtv.setEnabled(enable);
        mDeleteSourceAfterUnpackChx.setEnabled(false);
        mDeleteNoEndTaskChx.setEnabled(enable);
        mDeleteEndTaskChx.setEnabled(enable);
        mEditModuleEtv.setEnabled(enable);

        mAddExtrasTv.setEnabled(enable);
        setExtrasViewItemsEnable(enable);
    }

    /**
     * 设置扩展字段是否可编辑
     *
     * @param enable
     */
    private void setExtrasViewItemsEnable(boolean enable) {
        if (mExtrasViewItems == null || mExtrasViewItems.isEmpty()) {
            return;
        }
        for (ExtraItemView itemView : mExtrasViewItems) {
            if(itemView == null){
                continue;
            }
            itemView.keyEtv.setEnabled(enable);
            itemView.valueEtv.setEnabled(enable);
            itemView.deleteImgBtn.setEnabled(enable);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.network_wifi_chx:
            case R.id.network_mobile_chx:
            case R.id.network_bluetooth_chx:
                onNetworkTypesChanged();
                break;
            case R.id.extras_add_tv:
                addExtrasItem("", "");
                break;
            default:
                break;
        }
    }

    private void setExtras(HashMap<String, String> extras) {
        if (extras != null && !extras.isEmpty()) {
            String key = null;
            String value = null;
            for (Map.Entry entry : extras.entrySet()) {
                key = (String) entry.getKey();
                value = (String) entry.getValue();
                if (!TextUtils.isEmpty(key) && !TextUtils.isEmpty(value)) {
                    addExtrasItem(key, value);
                }
            }
        }
    }

    private void addExtrasItem(String key, String value) {
        ExtraItemView itemView = new ExtraItemView(mContext, new ExtraItemView.OnExtraItemViewListener() {
            @Override
            public void OnDeleteBtnClick(ExtraItemView itemView) {
                if (mExtrasViewItems != null && mExtrasViewItems.size() > 0) {
                    mExtrasViewItems.remove(itemView);
                }
                if (mExtrasLy != null) {
                    mExtrasLy.removeView(itemView.rootView);
                    mExtrasLy.invalidate();
                }
            }
        });

        itemView.keyEtv.setText(key);
        itemView.valueEtv.setText(value);

        mExtrasViewItems.add(itemView);
        mExtrasLy.addView(itemView.rootView);
        mExtrasLy.invalidate();
    }

    private void onNetworkTypesChanged() {
        int networkTypes = getNetworkTypes();
        // 可以移动数据网络，默认可以使用Wifi网络
        if (NetworkParseUtil.containsMobile(networkTypes)) {
            networkTypes = NetworkParseUtil.addNetworkType(networkTypes, NetworkType.NETWORK_WIFI);
        }
        updateNetworkText(networkTypes);
    }

    private void updateNetworkText(int networkTypes) {
        String networkStr = "";
        if (NetworkParseUtil.containsWifi(networkTypes)) {
            networkStr += " Wifi ";
        }
        if (NetworkParseUtil.containsMobile(networkTypes)) {
            networkStr += " Mobile ";
        }
        if (NetworkParseUtil.containsBluetooth(networkTypes)) {
            networkStr += " Bluetooth ";
        }
        mNetworkTypesTv.setText(String.valueOf(networkTypes) + " " + networkStr);
    }

    private static <T extends View> T findView(View rootView, int resId) {
        return (T) rootView.findViewById(resId);
    }

    @Override
    public int getId() {
        return parseInt(mIdTv.getText().toString(), -1);
    }

    @Override
    public String getUrl() {
        return mUrlEtv.getText().toString();
    }

    @Override
    public String getFileName() {
        String fileName = mFileNameEtv.getText().toString();
        return TextUtils.isEmpty(fileName) ? null : fileName;
    }

    @Override
    public String getFileExtension() {
        String string = mFileExtensionEtv.getText().toString();
        return TextUtils.isEmpty(string) ? null : string;
    }

    @Override
    public String getSavePath() {
        String string = mSavePathEtv.getText().toString();
        return TextUtils.isEmpty(string) ? null : string;
    }

    @Override
    public long getPresetFileSize() {
        String string = mPresetFileSizeTv.getText().toString();
        return TextUtils.isEmpty(string) ? -1 : parseLong(string, -1);
    }

    @Override
    public boolean isAutoCheckSize() {
        return mAutoCheckSizeChx.isChecked();
    }

    @Override
    public int getPriority() {
        String string = mPriorityEtv.getText().toString();
        return TextUtils.isEmpty(string) ? 0 : parseInt(string, 0);
    }

    @Override
    public String getCheckType() {
        String string = mCheckTypeEtv.getText().toString();
        return TextUtils.isEmpty(string) ? null : string;
    }

    @Override
    public String getCheckCode() {
        String string = mCheckCodeEtv.getText().toString();
        return TextUtils.isEmpty(string) ? null : string;
    }

    @Override
    public boolean isCheckEnable() {
        return mCheckEnableChx.isChecked();
    }

    @Override
    public int getNetworkTypes() {
        boolean wifiEnable = mNetworkWifiChx.isChecked();
        boolean mobileEnable = mNetworkMobileChx.isChecked();
        boolean bluetoothEnable = mNetworkBluetoothChx.isChecked();
        int networkTypes = NetworkType.NETWORK_UNKNOWN;
        if (wifiEnable) {
            networkTypes |= NetworkType.NETWORK_WIFI;
        }
        if (mobileEnable) {
            networkTypes |= NetworkType.NETWORK_MOBILE;
        }
        if (bluetoothEnable) {
            networkTypes |= NetworkType.NETWORK_BLUETOOTH;
        }
        return networkTypes;
    }

    @Override
    public boolean isNeedQueue() {
        return mNeedQueueChx.isChecked();
    }

    @Override
    public String getReserver() {
        String string = mReserverEtv.getText().toString();
        return TextUtils.isEmpty(string) ? null : string;
    }

    @Override
    public HashMap<String, String> getExtrasMap() {
        HashMap<String, String> result = new HashMap<>();
        if (mExtrasViewItems != null && !mExtrasViewItems.isEmpty()) {
            EditText keyEtv = null;
            EditText valueEtv = null;
            String key = null;
            String value = null;
            boolean hasEmpty = false;

            for (ExtraItemView itemView : mExtrasViewItems) {
                keyEtv = itemView.keyEtv;
                valueEtv = itemView.valueEtv;
                key = keyEtv.getText().toString();
                value = valueEtv.getText().toString();
                if (!TextUtils.isEmpty(key) &&
                        !TextUtils.isEmpty(value)) {
                    result.put(key, value);
                } else {
                    hasEmpty = true;
                }
            }
            if (hasEmpty) {
                ToastUtil.showToast(mContext, " 扩展字段中含有空值，将会排除 ");
            }
        }
        return result;
    }

    @Override
    public int getNotificationVisibility() {
        String string = mNotificationTv.getText().toString();
        return TextUtils.isEmpty(string) ? 0 : parseInt(string, 0);
    }

    @Override
    public boolean hasAllowAdjustSavePath() {
        return mAllowAdjustSavePathChx.isChecked();
    }

    @Override
    public boolean isShowRealTimeInfo() {
        return mShowRealTimeChx.isChecked();
    }

    @Override
    public int getMinProgressTime() {
        String str = mMinProgressTimeEtv.getText().toString();
        int number = C.DownLoadConfig.DEFAULT_MIN_PROGRESS_TIME;
        try {
            number = Integer.parseInt(str);
        } catch (Exception e) {
            //ToastUtil.showToast(mContext, "转换进度回调时间异常，使用默认值:"+ number);
        }
        return number;
    }

    @Override
    public boolean isAutoUnpack() {
        return mAutoUnpackChx.isChecked();
    }

    @Override
    public String getUnpackPath() {
        String string = mUnpackPathEtv.getText().toString();
        return TextUtils.isEmpty(string) ? null : string;
    }

    @Override
    public boolean isDeleteSourceAfterUnpack() {
        return mDeleteSourceAfterUnpackChx.isChecked();
    }

    @Override
    public boolean isDeleteNoEndTaskAndCache() {
        return mDeleteNoEndTaskChx.isChecked();
    }

    @Override
    public boolean isDeleteEndTaskAndCache() {
        return mDeleteEndTaskChx.isChecked();
    }

    @Override
    public int getDownloadThreads() {
        return 1;
    }

    @Override
    public String getModuleName() {
        return mEditModuleEtv.getEditableText().toString();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Throwable throwable) {
            L.e(throwable, " parse int error ! ");
        }
        return defaultValue;
    }

    private long parseLong(String value, long defaultValue) {
        try {
            return Long.parseLong(value);
        } catch (Throwable throwable) {
            L.e(throwable, " parse long error ! ");
        }
        return defaultValue;
    }

    public void hidePanel() {
        mRootView.setVisibility(View.GONE);
        final InputMethodManager inputMethodManager = (InputMethodManager) mContext.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
    }

    public void showPanel() {
        mRootView.setVisibility(View.VISIBLE);
    }

    public boolean isShow() {
        return mRootView.getVisibility() == View.VISIBLE;
    }

    private static class ExtraItemView {
        public View rootView;
        public EditText keyEtv;
        public EditText valueEtv;
        public ImageView deleteImgBtn;

        public ExtraItemView(Context context, final OnExtraItemViewListener listener) {
            View view = LayoutInflater.from(context).inflate(R.layout.layout_extras_item, null, true);
            this.rootView = view;
            this.keyEtv = findView(view, R.id.extras_key_etv);
            this.valueEtv = findView(view, R.id.extras_value_etv);
            this.deleteImgBtn = findView(view, R.id.extras_delete_img_btn);
            this.deleteImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.OnDeleteBtnClick(ExtraItemView.this);
                    }
                }
            });
        }

        public interface OnExtraItemViewListener {
            void OnDeleteBtnClick(ExtraItemView itemView);
        }
    }
}
