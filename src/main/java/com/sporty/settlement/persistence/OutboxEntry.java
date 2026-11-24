package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.BetSettlementCommand;

/**
 * A settlement request that is stored in the outbox and is not published yet.
 *
 * @param id      outbox row identifier, used to remove the row after it is published
 * @param command the settlement request to publish
 */
public record OutboxEntry(Long id, BetSettlementCommand command) {
}
