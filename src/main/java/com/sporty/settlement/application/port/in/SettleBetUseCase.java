package com.sporty.settlement.application.port.in;

import com.sporty.settlement.domain.BetSettlementCommand;

public interface SettleBetUseCase {

    /**
     * Settles the bet as WON or LOST. Settling an already settled bet is a no-op, so redelivered
     * messages are safe.
     */
    void settle(BetSettlementCommand command);
}
