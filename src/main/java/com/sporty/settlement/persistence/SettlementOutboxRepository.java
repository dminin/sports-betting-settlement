package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.BetSettlementCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Transactional outbox of settlement requests. A row stays until its request is published.
 */
@Repository
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class SettlementOutboxRepository {

    private final SpringDataSettlementOutboxJpaRepository jpaRepository;

    /**
     * Stores the requests. Joins the caller's transaction, so the rows commit with the caller's
     * other writes or not at all.
     */
    public void add(List<BetSettlementCommand> commands) {
        jpaRepository.saveAll(
            commands.stream()
                .map(
                    command ->
                        new SettlementOutboxJpaEntity(
                            null, command.betId(), command.eventId(), command.outcomeWinnerId()))
                .toList());
    }

    /**
     * Finds at most {@code limit} stored requests, the oldest first.
     */
    public List<OutboxEntry> findOldest(int limit) {
        return jpaRepository.findByOrderById(Limit.of(limit)).stream()
            .map(
                entity ->
                    new OutboxEntry(
                        entity.getId(),
                        new BetSettlementCommand(
                            entity.getBetId(), entity.getEventId(), entity.getOutcomeWinnerId())))
            .toList();
    }

    /**
     * Removes a published request. Removing an unknown identifier does nothing.
     */
    public void remove(Long id) {
        jpaRepository.deleteById(id);
    }
}
