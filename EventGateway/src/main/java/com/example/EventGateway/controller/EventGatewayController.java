package com.example.EventGateway.controller;

import com.example.EventGateway.dto.EventDto;
import com.example.EventGateway.port.EventGatewayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventGatewayController {
    private final EventGatewayService eventGatewayService;

    @PostMapping
    public ResponseEntity<String> createEvent(@Valid @RequestBody EventDto event) {
        return eventGatewayService.createEvent(event);
    }

    @GetMapping("/{id}")
    public ResponseEntity<EventDto> getEventById(@PathVariable String id) {
        return eventGatewayService.getEntityById(id);
    }

    @GetMapping
    public ResponseEntity<List<EventDto>> getEventsByAccountId(@RequestParam("account") String accountId) {
        return eventGatewayService.getEventsByAccountId(accountId);
    }
}
