package com.wiblog.cmp.server.controller;

import com.wiblog.cmp.server.ResponseCache;
import com.wiblog.cmp.server.util.StatusInfo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @author pwm
 * @date 2020/2/29
 */
@Controller
public class CmpWebController {
    /**
     * 跳转首页
     */
    @GetMapping("/")
    public String index(){
        return "index";
    }

    @GetMapping("/endpoint")
    @ResponseBody
    public Object endpoint(){
        return ResponseCache.single().getRegistry();
    }

    @GetMapping("/status")
    @ResponseBody
    public Object status(){

        StatusInfo statusInfo = StatusInfo.Builder.newBuilder().build();
        return statusInfo;
    }
}
