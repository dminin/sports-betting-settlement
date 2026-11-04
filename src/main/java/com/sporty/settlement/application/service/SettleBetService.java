package com.sporty.settlement.application.service;

import com.sporty.settlement.application.BetNotFoundException;
import com.sporty.settlement.application.port.in.SettleBetUseCase;
import com.sporty.settlement.application.port.out.BetRepository;
import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import java.time.Clock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class SettleBetService implements SettleBetUseCase {

    private static final Logger log = LoggerFactory.getLogger(SettleBetService.class);

    private final BetRepository betRepository;
    private final Clock clock;

    SettleBetService(BetRepository betRepository, Clock clock) {
        this.betRepository = betRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void settle(BetSettlementCommand command) {
        Bet bet = betRepository.findById(command.betId())
                .orElseThrow(() -> new BetNotFoundException(command.betId()));
        if (bet.isSettled()) {
            log.info("Bet {} is already settled as {}, skipping duplicate settlement", bet.id(), bet.status());
            return;
        }
        bet.settle(command, clock.instant());
        betRepository.save(bet);
        log.info("Settled bet {} of user {} as {}", bet.id(), bet.userId(), bet.status());
    }
}
