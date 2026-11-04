package com.sporty.settlement.adapter.in.web;

import com.sporty.settlement.application.port.in.CreateBetUseCase.CreateBetCommand;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

record CreateBetRequest(
        @NotBlank String userId,
        @NotBlank String eventId,
        @NotBlank String eventMarketId,
        @NotBlank String eventWinnerId,
        @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount) {

    CreateBetCommand toCommand() {
        return new CreateBetCommand(userId, eventId, eventMarketId, eventWinnerId, amount);
    }
}
