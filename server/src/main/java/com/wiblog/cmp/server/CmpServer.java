package com.wiblog.cmp.server;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.wiblog.cmp.server.bean.CmpServerConfig;
import com.wiblog.cmp.server.bean.InstanceInfo;
import com.wiblog.cmp.server.bean.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @author pwm
 * @date 2020/4/25
 */
@Component
public class CmpServer {

    public static final Logger log = LoggerFactory.getLogger(CmpServer.class);

    private CmpServerConfig serverConfig;

    private final ScheduledExecutorService scheduler;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final Lock read = readWriteLock.readLock();

    private final ConcurrentHashMap<String, Lease<InstanceInfo>> register;

    public CmpServer(CmpServerConfig serverConfig) {
        this.serverConfig = serverConfig;
        this.register = ResponseCache.single().getRegistry();
        // 用于服务续约的线程池
        scheduler = Executors.newScheduledThreadPool(1,
                new ThreadFactoryBuilder()
                        .setNameFormat("RenewalThreshold-%d")
                        .setDaemon(true)
                        .build());

        scheduler.scheduleWithFixedDelay(() -> {
            log.info("服务下线任务执行");
            evictInstance();
        },serverConfig.getEvictionIntervalTimerInMs(),serverConfig.getEvictionIntervalTimerInMs(), TimeUnit.MILLISECONDS);

    }

    /**
     * 自动过期任务
     */
    private void evictInstance(){
        // TODO 支持多实例
        // 拿出各个实例的lastRenewalTimestamp
        try {
            // 过期的实例
            List<Lease<InstanceInfo>> expiredLeases = new ArrayList<>();

            Set<Map.Entry<String, Lease<InstanceInfo>>> entrySet = register.entrySet();
            for (Map.Entry<String, Lease<InstanceInfo>> entry: entrySet){
                Lease<InstanceInfo> lease = entry.getValue();
                if (lease != null && lease.isExpired() && lease.getHolder() != null){

                    expiredLeases.add(lease);
                }
            }

            for (Lease<InstanceInfo> lease : expiredLeases){
                String appName = lease.getHolder().getAppName();
                internalCancel(appName);
            }
        }catch (Exception e){
            log.error(e.getMessage(),e);
        }

    }

    /**
     * 服务下线
     * @param appName
     */
    private boolean internalCancel(String appName){
        try {
            read.lock();
            Lease<InstanceInfo> leaseToCancel = register.remove(appName);
            if (leaseToCancel == null) {
                log.warn("服务下线失败，{}未注册",appName);
                return false;
            }
            log.info("服务下线 {}", appName);
            return true;
        }finally {
            read.unlock();
        }
    }

    /**
     * 服务端下线 会自动调用该方法
     */
    public void shutdown() {
        log.info("服务端下线");
    }
}
