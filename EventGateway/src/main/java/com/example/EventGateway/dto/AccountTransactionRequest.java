package com.example.EventGateway.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountTransactionRequest(
        String eventId,
        BigDecimal amount,
        String type,
        Instant eventTimestamp
) {
}
