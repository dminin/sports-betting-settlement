package com.sporty.settlement.application;

public class BetNotFoundException extends RuntimeException {

    public BetNotFoundException(Long betId) {
        super("Bet " + betId + " not found");
    }
}
