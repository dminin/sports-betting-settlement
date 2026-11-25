package com.sporty.settlement.messaging.dto;

import com.sporty.settlement.domain.EventOutcome;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

class EventOutcomeMessageTest {

    private static final String OUTCOME_JSON =
            """
            {"eventId":"EVT-1","eventName":"A vs B","eventWinnerId":"TEAM-A"}
            """;

    private final JsonMapper jsonMapper = JsonMapper.builder().build();

    @Test
    void serializesOnlyTheThreeRequiredOutcomeFields() {
        EventOutcomeMessage message =
                EventOutcomeMessage.from(new EventOutcome("EVT-1", "A vs B", "TEAM-A"));

        assertThat(jsonMapper.readTree(jsonMapper.writeValueAsString(message)))
                .isEqualTo(jsonMapper.readTree(OUTCOME_JSON));
    }

    @Test
    void deserializesAnOutcomeWithoutAMarket() {
        EventOutcomeMessage message =
                jsonMapper.readValue(OUTCOME_JSON, EventOutcomeMessage.class);

        assertThat(message.toDomain()).isEqualTo(new EventOutcome("EVT-1", "A vs B", "TEAM-A"));
    }
}
