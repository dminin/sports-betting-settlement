package com.sporty.settlement.persistence;

import com.sporty.settlement.domain.BetStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataBetJpaRepository extends JpaRepository<BetJpaEntity, Long> {

    List<BetJpaEntity> findByEventIdAndStatusOrderById(String eventId, BetStatus status);

    @Query("""
            select b from BetJpaEntity b
            where (:eventId is null or b.eventId = :eventId)
              and (:status is null or b.status = :status)
            order by b.id
            """)
    List<BetJpaEntity> search(@Param("eventId") String eventId, @Param("status") BetStatus status);
}
