package io.f1.backend.global.config;

import com.redis.testcontainers.RedisContainer;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Configuration
@Testcontainers
public class RedissonTestContainerConfig {

    @Container
    public static RedisContainer redisContainer =
        new RedisContainer(
            RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

    static {
        redisContainer.start();
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        String address = String.format("redis://%s:%d",
            RedissonTestContainerConfig.redisContainer.getHost(),
            RedissonTestContainerConfig.redisContainer.getFirstMappedPort());

        config.useSingleServer()
            .setAddress(address);

        return Redisson.create(config);
    }
}
