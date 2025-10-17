package io.f1.backend.global.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.ConfluentKafkaContainer;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@TestConfiguration
public class KafkaTestContainerConfig {

    @Container
    public static ConfluentKafkaContainer kafkaContainer =
            new ConfluentKafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"))
                    .withExposedPorts(9092);

    static {
        kafkaContainer.start();
        System.setProperty(
                "spring.kafka.bootstrap-servers",
                kafkaContainer.getHost() + ":" + kafkaContainer.getMappedPort(9092));
    }
}
