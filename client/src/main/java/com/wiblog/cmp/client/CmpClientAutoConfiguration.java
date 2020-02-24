package com.wiblog.cmp.client;

import com.wiblog.cmp.client.bean.CmpClientConfig;
import com.wiblog.cmp.client.bean.CmpInstanceConfig;
import com.wiblog.cmp.client.bean.InstanceInfo;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.SearchStrategy;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.ConfigurableEnvironment;

import java.util.*;

/**
 * @author pwm
 * @date 2020/1/31
 */
@Configuration
@EnableConfigurationProperties
// 加载默认client配置项
@ConditionalOnClass(DefaultClientConfig.class)
public class CmpClientAutoConfiguration {

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
        String ipAddress = getProperty("cmp.instance.ip-address");
        System.out.println(ipAddress);
        CmpInstanceConfig instance = new CmpInstanceConfig();
        instance.setIpAddress(ipAddress);
        return instance;
    }

    @Bean
    @ConditionalOnMissingBean(value = CmpClientConfig.class, search = SearchStrategy.CURRENT)
    public CmpClientConfig eurekaClientConfigBean(ConfigurableEnvironment env) {
        CmpClientConfig client = new CmpClientConfig();
        return client;
    }

    @Bean
    @ConditionalOnMissingBean(value = ApplicationInfoManager.class, search = SearchStrategy.CURRENT)
    public ApplicationInfoManager eurekaApplicationInfoManager(
            CmpInstanceConfig config) {
        // 构造客户端信息
        InstanceInfo instanceInfo = new InstanceInfoFactory().create(config);
        // 交给manager管理 用于注册
        return new ApplicationInfoManager(config, instanceInfo);
    }



    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean(value = CmpClient.class, search = SearchStrategy.CURRENT)
    public CmpClient cmpClient(CmpClientConfig client,CmpInstanceConfig cmpInstanceConfig){
        // 初始化客户端
        CmpClient cmpClient = new CmpClient(client);
        return cmpClient;
    }


}
