package com.sporty.settlement.adapter.messaging;

import com.sporty.settlement.domain.BetSettlementCommand;

/**
 * Wire contract of the {@code bet-settlements} RocketMQ topic.
 */
public record BetSettlementMessage(Long betId, String eventId, String outcomeWinnerId) {

    public static BetSettlementMessage from(BetSettlementCommand command) {
        return new BetSettlementMessage(command.betId(), command.eventId(), command.outcomeWinnerId());
    }

    public BetSettlementCommand toCommand() {
        return new BetSettlementCommand(betId, eventId, outcomeWinnerId);
    }
}
