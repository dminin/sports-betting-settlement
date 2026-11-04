package com.sporty.settlement.adapter.out.rocketmq;

import com.sporty.settlement.adapter.messaging.BetSettlementMessage;
import com.sporty.settlement.application.port.in.SettleBetUseCase;
import com.sporty.settlement.application.port.out.BetSettlementPublisher;
import com.sporty.settlement.domain.BetSettlementCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

/**
 * Stand-in for RocketMQ in the {@code mock} profile: logs the payload that would be sent to
 * {@code bet-settlements} and hands it straight to the settlement use case, like the real consumer would.
 * Settlement failures are logged rather than propagated, because a broker decouples them from the producer.
 */
@Component
@Profile("mock")
class InProcessBetSettlementPublisher implements BetSettlementPublisher {

    private static final Logger log = LoggerFactory.getLogger(InProcessBetSettlementPublisher.class);

    private final SettleBetUseCase settleBetUseCase;
    private final JsonMapper jsonMapper;

    InProcessBetSettlementPublisher(SettleBetUseCase settleBetUseCase, JsonMapper jsonMapper) {
        this.settleBetUseCase = settleBetUseCase;
        this.jsonMapper = jsonMapper;
    }

    @Override
    public void publish(BetSettlementCommand command) {
        log.info("[mock RocketMQ] bet-settlements <- {}",
                jsonMapper.writeValueAsString(BetSettlementMessage.from(command)));
        try {
            settleBetUseCase.settle(command);
        } catch (RuntimeException e) {
            log.error("[mock RocketMQ] settlement of bet {} failed", command.betId(), e);
        }
    }
}
