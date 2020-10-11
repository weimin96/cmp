package com.wiblog.cmp.client.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cmp.log")
public class LogConfigProperties {

    private String logPath;

    public LogConfigProperties() {
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }
}
