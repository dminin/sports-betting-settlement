package com.sporty.settlement.web;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.service.BetService;
import com.sporty.settlement.web.dto.BetResponse;
import com.sporty.settlement.web.dto.CreateBetRequest;
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

    private final BetService betService;

    BetController(BetService betService) {
        this.betService = betService;
    }

    @GetMapping
    List<BetResponse> find(@RequestParam(required = false) String eventId,
                           @RequestParam(required = false) BetStatus status) {
        return betService.find(eventId, status).stream().map(BetResponse::from).toList();
    }

    @GetMapping("/{id}")
    BetResponse getById(@PathVariable Long id) {
        return BetResponse.from(betService.getById(id));
    }

    @PostMapping
    ResponseEntity<BetResponse> create(@Valid @RequestBody CreateBetRequest request) {
        Bet bet = betService.create(request.toCommand());
        return ResponseEntity.created(URI.create("/api/bets/" + bet.id())).body(BetResponse.from(bet));
    }
}
