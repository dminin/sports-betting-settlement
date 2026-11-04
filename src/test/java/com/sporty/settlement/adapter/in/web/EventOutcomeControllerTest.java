package com.sporty.settlement.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sporty.settlement.application.port.in.BetQueryUseCase;
import com.sporty.settlement.application.port.in.CreateBetUseCase;
import com.sporty.settlement.application.port.in.PublishEventOutcomeUseCase;
import com.sporty.settlement.application.port.out.MessagePublishingException;
import com.sporty.settlement.domain.EventOutcome;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
class EventOutcomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PublishEventOutcomeUseCase publishEventOutcomeUseCase;

    @MockitoBean
    private BetQueryUseCase betQueryUseCase;

    @MockitoBean
    private CreateBetUseCase createBetUseCase;

    @Test
    void acceptsValidOutcome() throws Exception {
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventId":"EVT-1","eventName":"A vs B","eventWinnerId":"TEAM-A"}"""))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.eventId").value("EVT-1"));

        verify(publishEventOutcomeUseCase).publish(new EventOutcome("EVT-1", "A vs B", "TEAM-A"));
    }

    @Test
    void rejectsOutcomeWithMissingWinner() throws Exception {
        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventId":"EVT-1","eventName":"A vs B"}"""))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(publishEventOutcomeUseCase, never()).publish(any());
    }

    @Test
    void returnsServiceUnavailableWhenBrokerIsDown() throws Exception {
        willThrow(new MessagePublishingException("down", null)).given(publishEventOutcomeUseCase).publish(any());

        mockMvc.perform(post("/api/event-outcomes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventId":"EVT-1","eventName":"A vs B","eventWinnerId":"TEAM-A"}"""))
                .andExpect(status().isServiceUnavailable());
    }
}
