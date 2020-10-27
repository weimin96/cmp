package com.wiblog.cmp.server.log;


import io.krakens.grok.api.Grok;
import io.krakens.grok.api.GrokCompiler;
import io.krakens.grok.api.Match;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;

@Component
@RabbitListener(queues = RabbitmqConfig.QUEUE_KEY)
public class LogListener {

    private static final Logger logger = LoggerFactory.getLogger(LogListener.class);

    @RabbitHandler
    public void process(String content){
        Map<String, Object> resultMap = GrokUtil.toLogMap(content);
        System.out.println("日志-》"+content);
        // TODO 入库
    }

    public static void main(String[] args) {
        GrokCompiler grokCompiler = GrokCompiler.newInstance();
        // 进行注册, registerDefaultPatterns()方法注册的是Grok内置的patterns
        grokCompiler.registerDefaultPatterns();
        final Grok grok = grokCompiler.compile("^%{TIMESTAMP_ISO8601:timestamp} \\s*%{WORD:method} %{NUMBER:pid} \\[\\s*%{GREEDYDATA:thread}] %{JAVACLASS:class}\\s*\\[\\s*%{NUMBER:line}\\]\\s*:%{GREEDYDATA:msg}");
        String content = "2020-10-11 22:43:11.121  INFO 5892 [p.logCollection] c.w.cmp.client.log.LogCollectionTask     [ 108]   :开始日志收集";
        Match grokMatch  = grok.match(content);
        Map<String, Object> resultMap = grokMatch.captureFlattened();
        System.out.println(resultMap);
    }
}
