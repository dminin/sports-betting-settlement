package com.sporty.settlement.service;

import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.messaging.MessagePublishingException;

public interface BetSettlementPublisher {

    /**
     * @throws MessagePublishingException if the settlement request could not be sent
     */
    void publish(BetSettlementCommand command);
}
