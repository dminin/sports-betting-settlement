package com.sporty.settlement.messaging.dto;

import com.sporty.settlement.domain.BetSettlementCommand;

/** Wire contract of the {@code bet-settlements} RocketMQ topic. */
public record BetSettlementMessage(Long betId, String eventId, String outcomeWinnerId) {

  /** Creates a wire message from a domain settlement command. */
  public static BetSettlementMessage from(BetSettlementCommand command) {
    return new BetSettlementMessage(command.betId(), command.eventId(), command.outcomeWinnerId());
  }

  /** Converts this wire message to a validated domain command. */
  public BetSettlementCommand toCommand() {
    return new BetSettlementCommand(betId, eventId, outcomeWinnerId);
  }
}
