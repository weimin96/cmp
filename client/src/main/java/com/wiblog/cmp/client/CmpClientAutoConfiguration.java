package com.wiblog.cmp.client;

import com.wiblog.cmp.client.bean.InstanceInfo;
import com.wiblog.cmp.client.config.HttpClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.web.client.RestTemplate;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;


/**
 * @author pwm
 * @date 2020/1/31
 */
@Configuration
@EnableConfigurationProperties
// 加载默认client配置项
//@ConditionalOnClass(DefaultClientConfig.class)
@AutoConfigureAfter(HttpClientConfig.class)
@Import(HttpClientConfig.class)
public class CmpClientAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(CmpClientAutoConfiguration.class);

    private ConfigurableEnvironment env;

    /**
     * 构造方法注入ConfigurableEnvironment
     */
    public CmpClientAutoConfiguration(ConfigurableEnvironment env) {
        System.out.println("初始化客户端");
        this.env = env;
    }

    private String getProperty(String property) {
        return this.env.containsProperty(property) ? this.env.getProperty(property) : "";
    }

    /**
     * 获取客户端配置信息
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = InstanceInfo.class, search = SearchStrategy.CURRENT)
    public InstanceInfo eurekaApplicationInfoManager() {
        logger.info("初始化客户端连接信息");
        // 客户端host
        String ipAddress;
        try {
            ipAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            ipAddress = "unknown";
        }
        // 服务端地址
        String serviceUrl = getProperty("cmp.instance.service-url");
        // 客户端名称
        String applicationName = getProperty("spring.application.name");
        // 客户端端口号
        int port = Integer.valueOf(env.getProperty("server.port",env.getProperty("port","8080")));
        InstanceInfo instanceInfo = new InstanceInfo();
        instanceInfo.setInstanceId(UUID.randomUUID().toString());
        instanceInfo.setIpAddr(ipAddress);
        instanceInfo.setServiceUrl(serviceUrl);
        instanceInfo.setPort(port);
        instanceInfo.setAppName(applicationName);
        logger.info(instanceInfo.toString());
        // 构造客户端信息
        return instanceInfo;
    }



    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(value = CmpClient.class, search = SearchStrategy.CURRENT)
    public CmpClient cmpClient(InstanceInfo instanceInfo,RestTemplate restTemplate){
        // 初始化客户端
        return new CmpClient(instanceInfo,restTemplate);
    }



}
