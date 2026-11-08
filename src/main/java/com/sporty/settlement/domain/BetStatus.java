package com.sporty.settlement.domain;

/** Lifecycle state of a bet. */
public enum BetStatus {
  PENDING,
  WON,
  LOST;

  public boolean isSettled() {
    return this != PENDING;
  }
}
