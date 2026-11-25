package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.BetSettlementCommand;

public record OutboxEntry(Long id, BetSettlementCommand command) {}
