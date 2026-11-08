package com.sporty.settlement.web.dto;

import com.sporty.settlement.domain.EventOutcome;
import jakarta.validation.constraints.NotBlank;

/** Request body that reports the final winner of an event. */
public record EventOutcomeRequest(
    @NotBlank String eventId, @NotBlank String eventName, @NotBlank String eventWinnerId) {

  /** Converts this request to a validated domain outcome. */
  public EventOutcome toDomain() {
    return new EventOutcome(eventId, eventName, eventWinnerId);
  }
}
