package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.application.port.in.BetQueryUseCase;
import com.sporty.settlement.application.port.in.CreateBetUseCase;
import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/bets")
class BetController {

    private final BetQueryUseCase betQueryUseCase;
    private final CreateBetUseCase createBetUseCase;

    BetController(BetQueryUseCase betQueryUseCase, CreateBetUseCase createBetUseCase) {
        this.betQueryUseCase = betQueryUseCase;
        this.createBetUseCase = createBetUseCase;
    }

    @GetMapping
    List<BetResponse> find(@RequestParam(required = false) String eventId,
                           @RequestParam(required = false) BetStatus status) {
        return betQueryUseCase.find(eventId, status).stream().map(BetResponse::from).toList();
    }

    @GetMapping("/{id}")
    BetResponse getById(@PathVariable Long id) {
        return BetResponse.from(betQueryUseCase.getById(id));
    }

    @PostMapping
    ResponseEntity<BetResponse> create(@Valid @RequestBody CreateBetRequest request) {
        Bet bet = createBetUseCase.create(request.toCommand());
        return ResponseEntity.created(URI.create("/api/bets/" + bet.id())).body(BetResponse.from(bet));
    }
}
