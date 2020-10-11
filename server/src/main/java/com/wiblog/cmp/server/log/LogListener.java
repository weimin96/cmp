package com.wiblog.cmp.server.log;


import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = RabbitmqConfig.QUEUE_KEY)
public class LogListener {

    @RabbitHandler
    public void process(String content){
        System.out.println(content);
    }
}
