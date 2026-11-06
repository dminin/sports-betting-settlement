package com.sporty.settlement.service;

import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.messaging.kafka.KafkaEventOutcomePublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PublishEventOutcomeService {

    private static final Logger log = LoggerFactory.getLogger(PublishEventOutcomeService.class);

    private final KafkaEventOutcomePublisher eventOutcomePublisher;

    PublishEventOutcomeService(KafkaEventOutcomePublisher eventOutcomePublisher) {
        this.eventOutcomePublisher = eventOutcomePublisher;
    }

    public void publish(EventOutcome outcome) {
        eventOutcomePublisher.publish(outcome);
        log.info("Published outcome of event {} (winner {})", outcome.eventId(), outcome.eventWinnerId());
    }
}
