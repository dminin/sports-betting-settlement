package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.application.port.in.PublishEventOutcomeUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-outcomes")
class EventOutcomeController {

    private final PublishEventOutcomeUseCase publishEventOutcomeUseCase;

    EventOutcomeController(PublishEventOutcomeUseCase publishEventOutcomeUseCase) {
        this.publishEventOutcomeUseCase = publishEventOutcomeUseCase;
    }

    /**
     * Publishes the outcome to Kafka. Returns 202 because bets are settled asynchronously.
     */
    @PostMapping
    ResponseEntity<EventOutcomeRequest> publish(@Valid @RequestBody EventOutcomeRequest request) {
        publishEventOutcomeUseCase.publish(request.toDomain());
        return ResponseEntity.accepted().body(request);
    }
}
