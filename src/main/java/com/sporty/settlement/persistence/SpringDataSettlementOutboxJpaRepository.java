package com.sporty.settlement.persistence;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataSettlementOutboxJpaRepository
        extends JpaRepository<SettlementOutboxJpaEntity, Long> {

    List<SettlementOutboxJpaEntity> findByOrderById(Limit limit);
}
