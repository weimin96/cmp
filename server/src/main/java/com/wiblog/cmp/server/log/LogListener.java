package com.wiblog.cmp.server.log;


import com.alibaba.fastjson.JSONObject;
import com.wiblog.cmp.common.constant.CmpConstant;
import com.wiblog.cmp.common.logger.LogMessage;
import com.wiblog.cmp.server.util.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RabbitListener(queues = CmpConstant.Logger.QUEUE_KEY)
public class LogListener {

    private static final Logger logger = LoggerFactory.getLogger(LogListener.class);

    @Autowired
    private EsLogRepository esLogRepository;

    @RabbitHandler
    public void process(String jsonContent) {
        LogMessage logMessage = JSONObject.parseObject(jsonContent, LogMessage.class);
        Map<String, Object> resultMap = GrokUtil.toLogMap(logMessage.getStr());
        // TODO 入库

        EsLog esLog = (EsLog) MapUtils.mapToObject(resultMap, EsLog.class);
        esLog.timestamp(DateUtils.parse((String) resultMap.get("timestamp")));
        System.out.println("日志-》" + logMessage);

        esLogRepository.save(esLog);
    }
}
