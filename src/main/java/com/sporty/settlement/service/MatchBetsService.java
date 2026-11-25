package com.sporty.settlement.service;

import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.persistence.BetRepository;
import com.sporty.settlement.persistence.SettlementOutboxRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Matches an event outcome to the pending bets it settles. Does no broker I/O: the settlement
 * requests go to the outbox in the same transaction as the read.
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MatchBetsService {

    private final BetRepository betRepository;
    private final SettlementOutboxRepository outboxRepository;

    /**
     * Stores one settlement request in the outbox for each pending bet on the outcome's event and
     * market. {@link SettlementOutboxRelay} publishes the requests later. A repeated outcome can
     * store a second request for a bet that is not settled yet; settlement is idempotent.
     */
    @Transactional
    public void requestSettlements(EventOutcome outcome) {
        List<BetSettlementCommand> commands =
                betRepository.findPending(outcome.eventId(), outcome.eventMarketId()).stream()
                        .map(outcome::settlementCommandFor)
                        .toList();
        outboxRepository.add(commands);
        log.info(
                "Stored {} settlement requests in the outbox for event {} market {}",
                commands.size(),
                outcome.eventId(),
                outcome.eventMarketId());
    }
}
