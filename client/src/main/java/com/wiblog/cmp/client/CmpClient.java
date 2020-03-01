package com.wiblog.cmp.client;

import com.alibaba.fastjson.JSONObject;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wiblog.cmp.client.bean.InstanceInfo;
import com.wiblog.cmp.client.common.HttpResponse;
import com.wiblog.cmp.client.common.StateEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.ws.rs.core.Response;
import java.util.concurrent.*;

/**
 * @author pwm
 * @date 2020/2/1
 */
@Component
public class CmpClient {

    public static final Logger log = LoggerFactory.getLogger(CmpClient.class);

    private final ScheduledExecutorService scheduler;

    private final ThreadPoolExecutor heartbeatExecutor;
    private final ThreadPoolExecutor cacheRefreshExecutor;

    InstanceInfo instanceInfo;

    RestTemplate restTemplate;

    public CmpClient(InstanceInfo instanceInfo, RestTemplate restTemplate) {
        this.instanceInfo = instanceInfo;
        this.restTemplate = restTemplate;
        scheduler = Executors.newScheduledThreadPool(2,
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-%d")
                        .setDaemon(true)
                        .build());
        // 注册信息线程池

        // 心跳线程池
        heartbeatExecutor = new ThreadPoolExecutor(
                1, 2, 0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-HeartbeatExecutor-%d")
                        .setDaemon(true)
                        .build()
        );

        //
        cacheRefreshExecutor = new ThreadPoolExecutor(
                1, 2, 0, TimeUnit.SECONDS,
                new SynchronousQueue<Runnable>(),
                new ThreadFactoryBuilder()
                        .setNameFormat("DiscoveryClient-CacheRefreshExecutor-%d")
                        .setDaemon(true)
                        .build()
        );

        // 注册
        if (register()) {

        }

        initScheduledTasks();

    }

    private void initScheduledTasks() {
        // Heartbeat timer
        scheduler.schedule(
                new CmpTimerTask(
                        "heartbeat",
                        scheduler,
                        heartbeatExecutor,
                        60,
                        TimeUnit.SECONDS,
                        3,
                        new HeartbeatThread()
                ),
                60, TimeUnit.SECONDS);
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
            /*if (renew()) {
                lastSuccessfulHeartbeatTimestamp = System.currentTimeMillis();
            }*/
        }
    }

    /**
     * 客户端注册 rest请求
     *
     * @return
     */
    private boolean register() {
        log.info("注册");
        String data = JSONObject.toJSONString(instanceInfo);
        Response response = null;
        try {
            response = restTemplate.postForObject(instanceInfo.getServiceUrl() + "register", data, Response.class);
        } catch (Exception e) {
            log.error("注册异常", e);
        }
        return response != null && 204 == response.getStatus();
    }

}

/**
 * 心跳请求
 */
    /*boolean renew() {
        HttpResponse<InstanceInfo> httpResponse;
        // 客户端
        JSONObject jsonObject = restTemplate.postForObject(url, jsonString, JSONObject.class);
        try {
            httpResponse = eurekaTransport.registrationClient.sendHeartBeat(instanceInfo.getAppName(), instanceInfo.getId(), instanceInfo, null);
            if (httpResponse.getStatusCode() == 404) {
                REREGISTER_COUNTER.increment();
                long timestamp = instanceInfo.setIsDirtyWithTime();
                boolean success = register();
                if (success) {
                    instanceInfo.unsetIsDirty(timestamp);
                }
                return success;
            }
            return httpResponse.getStatusCode() == 200;
        } catch (Throwable e) {
            return false;
        }
    }*/

