package com.wiblog.cmp.client.log;

import com.wiblog.cmp.client.bean.CmpResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cmp/log")
public class LogController {

    /**
     * 历史日志列表
     * @return
     */
    @GetMapping("/list")
    public CmpResponse getLogList(){
        return null;
    }
}
