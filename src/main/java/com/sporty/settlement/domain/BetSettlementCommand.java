package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;

/**
 * Request to settle a single bet against the winner of its event.
 */
public record BetSettlementCommand(Long betId, String eventId, String outcomeWinnerId) {

    public BetSettlementCommand {
        if (betId == null) {
            throw new IllegalArgumentException("betId must not be null");
        }
        requireNonBlank(eventId, "eventId");
        requireNonBlank(outcomeWinnerId, "outcomeWinnerId");
    }
}
