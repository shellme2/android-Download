package com.eebbk.bfc.download.demo.baseui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;

import com.eebbk.bfc.download.demo.DownloadDemoApplication;
import com.eebbk.bfc.download.demo.R;
import com.eebbk.bfc.download.demo.util.DemoUtil;
import com.eebbk.bfc.download.demo.util.IntentUtil;
import com.eebbk.bfc.download.demo.util.L;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    private static final int WRITE_EXTERNAL_STORAGE_REQUEST_CODE = 999;
    private String mSaveLogPath = null;

    private Button mBasicFunctionTestBtn;
    private Button mSafeTestBtn;
    private Button mPerformanceTestBtn;
    private Button mLimitTestBtn;
    private Button mOtherTestBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    WRITE_EXTERNAL_STORAGE_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == WRITE_EXTERNAL_STORAGE_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
                boolean isDebug = DownloadDemoApplication.SISDEBUG;
                mSaveLogPath = DemoUtil.getSaveLogPath();
                L.setLog(L.buildLog(isDebug, mSaveLogPath), isDebug);
                L.e(" init save log success " + mSaveLogPath);
            } else {
                // Permission Denied
                L.e(" no permission granted ");
            }
        }
    }

    private void initView(){
        mBasicFunctionTestBtn = findView(R.id.basic_function_test_btn);
        mSafeTestBtn = findView(R.id.safe_test_btn);
        mPerformanceTestBtn = findView(R.id.performance_test_btn);
        mLimitTestBtn = findView(R.id.limit_test_btn);
        mOtherTestBtn = findView(R.id.other_test_btn);

        findView(R.id.net_test_btn).setOnClickListener(this);
        mBasicFunctionTestBtn.setOnClickListener(this);
        mSafeTestBtn.setOnClickListener(this);
        mPerformanceTestBtn.setOnClickListener(this);
        mLimitTestBtn.setOnClickListener(this);
        mOtherTestBtn.setOnClickListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 主界面销毁，应用退出，如果下载服务空闲，自动停止
//        DownloadController.getInstance().stopServiceIfIdle();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.basic_function_test_btn: {
                IntentUtil.gotoBasicFunctionTest(MainActivity.this);
                break;
            }
            case R.id.safe_test_btn: {
                IntentUtil.gotoSafeTest(MainActivity.this);
                break;
            }
            case R.id.net_test_btn:
                IntentUtil.gotoNetTest(MainActivity.this);
                break;
            case R.id.performance_test_btn: {
                IntentUtil.gotoPerformanceTest(MainActivity.this);
                break;
            }
            case R.id.limit_test_btn: {
                IntentUtil.gotoLimitTest(MainActivity.this);
                break;
            }
            case R.id.other_test_btn: {
                IntentUtil.gotoOtherTest(MainActivity.this);
                break;
            }
            default: {
                break;
            }
        }
    }

}
