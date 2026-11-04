package com.sporty.settlement.config;

import java.util.Map;
import org.apache.rocketmq.client.apis.ClientConfiguration;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.consumer.FilterExpression;
import org.apache.rocketmq.client.apis.consumer.FilterExpressionType;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.consumer.PushConsumer;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Real RocketMQ wiring through the gRPC proxy. Replaced by an in-process publisher in the {@code mock} profile.
 */
@Configuration(proxyBeanMethods = false)
@Profile("!mock")
class RocketMqConfig {

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

    @Bean
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
}
