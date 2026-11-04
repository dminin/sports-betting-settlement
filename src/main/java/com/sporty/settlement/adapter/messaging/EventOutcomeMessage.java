package com.sporty.settlement.adapter.messaging;

import com.sporty.settlement.domain.EventOutcome;

/**
 * Wire contract of the {@code event-outcomes} Kafka topic.
 */
public record EventOutcomeMessage(String eventId, String eventName, String eventWinnerId) {

    public static EventOutcomeMessage from(EventOutcome outcome) {
        return new EventOutcomeMessage(outcome.eventId(), outcome.eventName(), outcome.eventWinnerId());
    }

    public EventOutcome toDomain() {
        return new EventOutcome(eventId, eventName, eventWinnerId);
    }
}
