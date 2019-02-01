package com.eebbk.bfc.download.demo.util;

import android.app.Activity;
import android.content.Intent;

import com.eebbk.bfc.download.demo.basic.ui.BasicFunctionTestActivity;
import com.eebbk.bfc.download.demo.basic.ui.MultiTaskActivity;
import com.eebbk.bfc.download.demo.basic.ui.ShowVersionInfoActivity;
import com.eebbk.bfc.download.demo.basic.ui.SingleTaskActivity;
import com.eebbk.bfc.download.demo.limit.ui.LimitTestActivity;
import com.eebbk.bfc.download.demo.limit.ui.SingleTaskCycleDownloadTestActivity;
import com.eebbk.bfc.download.demo.net_test.NetTestActivity;
import com.eebbk.bfc.download.demo.other.OtherTestActivity;
import com.eebbk.bfc.download.demo.performance.PerformanceTestActivity;
import com.eebbk.bfc.download.demo.safe.SafeTestActivity;

/**
 * Desc: 界面跳转
 * Author: llp
 * Create Time: 2016-11-14 20:19
 * Email: jacklulu29@gmail.com
 */

public class IntentUtil {

    private static final Class<?> sBasicFunctionTest = BasicFunctionTestActivity.class;
    private static final Class<?> sSafeTest = SafeTestActivity.class;
    private static final Class<?> sPerformanceTest = PerformanceTestActivity.class;
    private static final Class<?> sLimitTest = LimitTestActivity.class;
    private static final Class<?> sOtherTest = OtherTestActivity.class;

    private static final Class<?> sSingleTask = SingleTaskActivity.class;
    private static final Class<?> sMultiTask = MultiTaskActivity.class;
    private static final Class<?> sShowVersionInfo = ShowVersionInfoActivity.class;

    private static final Class<?> sSingleTaskCycleDownloadTest = SingleTaskCycleDownloadTestActivity.class;

    private IntentUtil(){
        // private construct
    }

    public static void gotoBasicFunctionTest(Activity activity){
        startTestActivity(activity, sBasicFunctionTest);
    }

    public static void gotoSafeTest(Activity activity){
        startTestActivity(activity, sSafeTest);
    }

    public static void gotoNetTest(Activity activity){
        startTestActivity(activity, NetTestActivity.class);
    }

    public static void gotoPerformanceTest(Activity activity){
        startTestActivity(activity, sPerformanceTest);
    }

    public static void gotoLimitTest(Activity activity){
        startTestActivity(activity, sLimitTest);
    }

    public static void gotoOtherTest(Activity activity){
        startTestActivity(activity, sOtherTest);
    }

    // -----------------------------------------------------------
    public static void gotoSingleTaskActivity(Activity activity){
        startTestActivity(activity, sSingleTask);
    }

    public static void gotoMultiTaskActivity(Activity activity){
        startTestActivity(activity, sMultiTask);
    }

    public static void gotoShowVersionInfoActivity(Activity activity){
        startTestActivity(activity, sShowVersionInfo);
    }


    public static void gotoSingleTaskCycleDownloadTest(Activity activity){
        startTestActivity(activity, sSingleTaskCycleDownloadTest);
    }

    public static void startTestActivity(Activity activity, Class<?> activityClass){
        Intent intent = new Intent(activity, activityClass);
        activity.startActivity(intent);
    }
}
