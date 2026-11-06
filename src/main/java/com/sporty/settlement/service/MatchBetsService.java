package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.persistence.BetRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Not transactional on purpose: no database transaction is held open while talking to the broker.
 */
@Service
public class MatchBetsService {

    private static final Logger log = LoggerFactory.getLogger(MatchBetsService.class);

    private final BetRepository betRepository;
    private final BetSettlementPublisher betSettlementPublisher;

    MatchBetsService(BetRepository betRepository, BetSettlementPublisher betSettlementPublisher) {
        this.betRepository = betRepository;
        this.betSettlementPublisher = betSettlementPublisher;
    }

    public void requestSettlements(EventOutcome outcome) {
        List<Bet> pendingBets = betRepository.findPendingByEventId(outcome.eventId());
        pendingBets.forEach(bet -> betSettlementPublisher.publish(outcome.settlementCommandFor(bet)));
        log.info("Requested settlement of {} pending bets for event {}", pendingBets.size(), outcome.eventId());
    }
}
