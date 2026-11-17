package com.example.EventGateway.mapper;

import com.example.EventGateway.dto.EventDto;
import com.example.EventGateway.entity.Event;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EventGatewayMapperTest {

    private final EventGatewayMapper mapper = new EventGatewayMapper();

    @Test
    void mapsEventDtoToEntityWithNullMetadata() {
        Event entity = mapper.toEntity(eventDto(null));

        assertEquals("event-1", entity.getEventId());
        assertEquals("account-1", entity.getAccountId());
        assertEquals(Instant.parse("2025-01-02T03:04:05Z"), entity.getEventTimestamp());
        assertNull(entity.getMetadata());
    }

    @Test
    void mapsMetadataValuesAndNullValuesToEntity() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("source", "mobile");
        metadata.put("optional", null);

        Event entity = mapper.toEntity(eventDto(metadata));

        assertEquals("mobile", entity.getMetadata().get("source"));
        assertTrue(entity.getMetadata().containsKey("optional"));
        assertNull(entity.getMetadata().get("optional"));
    }

    @Test
    void mapsEntityToResponseWithNullMetadata() {
        EventDto response = mapper.toResponseBody(entity(null));

        assertEquals("event-1", response.getEventId());
        assertEquals("2025-01-02T03:04:05Z", response.getEventTimestamp());
        assertNull(response.getMetadata());
    }

    @Test
    void mapsMetadataValuesAndNullValuesToResponse() {
        Map<String, String> metadata = new LinkedHashMap<>();
        metadata.put("source", "mobile");
        metadata.put("optional", null);

        EventDto response = mapper.toResponseBody(entity(metadata));

        assertEquals("mobile", response.getMetadata().get("source"));
        assertTrue(response.getMetadata().containsKey("optional"));
        assertNull(response.getMetadata().get("optional"));
    }

    private EventDto eventDto(Map<String, Object> metadata) {
        return EventDto.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp("2025-01-02T03:04:05Z")
                .metadata(metadata)
                .build();
    }

    private Event entity(Map<String, String> metadata) {
        return Event.builder()
                .eventId("event-1")
                .accountId("account-1")
                .type("CREDIT")
                .amount(new BigDecimal("12.50"))
                .currency("USD")
                .eventTimestamp(Instant.parse("2025-01-02T03:04:05Z"))
                .metadata(metadata)
                .build();
    }
}
