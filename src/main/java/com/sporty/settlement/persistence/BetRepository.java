package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class BetRepository {

    private final SpringDataBetJpaRepository jpaRepository;

    /**
     * Persists the bet. Saving a stale copy (one whose version was changed concurrently) fails with
     * an optimistic locking exception.
     */
    public Bet save(Bet bet) {
        return BetEntityMapper.toDomain(jpaRepository.save(BetEntityMapper.toEntity(bet)));
    }

    public Optional<Bet> findById(Long id) {
        return jpaRepository.findById(id).map(BetEntityMapper::toDomain);
    }

    /** Finds pending bets of an event across all markets in identifier order. */
    public List<Bet> findPending(String eventId) {
        return jpaRepository
                .findByEventIdAndStatusOrderById(eventId, BetStatus.PENDING)
                .stream()
                .map(BetEntityMapper::toDomain)
                .toList();
    }

    /**
     * Finds bets that match the supplied optional filters.
     *
     * @param eventId optional, {@code null} matches any event
     * @param status optional, {@code null} matches any status
     */
    public List<Bet> find(String eventId, BetStatus status) {
        return jpaRepository.search(eventId, status).stream()
                .map(BetEntityMapper::toDomain)
                .toList();
    }
}
