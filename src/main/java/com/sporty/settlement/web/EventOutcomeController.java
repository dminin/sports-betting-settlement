package com.sporty.settlement.web;

import com.sporty.settlement.service.PublishEventOutcomeService;
import com.sporty.settlement.web.dto.EventOutcomeRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-outcomes")
class EventOutcomeController {

    private final PublishEventOutcomeService publishEventOutcomeService;

    EventOutcomeController(PublishEventOutcomeService publishEventOutcomeService) {
        this.publishEventOutcomeService = publishEventOutcomeService;
    }

    /**
     * Publishes the outcome to Kafka. Returns 202 because bets are settled asynchronously.
     */
    @PostMapping
    ResponseEntity<EventOutcomeRequest> publish(@Valid @RequestBody EventOutcomeRequest request) {
        publishEventOutcomeService.publish(request.toDomain());
        return ResponseEntity.accepted().body(request);
    }
}
