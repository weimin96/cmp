package com.wiblog.cmp.client.bean;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 存放客户端实例连接配置信息
 * @author pwm
 * @date 2020/2/1
 */
@ConfigurationProperties("cmp.instance")
public class CmpInstanceConfig {

    private String ipAddress;

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }
}
