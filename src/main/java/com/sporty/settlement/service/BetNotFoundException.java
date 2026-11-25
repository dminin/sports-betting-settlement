package com.sporty.settlement.service;

public class BetNotFoundException extends RuntimeException {

    public BetNotFoundException(Long betId) {
        super("Bet " + betId + " not found");
    }
}
