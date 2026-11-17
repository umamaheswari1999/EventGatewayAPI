package com.example.EventGateway.controller;

import com.example.EventGateway.dto.EventDto;
import com.example.EventGateway.exception.GlobalExceptionHandler;
import com.example.EventGateway.exception.ServiceUnavailableException;
import com.example.EventGateway.port.EventGatewayService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EventGatewayControllerTest {

    private final EventGatewayService service = mock(EventGatewayService.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EventGatewayController(service))
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void createEventReturnsSuccess() throws Exception {
        when(service.createEvent(any(EventDto.class)))
                .thenReturn(ResponseEntity.created(null).body("Event created successfully"));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson()))
                .andExpect(status().isCreated())
                .andExpect(content().string("Event created successfully"));

        verify(service).createEvent(any(EventDto.class));
    }

    @Test
    void createEventRejectsInvalidFields() throws Exception {
        ObjectNode valid = (ObjectNode) objectMapper.readTree(validEventJson());
        List<ObjectNode> invalidEvents = new ArrayList<>();
        invalidEvents.add(valid.deepCopy().putNull("eventId"));
        invalidEvents.add(valid.deepCopy().put("eventId", ""));
        invalidEvents.add(valid.deepCopy().putNull("accountId"));
        invalidEvents.add(valid.deepCopy().put("accountId", " "));
        invalidEvents.add(valid.deepCopy().putNull("type"));
        invalidEvents.add(valid.deepCopy().put("type", "TRANSFER"));
        invalidEvents.add(valid.deepCopy().put("type", ""));
        invalidEvents.add(valid.deepCopy().putNull("amount"));
        invalidEvents.add(valid.deepCopy().put("amount", 0));
        invalidEvents.add(valid.deepCopy().put("amount", -1));
        invalidEvents.add(valid.deepCopy().putNull("currency"));
        invalidEvents.add(valid.deepCopy().put("currency", ""));
        invalidEvents.add(valid.deepCopy().putNull("eventTimestamp"));
        invalidEvents.add(valid.deepCopy().put("eventTimestamp", ""));
        invalidEvents.add(valid.deepCopy().put("eventTimestamp", "not-a-timestamp"));

        for (ObjectNode invalidEvent : invalidEvents) {
            mockMvc.perform(post("/events")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalidEvent)))
                    .andExpect(status().isBadRequest());
        }

        verifyNoInteractions(service);
    }

    @Test
    void createEventRejectsMalformedJson() throws Exception {
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    @Test
    void createEventReturnsServiceUnavailableWhenAccountServiceIsDown() throws Exception {
        when(service.createEvent(any(EventDto.class)))
                .thenThrow(new ServiceUnavailableException("Account Service is currently unavailable."));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson()))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value("Account Service is currently unavailable."));

        verify(service).createEvent(any(EventDto.class));
    }

    @Test
    void createEventReturnsConflictForDatabaseIntegrityErrors() throws Exception {
        when(service.createEvent(any(EventDto.class)))
                .thenThrow(new DataIntegrityViolationException("database constraint details"));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validEventJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.detail").value("The request conflicts with existing data."));

        verify(service).createEvent(any(EventDto.class));
    }

    @Test
    void getEventByIdReturnsEvent() throws Exception {
        when(service.getEntityById("event-1"))
                .thenReturn(ResponseEntity.ok(event()));

        mockMvc.perform(get("/events/event-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.eventId").value("event-1"))
                .andExpect(jsonPath("$.accountId").value("account-1"));

        verify(service).getEntityById("event-1");
    }

    @Test
    void getEventByIdReturnsNotFound() throws Exception {
        when(service.getEntityById("missing")).thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/events/missing"))
                .andExpect(status().isNotFound());

        verify(service).getEntityById("missing");
    }

    @Test
    void getEventsByAccountReturnsEvents() throws Exception {
        when(service.getEventsByAccountId("account-1"))
                .thenReturn(ResponseEntity.ok(List.of(event())));

        mockMvc.perform(get("/events").queryParam("account", "account-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].eventId").value("event-1"));

        verify(service).getEventsByAccountId("account-1");
    }

    @Test
    void getEventsByAccountReturnsNotFound() throws Exception {
        when(service.getEventsByAccountId("empty-account"))
                .thenReturn(ResponseEntity.notFound().build());

        mockMvc.perform(get("/events").queryParam("account", "empty-account"))
                .andExpect(status().isNotFound());

        verify(service).getEventsByAccountId("empty-account");
    }

    @Test
    void getEventsByAccountRejectsMissingAccount() throws Exception {
        mockMvc.perform(get("/events"))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(service);
    }

    private EventDto event() {
        return EventDto.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp("2025-01-02T03:04:05Z")
                .build();
    }

    private String validEventJson() throws Exception {
        return objectMapper.writeValueAsString(event());
    }
}
