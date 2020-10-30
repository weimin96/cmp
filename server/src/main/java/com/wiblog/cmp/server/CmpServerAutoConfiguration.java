package com.wiblog.cmp.server;

import com.wiblog.cmp.server.bean.CmpServerConfig;
import com.wiblog.cmp.server.controller.CmpController;
import com.wiblog.cmp.server.controller.CmpWebController;
import com.wiblog.cmp.server.log.LogListener;
import com.wiblog.cmp.server.log.RabbitmqConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

/**
 * @author pwm
 */
@Configuration
@EnableConfigurationProperties
@AutoConfigureAfter({CmpController.class, RabbitmqConfig.class})
// TODO 换个地方注入组件
@EnableElasticsearchRepositories(basePackages = "com.wiblog.cmp.server.log")
@Import({CmpController.class, RabbitmqConfig.class, LogListener.class})
public class CmpServerAutoConfiguration {
    private static final Logger log = LoggerFactory.getLogger(CmpServerAutoConfiguration.class);

    public CmpServerAutoConfiguration() {
        log.info("初始化cmp服务端");
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(value = {CmpServer.class, CmpServerConfig.class}, search = SearchStrategy.CURRENT)
    public CmpServer cmpServer(CmpServerConfig serverConfig) {
        // 初始化客户端
        return new CmpServer(serverConfig);
    }

    @Bean
    public CmpServerConfig cmpServerConfig(){
        return new CmpServerConfig();
    }

    @Bean
    public CmpWebController eurekaController() {
        return new CmpWebController();
    }
}
