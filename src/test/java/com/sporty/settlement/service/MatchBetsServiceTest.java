package com.sporty.settlement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.persistence.BetRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MatchBetsServiceTest {

    private static final EventOutcome OUTCOME = new EventOutcome("EVT-1", "A vs B", "TEAM-A");

    @Mock
    private BetRepository betRepository;

    @Mock
    private BetSettlementPublisher betSettlementPublisher;

    @InjectMocks
    private MatchBetsService matchBetsService;

    @Test
    void publishesOneSettlementRequestPerPendingBet() {
        given(betRepository.findPendingByEventId("EVT-1")).willReturn(List.of(pendingBet(1L), pendingBet(2L)));

        int requested = matchBetsService.requestSettlements(OUTCOME);

        assertThat(requested).isEqualTo(2);
        verify(betSettlementPublisher).publish(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"));
        verify(betSettlementPublisher).publish(new BetSettlementCommand(2L, "EVT-1", "TEAM-A"));
    }

    @Test
    void publishesNothingWhenNoBetIsPending() {
        given(betRepository.findPendingByEventId("EVT-1")).willReturn(List.of());

        int requested = matchBetsService.requestSettlements(OUTCOME);

        assertThat(requested).isZero();
        verify(betSettlementPublisher, never()).publish(any());
    }

    private static Bet pendingBet(Long id) {
        return Bet.restore(id, "user-" + id, "EVT-1", "MKT-1", "TEAM-B", new BigDecimal("5.00"),
                BetStatus.PENDING, null, 0L);
    }
}
