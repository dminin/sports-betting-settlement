package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;

/** Final result of a sports event: which participant won. */
public record EventOutcome(String eventId, String eventName, String eventWinnerId) {

  /** Validates the event outcome fields. */
  public EventOutcome {
    requireNonBlank(eventId, "eventId");
    requireNonBlank(eventName, "eventName");
    requireNonBlank(eventWinnerId, "eventWinnerId");
  }

  /** Creates a request to settle the supplied bet against this outcome. */
  public BetSettlementCommand settlementCommandFor(Bet bet) {
    return new BetSettlementCommand(bet.id(), eventId, eventWinnerId);
  }
}
