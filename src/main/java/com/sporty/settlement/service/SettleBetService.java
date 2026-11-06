package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.persistence.BetRepository;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SettleBetService {

    private static final Logger log = LoggerFactory.getLogger(SettleBetService.class);

    private final BetRepository betRepository;
    private final Clock clock;

    SettleBetService(BetRepository betRepository, Clock clock) {
        this.betRepository = betRepository;
        this.clock = clock;
    }

    /**
     * Settles the bet as WON or LOST. Settling an already settled bet is a no-op, so redelivered
     * messages are safe.
     */
    @Transactional
    public void settle(BetSettlementCommand command) {
        Bet bet = betRepository.findById(command.betId())
                .orElseThrow(() -> new BetNotFoundException(command.betId()));
        if (!bet.settle(command, clock.instant())) {
            log.info("Bet {} is already settled as {}, skipping duplicate settlement", bet.id(), bet.status());
            return;
        }
        betRepository.save(bet);
        log.info("Settled bet {} of user {} as {}", bet.id(), bet.userId(), bet.status());
    }
}
