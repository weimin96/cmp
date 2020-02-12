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
                new TimedSupervisorTask(
                        "heartbeat",
                        scheduler,
                        heartbeatExecutor,
                        renewalIntervalInSecs,
                        TimeUnit.SECONDS,
                        expBackOffBound,
                        new HeartbeatThread()
                ),
                renewalIntervalInSecs, TimeUnit.SECONDS);
    }

    /**
     * 客户端下线 会自动调用该方法
     */
    public void shutdown() {

    }
}
