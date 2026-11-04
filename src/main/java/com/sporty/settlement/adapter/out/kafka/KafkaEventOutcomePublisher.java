package com.sporty.settlement.adapter.out.kafka;

import com.sporty.settlement.adapter.messaging.EventOutcomeMessage;
import com.sporty.settlement.application.port.out.EventOutcomePublisher;
import com.sporty.settlement.application.port.out.MessagePublishingException;
import com.sporty.settlement.config.EventOutcomesTopicProperties;
import com.sporty.settlement.domain.EventOutcome;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
class KafkaEventOutcomePublisher implements EventOutcomePublisher {

    private static final long SEND_TIMEOUT_SECONDS = 10;

    private final KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate;
    private final String topic;

    KafkaEventOutcomePublisher(KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate,
                               EventOutcomesTopicProperties properties) {
        this.kafkaTemplate = kafkaTemplate;
        this.topic = properties.eventOutcomesTopic();
    }

    @Override
    public void publish(EventOutcome outcome) {
        try {
            kafkaTemplate.send(topic, outcome.eventId(), EventOutcomeMessage.from(outcome))
                    .get(SEND_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new MessagePublishingException("Interrupted while publishing outcome of " + outcome.eventId(), e);
        } catch (ExecutionException | TimeoutException e) {
            throw new MessagePublishingException("Failed to publish outcome of " + outcome.eventId(), e);
        }
    }
}
