package com.sporty.settlement.adapter.out.persistence;

import com.sporty.settlement.domain.Bet;

final class BetEntityMapper {

    private BetEntityMapper() {
    }

    static BetJpaEntity toEntity(Bet bet) {
        return new BetJpaEntity(bet.id(), bet.userId(), bet.eventId(), bet.eventMarketId(), bet.eventWinnerId(),
                bet.amount(), bet.status(), bet.settledAt(), bet.version());
    }

    static Bet toDomain(BetJpaEntity entity) {
        return Bet.restore(entity.getId(), entity.getUserId(), entity.getEventId(), entity.getEventMarketId(),
                entity.getEventWinnerId(), entity.getAmount(), entity.getStatus(), entity.getSettledAt(),
                entity.getVersion());
    }
}
