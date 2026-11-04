package com.sporty.settlement.application.service;

import com.sporty.settlement.application.port.in.MatchBetsForOutcomeUseCase;
import com.sporty.settlement.application.port.out.BetRepository;
import com.sporty.settlement.application.port.out.BetSettlementPublisher;
import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.EventOutcome;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Not transactional on purpose: no database transaction is held open while talking to the broker.
 */
@Service
class MatchBetsService implements MatchBetsForOutcomeUseCase {

    private static final Logger log = LoggerFactory.getLogger(MatchBetsService.class);

    private final BetRepository betRepository;
    private final BetSettlementPublisher betSettlementPublisher;

    MatchBetsService(BetRepository betRepository, BetSettlementPublisher betSettlementPublisher) {
        this.betRepository = betRepository;
        this.betSettlementPublisher = betSettlementPublisher;
    }

    @Override
    public int requestSettlements(EventOutcome outcome) {
        List<Bet> pendingBets = betRepository.findPendingByEventId(outcome.eventId());
        pendingBets.forEach(bet -> betSettlementPublisher.publish(outcome.settlementCommandFor(bet)));
        log.info("Requested settlement of {} pending bets for event {}", pendingBets.size(), outcome.eventId());
        return pendingBets.size();
    }
}
