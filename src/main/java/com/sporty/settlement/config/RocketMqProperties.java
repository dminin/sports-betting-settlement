package com.sporty.settlement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param endpoint      gRPC endpoint of the RocketMQ proxy, e.g. {@code localhost:8081}
 * @param topic         topic carrying bet settlement requests
 * @param consumerGroup consumer group of the settlement consumer
 */
@ConfigurationProperties("app.rocketmq")
public record RocketMqProperties(String endpoint, String topic, String consumerGroup) {
}
