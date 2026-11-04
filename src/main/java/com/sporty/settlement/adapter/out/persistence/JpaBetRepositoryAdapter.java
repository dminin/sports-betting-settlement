package com.sporty.settlement.adapter.out.persistence;

import com.sporty.settlement.application.port.out.BetRepository;
import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaBetRepositoryAdapter implements BetRepository {

    private final SpringDataBetJpaRepository jpaRepository;

    JpaBetRepositoryAdapter(SpringDataBetJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Bet save(Bet bet) {
        return BetEntityMapper.toDomain(jpaRepository.save(BetEntityMapper.toEntity(bet)));
    }

    @Override
    public Optional<Bet> findById(Long id) {
        return jpaRepository.findById(id).map(BetEntityMapper::toDomain);
    }

    @Override
    public List<Bet> findPendingByEventId(String eventId) {
        return jpaRepository.findByEventIdAndStatusOrderById(eventId, BetStatus.PENDING).stream()
                .map(BetEntityMapper::toDomain)
                .toList();
    }

    @Override
    public List<Bet> find(String eventId, BetStatus status) {
        return jpaRepository.search(eventId, status).stream()
                .map(BetEntityMapper::toDomain)
                .toList();
    }
}
