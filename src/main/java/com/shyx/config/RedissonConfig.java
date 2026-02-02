package com.shyx.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {
    // Redisson配置类
    @Bean
    public RedissonClient redissonClient() {
        // 创建配置对象
        Config config = new Config();
        // 添加redis地址
        config.useSingleServer().setAddress("redis://192.168.44.99:6379").setPassword("123456");
        // 创建客户端
        return Redisson.create(config);
    }
}
