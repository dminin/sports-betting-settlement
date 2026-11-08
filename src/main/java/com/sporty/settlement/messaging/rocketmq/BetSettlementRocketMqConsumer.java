package com.sporty.settlement.messaging.rocketmq;

import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.messaging.dto.BetSettlementMessage;
import com.sporty.settlement.service.BetNotFoundException;
import com.sporty.settlement.service.SettleBetService;
import java.nio.ByteBuffer;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.consumer.MessageListener;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;

/**
 * Settles bets from {@code bet-settlements}. Transient failures return {@link
 * ConsumeResult#FAILURE} so the broker redelivers (and eventually dead-letters) the message;
 * messages that can never succeed are logged and acknowledged.
 */
@Slf4j
@Component("betSettlementListener")
@Profile("!mock")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class BetSettlementRocketMqConsumer implements MessageListener {

  private final SettleBetService settleBetService;
  private final JsonMapper jsonMapper;

  @Override
  public ConsumeResult consume(MessageView messageView) {
    try {
      BetSettlementCommand command = parse(messageView.getBody()).toCommand();
      settleBetService.settle(command);
      return ConsumeResult.SUCCESS;
    } catch (JacksonException | IllegalArgumentException | BetNotFoundException e) {
      log.error("Discarding unprocessable settlement message {}", messageView.getMessageId(), e);
      return ConsumeResult.SUCCESS;
    } catch (RuntimeException e) {
      log.warn(
          "Settlement message {} failed on delivery attempt {}, it will be redelivered",
          messageView.getMessageId(),
          messageView.getDeliveryAttempt(),
          e);
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
