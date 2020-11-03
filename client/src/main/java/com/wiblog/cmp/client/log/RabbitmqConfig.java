package com.wiblog.cmp.client.log;

import com.wiblog.cmp.common.constant.CmpConstant;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.batch.BatchingStrategy;
import org.springframework.amqp.rabbit.batch.SimpleBatchingStrategy;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.BatchingRabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.amqp.SimpleRabbitListenerContainerFactoryConfigurer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * <p>rabbitmq配置</p>
 *
 * <p>批量数据首先存在内存中，系统停止的时候将会丢失</p>
 */
@Configuration
public class RabbitmqConfig {

    /**
     * 一次批量的最大数量
     */
    private static final int BATCH_SIZE = 100;

    /**
     * 缓存限制 一条批量消息的最大大小 1k,超出后只会发送一部分数据过去
     */
    private static final int BUFFER_LIMIT = 1024;

    /**
     * 超时时间 超时后将会发送一部分数据过去
     */
    private static final long TIMEOUT = 1000;

    @Bean
    public RabbitTemplate cmpRabbitTemplate(CachingConnectionFactory factory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(factory);
        return rabbitTemplate;
    }




    /**
     * 创建日志消息队列
     */
    public Queue cmpBatchLogQueue() {
        return new Queue(CmpConstant.Logger.QUEUE_KEY);
    }

    @Bean
    public BatchingRabbitTemplate cmpBatchingRabbitTemplate(ConnectionFactory factory) {
        BatchingStrategy batchingStrategy = new SimpleBatchingStrategy(BATCH_SIZE, BUFFER_LIMIT, TIMEOUT);
        TaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        return new BatchingRabbitTemplate(factory,batchingStrategy,taskScheduler);
    }


}
