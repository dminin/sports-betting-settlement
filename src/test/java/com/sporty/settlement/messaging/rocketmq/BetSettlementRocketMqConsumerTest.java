package com.sporty.settlement.messaging.rocketmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sporty.settlement.domain.Bet;
import com.sporty.settlement.domain.BetSettlementCommand;
import com.sporty.settlement.service.BetNotFoundException;
import com.sporty.settlement.service.SettleBetService;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import tools.jackson.databind.json.JsonMapper;

class BetSettlementRocketMqConsumerTest {

    private static final String VALID_BODY = """
            {"betId":1,"eventId":"EVT-1","outcomeWinnerId":"TEAM-A"}""";

    private SettleBetService settleBetService;
    private BetSettlementRocketMqConsumer consumer;

    @BeforeEach
    void setUp() {
        settleBetService = mock(SettleBetService.class);
        consumer = new BetSettlementRocketMqConsumer(settleBetService, JsonMapper.builder().build());
    }

    @Test
    void settlesBetAndAcknowledges() {
        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.SUCCESS);

        verify(settleBetService).settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"));
    }

    @Test
    void acknowledgesPermanentFailures() {
        assertThat(consumer.consume(message("not json"))).isEqualTo(ConsumeResult.SUCCESS);
        assertThat(consumer.consume(message("""
                {"betId":null,"eventId":"EVT-1","outcomeWinnerId":"TEAM-A"}""")))
                .isEqualTo(ConsumeResult.SUCCESS);
        verify(settleBetService, never()).settle(any());

        willThrow(new BetNotFoundException(1L)).given(settleBetService).settle(any());
        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.SUCCESS);
        verify(settleBetService).settle(any());
    }

    @Test
    void requestsRedeliveryOnTransientFailures() {
        willThrow(new ObjectOptimisticLockingFailureException(Bet.class, 1L))
                .given(settleBetService).settle(any());
        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.FAILURE);

        willThrow(new IllegalStateException("database unavailable")).given(settleBetService).settle(any());
        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.FAILURE);

        verify(settleBetService, times(2)).settle(any());
    }

    private static MessageView message(String body) {
        MessageView view = mock(MessageView.class);
        given(view.getBody()).willReturn(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer());
        return view;
    }
}
