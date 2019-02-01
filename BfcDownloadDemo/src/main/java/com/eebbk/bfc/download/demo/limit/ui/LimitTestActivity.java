package com.eebbk.bfc.download.demo.limit.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;
import com.eebbk.bfc.download.demo.util.IntentUtil;

public class LimitTestActivity extends BaseActivity implements View.OnClickListener {

    private Button mSingleTaskCycleDownloadBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_limit_test);
        setTitle("极限测试");
        initView();
    }

    private void initView(){
        mSingleTaskCycleDownloadBtn = findView(R.id.single_task_cycle_download_test_btn);


        mSingleTaskCycleDownloadBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.single_task_cycle_download_test_btn:
                IntentUtil.gotoSingleTaskCycleDownloadTest(LimitTestActivity.this);
                break;
            default:
                break;
        }
    }

}
