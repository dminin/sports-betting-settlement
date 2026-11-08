package com.sporty.settlement.web.dto;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.math.BigDecimal;
import java.time.Instant;

/** API representation of a bet and its settlement state. */
public record BetResponse(
    Long id,
    String userId,
    String eventId,
    String eventMarketId,
    String eventWinnerId,
    BigDecimal amount,
    BetStatus status,
    Instant settledAt) {

  /** Creates a response from a domain bet. */
  public static BetResponse from(Bet bet) {
    return new BetResponse(
        bet.id(),
        bet.userId(),
        bet.eventId(),
        bet.eventMarketId(),
        bet.eventWinnerId(),
        bet.amount(),
        bet.status(),
        bet.settledAt());
  }
}
