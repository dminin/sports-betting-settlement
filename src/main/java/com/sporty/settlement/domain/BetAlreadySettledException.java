package com.sporty.settlement.domain;

public class BetAlreadySettledException extends RuntimeException {

    public BetAlreadySettledException(Long betId, BetStatus status) {
        super("Bet " + betId + " is already settled as " + status);
    }
}
