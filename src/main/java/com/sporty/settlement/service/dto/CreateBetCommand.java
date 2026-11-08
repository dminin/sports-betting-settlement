package com.sporty.settlement.service.dto;

import java.math.BigDecimal;

/** Service input for placing a bet. */
public record CreateBetCommand(
    String userId, String eventId, String eventMarketId, String eventWinnerId, BigDecimal amount) {}
