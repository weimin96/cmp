package com.wiblog.cmp.server;

import com.wiblog.cmp.server.bean.InstanceInfo;
import com.wiblog.cmp.server.bean.Lease;

import java.util.concurrent.ConcurrentHashMap;

/**
 * 缓存
 *
 * @author pwm
 * @date 2020/4/25
 */
public class ResponseCache {

    private static final ResponseCache cache = new ResponseCache();

    public static ResponseCache single(){
        return cache;
    }

    private ConcurrentHashMap<String, Lease<InstanceInfo>> registry;

    private ResponseCache() {
    }

    public ConcurrentHashMap<String, Lease<InstanceInfo>> getRegistry() {
        if (registry == null){
            registry = new ConcurrentHashMap<>();
        }
        return registry;
    }

    public void setRegistry(ConcurrentHashMap<String, Lease<InstanceInfo>> registry) {
        this.registry = registry;
    }
}
