package com.sporty.settlement.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.persistence.BetRepository;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SettleBetServiceTest {

    private static final Instant NOW = Instant.parse("2026-11-04T12:00:00Z");
    private static final BetSettlementCommand COMMAND = new BetSettlementCommand(1L, "EVT-1", "TEAM-A");

    private BetRepository betRepository;
    private SettleBetService settleBetService;

    @BeforeEach
    void setUp() {
        betRepository = mock(BetRepository.class);
        settleBetService = new SettleBetService(betRepository, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    @Test
    void settlesPendingBetAndSavesIt() {
        given(betRepository.findById(1L)).willReturn(Optional.of(bet(BetStatus.PENDING, null)));

        settleBetService.settle(COMMAND);

        ArgumentCaptor<Bet> saved = ArgumentCaptor.forClass(Bet.class);
        verify(betRepository).save(saved.capture());
        assertThat(saved.getValue().status()).isEqualTo(BetStatus.WON);
        assertThat(saved.getValue().settledAt()).isEqualTo(NOW);
    }

    @Test
    void skipsBetThatIsAlreadySettled() {
        given(betRepository.findById(1L)).willReturn(Optional.of(bet(BetStatus.LOST, NOW.minusSeconds(60))));

        settleBetService.settle(COMMAND);

        verify(betRepository, never()).save(any());
    }

    @Test
    void failsForUnknownBet() {
        given(betRepository.findById(1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> settleBetService.settle(COMMAND)).isInstanceOf(BetNotFoundException.class);
    }

    private static Bet bet(BetStatus status, Instant settledAt) {
        return Bet.restore(1L, "user-1", "EVT-1", "MKT-1", "TEAM-A", new BigDecimal("10.00"), status, settledAt, 0L);
    }
}
