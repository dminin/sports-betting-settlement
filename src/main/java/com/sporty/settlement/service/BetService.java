package com.sporty.settlement.service;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.persistence.BetRepository;
import com.sporty.settlement.service.dto.CreateBetCommand;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
public class BetService {

    private final BetRepository betRepository;

    /** Returns the requested bet or throws when it does not exist. */
    @Transactional(readOnly = true)
    public Bet getById(Long id) {
        return betRepository.findById(id).orElseThrow(() -> new BetNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<Bet> find(String eventId, BetStatus status) {
        return betRepository.find(eventId, status);
    }

    @Transactional
    public Bet create(CreateBetCommand command) {
        return betRepository.save(
                Bet.place(
                        command.userId(),
                        command.eventId(),
                        command.eventMarketId(),
                        command.eventWinnerId(),
                        command.amount()));
    }
}
