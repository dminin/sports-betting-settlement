package com.sporty.settlement.service;

/** Indicates that no bet exists for a requested identifier. */
public class BetNotFoundException extends RuntimeException {

  /** Creates an exception for the missing bet identifier. */
  public BetNotFoundException(Long betId) {
    super("Bet " + betId + " not found");
  }
}
