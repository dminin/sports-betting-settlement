package com.sporty.settlement.service;

import com.sporty.settlement.config.OutboxProperties;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.messaging.MessagePublishingException;
import com.sporty.settlement.persistence.OutboxEntry;
import com.sporty.settlement.persistence.SettlementOutboxRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class SettlementOutboxRelayTest {

    private static final BetSettlementCommand FIRST =
            new BetSettlementCommand(1L, "EVT-1", "TEAM-A");
    private static final BetSettlementCommand SECOND =
            new BetSettlementCommand(2L, "EVT-1", "TEAM-A");

    @Mock private SettlementOutboxRepository outboxRepository;

    @Mock private BetSettlementPublisher betSettlementPublisher;

    private SettlementOutboxRelay relay;

    @BeforeEach
    void setUp() {
        relay =
                new SettlementOutboxRelay(
                        outboxRepository,
                        betSettlementPublisher,
                        new OutboxProperties(Duration.ofSeconds(1), 10));
        given(outboxRepository.findOldest(10))
                .willReturn(List.of(new OutboxEntry(11L, FIRST), new OutboxEntry(12L, SECOND)));
    }

    @Test
    void publishesEachRequestThenRemovesIt() {
        relay.publishPending();

        InOrder order = inOrder(betSettlementPublisher, outboxRepository);
        order.verify(betSettlementPublisher).publish(FIRST);
        order.verify(outboxRepository).remove(11L);
        order.verify(betSettlementPublisher).publish(SECOND);
        order.verify(outboxRepository).remove(12L);
    }

    @Test
    void keepsTheFailedRequestAndStopsTheRunWhenPublishFails() {
        willThrow(new MessagePublishingException("broker down", new RuntimeException()))
                .given(betSettlementPublisher)
                .publish(FIRST);

        relay.publishPending();

        verify(outboxRepository, never()).remove(any());
        verify(betSettlementPublisher, never()).publish(SECOND);
    }
}
