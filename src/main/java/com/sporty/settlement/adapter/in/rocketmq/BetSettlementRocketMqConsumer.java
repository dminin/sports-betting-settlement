package com.sporty.settlement.adapter.in.rocketmq;

import com.sporty.settlement.adapter.messaging.BetSettlementMessage;
import com.sporty.settlement.application.BetNotFoundException;
import com.sporty.settlement.application.port.in.SettleBetUseCase;
import com.sporty.settlement.domain.BetSettlementCommand;
import java.nio.ByteBuffer;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Settles bets from {@code bet-settlements}. Transient failures return {@link ConsumeResult#FAILURE} so the
 * broker redelivers (and eventually dead-letters) the message; messages that can never succeed are logged
 * and acknowledged.
 */
@Component("betSettlementListener")
@Profile("!mock")
class BetSettlementRocketMqConsumer implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(BetSettlementRocketMqConsumer.class);

    private final SettleBetUseCase settleBetUseCase;
    private final JsonMapper jsonMapper;

    BetSettlementRocketMqConsumer(SettleBetUseCase settleBetUseCase, JsonMapper jsonMapper) {
        this.settleBetUseCase = settleBetUseCase;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public ConsumeResult consume(MessageView messageView) {
        try {
            BetSettlementCommand command = parse(messageView.getBody()).toCommand();
            settleBetUseCase.settle(command);
            return ConsumeResult.SUCCESS;
        } catch (JacksonException | IllegalArgumentException | BetNotFoundException e) {
            log.error("Discarding unprocessable settlement message {}", messageView.getMessageId(), e);
            return ConsumeResult.SUCCESS;
        } catch (RuntimeException e) {
            log.warn("Settlement message {} failed on delivery attempt {}, it will be redelivered",
                    messageView.getMessageId(), messageView.getDeliveryAttempt(), e);
            return ConsumeResult.FAILURE;
        }
    }

    private BetSettlementMessage parse(ByteBuffer body) {
        ByteBuffer buffer = body.duplicate();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return jsonMapper.readValue(bytes, BetSettlementMessage.class);
    }
}
