package com.wiblog.cmp.server;

import com.wiblog.cmp.server.controller.CmpController;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * @author pwm
 * @date 2020/1/31
 */
@Configuration
@AutoConfigureAfter(CmpController.class)
@Import(CmpController.class)
public class CmpServerAutoConfiguration {

    public CmpServerAutoConfiguration(){
        System.out.println("初始化");
    }


}
