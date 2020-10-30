package com.wiblog.cmp.server.controller;

import com.wiblog.cmp.common.CmpResponse;
import com.wiblog.cmp.common.bean.InstanceInfo;
import com.wiblog.cmp.server.ResponseCache;
import com.wiblog.cmp.server.bean.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pwm
 */
@RestController
@RequestMapping("/cmp")
public class CmpController {

    private static final Logger logger = LoggerFactory.getLogger(CmpController.class);

    /**
     * 存放注册实例
     */
    private final ConcurrentHashMap<String, Lease<InstanceInfo>> registry = ResponseCache.single().getRegistry();

    public CmpController() {
        logger.info("初始化构造器");
    }

    /**
     * 客户端注册
     *
     * @param info 实例信息
     * @return
     */
    @PostMapping(value = "/register")
    public CmpResponse register(@RequestBody InstanceInfo info) {
        logger.info("接收注册信息-{}",info);
        // 校验
        if (info == null){
            return CmpResponse.status(400).entity("Missing info").build();
        }else if(isBlank(info.getInstanceId())) {
            return CmpResponse.status(400).entity("Missing instanceId").build();
        } else if (isBlank(info.getIpAddr())) {
            return CmpResponse.status(400).entity("Missing ip address").build();
        } else if (isBlank(info.getAppName())) {
            return CmpResponse.status(400).entity("Missing appName").build();
        }
        // 保存服务注册信息
        // 获取appName 下的注册信息
        Lease<InstanceInfo> appMap = registry.get(info.getAppName());
        // 没有对应appName的客户端
        if (appMap == null) {
            // 如果之前不存在实例的租约，说明是新实例注册
            appMap = new Lease<>(info, info.getExpiredTime());
        } else {
            // 更新注册信息
            appMap.setHolder(info);
        }
        registry.put(info.getAppName(), appMap);
        // 放入缓存
        ResponseCache.single().setRegistry(registry);
        // 重新计算阈值
        // 保存注册队列
        // 更新缓存
        // 同步服务注册信息
        return CmpResponse.status(204).build();
    }

    /**
     * 客户端注册
     *
     * @return
     */
    @PostMapping(value = "/renew")
    public CmpResponse renewLease(@RequestBody String name) {
        logger.info("接收心跳信息-{}",name);
        if (isBlank(name)){
            return CmpResponse.status(400).entity("Missing App Name").build();
        }
        Lease<?> lease = registry.get(name);
        // 之前没有
        if (lease == null){
            // 客户端重新请求注册
            return CmpResponse.status(404).entity("Not Found App Instance").build();
        }
        // 更新时间
        // TODO 多实例同步数据
        lease.renew();
        return CmpResponse.status(200).build();
    }


    /**
     * 客户端下线
     *
     * @return
     */
    @PostMapping(value = "/evict/{name}",consumes = "application/json")
    public CmpResponse evictLease(@PathVariable("name")String name) {
        if (isBlank(name)) {
            return CmpResponse.status(400).entity("Missing App Name").build();
        }
        // TODO 多实例同步数据
        registry.remove(name);
        return CmpResponse.status(200).build();
    }

    private boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }
}
