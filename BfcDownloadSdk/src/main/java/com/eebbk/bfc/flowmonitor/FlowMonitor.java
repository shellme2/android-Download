package com.eebbk.bfc.flowmonitor;

/**
 * Created by lzy on 2018/7/16.
 */
public class FlowMonitor {
    public static String FLOW = "FlowMonitor_Flow";
    public static String INFO = "FlowMonitor_Info";
    private static IFlowOperate mFlowOperate = null;

    public static void init() {
        mFlowOperate = new DefaultFlowOperate();
    }

    public static void init(IFlowOperate operate) {
        mFlowOperate = operate;
    }

    public static IFlowOperate getOperate() {
        return mFlowOperate;
    }
}
