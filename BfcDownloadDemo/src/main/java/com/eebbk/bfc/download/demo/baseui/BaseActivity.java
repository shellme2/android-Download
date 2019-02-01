package com.eebbk.bfc.download.demo.baseui;

import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class BaseActivity extends AppCompatActivity {

    public <T extends View> T findView(@IdRes int resId){
        return (T)findViewById(resId);
    }

}
