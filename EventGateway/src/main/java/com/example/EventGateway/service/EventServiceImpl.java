package com.example.EventGateway.service;

import com.example.EventGateway.dto.EventDto;
import com.example.EventGateway.entity.Event;
import com.example.EventGateway.mapper.EventGatewayMapper;
import com.example.EventGateway.port.EventGatewayService;
import com.example.EventGateway.repo.EventGatewayRepo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class EventServiceImpl implements EventGatewayService {

    private final EventGatewayRepo eventGatewayRepo;
    private final EventGatewayMapper eventGatewayMapper;
    private final EventIngestionService eventIngestionService;

    public EventServiceImpl(
            EventGatewayRepo eventGatewayRepo,
            EventGatewayMapper eventGatewayMapper,
            EventIngestionService eventIngestionService) {
        this.eventGatewayRepo = eventGatewayRepo;
        this.eventGatewayMapper = eventGatewayMapper;
        this.eventIngestionService = eventIngestionService;
    }

    @Override
    public ResponseEntity<String> createEvent(EventDto event) {

        Event entity = eventGatewayMapper.toEntity(event);
        eventGatewayRepo.save(entity);
        eventIngestionService.processAndForwardEvent(entity);
        return ResponseEntity.created(null).body("Event created successfully");
    }

    @Override
    public ResponseEntity<EventDto> getEntityById(String id) {
        Event event = eventGatewayRepo.findById(id).orElse(null);
        if (event == null) {
            return ResponseEntity.notFound().build();
        }
        EventDto eventDto = eventGatewayMapper.toResponseBody(event);
        return ResponseEntity.ok(eventDto);
    }

    @Override
    public ResponseEntity<List<EventDto>> getEventsByAccountId(String accountId) {
        List<Event> events = eventGatewayRepo.findByAccountIdOrderByEventTimestampAsc(accountId);
        if (events.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        List<EventDto> eventDtos = events.stream().map(eventGatewayMapper::toResponseBody).collect(java.util.stream.Collectors.toList());
        return ResponseEntity.ok(eventDtos);
    }
}
