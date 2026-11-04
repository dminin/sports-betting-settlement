package com.sporty.settlement.application.port.in;

import com.sporty.settlement.domain.EventOutcome;

public interface MatchBetsForOutcomeUseCase {

    /**
     * Finds the pending bets of the outcome's event and requests settlement for each of them.
     *
     * @return number of settlement requests published
     */
    int requestSettlements(EventOutcome outcome);
}
