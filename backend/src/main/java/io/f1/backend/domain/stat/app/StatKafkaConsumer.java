package io.f1.backend.domain.stat.app;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.f1.backend.domain.stat.dao.StatBatchRepository;
import io.f1.backend.domain.stat.dto.StatChangeEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatKafkaConsumer {

    private final StatBatchRepository statBatchRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "stat-changes")
    public void handleStatChanges(List<String> messages) {

        try {
            List<StatChangeEvent> events = new ArrayList<>();
            for (String message : messages) {
                StatChangeEvent event = objectMapper.readValue(message, StatChangeEvent.class);
                events.add(event);
            }

            statBatchRepository.batchUpdateStats(events);

        } catch (Exception e) {
            log.error("Failed to process stat change events", e);
            throw new RuntimeException("Failed to process stat change events", e);
        }
    }
}
