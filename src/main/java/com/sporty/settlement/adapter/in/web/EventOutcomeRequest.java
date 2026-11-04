package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.domain.EventOutcome;
import jakarta.validation.constraints.NotBlank;

record EventOutcomeRequest(
        @NotBlank String eventId,
        @NotBlank String eventName,
        @NotBlank String eventWinnerId) {

    EventOutcome toDomain() {
        return new EventOutcome(eventId, eventName, eventWinnerId);
    }
}
