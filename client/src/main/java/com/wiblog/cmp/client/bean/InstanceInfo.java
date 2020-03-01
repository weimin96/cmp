package com.wiblog.cmp.client.bean;

/**
 * 客户端信息
 *
 * @author pwm
 * @date 2020/2/16
 */
public class InstanceInfo {

    private volatile String instanceId;

    private volatile String appName;

    private volatile String ipAddr;

    private volatile int port;

    private volatile String serviceUrl;


    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void setInstanceId(String instanceId) {
        this.instanceId = instanceId;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getIpAddr() {
        return ipAddr;
    }

    public void setIpAddr(String ipAddr) {
        this.ipAddr = ipAddr;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    @Override
    public String toString() {
        return "InstanceInfo{" +
                "instanceId='" + instanceId + '\'' +
                ", appName='" + appName + '\'' +
                ", ipAddr='" + ipAddr + '\'' +
                ", port=" + port +
                ", serviceUrl='" + serviceUrl + '\'' +
                '}';
    }
}
