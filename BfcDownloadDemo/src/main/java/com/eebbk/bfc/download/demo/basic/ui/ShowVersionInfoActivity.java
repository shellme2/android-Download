package com.eebbk.bfc.download.demo.basic.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.baseui.BaseActivity;
import com.eebbk.bfc.sdk.downloadmanager.SDKVersion;

public class ShowVersionInfoActivity extends BaseActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_version_info);
        setTitle("版本信息");

        initView();
    }

    private void initView(){
        TextView infoTv = findView(R.id.version_info_tv);
        StringBuilder sb = new StringBuilder();
        sb.append("库名称: " + SDKVersion.getLibraryName());
        sb.append("\r\n \r\n版本序号: " + SDKVersion.getSDKInt());
        sb.append("\r\n \r\n版本名称: " + SDKVersion.getVersionName());
        sb.append("\r\n \r\n构建版本: " + SDKVersion.getBuildName());
        sb.append("\r\n \r\n构建时间: " + SDKVersion.getBuildTime());
        sb.append("\r\n \r\nTAG标签: " + SDKVersion.getBuildTag());
        sb.append("\r\n \r\nHEAD值: " + SDKVersion.getBuildHead());
        infoTv.setText(sb.toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
    }

}
