package com.sporty.settlement.application.port.in;

import com.sporty.settlement.domain.Bet;
import java.math.BigDecimal;

public interface CreateBetUseCase {

    Bet create(CreateBetCommand command);

    record CreateBetCommand(String userId, String eventId, String eventMarketId, String eventWinnerId,
                            BigDecimal amount) {
    }
}
