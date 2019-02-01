package com.eebbk.bfc.download.demo.other;

import android.os.Bundle;
import android.view.View;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;

public class OtherTestActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_other_test);
        setTitle("其他测试");
        initView();
    }

    private void initView(){

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
    }

}
