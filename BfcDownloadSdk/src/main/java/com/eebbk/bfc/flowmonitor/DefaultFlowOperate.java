package com.eebbk.bfc.flowmonitor;

import android.util.Log;

/**
 * Created by lzy on 2018/7/16.
 */
public class DefaultFlowOperate implements IFlowOperate {

    @Override
    public void addInfo(IFlowBean bean) {
        Log.i(FlowMonitor.INFO, bean.getUniqueTag() + "#" + bean.getInfo());
    }

    @Override
    public void addFlow(String... subFlow) {
        Log.i(FlowMonitor.FLOW, stringJoin(" -> ", subFlow));
    }

    @Override
    public void addFlow(IFlowBean bean) {
        Log.i(FlowMonitor.FLOW, bean.getUniqueTag() + "#" + stringJoin(" -> ", bean.getSubFlow()));
    }

    private String stringJoin(String split, String... str) {
        StringBuilder result = new StringBuilder();

        boolean isFirst = true;
        for (String item : str) {
            if (isFirst) {
                isFirst = false;
            } else {
                result.append(split);
            }
            result.append(item);
        }
        return result.toString();
    }
}
