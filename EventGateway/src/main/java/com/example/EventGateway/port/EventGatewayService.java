package com.example.EventGateway.port;

import com.example.EventGateway.dto.EventDto;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface EventGatewayService {
    ResponseEntity<String> createEvent(EventDto event);
    ResponseEntity<EventDto> getEntityById(String id);
    ResponseEntity<List<EventDto>> getEventsByAccountId(String accountId);
}
