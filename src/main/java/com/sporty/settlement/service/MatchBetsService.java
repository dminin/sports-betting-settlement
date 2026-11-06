package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.persistence.BetRepository;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Not transactional on purpose: no database transaction is held open while talking to the broker.
 */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class MatchBetsService {

    private final BetRepository betRepository;
    private final BetSettlementPublisher betSettlementPublisher;

    public void requestSettlements(EventOutcome outcome) {
        List<Bet> pendingBets = betRepository.findPendingByEventId(outcome.eventId());
        pendingBets.forEach(bet -> betSettlementPublisher.publish(outcome.settlementCommandFor(bet)));
        log.info("Requested settlement of {} pending bets for event {}", pendingBets.size(), outcome.eventId());
    }
}
