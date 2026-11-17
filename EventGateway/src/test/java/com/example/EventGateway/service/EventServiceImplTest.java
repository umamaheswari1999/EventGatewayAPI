package com.example.EventGateway.service;

import com.example.EventGateway.dto.EventDto;
import com.example.EventGateway.entity.Event;
import com.example.EventGateway.exception.ServiceUnavailableException;
import com.example.EventGateway.mapper.EventGatewayMapper;
import com.example.EventGateway.repo.EventGatewayRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class EventServiceImplTest {

    private final EventGatewayRepo repository = mock(EventGatewayRepo.class);
    private final EventGatewayMapper mapper = new EventGatewayMapper();
    private final EventIngestionService ingestionService = mock(EventIngestionService.class);
    private EventServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new EventServiceImpl(repository, mapper, ingestionService);
    }

    @Test
    void createEventSavesAndForwardsEvent() {
        EventDto request = eventDto();
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResponseEntity<String> response = service.createEvent(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Event created successfully", response.getBody());
        verify(repository).save(argThat(saved ->
                saved.getEventId().equals("event-1")
                        && saved.getAccountId().equals("account-1")
                        && saved.getAmount().compareTo(new BigDecimal("12.50")) == 0));
        verify(ingestionService).processAndForwardEvent(any(Event.class));
    }

    @Test
    void createEventPropagatesForwardingFailure() {
        when(repository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new ServiceUnavailableException("Account service unavailable"))
                .when(ingestionService).processAndForwardEvent(any(Event.class));

        ServiceUnavailableException exception = assertThrows(
                ServiceUnavailableException.class,
                () -> service.createEvent(eventDto()));

        assertEquals("Account service unavailable", exception.getMessage());
        verify(repository).save(any(Event.class));
        verify(ingestionService).processAndForwardEvent(any(Event.class));
    }

    @Test
    void getEventByIdReturnsMappedEventWhenFound() {
        when(repository.findById("event-1")).thenReturn(Optional.of(event()));

        ResponseEntity<EventDto> response = service.getEntityById("event-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("event-1", response.getBody().getEventId());
        verify(repository).findById("event-1");
    }

    @Test
    void getEventByIdReturnsNotFoundWhenMissing() {
        when(repository.findById("missing")).thenReturn(Optional.empty());

        ResponseEntity<EventDto> response = service.getEntityById("missing");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void getEventsByAccountReturnsMappedEventsWhenFound() {
        when(repository.findByAccountIdOrderByEventTimestampAsc("account-1"))
                .thenReturn(List.of(event()));

        ResponseEntity<List<EventDto>> response = service.getEventsByAccountId("account-1");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
        assertEquals("event-1", response.getBody().get(0).getEventId());
        verify(repository).findByAccountIdOrderByEventTimestampAsc("account-1");
    }

    @Test
    void getEventsByAccountReturnsNotFoundWhenEmpty() {
        when(repository.findByAccountIdOrderByEventTimestampAsc("empty-account"))
                .thenReturn(List.of());

        ResponseEntity<List<EventDto>> response = service.getEventsByAccountId("empty-account");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    private EventDto eventDto() {
        return EventDto.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp("2025-01-02T03:04:05Z")
                .build();
    }

    private Event event() {
        return Event.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp(Instant.parse("2025-01-02T03:04:05Z"))
                .build();
    }
}
