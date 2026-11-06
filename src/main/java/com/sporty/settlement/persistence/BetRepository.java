package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class BetRepository {

    private final SpringDataBetJpaRepository jpaRepository;

    /**
     * Persists the bet. Saving a stale copy (one whose version was changed concurrently) fails with an
     * optimistic locking exception.
     */
    public Bet save(Bet bet) {
        return BetEntityMapper.toDomain(jpaRepository.save(BetEntityMapper.toEntity(bet)));
    }

    public Optional<Bet> findById(Long id) {
        return jpaRepository.findById(id).map(BetEntityMapper::toDomain);
    }

    public List<Bet> findPendingByEventId(String eventId) {
        return jpaRepository.findByEventIdAndStatusOrderById(eventId, BetStatus.PENDING).stream()
                .map(BetEntityMapper::toDomain)
                .toList();
    }

    /**
     * @param eventId optional, {@code null} matches any event
     * @param status  optional, {@code null} matches any status
     */
    public List<Bet> find(String eventId, BetStatus status) {
        return jpaRepository.search(eventId, status).stream()
                .map(BetEntityMapper::toDomain)
                .toList();
    }
}
