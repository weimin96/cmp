package com.wiblog.cmp.server.log;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RabbitmqConfig {

    public static final String EXCHANGE_KEY = "cmp_log_exchange";

    public static final String QUEUE_KEY = "cmp_log_queue";

    public static final String ROUTING_KEY = "cmp_log_routing_key";

    @Bean
    public RabbitTemplate rabbitTemplate(CachingConnectionFactory factory){
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        return rabbitTemplate;
    }

    // 创建一个立即消费队列
    public Queue immediateQueue() {
        // 第一个参数是创建的queue的名字，第二个参数是是否支持持久化
        return new Queue(QUEUE_KEY, true);
    }

    public DirectExchange immediateExchange() {
        // 一共有三种构造方法，可以只传exchange的名字， 第二种，可以传exchange名字，是否支持持久化，是否可以自动删除，
        //第三种在第二种参数上可以增加Map，Map中可以存放自定义exchange中的参数
        return new DirectExchange(EXCHANGE_KEY, true, false);
    }

    @Bean
    public Binding immediateBinding() {
        //把立即消费的队列和立即消费的exchange绑定在一起
        return BindingBuilder.bind(immediateQueue()).to(immediateExchange()).with(ROUTING_KEY);
    }
}
