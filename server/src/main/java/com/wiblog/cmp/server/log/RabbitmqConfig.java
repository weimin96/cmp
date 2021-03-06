package com.wiblog.cmp.server.log;

import com.wiblog.cmp.common.constant.CmpConstant;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
public class RabbitmqConfig {

    /**
     * 一次批量的数量
     */
    private static final int BATCH_SIZE = 100;

    @Autowired
    RabbitAdmin rabbitAdmin;

    /*@Bean
    public RabbitTemplate cmpRabbitTemplate(CachingConnectionFactory factory){
        return new RabbitTemplate(factory);
    }*/

    /**
     * 创建日志消息队列
     */
    public Queue cmpLogQueue() {
        return new Queue(CmpConstant.Logger.QUEUE_KEY, true);
    }

    /**
     * 创建日志消息交换机
     */
    public DirectExchange cmpLogExchange() {
        return new DirectExchange(CmpConstant.Logger.EXCHANGE_KEY, true, false);
    }

    /**
     * 绑定消息队列和交换机
     */
    @Bean
    public Binding cmpBinding() {
        return BindingBuilder.bind(cmpLogQueue()).to(cmpLogExchange()).with(CmpConstant.Logger.ROUTING_KEY);
    }

    /**
     * 创建初始化RabbitAdmin对象
     */
    @Bean
    public RabbitAdmin cmpRabbitAdmin(ConnectionFactory connectionFactory) {
        RabbitAdmin rabbitAdmin = new RabbitAdmin(connectionFactory);
        rabbitAdmin.setAutoStartup(true);
        return rabbitAdmin;
    }

    /**
     * 主动创建交换机和对列
     */
    @Bean
    public void createCmpExchangeQueue() {
        rabbitAdmin.declareExchange(cmpLogExchange());
        rabbitAdmin.declareQueue(cmpLogQueue());
    }

    /*@Bean("batchQueueRabbitListenerContainerFactory")
    public SimpleRabbitListenerContainerFactory batchQueueRabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        //设置批量
        factory.setBatchListener(true);
        //设置BatchMessageListener生效
        factory.setConsumerBatchEnabled(true);
        //设置监听器一次批量处理的消息数量
        factory.setBatchSize(BATCH_SIZE);
        return factory;
    }*/
}
