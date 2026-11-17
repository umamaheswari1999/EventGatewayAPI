package com.example.EventGateway.mapper;


import com.example.EventGateway.dto.EventDto;
import com.example.EventGateway.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventGatewayMapper {

   public Event toEntity(EventDto request){
        return Event.builder()
                .eventId(request.getEventId())
                .accountId(request.getAccountId())
                .type(request.getType())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .eventTimestamp(Instant.parse(request.getEventTimestamp()))
                .metadata(request.getMetadata() != null ? request.getMetadata().entrySet().stream()
                        .collect(LinkedHashMap::new, (m, e) -> m.put(e.getKey(), e.getValue() != null ? e.getValue().toString() : null), LinkedHashMap::putAll)
                        : null)
                .build();
    }

    public EventDto toResponseBody(Event event) {
        return EventDto.builder()
                .eventId(event.getEventId())
                .accountId(event.getAccountId())
                .type(event.getType())
                .amount(event.getAmount())
                .currency(event.getCurrency())
                .eventTimestamp(String.valueOf(event.getEventTimestamp()))
                .metadata(mapMetadata(event.getMetadata()))
                .build();
    }

    private Map<String, Object> mapMetadata(Map<String, String> metadata) {
        if (metadata == null) {
            return null;
        }

        Map<String, Object> result = new LinkedHashMap<>();
        metadata.forEach((key, value) -> result.put(key, value == null ? null : value.toString()));
        return result;
    }

}
