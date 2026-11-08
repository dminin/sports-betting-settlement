package com.sporty.settlement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration for the Kafka event-outcomes topic.
 *
 * @param eventOutcomesTopic topic that carries event outcomes
 * @param eventOutcomesPartitions number of partitions to create
 */
@ConfigurationProperties("app.kafka")
public record EventOutcomesTopicProperties(
    String eventOutcomesTopic, int eventOutcomesPartitions) {}
