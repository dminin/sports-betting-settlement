package com.sporty.settlement.application.port.in;

import com.sporty.settlement.domain.EventOutcome;

public interface PublishEventOutcomeUseCase {

    void publish(EventOutcome outcome);
}
