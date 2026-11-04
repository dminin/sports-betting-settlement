package com.sporty.settlement.application.port.out;

import com.sporty.settlement.domain.BetSettlementCommand;

public interface BetSettlementPublisher {

    /**
     * @throws MessagePublishingException if the settlement request could not be sent
     */
    void publish(BetSettlementCommand command);
}
