package com.wiblog.cmp.server.log;


import com.alibaba.fastjson.JSONObject;
import com.wiblog.cmp.server.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;

@Component
@RabbitListener(queues = RabbitmqConfig.QUEUE_KEY)
public class LogListener {

    private static final Logger logger = LoggerFactory.getLogger(LogListener.class);

    @Autowired
    private EsLogRepository esLogRepository;

    @RabbitHandler
    public void process(String jsonContent){
        LogMessage logMessage = JSONObject.parseObject(jsonContent,LogMessage.class);
        Map<String, Object> resultMap = GrokUtil.toLogMap(logMessage.getStr());
        System.out.println("日志-》"+resultMap);
        // TODO 入库
        EsLog esLog = new EsLog();
        esLog.setLevel((String) resultMap.get("level"));
        esLog.setMsg((String) resultMap.get("msg"));
        esLog.setCreateTime(DateUtils.parse((String) resultMap.get("timestamp")));
        esLogRepository.save(esLog);
    }

    public static void main(String[] args) {
        Date date = DateUtils.parse("2020-10-29 19:48:25.096");
        System.out.println(date);
    }
}
