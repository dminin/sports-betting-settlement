package com.sporty.settlement;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetStatus;
import com.sporty.settlement.service.BetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Full flow against a real Kafka broker: REST -> Kafka -> matching -> bet-settlements ->
 * settlement. Subclasses choose how {@code bet-settlements} is delivered. Each subclass gets its
 * own context and brokers, so the context is closed before the containers of the next subclass
 * start.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext
@Testcontainers(disabledWithoutDocker = true)
abstract class AbstractBetSettlementFlowTest {

    @Container
    @ServiceConnection
    static final KafkaContainer KAFKA = new KafkaContainer("apache/kafka:4.2.2");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BetService betService;

    private static Map<String, Bet> byUser(List<Bet> bets) {
        return bets.stream().collect(Collectors.toMap(Bet::userId, Function.identity()));
    }

    @Test
    void publishedOutcomeSettlesPendingBetsOfTheEventMarket() throws Exception {
        createBet("user-a", "EVT-IT-1", "MKT-1", "TEAM-X");
        createBet("user-b", "EVT-IT-1", "MKT-1", "TEAM-Y");
        createBet("user-c", "EVT-IT-1", "MKT-2", "TEAM-X");
        createBet("user-d", "EVT-IT-2", "MKT-1", "TEAM-X");

        mockMvc
            .perform(
                post("/api/event-outcomes")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {"eventId":"EVT-IT-1","eventMarketId":"MKT-1","eventName":"X vs Y",
                             "eventWinnerId":"TEAM-X"}\
                            """))
            .andExpect(status().isAccepted());

        await()
            .atMost(Duration.ofSeconds(60))
            .untilAsserted(() -> {
                Map<String, Bet> betsByUser = byUser(betService.find("EVT-IT-1", null));
                assertThat(betsByUser.get("user-a").status()).isEqualTo(BetStatus.WON);
                assertThat(betsByUser.get("user-a").settledAt()).isNotNull();
                assertThat(betsByUser.get("user-b").status()).isEqualTo(BetStatus.LOST);
                assertThat(betsByUser.get("user-b").settledAt()).isNotNull();
            });
        assertThat(byUser(betService.find("EVT-IT-1", null)).get("user-c").status())
            .isEqualTo(BetStatus.PENDING);
        assertThat(betService.find("EVT-IT-2", null))
            .singleElement()
            .extracting(Bet::status)
            .isEqualTo(BetStatus.PENDING);
    }

    private void createBet(String userId, String eventId, String marketId, String winnerId)
        throws Exception {
        mockMvc
            .perform(
                post("/api/bets")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        """
                            {"userId":"%s","eventId":"%s","eventMarketId":"%s",
                             "eventWinnerId":"%s","amount":10.00}\
                            """
                            .formatted(userId, eventId, marketId, winnerId)))
            .andExpect(status().isCreated());
    }
}
