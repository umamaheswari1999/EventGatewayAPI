package com.example.EventGateway.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class IncomingEventDtoTest {

    @Test
    void recordConstructorAccessorsEqualityHashCodeAndToStringWork() {
        Instant timestamp = Instant.parse("2025-01-02T03:04:05Z");
        Map<String, Object> metadata = Map.of("source", "mobile");
        IncomingEventDto request = new IncomingEventDto(
                "event-1", "account-1", "DEBIT", new BigDecimal("8.75"), "USD", timestamp, metadata);
        IncomingEventDto equal = new IncomingEventDto(
                "event-1", "account-1", "DEBIT", new BigDecimal("8.75"), "USD", timestamp, metadata);
        IncomingEventDto different = new IncomingEventDto(
                "event-2", "account-1", "DEBIT", new BigDecimal("8.75"), "USD", timestamp, metadata);

        assertEquals("event-1", request.eventId());
        assertEquals("account-1", request.accountId());
        assertEquals("DEBIT", request.type());
        assertEquals(new BigDecimal("8.75"), request.amount());
        assertEquals("USD", request.currency());
        assertEquals(timestamp, request.eventTimestamp());
        assertEquals(metadata, request.metadata());
        assertEquals(request, request);
        assertEquals(request, equal);
        assertEquals(request.hashCode(), equal.hashCode());
        assertNotEquals(request, different);
        assertNotEquals(request, null);
        assertNotEquals(request, "request");
        assertTrue(request.toString().contains("eventId=event-1"));
    }

    @Test
    void recordHandlesNullComponents() {
        IncomingEventDto empty = new IncomingEventDto(null, null, null, null, null, null, null);
        IncomingEventDto anotherEmpty = new IncomingEventDto(null, null, null, null, null, null, null);

        assertEquals(empty, anotherEmpty);
        assertEquals(empty.hashCode(), anotherEmpty.hashCode());
        assertNotEquals(empty, new IncomingEventDto(
                "event-1", null, null, null, null, null, null));
        assertTrue(empty.toString().contains("eventId=null"));
    }
}
