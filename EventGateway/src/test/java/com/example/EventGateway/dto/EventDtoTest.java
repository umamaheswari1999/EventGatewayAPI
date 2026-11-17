package com.example.EventGateway.dto;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventDtoTest {

    @Test
    void noArgsConstructorGettersAndSettersCoverEveryField() {
        EventDto dto = new EventDto();
        dto.setEventId("event-1");
        dto.setAccountId("account-1");
        dto.setType("CREDIT");
        dto.setAmount(new BigDecimal("12.50"));
        dto.setCurrency("USD");
        dto.setEventTimestamp("2025-01-02T03:04:05Z");
        dto.setMetadata(Map.of("source", "mobile"));

        assertEquals("event-1", dto.getEventId());
        assertEquals("account-1", dto.getAccountId());
        assertEquals("CREDIT", dto.getType());
        assertEquals(new BigDecimal("12.50"), dto.getAmount());
        assertEquals("USD", dto.getCurrency());
        assertEquals("2025-01-02T03:04:05Z", dto.getEventTimestamp());
        assertEquals(Map.of("source", "mobile"), dto.getMetadata());
    }

    @Test
    void allArgsConstructorAndBuilderPopulateEveryField() {
        EventDto fromConstructor = new EventDto(
                "event-1",
                "account-1",
                "DEBIT",
                new BigDecimal("3.25"),
                "EUR",
                "2025-02-03T04:05:06Z",
                Map.of("source", "web"));
        EventDto fromBuilder = EventDto.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("DEBIT")
                .amount(new BigDecimal("3.25"))
                .currency("EUR")
                .eventTimestamp("2025-02-03T04:05:06Z")
                .metadata(Map.of("source", "web"))
                .build();

        assertEquals(fromConstructor, fromBuilder);
    }

    @Test
    void generatedEqualityHashCodeAndToStringHandleObjects() {
        EventDto first = dto();
        EventDto equal = dto();
        EventDto different = EventDto.builder()
                .eventId("event-2")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp("2025-01-02T03:04:05Z")
                .metadata(Map.of("source", "mobile"))
                .build();

        assertEquals(first, first);
        assertEquals(first, equal);
        assertEquals(first.hashCode(), equal.hashCode());
        assertNotEquals(first, different);
        assertNotEquals(first, null);
        assertNotEquals(first, "event");
        assertTrue(first.toString().contains("eventId=event-1"));
    }

    @Test
    void generatedEqualityAndHashCodeHandleNullFields() {
        EventDto empty = new EventDto();
        EventDto anotherEmpty = new EventDto();

        assertEquals(empty, anotherEmpty);
        assertEquals(empty.hashCode(), anotherEmpty.hashCode());
        assertNotEquals(empty, dto());
        assertTrue(empty.toString().contains("eventId=null"));
    }

    private EventDto dto() {
        return EventDto.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp("2025-01-02T03:04:05Z")
                .metadata(Map.of("source", "mobile"))
                .build();
    }
}
