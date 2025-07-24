package io.f1.backend.global.util;

import static io.f1.backend.global.util.RedisPublisher.USER_DELETE;
import static io.f1.backend.global.util.RedisPublisher.USER_NEW;
import static io.f1.backend.global.util.RedisPublisher.USER_UPDATE;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.f1.backend.domain.stat.app.StatService;
import io.f1.backend.domain.user.dto.UserSummary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUserSubscriber implements MessageListener {

    private final StatService statService;
    private final ObjectMapper om;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String channel = new String(message.getChannel());
        String json = new String(message.getBody());
        try {
            switch (channel) {
                case USER_NEW -> {
                    UserSummary userSummary = om.readValue(json, UserSummary.class);
                    statService.addUser(userSummary.userId(), userSummary.nickname());
                }
                case USER_UPDATE -> {
                    UserSummary userSummary = om.readValue(json, UserSummary.class);
                    statService.updateNickname(userSummary.userId(), userSummary.nickname());
                }
                case USER_DELETE -> {
                    long userId = Long.parseLong(om.readValue(json, String.class));
                    statService.removeUser(userId);
                }
            }
        } catch (JsonProcessingException e) {
            log.error("RedisUserSubscriber Json Processing error, channel={}", channel, e);
        }
    }
}
