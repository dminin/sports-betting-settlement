package com.sporty.settlement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("app.kafka")
public record EventOutcomesTopicProperties(String eventOutcomesTopic, int eventOutcomesPartitions) {
}
