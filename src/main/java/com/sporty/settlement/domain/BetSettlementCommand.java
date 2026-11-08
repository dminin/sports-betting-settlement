package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;
import static com.sporty.settlement.domain.Preconditions.requireNonNull;

/** Request to settle a single bet against the winner of its event. */
public record BetSettlementCommand(Long betId, String eventId, String outcomeWinnerId) {

  /** Validates the settlement request fields. */
  public BetSettlementCommand {
    requireNonNull(betId, "betId");
    requireNonBlank(eventId, "eventId");
    requireNonBlank(outcomeWinnerId, "outcomeWinnerId");
  }
}
