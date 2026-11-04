package com.sporty.settlement.application.port.out;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.util.List;
import java.util.Optional;

public interface BetRepository {

    /**
     * Persists the bet. Saving a stale copy (one whose version was changed concurrently) fails with an
     * optimistic locking exception.
     */
    Bet save(Bet bet);

    Optional<Bet> findById(Long id);

    List<Bet> findPendingByEventId(String eventId);

    /**
     * @param eventId optional, {@code null} matches any event
     * @param status  optional, {@code null} matches any status
     */
    List<Bet> find(String eventId, BetStatus status);
}
