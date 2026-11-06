package com.sporty.settlement.config;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Real RocketMQ wiring through the gRPC proxy. Replaced by an in-process publisher in the {@code mock} profile.
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
@Profile("!mock")
class RocketMqConfig {

    private static final Duration CONSUMER_CLOSE_TIMEOUT = Duration.ofSeconds(5);

    @Bean
    ClientServiceProvider rocketMqClientServiceProvider() {
        return ClientServiceProvider.loadService();
    }

    @Bean
    ClientConfiguration rocketMqClientConfiguration(RocketMqProperties properties) {
        return ClientConfiguration.newBuilder()
                .setEndpoints(properties.endpoint())
                .enableSsl(false)
                .build();
    }

    @Bean
    Producer rocketMqProducer(ClientServiceProvider provider, ClientConfiguration clientConfiguration,
                              RocketMqProperties properties) throws ClientException {
        return provider.newProducerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setTopics(properties.topic())
                .build();
    }

    @Bean(destroyMethod = "")
    PushConsumer betSettlementPushConsumer(ClientServiceProvider provider, ClientConfiguration clientConfiguration,
                                           RocketMqProperties properties,
                                           MessageListener betSettlementListener) throws ClientException {
        return provider.newPushConsumerBuilder()
                .setClientConfiguration(clientConfiguration)
                .setConsumerGroup(properties.consumerGroup())
                .setSubscriptionExpressions(Map.of(properties.topic(), FilterExpression.SUB_ALL))
                .setMessageListener(betSettlementListener)
                .build();
    }

    /**
     * {@link PushConsumer#close()} blocks until the in-flight long-polling receive (~30s) returns. Bound it so
     * shutdown stays fast; messages received but not acknowledged are redelivered by the broker.
     */
    @Bean
    DisposableBean betSettlementPushConsumerCloser(PushConsumer betSettlementPushConsumer) {
        return () -> {
            try {
                CompletableFuture.runAsync(() -> closeQuietly(betSettlementPushConsumer))
                        .get(CONSUMER_CLOSE_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                log.warn("RocketMQ push consumer did not close within {}, abandoning in-flight receive",
                        CONSUMER_CLOSE_TIMEOUT);
            }
        };
    }

    private static void closeQuietly(PushConsumer consumer) {
        try {
            consumer.close();
        } catch (IOException e) {
            log.warn("Failed to close RocketMQ push consumer", e);
        }
    }
}
