package com.sporty.settlement.web.dto;

import com.sporty.settlement.domain.EventOutcome;
import jakarta.validation.constraints.NotBlank;

public record EventOutcomeRequest(
        @NotBlank String eventId,
        @NotBlank String eventName,
        @NotBlank String eventWinnerId) {

    public EventOutcome toDomain() {
        return new EventOutcome(eventId, eventName, eventWinnerId);
    }
}
