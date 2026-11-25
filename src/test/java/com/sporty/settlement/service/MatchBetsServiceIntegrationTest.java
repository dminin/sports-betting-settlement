package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.service.dto.CreateBetCommand;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@SpringBootTest(
        properties = {
            "spring.kafka.listener.auto-startup=false",
            "spring.kafka.admin.auto-create=false",
            "app.seed.enabled=false"
        })
@ActiveProfiles("mock")
@Transactional
class MatchBetsServiceIntegrationTest {

    @Autowired private BetService betService;

    @Autowired private MatchBetsService matchBetsService;

    @Autowired private SettleBetService settleBetService;

    @MockitoBean private BetSettlementPublisher betSettlementPublisher;

    @Test
    void matchesOnlyPendingBetsOfTheEventAcrossAllMarkets() {
        Bet firstMarketBet = createBet("EVT-1", "MKT-1");
        final Bet secondMarketBet = createBet("EVT-1", "MKT-2");
        final Bet otherEventBet = createBet("EVT-2", "MKT-1");
        Bet settledBet = createBet("EVT-1", "MKT-3");
        settleBetService.settle(
                new BetSettlementCommand(settledBet.id(), "EVT-1", "TEAM-A"));

        matchBetsService.requestSettlements(new EventOutcome("EVT-1", "A vs B", "TEAM-A"));

        verify(betSettlementPublisher)
                .publish(new BetSettlementCommand(firstMarketBet.id(), "EVT-1", "TEAM-A"));
        verify(betSettlementPublisher)
                .publish(new BetSettlementCommand(secondMarketBet.id(), "EVT-1", "TEAM-A"));
        verify(betSettlementPublisher, times(2)).publish(any());
        assertThat(betService.getById(firstMarketBet.id()).status()).isEqualTo(BetStatus.PENDING);
        assertThat(betService.getById(secondMarketBet.id()).status()).isEqualTo(BetStatus.PENDING);
        assertThat(betService.getById(otherEventBet.id()).status()).isEqualTo(BetStatus.PENDING);
        assertThat(betService.getById(settledBet.id()).status()).isEqualTo(BetStatus.WON);
    }

    private Bet createBet(String eventId, String marketId) {
        return betService.create(
                new CreateBetCommand(
                        "user-1", eventId, marketId, "TEAM-A", new BigDecimal("10.00")));
    }
}
