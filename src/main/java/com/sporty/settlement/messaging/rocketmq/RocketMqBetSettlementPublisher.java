package com.sporty.settlement.messaging.rocketmq;

import com.sporty.settlement.config.RocketMqProperties;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.messaging.MessagePublishingException;
import com.sporty.settlement.messaging.dto.BetSettlementMessage;
import com.sporty.settlement.service.BetSettlementPublisher;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.ClientException;
import org.apache.rocketmq.client.apis.ClientServiceProvider;
import org.apache.rocketmq.client.apis.message.Message;
import org.apache.rocketmq.client.apis.producer.Producer;
import org.apache.rocketmq.client.apis.producer.SendReceipt;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
@Profile("!mock")
class RocketMqBetSettlementPublisher implements BetSettlementPublisher {

    private static final String SETTLEMENT_TAG = "SETTLE";

    private final Producer producer;
    private final ClientServiceProvider provider;
    private final JsonMapper jsonMapper;
    private final String topic;

    RocketMqBetSettlementPublisher(Producer producer, ClientServiceProvider provider, JsonMapper jsonMapper,
                                   RocketMqProperties properties) {
        this.producer = producer;
        this.provider = provider;
        this.jsonMapper = jsonMapper;
        this.topic = properties.topic();
    }

    @Override
    public void publish(BetSettlementCommand command) {
        Message message = provider.newMessageBuilder()
                .setTopic(topic)
                .setTag(SETTLEMENT_TAG)
                .setKeys(String.valueOf(command.betId()))
                .setBody(jsonMapper.writeValueAsBytes(BetSettlementMessage.from(command)))
                .build();
        try {
            SendReceipt receipt = producer.send(message);
            log.info("Sent settlement request for bet {} to {} (messageId={})",
                    command.betId(), topic, receipt.getMessageId());
        } catch (ClientException e) {
            throw new MessagePublishingException("Failed to send settlement request for bet " + command.betId(), e);
        }
    }
}
