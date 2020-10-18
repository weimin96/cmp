package com.wiblog.cmp.client.log;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "cmp.log")
public class LogConfigProperties {

    private String logDir;

    public LogConfigProperties() {
    }

    public String getLogDir() {
        return logDir;
    }

    public void setLogDir(String logDir) {
        this.logDir = logDir;
    }
}
