package com.sporty.settlement.web.dto;

import com.sporty.settlement.domain.EventOutcome;

import jakarta.validation.constraints.NotBlank;

/**
 * Request body that reports the final winner of one market of an event.
 */
public record EventOutcomeRequest(
    @NotBlank String eventId,
    @NotBlank String eventMarketId,
    @NotBlank String eventName,
    @NotBlank String eventWinnerId) {

    /**
     * Converts this request to a validated domain outcome.
     */
    public EventOutcome toDomain() {
        return new EventOutcome(eventId, eventMarketId, eventName, eventWinnerId);
    }
}
