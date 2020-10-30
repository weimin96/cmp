package com.wiblog.cmp.server.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

/**
 * @author pwm
 */
@Component
@ConfigurationProperties(CmpServerConfig.PREFIX)
public class CmpServerConfig {

    static final String PREFIX = "cmp.server";

    private static final int MINUTES = 60 * 1000;

    /**
     * 实例失效定时器执行间隔
     */
    private Integer evictionIntervalTimerInMs = 60 * 1000;

    public Integer getEvictionIntervalTimerInMs() {
        return evictionIntervalTimerInMs;
    }

    public void setEvictionIntervalTimerInMs(Integer evictionIntervalTimerInMs) {
        this.evictionIntervalTimerInMs = evictionIntervalTimerInMs;
    }
}
