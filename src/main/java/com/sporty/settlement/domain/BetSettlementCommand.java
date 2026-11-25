package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;
import static com.sporty.settlement.domain.Preconditions.requireNonNull;

public record BetSettlementCommand(Long betId, String eventId, String outcomeWinnerId) {

    public BetSettlementCommand {
        requireNonNull(betId, "betId");
        requireNonBlank(eventId, "eventId");
        requireNonBlank(outcomeWinnerId, "outcomeWinnerId");
    }
}
