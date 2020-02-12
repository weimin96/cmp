package com.wiblog.cmp.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wiblog.cmp.client.bean.CmpClientConfig;
import com.wiblog.cmp.client.bean.CmpInstanceConfig;
import sun.plugin2.main.server.HeartbeatThread;

import java.util.concurrent.*;

/**
 * @author pwm
 * @date 2020/2/1
 */
public class CmpClient {

    private final ScheduledExecutorService scheduler;

    private final ThreadPoolExecutor heartbeatExecutor;
    private final ThreadPoolExecutor cacheRefreshExecutor;

    public CmpClient(CmpClientConfig client) {
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
            if (renew()) {
                lastSuccessfulHeartbeatTimestamp = System.currentTimeMillis();
            }
        }
    }

    /**
     * 心跳请求
     */
    boolean renew() {
        HttpResponse<InstanceInfo> httpResponse;
        try {
            httpResponse = eurekaTransport.registrationClient.sendHeartBeat(instanceInfo.getAppName(), instanceInfo.getId(), instanceInfo, null);
            logger.debug(PREFIX + "{} - Heartbeat status: {}", appPathIdentifier, httpResponse.getStatusCode());
            if (httpResponse.getStatusCode() == 404) {
                REREGISTER_COUNTER.increment();
                logger.info(PREFIX + "{} - Re-registering apps/{}", appPathIdentifier, instanceInfo.getAppName());
                long timestamp = instanceInfo.setIsDirtyWithTime();
                boolean success = register();
                if (success) {
                    instanceInfo.unsetIsDirty(timestamp);
                }
                return success;
            }
            return httpResponse.getStatusCode() == 200;
        } catch (Throwable e) {
            logger.error(PREFIX + "{} - was unable to send heartbeat!", appPathIdentifier, e);
            return false;
        }
    }
}
