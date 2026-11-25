package com.sporty.settlement.service;

import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.messaging.MessagePublishingException;

public interface BetSettlementPublisher {

    /**
     * Publishes a settlement request.
     *
     * @throws MessagePublishingException if the settlement request could not be sent
     */
    void publish(BetSettlementCommand command);
}
