package com.wiblog.cmp.server.controller;

import com.alibaba.fastjson.JSONObject;
import com.wiblog.cmp.server.bean.InstanceInfo;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * @author pwm
 * @date 2020/2/26
 */
@RestController("/app")
public class CmpController {

    @PostMapping("/register")
    public String register(InstanceInfo instanceInfo) {
        System.out.println(instanceInfo);
        Map<String, Object> result = new HashMap<>();
        result.put("code", "204");
        return JSONObject.toJSONString(result);
    }
}
