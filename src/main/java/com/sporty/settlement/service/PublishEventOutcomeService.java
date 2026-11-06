package com.sporty.settlement.service;

import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.messaging.kafka.KafkaEventOutcomePublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PublishEventOutcomeService {

    private final KafkaEventOutcomePublisher eventOutcomePublisher;

    public void publish(EventOutcome outcome) {
        eventOutcomePublisher.publish(outcome);
        log.info("Published outcome of event {} (winner {})", outcome.eventId(), outcome.eventWinnerId());
    }
}
