package com.eebbk.bfc.flowmonitor;

/**
 * 流程监控操作接口.
 */
public interface IFlowOperate {

    void addInfo(IFlowBean bean);

    void addFlow(String... subFlow);

    void addFlow(IFlowBean bean);
}
