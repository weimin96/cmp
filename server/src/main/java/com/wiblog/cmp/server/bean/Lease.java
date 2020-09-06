package com.wiblog.cmp.server.bean;

/**
 * @author pwm
 * @date 2020/3/1
 */
public class Lease<T> {

    enum Action {
        Register, Cancel, Renew
    };

    /**
     * 租约过期的时间常量
     */
    public static final int DEFAULT_DURATION_IN_SECS = 90;

    private T holder;

    /**
     * 服务下线时间
     */
    private long evictionTimestamp;

    /**
     * 服务注册时间
     */
    private long registrationTimestamp;

    /**
     * 服务UP时间
     */
    private long serviceUpTimestamp;

    /**
     * 最后一次续约时间
     */
    private volatile long lastUpdateTimestamp;

    /**
     * 心跳过期时间，默认 90s
     */
    private long duration;

    public Lease(T r, int durationInSecs) {
        holder = r;
        registrationTimestamp = System.currentTimeMillis();
        lastUpdateTimestamp = registrationTimestamp;
        duration = (durationInSecs * 1000);

    }

    public void setHolder(T holder) {
        this.holder = holder;
    }

    /**
     *  客户端续约时，更新最后的更新时间 ， 用当前系统加上过期的时间
     */
    public void renew() {
        lastUpdateTimestamp = System.currentTimeMillis() + duration;

    }

    /**
     * 服务下线时，更新服务下线时间.
     */
    public void cancel() {
        if (evictionTimestamp <= 0) {
            evictionTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Mark the service as up. This will only take affect the first time called,
     * subsequent calls will be ignored.
     */
    public void serviceUp() {
        if (serviceUpTimestamp == 0) {
            serviceUpTimestamp = System.currentTimeMillis();
        }
    }

    /**
     * Set the leases service UP timestamp.
     */
    public void setServiceUpTimestamp(long serviceUpTimestamp) {
        this.serviceUpTimestamp = serviceUpTimestamp;
    }

    public boolean isExpired() {
        return isExpired(0L);
    }

    /**
     * Lease 每次心跳续约时都会更新最后一次续约时间 lastUpdateTimestamp。
     * 如果服务下线则会更新下线时间 evictionTimestamp，这样 evictionTimestamp > 0 就表示服务已经下线了。
     * 默认心跳续约时间超过 90s 服务就自动过期。
     * @param additionalLeaseMs additionalLeaseMs 是一种补偿机制，可以当成默认值 0ms。
     * @return
     */
    public boolean isExpired(long additionalLeaseMs) {
        return (evictionTimestamp > 0 || System.currentTimeMillis() > (lastUpdateTimestamp + duration + additionalLeaseMs));
    }

    /**
     * Gets the milliseconds since epoch when the lease was registered.
     *
     * @return the milliseconds since epoch when the lease was registered.
     */
    public long getRegistrationTimestamp() {
        return registrationTimestamp;
    }

    /**
     * Gets the milliseconds since epoch when the lease was last renewed.
     * Note that the value returned here is actually not the last lease renewal time but the renewal + duration.
     *
     * @return the milliseconds since epoch when the lease was last renewed.
     */
    public long getLastRenewalTimestamp() {
        return lastUpdateTimestamp;
    }

    /**
     * Gets the milliseconds since epoch when the lease was evicted.
     *
     * @return the milliseconds since epoch when the lease was evicted.
     */
    public long getEvictionTimestamp() {
        return evictionTimestamp;
    }

    /**
     * Gets the milliseconds since epoch when the service for the lease was marked as up.
     *
     * @return the milliseconds since epoch when the service for the lease was marked as up.
     */
    public long getServiceUpTimestamp() {
        return serviceUpTimestamp;
    }

    /**
     * Returns the holder of the lease.
     */
    public T getHolder() {
        return holder;
    }
}
