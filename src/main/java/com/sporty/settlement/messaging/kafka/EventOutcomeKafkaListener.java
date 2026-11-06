package com.sporty.settlement.messaging.kafka;

import com.sporty.settlement.messaging.dto.EventOutcomeMessage;
import com.sporty.settlement.service.MatchBetsService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class EventOutcomeKafkaListener {

    private final MatchBetsService matchBetsService;

    @KafkaListener(topics = "${app.kafka.event-outcomes-topic}")
    void onEventOutcome(@Payload EventOutcomeMessage message,
                        @Header(KafkaHeaders.RECEIVED_PARTITION) int partition,
                        @Header(KafkaHeaders.OFFSET) long offset) {
        log.info("Received outcome of event {} (partition={}, offset={})", message.eventId(), partition, offset);
        matchBetsService.requestSettlements(message.toDomain());
    }
}
