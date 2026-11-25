package com.sporty.settlement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class BetSettlementApplication {

    public static void main(String[] args) {
        SpringApplication.run(BetSettlementApplication.class, args);
    }
}
