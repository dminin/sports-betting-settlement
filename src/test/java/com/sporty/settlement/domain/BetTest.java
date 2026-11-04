package com.sporty.settlement.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class BetTest {

    private static final Instant SETTLED_AT = Instant.parse("2026-11-04T12:00:00Z");

    @Test
    void placedBetIsPending() {
        Bet bet = pendingBet(1L, "TEAM-A");

        assertThat(bet.status()).isEqualTo(BetStatus.PENDING);
        assertThat(bet.isSettled()).isFalse();
        assertThat(bet.settledAt()).isNull();
    }

    @Test
    void betOnTheWinnerIsWon() {
        Bet bet = pendingBet(1L, "TEAM-A");

        bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"), SETTLED_AT);

        assertThat(bet.status()).isEqualTo(BetStatus.WON);
        assertThat(bet.settledAt()).isEqualTo(SETTLED_AT);
    }

    @Test
    void betOnAnotherParticipantIsLost() {
        Bet bet = pendingBet(1L, "TEAM-A");

        bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-B"), SETTLED_AT);

        assertThat(bet.status()).isEqualTo(BetStatus.LOST);
        assertThat(bet.settledAt()).isEqualTo(SETTLED_AT);
    }

    @Test
    void betCannotBeSettledTwice() {
        Bet bet = pendingBet(1L, "TEAM-A");
        bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"), SETTLED_AT);

        assertThatThrownBy(() -> bet.settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-B"), SETTLED_AT))
                .isInstanceOf(BetAlreadySettledException.class);
        assertThat(bet.status()).isEqualTo(BetStatus.WON);
    }

    @Test
    void betCannotBeSettledWithAnotherEventsOutcome() {
        Bet bet = pendingBet(1L, "TEAM-A");

        assertThatThrownBy(() -> bet.settle(new BetSettlementCommand(1L, "EVT-OTHER", "TEAM-A"), SETTLED_AT))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(bet.status()).isEqualTo(BetStatus.PENDING);
    }

    @Test
    void amountMustBePositive() {
        assertThatThrownBy(() -> Bet.place("user-1", "EVT-1", "MKT-1", "TEAM-A", BigDecimal.ZERO))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void outcomeBuildsSettlementCommandForBet() {
        EventOutcome outcome = new EventOutcome("EVT-1", "A vs B", "TEAM-B");

        assertThat(outcome.settlementCommandFor(pendingBet(7L, "TEAM-A")))
                .isEqualTo(new BetSettlementCommand(7L, "EVT-1", "TEAM-B"));
    }

    static Bet pendingBet(Long id, String eventWinnerId) {
        return Bet.restore(id, "user-1", "EVT-1", "MKT-1", eventWinnerId, new BigDecimal("10.00"),
                BetStatus.PENDING, null, 0L);
    }
}
