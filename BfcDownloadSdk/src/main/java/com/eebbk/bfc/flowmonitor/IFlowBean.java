package com.eebbk.bfc.flowmonitor;

/**
 * Created by lzy on 2018/7/16.
 */
public class IFlowBean {

    private String[] subFlow;
    private String info;
    private String uniqueTag;
    private Object[] extras;

    private IFlowBean(Builder builder) {
        this.subFlow = builder.subFlow;
        this.info = builder.info;
        this.uniqueTag = builder.uniqueTag;
        this.extras = builder.extras;
    }

    public static class Builder {
        private String[] subFlow;
        private String info;
        private String uniqueTag;
        private Object[] extras;

        public Builder setFlow(String... subFlow) {
            this.subFlow = subFlow;
            return this;
        }

        public Builder setInfo(String info) {
            this.info = info;
            return this;
        }

        public Builder setUniqueTag(String uniqueTag) {
            this.uniqueTag = uniqueTag;
            return this;
        }

        public Builder setExtras(Object... extras) {
            this.extras = extras;
            return this;
        }

        public IFlowBean build() {
            return new IFlowBean(this);
        }
    }

    public String[] getSubFlow() {
        return subFlow;
    }

    public String getInfo() {
        return info;
    }

    public Object[] getExtras() {
        return extras;
    }

    public String getUniqueTag() {
        return uniqueTag;
    }
}
