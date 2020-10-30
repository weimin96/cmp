package com.wiblog.cmp.common.logger;

import java.io.Serializable;

/**
 * @author panweimin
 */
public class LogMessage implements Serializable {

    private static final long serialVersionUID = -9154095008255676966L;

    private String str;

    private String instanceId;

    private String appName;

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public LogMessage appName(String appName) {
        this.appName = appName;
        return this;
    }

    public void setStr(String str) {
        this.str = str;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getStr() {
        return str;
    }

    public LogMessage str(String str) {
        this.str = str;
        return this;
    }

    public LogMessage instanceId(String instanceId) {
        this.instanceId = instanceId;
        return this;
    }

    public static Builder builder(){
        return new Builder();
    }


    public static final class Builder {
        private String str;
        private String instanceId;
        private String appName;

        private Builder() {
        }

        public Builder str(String str) {
            this.str = str;
            return this;
        }

        public Builder instanceId(String instanceId) {
            this.instanceId = instanceId;
            return this;
        }

        public Builder appName(String appName) {
            this.appName = appName;
            return this;
        }

        public LogMessage build() {
            LogMessage logMessage = new LogMessage();
            logMessage.setStr(str);
            logMessage.setInstanceId(instanceId);
            logMessage.setAppName(appName);
            return logMessage;
        }
    }

    @Override
    public String toString() {
        return "LogMessage{" +
                "str='" + str + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", appName='" + appName + '\'' +
                '}';
    }
}
