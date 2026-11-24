package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;

/**
 * Final result of one market of a sports event: which participant won.
 */
public record EventOutcome(
    String eventId, String eventMarketId, String eventName, String eventWinnerId) {

    /**
     * Validates the event outcome fields.
     */
    public EventOutcome {
        requireNonBlank(eventId, "eventId");
        requireNonBlank(eventMarketId, "eventMarketId");
        requireNonBlank(eventName, "eventName");
        requireNonBlank(eventWinnerId, "eventWinnerId");
    }

    /**
     * Creates a request to settle the supplied bet against this outcome.
     */
    public BetSettlementCommand settlementCommandFor(Bet bet) {
        return new BetSettlementCommand(bet.id(), eventId, eventWinnerId);
    }
}
