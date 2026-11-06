package com.sporty.settlement.messaging.kafka;

import com.sporty.settlement.messaging.dto.EventOutcomeMessage;
import com.sporty.settlement.service.MatchBetsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
class EventOutcomeKafkaListener {

    private static final Logger log = LoggerFactory.getLogger(EventOutcomeKafkaListener.class);

    private final MatchBetsService matchBetsService;

    EventOutcomeKafkaListener(MatchBetsService matchBetsService) {
        this.matchBetsService = matchBetsService;
    }

    @KafkaListener(topics = "${app.kafka.event-outcomes-topic}")
    void onEventOutcome(@Payload EventOutcomeMessage message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received outcome of event {} (partition={}, offset={})", message.eventId(), partition, offset);
        matchBetsService.requestSettlements(message.toDomain());
    }
}
