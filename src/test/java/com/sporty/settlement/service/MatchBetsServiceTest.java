package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.persistence.BetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class MatchBetsServiceTest {

    private static final EventOutcome OUTCOME =
            new EventOutcome("EVT-1", "A vs B", "TEAM-A");

    @Mock private BetRepository betRepository;

    @Mock private BetSettlementPublisher betSettlementPublisher;

    @InjectMocks private MatchBetsService matchBetsService;

    private static Bet pendingBet(Long id, String marketId) {
        return Bet.restore(
                id,
                "user-" + id,
                "EVT-1",
                marketId,
                "TEAM-B",
                new BigDecimal("5.00"),
                BetStatus.PENDING,
                null,
                0L);
    }

    @Test
    void publishesOneSettlementRequestPerPendingBetAcrossMarkets() {
        given(betRepository.findPending("EVT-1"))
                .willReturn(List.of(pendingBet(1L, "MKT-1"), pendingBet(2L, "MKT-2")));

        matchBetsService.requestSettlements(OUTCOME);

        verify(betSettlementPublisher).publish(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"));
        verify(betSettlementPublisher).publish(new BetSettlementCommand(2L, "EVT-1", "TEAM-A"));
    }

    @Test
    void publishesNothingWhenNoBetIsPending() {
        given(betRepository.findPending("EVT-1")).willReturn(List.of());

        matchBetsService.requestSettlements(OUTCOME);

        verifyNoInteractions(betSettlementPublisher);
    }
}
