package com.wiblog.cmp.client;

import com.wiblog.cmp.client.bean.CmpClientConfig;
import com.wiblog.cmp.client.bean.CmpInstanceConfig;
import com.wiblog.cmp.client.bean.InstanceInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;


/**
 * @author pwm
 * @date 2020/1/31
 */
@Configuration
@EnableConfigurationProperties
// 加载默认client配置项
@ConditionalOnClass(DefaultClientConfig.class)
public class CmpClientAutoConfiguration {

    public static final Logger logger = LoggerFactory.getLogger(CmpClientAutoConfiguration.class);

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
     * 获取配置信息
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = CmpInstanceConfig.class, search = SearchStrategy.CURRENT)
    public CmpInstanceConfig eurekaInstanceConfigBean(){
        logger.info("获取客户端连接配置");
        String ipAddress = getProperty("cmp.instance.ip-address");
        System.out.println(ipAddress);
        CmpInstanceConfig instance = new CmpInstanceConfig();
        instance.setIpAddress(ipAddress);
        return instance;
    }

    /*@Bean
    @ConditionalOnMissingBean(value = CmpClientConfig.class, search = SearchStrategy.CURRENT)
    public CmpClientConfig eurekaClientConfigBean(ConfigurableEnvironment env) {
        CmpClientConfig client = new CmpClientConfig();
        return client;
    }*/

    /**
     * 获取客户端配置信息
     * @param config
     * @return
     */
    @Bean
    @ConditionalOnMissingBean(value = InstanceInfo.class, search = SearchStrategy.CURRENT)
    public InstanceInfo eurekaApplicationInfoManager(
            CmpInstanceConfig config) {
        logger.info("初始化客户端连接信息");
        // 构造客户端信息
        InstanceInfo instanceInfo = new InstanceInfo().create(config);
        return instanceInfo;
    }



    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(value = CmpClient.class, search = SearchStrategy.CURRENT)
    public CmpClient cmpClient(InstanceInfo instanceInfo){
        // 初始化客户端
        CmpClient cmpClient = new CmpClient(instanceInfo);
        return cmpClient;
    }


}
