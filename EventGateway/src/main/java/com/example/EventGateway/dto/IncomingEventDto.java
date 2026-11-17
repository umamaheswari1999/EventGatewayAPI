package com.example.EventGateway.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;

public record IncomingEventDto(
        @NotBlank(message = "Event ID is required")
        String eventId,

        @NotBlank(message = "Account ID is required")
        String accountId,

        @NotBlank(message = "Event type (CREDIT or DEBIT) is required")
        String type,

        @NotNull(message = "Amount is required")
        @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
        BigDecimal amount,

        @NotBlank(message = "Currency is required")
        String currency,

        @NotNull(message = "Event timestamp is required")
        Instant eventTimestamp,

        Map<String, Object> metadata // Optional additional context[cite: 3]
) {}