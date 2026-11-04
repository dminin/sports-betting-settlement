package com.sporty.settlement.domain;

import static com.sporty.settlement.domain.Preconditions.requireNonBlank;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;

/**
 * A user's stake on a participant ({@code eventWinnerId}) winning an event.
 * A bet starts {@link BetStatus#PENDING} and is settled exactly once.
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

    private Bet(Long id, String userId, String eventId, String eventMarketId, String eventWinnerId,
                BigDecimal amount, BetStatus status, Instant settledAt, Long version) {
        this.id = id;
        this.userId = requireNonBlank(userId, "userId");
        this.eventId = requireNonBlank(eventId, "eventId");
        this.eventMarketId = requireNonBlank(eventMarketId, "eventMarketId");
        this.eventWinnerId = requireNonBlank(eventWinnerId, "eventWinnerId");
        this.amount = requirePositive(amount);
        this.status = Objects.requireNonNull(status, "status must not be null");
        this.settledAt = settledAt;
        this.version = version;
    }

    public static Bet place(String userId, String eventId, String eventMarketId, String eventWinnerId,
                            BigDecimal amount) {
        return new Bet(null, userId, eventId, eventMarketId, eventWinnerId, amount, BetStatus.PENDING, null, null);
    }

    /**
     * Rebuilds a bet from persisted state; {@code version} is an opaque optimistic-locking token.
     */
    public static Bet restore(Long id, String userId, String eventId, String eventMarketId, String eventWinnerId,
                              BigDecimal amount, BetStatus status, Instant settledAt, Long version) {
        return new Bet(id, userId, eventId, eventMarketId, eventWinnerId, amount, status, settledAt, version);
    }

    public void settle(BetSettlementCommand command, Instant at) {
        if (status.isSettled()) {
            throw new BetAlreadySettledException(id, status);
        }
        if (!eventId.equals(command.eventId())) {
            throw new IllegalArgumentException(
                    "Bet " + id + " belongs to event " + eventId + ", not " + command.eventId());
        }
        this.status = eventWinnerId.equals(command.outcomeWinnerId()) ? BetStatus.WON : BetStatus.LOST;
        this.settledAt = Objects.requireNonNull(at, "settledAt must not be null");
    }

    public boolean isSettled() {
        return status.isSettled();
    }

    public Long id() {
        return id;
    }

    public String userId() {
        return userId;
    }

    public String eventId() {
        return eventId;
    }

    public String eventMarketId() {
        return eventMarketId;
    }

    public String eventWinnerId() {
        return eventWinnerId;
    }

    public BigDecimal amount() {
        return amount;
    }

    public BetStatus status() {
        return status;
    }

    public Instant settledAt() {
        return settledAt;
    }

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
