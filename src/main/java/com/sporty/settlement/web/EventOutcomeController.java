package com.sporty.settlement.web;

import com.sporty.settlement.service.PublishEventOutcomeService;
import com.sporty.settlement.web.dto.EventOutcomeRequest;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/event-outcomes")
@RequiredArgsConstructor(access = AccessLevel.PACKAGE)
class EventOutcomeController {

    private final PublishEventOutcomeService publishEventOutcomeService;

    /**
     * Publishes the outcome to Kafka. Returns 202 because bets are settled asynchronously.
     */
    @PostMapping
    ResponseEntity<EventOutcomeRequest> publish(@Valid @RequestBody EventOutcomeRequest request) {
        publishEventOutcomeService.publish(request.toDomain());
        return ResponseEntity.accepted().body(request);
    }
}
