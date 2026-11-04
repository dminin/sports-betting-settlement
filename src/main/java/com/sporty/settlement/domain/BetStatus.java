package com.sporty.settlement.domain;

public enum BetStatus {
    PENDING,
    WON,
    LOST;

    public boolean isSettled() {
        return this != PENDING;
    }
}
