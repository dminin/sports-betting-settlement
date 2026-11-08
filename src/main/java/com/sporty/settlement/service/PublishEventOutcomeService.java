package com.sporty.settlement.service;

import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.messaging.kafka.KafkaEventOutcomePublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Publishes reported event outcomes to the event-outcomes channel. */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class PublishEventOutcomeService {

  private final KafkaEventOutcomePublisher eventOutcomePublisher;

  /** Publishes an event outcome and records the successful operation. */
  public void publish(EventOutcome outcome) {
    eventOutcomePublisher.publish(outcome);
    log.info(
        "Published outcome of event {} (winner {})", outcome.eventId(), outcome.eventWinnerId());
  }
}
