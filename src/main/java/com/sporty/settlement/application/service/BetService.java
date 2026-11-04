package com.sporty.settlement.application.service;

import com.sporty.settlement.application.BetNotFoundException;
import com.sporty.settlement.application.port.in.BetQueryUseCase;
import com.sporty.settlement.application.port.in.CreateBetUseCase;
import com.sporty.settlement.application.port.out.BetRepository;
import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class BetService implements BetQueryUseCase, CreateBetUseCase {

    private final BetRepository betRepository;

    BetService(BetRepository betRepository) {
        this.betRepository = betRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public Bet getById(Long id) {
        return betRepository.findById(id).orElseThrow(() -> new BetNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Bet> find(String eventId, BetStatus status) {
        return betRepository.find(eventId, status);
    }

    @Override
    @Transactional
    public Bet create(CreateBetCommand command) {
        return betRepository.save(Bet.place(command.userId(), command.eventId(), command.eventMarketId(),
                command.eventWinnerId(), command.amount()));
    }
}
