package com.sporty.settlement.messaging.kafka;

import com.sporty.settlement.config.EventOutcomesTopicProperties;
import com.sporty.settlement.domain.EventOutcome;
import com.sporty.settlement.messaging.MessagePublishingException;
import com.sporty.settlement.messaging.dto.EventOutcomeMessage;
import java.util.concurrent.ExecutionException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/** Sends event outcomes to Kafka and reports broker failures to the service layer. */
@Component
public class KafkaEventOutcomePublisher {

  private final KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate;
  private final String topic;

  KafkaEventOutcomePublisher(
      KafkaTemplate<String, EventOutcomeMessage> kafkaTemplate,
      EventOutcomesTopicProperties properties) {
    this.kafkaTemplate = kafkaTemplate;
    this.topic = properties.eventOutcomesTopic();
  }

  /**
   * Publishes the outcome and returns once the broker has acknowledged it. The wait is bounded by
   * the Kafka producer timeouts in {@code application.yml}.
   *
   * @throws MessagePublishingException if the broker did not acknowledge the message
   */
  public void publish(EventOutcome outcome) {
    try {
      kafkaTemplate.send(topic, outcome.eventId(), EventOutcomeMessage.from(outcome)).get();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      throw new MessagePublishingException(
          "Interrupted while publishing outcome of " + outcome.eventId(), e);
    } catch (ExecutionException e) {
      throw new MessagePublishingException("Failed to publish outcome of " + outcome.eventId(), e);
    }
  }
}
