package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.math.BigDecimal;
import java.time.Instant;

record BetResponse(
        Long id,
        String userId,
        String eventId,
        String eventMarketId,
        String eventWinnerId,
        BigDecimal amount,
        BetStatus status,
        Instant settledAt) {

    static BetResponse from(Bet bet) {
        return new BetResponse(bet.id(), bet.userId(), bet.eventId(), bet.eventMarketId(), bet.eventWinnerId(),
                bet.amount(), bet.status(), bet.settledAt());
    }
}
