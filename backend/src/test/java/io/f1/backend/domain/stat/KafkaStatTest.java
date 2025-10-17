package io.f1.backend.domain.stat;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.database.rider.core.api.dataset.DataSet;
import com.github.database.rider.spring.api.DBRider;

import io.f1.backend.domain.stat.dao.StatJpaRepository;
import io.f1.backend.domain.stat.dto.StatChangeEvent;
import io.f1.backend.domain.stat.dto.StatWithUserSummary;
import io.f1.backend.domain.user.dao.UserRepository;
import io.f1.backend.global.config.KafkaTestContainerConfig;
import io.f1.backend.global.util.kafka.KafkaProducer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import java.time.Duration;

@DBRider
@SpringBootTest
@Import({KafkaTestContainerConfig.class})
class KafkaStatTest {

    @Autowired UserRepository userRepository;
    @Autowired KafkaProducer kafkaProducer;
    @Autowired StatJpaRepository statJpaRepository;

    @Test
    @DataSet("datasets/stat/one-user-stat.yml")
    @DisplayName("Kafka를 통해 게임 결과가 전송되면 Consumer가 비동기로 처리하여 MySQL에 반영된다")
    void kafkaConsumerProcessesGameResultAsynchronously() throws Exception {
        // given
        long userId = 1L;
        StatWithUserSummary originalStat = statJpaRepository.findStatWithUserSummary(userId).orElseThrow(AssertionError::new);

        int deltaScore = 100;
        StatChangeEvent event = StatChangeEvent.of(userId, true, deltaScore);
        kafkaProducer.sendWithKey("stat-changes", String.valueOf(userId), event);

        // when
        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .pollInterval(Duration.ofMillis(200))
                .until(() -> isStatUpdated(userId, originalStat.score() + deltaScore));

        // then
        StatWithUserSummary updatedStat = statJpaRepository.findStatWithUserSummary(userId).orElseThrow(AssertionError::new);
        assertThat(updatedStat.score()).isEqualTo(originalStat.score() + deltaScore);
        assertThat(updatedStat.totalGames()).isEqualTo(originalStat.totalGames() + 1);
        assertThat(updatedStat.winningGames()).isEqualTo(originalStat.winningGames() + 1);
    }

    private boolean isStatUpdated(long userId, long expectedScore) {
        try {
            return statJpaRepository.findStatWithUserSummary(userId).orElseThrow(AssertionError::new).score() == expectedScore;
        } catch (Exception e) {
            return false;
        }
    }
}
