package io.f1.backend.global.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisPublisher {

	private final RedisTemplate<String, Object> redisTemplate;
	public static final String USER_NEW = "user-new";
	public static final String USER_UPDATE = "user-update";
	public static final String USER_DELETE = "user-delete";

	public void publish(String channel, Object message) {
		redisTemplate.convertAndSend(channel, message);
	}
}
