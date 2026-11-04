package com.sporty.settlement.application.port.out;

import com.sporty.settlement.domain.EventOutcome;

public interface EventOutcomePublisher {

    /**
     * Publishes the outcome and returns once the broker has acknowledged it.
     *
     * @throws MessagePublishingException if the broker did not acknowledge the message
     */
    void publish(EventOutcome outcome);
}
