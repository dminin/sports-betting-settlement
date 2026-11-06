package com.sporty.settlement.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration(proxyBeanMethods = false)
class KafkaConfig {

    private static final long RETRY_INTERVAL_MS = 1_000L;
    private static final long MAX_RETRIES = 3L;

    @Bean
    NewTopic eventOutcomesTopic(EventOutcomesTopicProperties properties) {
        return TopicBuilder.name(properties.eventOutcomesTopic())
                .partitions(properties.eventOutcomesPartitions())
                .replicas(1)
                .build();
    }

    /**
     * Retries transient failures, then logs and skips the record. Deserialization failures are never retried
     * (the default), and neither are invalid payloads.
     */
    @Bean
    DefaultErrorHandler kafkaErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, ex) -> log.error("Giving up on record topic={} partition={} offset={} key={}",
                        record.topic(), record.partition(), record.offset(), record.key(), ex),
                new FixedBackOff(RETRY_INTERVAL_MS, MAX_RETRIES));
        errorHandler.addNotRetryableExceptions(IllegalArgumentException.class);
        return errorHandler;
    }
}
