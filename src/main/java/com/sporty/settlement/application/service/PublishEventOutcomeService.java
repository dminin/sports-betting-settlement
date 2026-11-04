package com.sporty.settlement.application.service;

import com.sporty.settlement.application.port.in.PublishEventOutcomeUseCase;
import com.sporty.settlement.application.port.out.EventOutcomePublisher;
import com.sporty.settlement.domain.EventOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
class PublishEventOutcomeService implements PublishEventOutcomeUseCase {

    private static final Logger log = LoggerFactory.getLogger(PublishEventOutcomeService.class);

    private final EventOutcomePublisher eventOutcomePublisher;

    PublishEventOutcomeService(EventOutcomePublisher eventOutcomePublisher) {
        this.eventOutcomePublisher = eventOutcomePublisher;
    }

    @Override
    public void publish(EventOutcome outcome) {
        eventOutcomePublisher.publish(outcome);
        log.info("Published outcome of event {} (winner {})", outcome.eventId(), outcome.eventWinnerId());
    }
}
