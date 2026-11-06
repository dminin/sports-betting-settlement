package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.BetStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "bets", indexes = @Index(name = "idx_bets_event_status", columnList = "event_id, status"))
class BetJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;

    @Column(name = "event_id", nullable = false, length = 255)
    private String eventId;

    @Column(name = "event_market_id", nullable = false, length = 255)
    private String eventMarketId;

    @Column(name = "event_winner_id", nullable = false, length = 255)
    private String eventWinnerId;

    @Column(name = "amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private BetStatus status;

    @Column(name = "settled_at")
    private Instant settledAt;

    @Version
    private Long version;

    protected BetJpaEntity() {
    }

    BetJpaEntity(Long id, String userId, String eventId, String eventMarketId, String eventWinnerId,
                 BigDecimal amount, BetStatus status, Instant settledAt, Long version) {
        this.id = id;
        this.userId = userId;
        this.eventId = eventId;
        this.eventMarketId = eventMarketId;
        this.eventWinnerId = eventWinnerId;
        this.amount = amount;
        this.status = status;
        this.settledAt = settledAt;
        this.version = version;
    }

    Long getId() {
        return id;
    }

    String getUserId() {
        return userId;
    }

    String getEventId() {
        return eventId;
    }

    String getEventMarketId() {
        return eventMarketId;
    }

    String getEventWinnerId() {
        return eventWinnerId;
    }

    BigDecimal getAmount() {
        return amount;
    }

    BetStatus getStatus() {
        return status;
    }

    Instant getSettledAt() {
        return settledAt;
    }

    Long getVersion() {
        return version;
    }
}
