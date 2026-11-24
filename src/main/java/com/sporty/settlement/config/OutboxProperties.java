package com.sporty.settlement.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * Configuration of the settlement outbox relay.
 *
 * @param pollInterval delay between the end of one relay run and the start of the next
 * @param batchSize    maximum number of requests that one relay run publishes
 */
@ConfigurationProperties("app.outbox")
public record OutboxProperties(Duration pollInterval, int batchSize) {
}
