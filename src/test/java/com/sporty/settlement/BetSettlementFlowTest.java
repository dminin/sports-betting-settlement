package com.sporty.settlement;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.service.BetService;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

/**
 * Full flow against a real Kafka broker: REST -> Kafka -> matching -> settlement (RocketMQ mocked in-process).
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("mock")
@Testcontainers(disabledWithoutDocker = true)
class BetSettlementFlowTest {

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.2.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BetService betService;

    @Test
    void publishedOutcomeSettlesPendingBetsOfTheEvent() throws Exception {
        createBet("user-a", "EVT-IT-1", "TEAM-X");
        createBet("user-b", "EVT-IT-1", "TEAM-Y");
        createBet("user-c", "EVT-IT-2", "TEAM-X");

        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventId":"EVT-IT-1","eventName":"X vs Y","eventWinnerId":"TEAM-X"}"""))
                .andExpect(status().isAccepted());

        await().atMost(Duration.ofSeconds(30)).untilAsserted(() -> {
            Map<String, Bet> betsByUser = byUser(betService.find("EVT-IT-1", null));
            assertThat(betsByUser.get("user-a").status()).isEqualTo(BetStatus.WON);
            assertThat(betsByUser.get("user-b").status()).isEqualTo(BetStatus.LOST);
            assertThat(betsByUser.values()).allSatisfy(bet -> assertThat(bet.settledAt()).isNotNull());
        });
        assertThat(betService.find("EVT-IT-2", null))
                .singleElement()
                .extracting(Bet::status)
                .isEqualTo(BetStatus.PENDING);
    }

    private void createBet(String userId, String eventId, String winnerId) throws Exception {
        mockMvc.perform(post("/api/bets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":"%s","eventId":"%s","eventMarketId":"MKT-1","eventWinnerId":"%s","amount":10.00}"""
                                .formatted(userId, eventId, winnerId)))
                .andExpect(status().isCreated());
    }

    private static Map<String, Bet> byUser(List<Bet> bets) {
        return bets.stream().collect(Collectors.toMap(Bet::userId, Function.identity()));
    }
}
