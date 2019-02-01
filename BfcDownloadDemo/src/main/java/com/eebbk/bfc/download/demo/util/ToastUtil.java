package com.eebbk.bfc.download.demo.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-10-24 17:19
 * Email: jacklulu29@gmail.com
 */

public class ToastUtil {

    private ToastUtil(){
        // private construct
    }

    public static void showToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void showLongToast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

}
