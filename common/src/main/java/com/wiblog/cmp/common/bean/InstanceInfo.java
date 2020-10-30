package com.wiblog.cmp.common.bean;

/**
 * 客户端信息
 *
 * @author pwm
 */
public class InstanceInfo {

    private volatile String instanceId;

    private volatile String appName;

    private volatile String ipAddr;

    private volatile int port;

    private volatile String serviceUrl;

    private volatile int expiredTime;

    private volatile boolean state;

    public boolean getState() {
        return state;
    }

    public void setState(boolean state) {
        this.state = state;
    }

    public int getExpiredTime() {
        return expiredTime;
    }

    public void setExpiredTime(int expiredTime) {
        this.expiredTime = expiredTime;
    }

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
