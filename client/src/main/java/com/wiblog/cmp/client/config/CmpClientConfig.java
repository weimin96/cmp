package com.wiblog.cmp.client.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author pwm
 * @date 2020/8/30
 */
@Component
@ConfigurationProperties(CmpClientConfig.PREFIX)
public class CmpClientConfig {

    static final String PREFIX = "cmp.client";

    private static final int MINUTES = 60 * 1000;

    /**
     * 心跳定时器执行间隔
     */
    private Integer heartIntervalTimerInSeconds = 60;

    public Integer getHeartIntervalTimerInSeconds() {
        return heartIntervalTimerInSeconds;
    }

    public void setHeartIntervalTimerInSeconds(Integer heartIntervalTimerInSeconds) {
        this.heartIntervalTimerInSeconds = heartIntervalTimerInSeconds;
    }
}
