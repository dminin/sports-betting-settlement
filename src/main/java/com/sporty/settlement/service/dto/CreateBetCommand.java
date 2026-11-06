package com.sporty.settlement.service.dto;

import java.math.BigDecimal;

public record CreateBetCommand(String userId, String eventId, String eventMarketId, String eventWinnerId,
                               BigDecimal amount) {
}
