package com.sporty.settlement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BetTest {

    private static final Instant SETTLED_AT = Instant.parse("2026-11-04T12:00:00Z");

    @Test
    void placeCreatesPendingBetWithPositiveAmount() {
        Bet bet = Bet.place("user-1", "EVT-1", "MKT-1", "TEAM-A", new BigDecimal("10.00"));

        assertThat(bet.status()).isEqualTo(BetStatus.PENDING);
        assertThat(bet.isSettled()).isFalse();
        assertThat(bet.settledAt()).isNull();

        assertThatThrownBy(() -> Bet.place("user-1", "EVT-1", "MKT-1", "TEAM-A", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void settleSetsWonOrLostFromOutcomeWinner() {
        Bet won = pendingBet(1L, "TEAM-A");
        assertThat(won.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"), SETTLED_AT)).isTrue();
        assertThat(won.status()).isEqualTo(BetStatus.WON);
        assertThat(won.settledAt()).isEqualTo(SETTLED_AT);

        Bet lost = pendingBet(2L, "TEAM-A");
        assertThat(lost.settle(new BetSettlementCommand(2L, "EVT-1", "TEAM-B"), SETTLED_AT)).isTrue();
        assertThat(lost.status()).isEqualTo(BetStatus.LOST);
        assertThat(lost.settledAt()).isEqualTo(SETTLED_AT);
    }

    @Test
    void settlingTwiceIsANoOp() {
        Bet bet = pendingBet(1L, "TEAM-A");
        bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"), SETTLED_AT);

        assertThat(bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-B"), SETTLED_AT)).isFalse();
        assertThat(bet.status()).isEqualTo(BetStatus.WON);
        assertThat(bet.settledAt()).isEqualTo(SETTLED_AT);
    }

    @Test
    void settleRejectsInvalidCommandWithoutChangingState() {
        Bet bet = pendingBet(1L, "TEAM-A");

        assertThatThrownBy(() -> bet.settle(new BetSettlementCommand(1L, "EVT-OTHER", "TEAM-A"), SETTLED_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bet.settle(new BetSettlementCommand(99L, "EVT-1", "TEAM-A"), SETTLED_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("settledAt");

        assertThat(bet.status()).isEqualTo(BetStatus.PENDING);
        assertThat(bet.settledAt()).isNull();
    }

    private static Bet pendingBet(Long id, String eventWinnerId) {
        return Bet.restore(id, "user-1", "EVT-1", "MKT-1", eventWinnerId, new BigDecimal("10.00"),
                BetStatus.PENDING, null, 0L);
    }
}
