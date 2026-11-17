package com.example.EventGateway.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class AccountTransactionRequestTest {

    @Test
    void recordConstructorAccessorsEqualityHashCodeAndToStringWork() {
        Instant timestamp = Instant.parse("2025-01-02T03:04:05Z");
        AccountTransactionRequest request =
                new AccountTransactionRequest("event-1", new BigDecimal("4.50"), "CREDIT", timestamp);
        AccountTransactionRequest equal =
                new AccountTransactionRequest("event-1", new BigDecimal("4.50"), "CREDIT", timestamp);
        AccountTransactionRequest different =
                new AccountTransactionRequest("event-2", new BigDecimal("4.50"), "CREDIT", timestamp);

        assertEquals("event-1", request.eventId());
        assertEquals(new BigDecimal("4.50"), request.amount());
        assertEquals("CREDIT", request.type());
        assertEquals(timestamp, request.eventTimestamp());
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
        AccountTransactionRequest empty = new AccountTransactionRequest(null, null, null, null);
        AccountTransactionRequest anotherEmpty = new AccountTransactionRequest(null, null, null, null);

        assertEquals(empty, anotherEmpty);
        assertEquals(empty.hashCode(), anotherEmpty.hashCode());
        assertNotEquals(empty, new AccountTransactionRequest("event-1", null, null, null));
        assertTrue(empty.toString().contains("eventId=null"));
    }
}
