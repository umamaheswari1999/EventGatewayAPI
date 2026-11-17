package com.example.EventGateway.feignClient;

import com.example.EventGateway.dto.AccountTransactionRequest;
import com.example.EventGateway.exception.ServiceUnavailableException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountClientFallbackTest {

    @Test
    void reportsAccountServiceUnavailable() {
        AccountClientFallback fallback = new AccountClientFallback();
        AccountTransactionRequest request = new AccountTransactionRequest(
                "event-1",
                new BigDecimal("5.00"),
                "CREDIT",
                Instant.parse("2025-01-02T03:04:05Z"));

        ServiceUnavailableException exception = assertThrows(ServiceUnavailableException.class,
                () -> fallback.applyTransaction("account-1", request));

        assertEquals(
                "Account Service is currently unavailable. Transaction for account account-1 could not be processed immediately.",
                exception.getMessage());
    }
}
