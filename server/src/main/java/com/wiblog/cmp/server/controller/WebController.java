package com.wiblog.cmp.server.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author pwm
 * @date 2020/2/29
 */
@Controller
public class WebController {
    /**
     * 跳转首页
     */
    @GetMapping("/")
    public String index(){
        return "index";
    }
}
