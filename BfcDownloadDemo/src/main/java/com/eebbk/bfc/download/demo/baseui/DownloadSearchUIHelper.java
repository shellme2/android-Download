package com.eebbk.bfc.download.demo.baseui;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.basic.ui.IMultiTaskView;
import com.eebbk.bfc.download.demo.util.ToastUtil;
import com.eebbk.bfc.sdk.download.Status;
import com.eebbk.bfc.download.demo.util.L;
import com.eebbk.bfc.sdk.downloadmanager.ITask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-25 2:36
 * Email: jacklulu29@gmail.com
 */

public class DownloadSearchUIHelper implements View.OnClickListener {

    private Context mContext;
    private View mRootView;
    private Spinner mFindTypesSpinner;
    private EditText mEditIdEtv;
    private Spinner mStatusSpinner;
    private RelativeLayout mExtrasRly;
    private EditText mModuleEtv;

    private Button mAddExtrasBtn;
    private LinearLayout mExtrasContentLy;

    private Button mFindBtn;
    private Button mCancelBtn;

    private int mState = Status.DOWNLOAD_INVALID;
    private int mFindType = IMultiTaskView.SEARCH_ALL;

    private ISearchHandler mHandler;

    private ArrayList<ExtraItemView> mExtrasViewItems = new ArrayList<>();

    public DownloadSearchUIHelper(ISearchHandler handler){
        mHandler = handler;
    }

    public void initView(Context context, View rootView){
        mContext = context;
        mRootView = rootView;
        mFindTypesSpinner = findView(rootView, R.id.find_type_spinner);
        String[] findTypesArray = rootView.getResources().getStringArray(R.array.find_types_array);
        ArrayAdapter<String> findTypesAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                findTypesArray);
        mFindTypesSpinner.setAdapter(findTypesAdapter);
        mEditIdEtv = findView(rootView, R.id.find_edit_id_etv);
        mStatusSpinner = findView(rootView, R.id.find_status_spinner);
        String[] statusArray = rootView.getResources().getStringArray(R.array.status_array);
        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(
                context,
                android.R.layout.simple_spinner_item,
                statusArray);
        mStatusSpinner.setAdapter(statusAdapter);

        mExtrasRly = findView(rootView, R.id.extras_rly);
        mAddExtrasBtn = findView(rootView, R.id.find_extras_add_btn);
        mAddExtrasBtn.setOnClickListener(this);
        mExtrasContentLy = findView(rootView, R.id.find_extras_ly);

        mModuleEtv = findView(rootView, R.id.module_name_etv);

        mFindTypesSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onFindTypesSpinnerSelected(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        mStatusSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                onStatusSpinnerSelected(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // do nothing
            }
        });

        mFindBtn = findView(rootView, R.id.find_btn);
        mFindBtn.setOnClickListener(this);
        mCancelBtn = findView(rootView, R.id.cancel_btn);
        mCancelBtn.setOnClickListener(this);
    }

    private void onFindTypesSpinnerSelected(int position){
        mFindType = position;
        switch (position){
            case IMultiTaskView.SEARCH_BY_ID:
                mEditIdEtv.setVisibility(View.VISIBLE);
                mStatusSpinner.setVisibility(View.GONE);
                mExtrasRly.setVisibility(View.GONE);
                break;
            case IMultiTaskView.SEARCH_BY_STATUS:
                mEditIdEtv.setVisibility(View.GONE);
                mStatusSpinner.setVisibility(View.VISIBLE);
                mExtrasRly.setVisibility(View.GONE);
                break;
            case IMultiTaskView.SEARCH_BY_EXTRAS:
                mEditIdEtv.setVisibility(View.GONE);
                mStatusSpinner.setVisibility(View.GONE);
                mExtrasRly.setVisibility(View.VISIBLE);
                break;
            case IMultiTaskView.SEARCH_ALL:
                mEditIdEtv.setVisibility(View.GONE);
                mStatusSpinner.setVisibility(View.GONE);
                mExtrasRly.setVisibility(View.GONE);
                break;
            default:
                mFindType = 0;
                break;
        }
    }

    private void onStatusSpinnerSelected(int position){
        switch (position){
            case 0:
                mState = Status.DOWNLOAD_INVALID;
                break;
            case 1:
                mState = Status.DOWNLOAD_WAITING;
                break;
            case 2:
                mState = Status.DOWNLOAD_STARTED;
                break;
            case 3:
                mState = Status.DOWNLOAD_CONNECTED;
                break;
            case 4:
                mState = Status.DOWNLOAD_PROGRESS;
                break;
            case 5:
                mState = Status.DOWNLOAD_PAUSE;
                break;
            case 6:
                mState = Status.DOWNLOAD_RETRY;
                break;
            case 7:
                mState = Status.DOWNLOAD_FAILURE;
                break;
            case 8:
                mState = Status.DOWNLOAD_SUCCESS;
                break;
            case 9:
                mState = Status.CHECK_STARTED;
                break;
            case 10:
                mState = Status.CHECK_PROGRESS;
                break;
            case 11:
                mState = Status.CHECK_FAILURE;
                break;
            case 12:
                mState = Status.CHECK_SUCCESS;
                break;
            case 13:
                mState = Status.UNPACK_STARTED;
                break;
            case 14:
                mState = Status.UNPACK_PROGRESS;
                break;
            case 15:
                mState = Status.UNPACK_FAILURE;
                break;
            case 16:
                mState = Status.UNPACK_SUCCESS;
                break;
            default:
                mState = Status.DOWNLOAD_INVALID;
                break;
        }
    }

    public String getModuleName(){
        return mModuleEtv.getEditableText().toString();
    }

    public int getSearchType() {
        return mFindType;
    }

    public int getSearchId() {
        int id = ITask.INVALID_GENERATE_ID;
        String idStr = mEditIdEtv.getText().toString();
        if(!TextUtils.isEmpty(idStr)){
            try {
                id = Integer.parseInt(idStr);
            } catch (Exception e){
                L.e(e, " parse int error ");
            }
        }
        return id;
    }

    public int getSearchStatus() {
        return mState;
    }

    public String[] getSearchExtraKeys() {
        HashMap<String, String> result = getExtrasMap();
        return result.keySet().toArray(new String[result.size()]);
    }

    public String[] getSearchExtraValues() {
        HashMap<String, String> result = getExtrasMap();
        return result.values().toArray(new String[result.size()]);
    }

    public HashMap<String, String> getExtrasMap(){
        HashMap<String, String> result = new HashMap<>();
        if(mExtrasViewItems != null && !mExtrasViewItems.isEmpty()){
            EditText keyEtv = null;
            EditText valueEtv = null;
            String key = null;
            String value = null;
            boolean hasEmpty = false;

            for(ExtraItemView itemView: mExtrasViewItems){
                keyEtv  = itemView.keyEtv;
                valueEtv = itemView.valueEtv;
                key = keyEtv.getText().toString();
                value = valueEtv.getText().toString();
                if(!TextUtils.isEmpty(key) &&
                        !TextUtils.isEmpty(value)){
                    result.put(key, value);
                } else {
                    hasEmpty = true;
                }
            }
            if(hasEmpty){
                ToastUtil.showToast(mContext, " 扩展字段中含有空值，将会排除 ");
            }
        }
        return result;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.find_btn:
                mHandler.searchTasks(false);
                break;
            case R.id.cancel_btn:
                mHandler.closeSearchPanel();
                break;
            case R.id.find_extras_add_btn:
                addExtrasItem("", "");
                break;
            default:
                break;
        }
    }

    private void setExtras(HashMap<String, String> extras){
        if(extras != null && !extras.isEmpty()){
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

    private void addExtrasItem(String key, String value){
        ExtraItemView itemView = new ExtraItemView(mContext, new ExtraItemView.OnExtraItemViewListener() {
            @Override
            public void OnDeleteBtnClick(ExtraItemView itemView) {
                if(mExtrasViewItems != null && mExtrasViewItems.size() > 0){
                    mExtrasViewItems.remove(itemView);
                }
                if(mExtrasContentLy != null){
                    mExtrasContentLy.removeView(itemView.rootView);
                    mExtrasContentLy.invalidate();
                }
            }
        });

        itemView.keyEtv.setText(key);
        itemView.valueEtv.setText(value);

        mExtrasViewItems.add(itemView);
        mExtrasContentLy.addView(itemView.rootView);
        mExtrasContentLy.invalidate();
    }

    public void hidePanel(){
        mRootView.setVisibility(View.GONE);
        final InputMethodManager inputMethodManager = (InputMethodManager) mContext.getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(mRootView.getWindowToken(), 0);
    }

    public void showPanel(){
        mRootView.setVisibility(View.VISIBLE);
    }

    public boolean isShow(){
        return mRootView.getVisibility() == View.VISIBLE;
    }

    private static <T extends View> T findView(View rootView, int resId){
        return (T)rootView.findViewById(resId);
    }

    private static class ExtraItemView {
        public View rootView;
        public EditText keyEtv;
        public EditText valueEtv;
        public ImageView deleteImgBtn;

        public ExtraItemView(Context context, final OnExtraItemViewListener listener){
            View view = LayoutInflater.from(context).inflate(R.layout.layout_extras_item, null, true);
            this.rootView = view;
            this.keyEtv = findView(view, R.id.extras_key_etv);
            this.valueEtv = findView(view, R.id.extras_value_etv);
            this.deleteImgBtn = findView(view, R.id.extras_delete_img_btn);
            this.deleteImgBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(listener != null){
                        listener.OnDeleteBtnClick(ExtraItemView.this);
                    }
                }
            });
        }

        public interface OnExtraItemViewListener{
            void OnDeleteBtnClick(ExtraItemView itemView);
        }
    }

    public interface ISearchHandler {

        void searchTasks(boolean ifErrorThenAll);

        void closeSearchPanel();
    }

}
