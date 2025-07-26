package io.f1.backend.global.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${spring.datasource.data.redis.host}")
    private String redisHost;

    @Value("${spring.datasource.data.redis.port}")
    private int redisPort;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        final String address = "redis://%s:%d".formatted(redisHost, redisPort);

        config.useSingleServer().setAddress(address);
        return Redisson.create(config);
    }
}
