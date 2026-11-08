package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;
import static com.sporty.settlement.domain.Preconditions.requireNonNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A user's stake on a participant ({@code eventWinnerId}) winning an event. A bet starts {@link
 * BetStatus#PENDING} and is settled exactly once.
 */
public final class Bet {

  private final Long id;
  private final String userId;
  private final String eventId;
  private final String eventMarketId;
  private final String eventWinnerId;
  private final BigDecimal amount;
  private final Long version;
  private BetStatus status;
  private Instant settledAt;

  private Bet(
      Long id,
      String userId,
      String eventId,
      String eventMarketId,
      String eventWinnerId,
      BigDecimal amount,
      BetStatus status,
      Instant settledAt,
      Long version) {
    this.id = id;
    this.userId = requireNonBlank(userId, "userId");
    this.eventId = requireNonBlank(eventId, "eventId");
    this.eventMarketId = requireNonBlank(eventMarketId, "eventMarketId");
    this.eventWinnerId = requireNonBlank(eventWinnerId, "eventWinnerId");
    this.amount = requirePositive(amount);
    this.status = requireNonNull(status, "status");
    this.settledAt = settledAt;
    this.version = version;
  }

  /** Creates a new pending bet from a user's validated stake details. */
  public static Bet place(
      String userId,
      String eventId,
      String eventMarketId,
      String eventWinnerId,
      BigDecimal amount) {
    return new Bet(
        null, userId, eventId, eventMarketId, eventWinnerId, amount, BetStatus.PENDING, null, null);
  }

  /** Rebuilds a bet from persisted state; {@code version} is an opaque optimistic-locking token. */
  public static Bet restore(
      Long id,
      String userId,
      String eventId,
      String eventMarketId,
      String eventWinnerId,
      BigDecimal amount,
      BetStatus status,
      Instant settledAt,
      Long version) {
    return new Bet(
        id, userId, eventId, eventMarketId, eventWinnerId, amount, status, settledAt, version);
  }

  /**
   * Settles this bet as WON or LOST. An already settled bet is left unchanged, so redelivered
   * requests are safe.
   *
   * @return {@code true} if this call settled the bet; {@code false} if it was already settled
   */
  public boolean settle(BetSettlementCommand command, Instant at) {
    final Instant settledAtValue = requireNonNull(at, "settledAt");
    if (status.isSettled()) {
      return false;
    }
    if (id != null && !id.equals(command.betId())) {
      throw new IllegalArgumentException(
          "Settlement targets bet " + command.betId() + ", but this bet is " + id);
    }
    if (!eventId.equals(command.eventId())) {
      throw new IllegalArgumentException(
          "Bet " + id + " belongs to event " + eventId + ", not " + command.eventId());
    }
    this.status = eventWinnerId.equals(command.outcomeWinnerId()) ? BetStatus.WON : BetStatus.LOST;
    this.settledAt = settledAtValue;
    return true;
  }

  public boolean isSettled() {
    return status.isSettled();
  }

  /** Returns the persistent identifier, or {@code null} before the bet is stored. */
  public Long id() {
    return id;
  }

  /** Returns the identifier of the user who placed the bet. */
  public String userId() {
    return userId;
  }

  /** Returns the identifier of the event on which the bet was placed. */
  public String eventId() {
    return eventId;
  }

  /** Returns the identifier of the event market. */
  public String eventMarketId() {
    return eventMarketId;
  }

  /** Returns the participant selected to win. */
  public String eventWinnerId() {
    return eventWinnerId;
  }

  /** Returns the user's stake. */
  public BigDecimal amount() {
    return amount;
  }

  /** Returns the current settlement status. */
  public BetStatus status() {
    return status;
  }

  /** Returns the settlement time, or {@code null} while the bet is pending. */
  public Instant settledAt() {
    return settledAt;
  }

  /** Returns the optimistic-locking token, or {@code null} before persistence. */
  public Long version() {
    return version;
  }

  private static BigDecimal requirePositive(BigDecimal amount) {
    if (amount == null || amount.signum() <= 0) {
      throw new IllegalArgumentException("amount must be greater than 0");
    }
    return amount;
  }
}
