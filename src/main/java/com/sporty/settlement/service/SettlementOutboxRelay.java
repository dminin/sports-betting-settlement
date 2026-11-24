package com.sporty.settlement.service;

import com.sporty.settlement.config.OutboxProperties;
import com.sporty.settlement.messaging.MessagePublishingException;
import com.sporty.settlement.persistence.OutboxEntry;
import com.sporty.settlement.persistence.SettlementOutboxRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Publishes the settlement requests in the outbox, the oldest first. A row is removed only after
 * its publish succeeds, so delivery is at least once: a crash between the two steps publishes the
 * request again, and settlement is idempotent. Not transactional on purpose: no database
 * transaction is held open while talking to the broker.
 */
@Slf4j
@Component
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class SettlementOutboxRelay {

    private final SettlementOutboxRepository outboxRepository;
    private final BetSettlementPublisher betSettlementPublisher;
    private final OutboxProperties outboxProperties;

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval}")
    void publishPending() {
        for (OutboxEntry entry : outboxRepository.findOldest(outboxProperties.batchSize())) {
            try {
                betSettlementPublisher.publish(entry.command());
            } catch (MessagePublishingException e) {
                log.warn(
                    "Could not publish settlement of bet {}, retrying on the next run",
                    entry.command().betId(),
                    e);
                return;
            }
            outboxRepository.remove(entry.id());
        }
    }
}
