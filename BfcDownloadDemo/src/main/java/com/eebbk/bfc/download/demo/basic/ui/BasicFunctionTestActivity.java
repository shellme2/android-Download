package com.eebbk.bfc.download.demo.basic.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;
import com.eebbk.bfc.download.demo.util.IntentUtil;

public class BasicFunctionTestActivity extends BaseActivity implements View.OnClickListener {

    private Button mSingleTaskBtn;
    private Button mMultiTaskBtn;
    private Button mShowVersionInfoBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_basic_function_test);
        setTitle("基本功能测试");

        initView();
    }

    private void initView(){
        mSingleTaskBtn = findView(R.id.single_task_btn);
        mMultiTaskBtn = findView(R.id.multi_task_btn);
        mShowVersionInfoBtn = findView(R.id.show_version_info_btn);

        mSingleTaskBtn.setOnClickListener(this);
        mMultiTaskBtn.setOnClickListener(this);
        mShowVersionInfoBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.single_task_btn:
                IntentUtil.gotoSingleTaskActivity(BasicFunctionTestActivity.this);
                break;
            case R.id.multi_task_btn:
                IntentUtil.gotoMultiTaskActivity(BasicFunctionTestActivity.this);
                break;
            case R.id.show_version_info_btn:
                IntentUtil.gotoShowVersionInfoActivity(BasicFunctionTestActivity.this);
                break;
            default:
                break;
        }
    }

}
