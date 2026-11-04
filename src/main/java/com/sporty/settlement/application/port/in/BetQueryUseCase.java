package com.sporty.settlement.application.port.in;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.util.List;

public interface BetQueryUseCase {

    Bet getById(Long id);

    /**
     * @param eventId optional, {@code null} matches any event
     * @param status  optional, {@code null} matches any status
     */
    List<Bet> find(String eventId, BetStatus status);
}
