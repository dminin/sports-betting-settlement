package com.sporty.settlement.adapter.in.rocketmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.sporty.settlement.application.BetNotFoundException;
import com.sporty.settlement.application.port.in.SettleBetUseCase;
import com.sporty.settlement.domain.BetSettlementCommand;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.apache.rocketmq.client.apis.consumer.ConsumeResult;
import org.apache.rocketmq.client.apis.message.MessageView;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

class BetSettlementRocketMqConsumerTest {

    private static final String VALID_BODY = """
            {"betId":1,"eventId":"EVT-1","outcomeWinnerId":"TEAM-A"}""";

    private SettleBetUseCase settleBetUseCase;
    private BetSettlementRocketMqConsumer consumer;

    @BeforeEach
    void setUp() {
        settleBetUseCase = mock(SettleBetUseCase.class);
        consumer = new BetSettlementRocketMqConsumer(settleBetUseCase, JsonMapper.builder().build());
    }

    @Test
    void settlesBetAndAcknowledges() {
        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.SUCCESS);

        verify(settleBetUseCase).settle(new BetSettlementCommand(1L, "EVT-1", "TEAM-A"));
    }

    @Test
    void acknowledgesMalformedMessageWithoutSettling() {
        assertThat(consumer.consume(message("not json"))).isEqualTo(ConsumeResult.SUCCESS);

        verify(settleBetUseCase, never()).settle(any());
    }

    @Test
    void acknowledgesMessageForUnknownBet() {
        willThrow(new BetNotFoundException(1L)).given(settleBetUseCase).settle(any());

        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.SUCCESS);
    }

    @Test
    void requestsRedeliveryOnTransientFailure() {
        willThrow(new IllegalStateException("database unavailable")).given(settleBetUseCase).settle(any());

        assertThat(consumer.consume(message(VALID_BODY))).isEqualTo(ConsumeResult.FAILURE);
    }

    private static MessageView message(String body) {
        MessageView view = mock(MessageView.class);
        given(view.getBody()).willReturn(ByteBuffer.wrap(body.getBytes(StandardCharsets.UTF_8)).asReadOnlyBuffer());
        return view;
    }
}
