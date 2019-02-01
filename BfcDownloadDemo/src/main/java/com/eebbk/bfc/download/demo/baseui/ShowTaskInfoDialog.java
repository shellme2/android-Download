package com.eebbk.bfc.download.demo.baseui;

import android.app.AlertDialog;
import android.content.Context;

/**
 * Desc:
 * Author: llp
 * Create Time: 2016-11-01 21:48
 * Email: jacklulu29@gmail.com
 */

public class ShowTaskInfoDialog {

    private ShowTaskInfoDialog(){
        // private construct
    }

    public static void show(Context context, CharSequence taskInfo){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setMessage(taskInfo)
                .setCancelable(true)
                .show();
    }

}
