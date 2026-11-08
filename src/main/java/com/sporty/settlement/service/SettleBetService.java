package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.persistence.BetRepository;
import java.time.Clock;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Applies settlement commands to stored bets. */
@Slf4j
@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SettleBetService {

  private final BetRepository betRepository;
  private final Clock clock;

  /**
   * Settles the bet as WON or LOST. Settling an already settled bet is a no-op, so redelivered
   * messages are safe.
   */
  @Transactional
  public void settle(BetSettlementCommand command) {
    Bet bet =
        betRepository
            .findById(command.betId())
            .orElseThrow(() -> new BetNotFoundException(command.betId()));
    if (!bet.settle(command, clock.instant())) {
      log.info(
          "Bet {} is already settled as {}, skipping duplicate settlement", bet.id(), bet.status());
      return;
    }
    betRepository.save(bet);
    log.info("Settled bet {} of user {} as {}", bet.id(), bet.userId(), bet.status());
  }
}
