package io.f1.backend.global.config;

import com.redis.testcontainers.RedisContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Configuration
@Testcontainers
public class RedisTestContainerConfig {
	@Container
	public static RedisContainer redisContainer = new RedisContainer(
		RedisContainer.DEFAULT_IMAGE_NAME.withTag(RedisContainer.DEFAULT_TAG));

	static {
		redisContainer.start();
	}

	@Bean
	RedisConnectionFactory redisConnectionFactory() {
		return new LettuceConnectionFactory(
			redisContainer.getHost(),
			redisContainer.getFirstMappedPort()
		);
	}
}