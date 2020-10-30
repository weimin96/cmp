package com.wiblog.cmp.client;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wiblog.cmp.client.config.CmpClientConfig;
import com.wiblog.cmp.common.bean.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.*;

/**
 * @author pwm
 */
public class CmpClient {

    private static final Logger log = LoggerFactory.getLogger(CmpClient.class);

    private final ScheduledExecutorService scheduler;

    private final ThreadPoolExecutor heartbeatExecutor;
    private final ThreadPoolExecutor cacheRefreshExecutor;

    InstanceInfo instanceInfo;

    RestTemplate restTemplate;

    CmpClientConfig clientConfig;

    public CmpClient(InstanceInfo instanceInfo, RestTemplate restTemplate, CmpClientConfig clientConfig) {
        this.instanceInfo = instanceInfo;
        this.restTemplate = restTemplate;
        this.clientConfig = clientConfig;
        scheduler = Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-%d")
                        .setDaemon(true)
                        .build());
        // 注册信息线程池

        // 心跳线程池
        heartbeatExecutor = new ThreadPoolExecutor(
                1, 2, 0, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-HeartbeatExecutor-%d")
                        .setDaemon(true)
                        .build()
        );

        //
        cacheRefreshExecutor = new ThreadPoolExecutor(
                1, 2, 0, TimeUnit.SECONDS,
                new SynchronousQueue<>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-CacheRefreshExecutor-%d")
                        .setDaemon(true)
                        .build()
        );

        // 注册
        if (register()) {
            // 注册成功
            log.info("注册成功");
            this.instanceInfo.setState(true);
        }

        // 心跳维持
        initScheduledTasks();

    }

    private void initScheduledTasks() {
        // Heartbeat timer
        scheduler.schedule(
                new CmpTimerTask(
                        "heartbeat",
                        scheduler,
                        heartbeatExecutor,
                        clientConfig.getHeartIntervalTimerInSeconds(),
                        TimeUnit.SECONDS,
                        3,
                        new HeartbeatThread()
                ),
                clientConfig.getHeartIntervalTimerInSeconds(), TimeUnit.SECONDS);
    }

    /**
     * 客户端下线 会自动调用该方法
     */
    public void shutdown() {

    }

    /**
     * 心跳任务
     */
    private class HeartbeatThread implements Runnable {

        @Override
        public void run() {
            if (renew()) {
                log.info("心跳成功");
                instanceInfo.setState(true);
            }else{
                instanceInfo.setState(false);
            }
        }
    }

    /**
     * 客户端注册 rest请求
     *
     * @return
     */
    private boolean register() {
        log.info("向注册中心注册-[{}:{}]", instanceInfo.getIpAddr(), instanceInfo.getPort());
        log.info("请求url-[{}register]", instanceInfo.getServiceUrl());
        JSONObject response = null;
        try {
            response = restTemplate.postForObject(instanceInfo.getServiceUrl() + "register", instanceInfo, JSONObject.class);
        } catch (ResourceAccessException e){
            log.error("连接注册中心失败-{}",e.getMessage());
        } catch (Exception e) {
            log.error("注册异常", e);
        }
        log.info("{}", response);
        return response != null && "204".equals(response.getString("status"));
    }

    /**
     * 心跳请求
     */
    boolean renew() {
        String url = instanceInfo.getServiceUrl() + "renew" ;
        log.info("向注册中心发送心跳-[{}:{}]", instanceInfo.getIpAddr(), instanceInfo.getPort());
        log.info("请求url-[{}]", url);
        JSONObject response;

        try {
            response = restTemplate.postForObject(url, instanceInfo.getAppName(), JSONObject.class);
            if (response != null) {
                String status = response.getString("status");
                if ("404".equals(status)) {
                    log.info("未注册，重新注册");
                    return register();
                }
                return "200".equals(status);
            }
        } catch (Throwable e) {
            log.error("心跳异常", e);
            return false;
        }
        return false;
    }
}

