package com.sporty.settlement.web.dto;

import com.sporty.settlement.service.dto.CreateBetCommand;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

/** Request body for placing a bet. */
public record CreateBetRequest(
    @NotBlank @Size(max = 255) String userId,
    @NotBlank @Size(max = 255) String eventId,
    @NotBlank @Size(max = 255) String eventMarketId,
    @NotBlank @Size(max = 255) String eventWinnerId,
    @NotNull @Positive @Digits(integer = 17, fraction = 2) BigDecimal amount) {

  /** Converts this request to a service command. */
  public CreateBetCommand toCommand() {
    return new CreateBetCommand(userId, eventId, eventMarketId, eventWinnerId, amount);
  }
}
