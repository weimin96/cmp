package com.wiblog.cmp.server.controller;

import com.wiblog.cmp.server.bean.InstanceInfo;
import com.wiblog.cmp.server.bean.Lease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pwm
 * @date 2020/2/26
 */
@RestController
@RequestMapping("/cmp")
public class CmpController {

    private static final Logger logger = LoggerFactory.getLogger(CmpController.class);

    /**
     * 存放注册实例
     */
    private final ConcurrentHashMap<String, Map<String, Lease<InstanceInfo>>> registry = new ConcurrentHashMap<>();

    public CmpController(){
        logger.info("初始化构造器");
    }

    /**
     * 客户端注册
     * @param info 实例信息
     * @return
     */
    @PostMapping("/register")
    public Response register(InstanceInfo info) {
        // 校验
        if (isBlank(info.getInstanceId())) {
            return Response.status(400).entity("Missing instanceId").build();
        } else if (isBlank(info.getIpAddr())) {
            return Response.status(400).entity("Missing ip address").build();
        } else if (isBlank(info.getAppName())) {
            return Response.status(400).entity("Missing appName").build();
        }
        // 保存服务注册信息
        // 获取appName 下的map
        Map<String, Lease<InstanceInfo>> gMap = registry.get(info.getAppName());
        // 没有对应appName的客户端
        if(gMap == null){
            gMap = new HashMap<>();
            gMap.put(info.getInstanceId(), new Lease<>(info, 20000));
        }else{
            // 获取续租实例
            Lease<InstanceInfo> existingLease = gMap.get(info.getInstanceId());
            if (existingLease == null){
                existingLease = new Lease<>(info, 20000);
            }else {
                // 更新实例信息
                existingLease.setHolder(info);
            }
            gMap.put(info.getInstanceId(),existingLease);
        }
        registry.put(info.getAppName(),gMap);
        // 重新计算阈值
        // 保存注册队列
        // 更新缓存
        // 同步服务注册信息
        return Response.status(204).build();
    }

    private boolean isBlank(String str) {
        return str == null || str.isEmpty();
    }
}
