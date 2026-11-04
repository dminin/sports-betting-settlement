package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;

import java.util.Objects;

/**
 * Request to settle a single bet against the winner of its event.
 */
public record BetSettlementCommand(Long betId, String eventId, String outcomeWinnerId) {

    public BetSettlementCommand {
        Objects.requireNonNull(betId, "betId must not be null");
        requireNonBlank(eventId, "eventId");
        requireNonBlank(outcomeWinnerId, "outcomeWinnerId");
    }
}
