package com.sporty.settlement.config;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.persistence.BetRepository;
import java.math.BigDecimal;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnBooleanProperty(name = "app.seed.enabled")
class SeedDataConfig {

    private static final Logger log = LoggerFactory.getLogger(SeedDataConfig.class);

    @Bean
    ApplicationRunner seedBets(BetRepository betRepository) {
        return args -> {
            List<Bet> bets = List.of(
                    Bet.place("user-1", "EVT-1001", "MKT-MATCH-WINNER", "TEAM-RMA", new BigDecimal("25.00")),
                    Bet.place("user-2", "EVT-1001", "MKT-MATCH-WINNER", "TEAM-FCB", new BigDecimal("10.00")),
                    Bet.place("user-3", "EVT-1001", "MKT-MATCH-WINNER", "TEAM-RMA", new BigDecimal("50.00")),
                    Bet.place("user-1", "EVT-2002", "MKT-MATCH-WINNER", "TEAM-LAL", new BigDecimal("15.00")),
                    Bet.place("user-4", "EVT-2002", "MKT-MATCH-WINNER", "TEAM-BOS", new BigDecimal("40.00")));
            bets.forEach(betRepository::save);
            log.info("Seeded {} pending bets for events EVT-1001 and EVT-2002", bets.size());
        };
    }
}
