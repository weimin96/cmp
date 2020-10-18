package com.wiblog.cmp.client.log;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

//@Component
public class LogClient {

    private static final Logger logger = LoggerFactory.getLogger(LogClient.class);

    private RabbitTemplate rabbitTemplate;

    private ScheduledExecutorService scheduler;

    public LogClient(RabbitTemplate rabbitTemplate,String logDir) {
        logger.info("初始化日志收集客户端");
        if (StringUtils.isEmpty(logDir)){
            logger.error("日志路径不能为空");
            return;
        }
        this.rabbitTemplate = rabbitTemplate;

        this.scheduler = Executors.newScheduledThreadPool(1, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("cmp.logCollection");
            return t;
        });
        LogCollectionTask logCollectionTask = new LogCollectionTask(logDir,rabbitTemplate);
        this.scheduler.scheduleWithFixedDelay(logCollectionTask, 0L, 10, TimeUnit.SECONDS);

    }
}
