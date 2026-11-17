package com.example.EventGateway.controller;

import com.example.EventGateway.dto.HealthResponse;
import com.example.EventGateway.feignClient.HealthStatusProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class HealthController {

    private final HealthStatusProvider healthStatusProvider;

    public HealthController(HealthStatusProvider healthStatusProvider) {
        this.healthStatusProvider = healthStatusProvider;
    }

    @GetMapping("/health")
    public  ResponseEntity<HealthResponse> health() {
        String status = healthStatusProvider.getStatus();
        HealthResponse response = new HealthResponse(status);

        if ("UP".equals(status)) {
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }
}
