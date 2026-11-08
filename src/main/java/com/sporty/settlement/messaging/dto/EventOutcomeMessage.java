package com.sporty.settlement.messaging.dto;

import com.sporty.settlement.domain.EventOutcome;

/** Wire contract of the {@code event-outcomes} Kafka topic. */
public record EventOutcomeMessage(String eventId, String eventName, String eventWinnerId) {

  /** Creates a wire message from a domain event outcome. */
  public static EventOutcomeMessage from(EventOutcome outcome) {
    return new EventOutcomeMessage(outcome.eventId(), outcome.eventName(), outcome.eventWinnerId());
  }

  /** Converts this wire message to a validated domain outcome. */
  public EventOutcome toDomain() {
    return new EventOutcome(eventId, eventName, eventWinnerId);
  }
}
